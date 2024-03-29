#include <immintrin.h>
#include <cpuid.h>
#include <org_tm4j_TSXJNI.h>

#define likely(x)       __builtin_expect(!!(x), 1)
#define unlikely(x)     __builtin_expect(!!(x), 0)

/* Serial lock to permit a transaction to run even if cannot be executed in an
 * hardware transaction.
 * NOTE: fits 2 cachelines because Haswell has a feature to prefetch adjacent
 * cache line. */
__attribute__((aligned(128)))
static volatile unsigned long glock = 0;

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

/*
 * For statistics collection.
 */
struct stats {
  unsigned long transactions;
  unsigned long serial;
  unsigned long aborts;
  unsigned long reason[8];
};
static const char *reason_str[] = {"UNKNOWN FAULT","_XABORT_EXPLICIT","_XABORT_RETRY","_XABORT_CONFLICT","_XABORT_CAPACITY","_XABORT_DEBUG","_XABORT_NESTED","UNKNOW"};
static struct stats tx_stats = {0};

/*
 * Class:     org_tm4j_TSXJNI
 * Method:    TMStats_getTransactions
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_tm4j_TSXJNI_TMStats_1getTransactions
  (JNIEnv *env, jobject obj)
{
  return tx_stats.transactions;
}

/*
 * Class:     org_tm4j_TSXJNI
 * Method:    TMStats_getSerials
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_tm4j_TSXJNI_TMStats_1getSerials
  (JNIEnv *env, jobject obj)
{
  return tx_stats.serial;
}

/*
 * Class:     org_tm4j_TSXJNI
 * Method:    TMStats_getAborts
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_tm4j_TSXJNI_TMStats_1getAborts
  (JNIEnv *env, jobject obj)
{
  return tx_stats.aborts;
}

/* TODO: create a JNI interface to get extended stats. */

/*
 * Display statistics. Unused from now.
 */
void display_stats(void) {
  unsigned int i;
  printf("#transactions: %lu\n", tx_stats.transactions);
  printf("#serial: %lu\n", tx_stats.serial);
  printf("#aborts: %lu\n", tx_stats.aborts);
  for (i = 0; i < 8; i++) {
    printf("#reason %s: %lu\n", reason_str[i], tx_stats.reason[i]);
  }
}

/**
 * Starts the transaction and returns the execution mode to give to tx_end function.
 */
static inline unsigned int tx_begin(int user_retries)
{
  unsigned int i;
  unsigned int xstatus;
  unsigned long retries = user_retries;
  tx_stats.transactions++;

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
  } else {
    glock_release();
  }
}

/*
 * Class:     org_tm4j_TSXJNI
 * Method:    execute
 * Signature: (Ljava/util/concurrent/Callable;I)Ljava/lang/Object;
 *
 * Execute the 'call' function from 'callable' object in a transaction.
 */
JNIEXPORT jobject JNICALL Java_org_tm4j_TSXJNI_execute
  (JNIEnv *env, jobject thisObj, jobject callable, jint retries)
{
  jclass thisClass;
  jmethodID midCallBack;
  jobject out;
  unsigned int mode;

  thisClass = (*env)->GetObjectClass(env, callable);
  midCallBack = (*env)->GetMethodID(env, thisClass, "call", "()Ljava/lang/Object;");
  if (unlikely(midCallBack == NULL))
    return NULL;

  // TODO A default number of retries is probably way better than letting the user to do it.
  mode = tx_begin(retries);
  out = (*env)->CallObjectMethod(env, thisObj, midCallBack);
  tx_end(mode);

  return out;
}

/*
 * Class:     org_tm4j_TSXJNI
 * Method:    hasRTMSupport
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_tm4j_TSXJNI_hasRTMSupport
  (JNIEnv *env, jobject jobj)
{
  unsigned int a, b, c, d;
  __cpuid_count(0x07, 0, a, b, c, d);
  if (b & (1 << 11))
    return 1;
  return 0;
}

