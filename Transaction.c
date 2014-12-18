#include <immintrin.h>
#include <cpuid.h>
#include "Transaction.h"

#define likely(x)       __builtin_expect(!!(x), 1)
#define unlikely(x)     __builtin_expect(!!(x), 0)

/* RTM macros. Not using bultin for the xbegin to avoid useless comparison */
#define XABORT(status) asm volatile(".byte 0xc6,0xf8,%P0" :: "i" (status))
#define XBEGIN(label)  asm volatile goto(".byte 0xc7,0xf8 ; .long %l0-1f\n1:" ::: "eax" : label)
#define XEND()         asm volatile(".byte 0x0f,0x01,0xd5")
#define XFAIL(label)   label: asm volatile("" ::: "eax")
#define XFAIL_STATUS(label, status) label: asm volatile("" : "=a" (status))

/* TODO MAX_RETRIES could a variable since it is not on the hot path */
#define MAX_RETRIES (8)

/* 2 cachelines here because haswell has a feature to prefetch adjacent cache */
__attribute__((aligned(128)))
static volatile unsigned long glock = 0;
struct stats {
  unsigned long commit;
  unsigned long serial;
  unsigned long aborts;
  unsigned long reason[8];
};
static const char *reason_str[] = {"UNKNOWN FAULT","_XABORT_EXPLICIT","_XABORT_RETRY","_XABORT_CONFLICT","_XABORT_CAPACITY","_XABORT_DEBUG","_XABORT_NESTED","UNKNOW"};

static struct stats tx_stats = {0};

/**
 * Wait for the serial lock to be released.
 */
static inline void glock_wait(void)
{
  while (glock) {
    _mm_pause();
  }
}

/**
 * Acquire the serial lock.
 */
static inline void glock_acquire(void)
{
  // TODO maybe use the same strategy as in pthread_spin_lock
  while (glock == 1 || __sync_bool_compare_and_swap(&glock, 0, 1) == 0) {
     _mm_pause();
  }
}

/**
 * Release the serial lock.
 */
static inline void glock_release(void)
{
  // XXX a barrier is probably required here even if it is x86?
  glock = 0;
}

/**
 * Starts the transaction and returns the execution mode to give to tx_end function.
 */
static inline unsigned int tx_begin(void)
{
  unsigned int i;
  unsigned int xstatus;
  unsigned long retries = MAX_RETRIES;

  // TODO maybe use macro from rtm-goto instead (can save few cycles)
  while (unlikely((xstatus = _xbegin()) != _XBEGIN_STARTED)) {
    tx_stats.aborts++;
    if (xstatus == 0) {
      tx_stats.reason[0]++;
    } else {
      for (i = 0; i < 7; i++) {
        if ((xstatus >> i) & 1)
          tx_stats.reason[i+1]++;
      }
    }
    /* TODO add statistics for the transaction (statistics do not need to be precise, maybe no need for atomic) */
    if ((retries-- <= 0) || (xstatus & (_XABORT_CAPACITY|_XABORT_DEBUG|_XABORT_NESTED)) || (xstatus == 0)) {
      // TODO if explicit abort (or due to glock) but wait a random time to avoid the lemming/convoy effect
      tx_stats.serial++;
      glock_acquire();
      break;
    }
    glock_wait();
  }
  // Monitor the serial lock
  if (xstatus == _XBEGIN_STARTED && unlikely(glock))
    _xabort(0xFF);

  return xstatus;
}

/**
 * Commits the transaction. This requires the return of tx_begin to know in what mode the transaction is executed (HTM or serial).
 */
static inline void tx_end(unsigned int xstatus)
{
  if (xstatus == _XBEGIN_STARTED) {
    _xend();
    tx_stats.commit++;
  } else {
    glock_release();
  }
}

/**
 * Execute the 'run' function from thisObj in a transaction.
 */
static inline void tx_execute(JNIEnv *env, jobject thisObj)
{
  unsigned int mode;

  jclass thisClass = (*env)->GetObjectClass(env, thisObj);

  jmethodID midCallBack = (*env)->GetMethodID(env, thisClass, "run", "()V");
  if (midCallBack == NULL)
    return;

  mode = tx_begin();
  (*env)->CallVoidMethod(env, thisObj, midCallBack);
  tx_end(mode);
}

/*
 * Class:     Transaction
 * Method:    execute
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_Transaction_execute__
  (JNIEnv *env, jobject thisObj)
{
  tx_execute(env, thisObj);
}

/*
 * Class:     Transaction
 * Method:    execute
 * Signature: (Ljava/lang/Runnable;)V
 */
JNIEXPORT void JNICALL Java_Transaction_execute__Ljava_lang_Runnable_2
  (JNIEnv *env, jclass klass, jobject thisObj)
{
  tx_execute(env, thisObj);
}

/*
 * Class:     Transaction
 * Method:    hasRTMSupport
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_Transaction_hasRTMSupport
  (JNIEnv *env, jclass klass)
{
  unsigned int a, b, c, d;
  __cpuid_count(0x07, 0, a, b, c, d);
  if (b & (1 << 11))
    return 1;
  return 0;
}


__attribute__((destructor))
static void display_stats() {
  unsigned int i;
  printf("#commit: %lu\n", tx_stats.commit);
  printf("#serial: %lu\n", tx_stats.serial);
  printf("#aborts: %lu\n", tx_stats.aborts);
  for (i = 0; i < 8; i++) {
    printf("#reason %s: %lu\n", reason_str[i], tx_stats.reason[i]);
  }
}
