package org.tm4j;


import java.util.concurrent.Callable;

/**
 * Dumb (and awful) TMExecutor which fulfills the atomicity requirements using a central lock. This
 * implementations can only be used for simple test cases and/or in rescue situation.
 *
 * @see org.tm4j.TMExecutor
 */
public class DumbExecutor implements TMExecutor {
    private long serialCount=0;
    public <T> T execute(Callable<T> c) throws Exception {
        synchronized (this) {
            serialCount++;
            return c.call();
        }
    }

    public String getDescription() {
        return "Dumb isolation mechanism based on a huge single lock - Not using transactional memory";
    }

    public boolean isHardware() {
        return false;
    }

    public boolean isSupported() {
        return true;
    }

    public TMStats getStats(){
        return new TMStats() {
            @Override
            public long getSerialCommitsCount() {
                return serialCount;
            }

            @Override
            public long getTransactionalCommitsCount() {
                return 0;
            }

            @Override
            public long getAbortsCount() {
                return 0;
            }
        };
    }
}
