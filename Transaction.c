#include <immintrin.h>
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

// TODO inline all this function.
static void glock_wait(void)
{
  while (glock) {
    _mm_pause();
  }
}

static void glock_acquire(void)
{
  // TODO maybe use the same strategy as in pthread_spin_lock
  while (glock == 1 || __sync_bool_compare_and_swap(&glock, 0, 1) == 0) {
     _mm_pause();
  }
}

static void glock_release(void)
{
  // TODO barrier required here?
  glock = 0;
}


/*
 * Class:     Transaction
 * Method:    execute
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_Transaction_execute
  (JNIEnv *env, jobject thisObj)
{
  unsigned int i;
  unsigned int xstatus;
  unsigned long retries = MAX_RETRIES;

  jclass thisClass = (*env)->GetObjectClass(env, thisObj);
  
  jmethodID midCallBack = (*env)->GetMethodID(env, thisClass, "run", "()V");
  if (midCallBack == NULL)
    return;

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

  (*env)->CallVoidMethod(env, thisObj, midCallBack);

  if (xstatus == _XBEGIN_STARTED) {
    _xend();
    tx_stats.commit++;
  } else {
    glock_release();
  }
}


__attribute__((destructor))
static void destroy_navigationBarImages() {
  unsigned int i;
  printf("#commit: %lu\n", tx_stats.commit);
  printf("#serial: %lu\n", tx_stats.serial);
  printf("#aborts: %lu\n", tx_stats.aborts);
  for (i = 0; i < 8; i++) {
    printf("#reason %s: %lu\n", reason_str[i], tx_stats.reason[i]);
  }
}
