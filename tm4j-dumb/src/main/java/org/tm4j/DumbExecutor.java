package org.tm4j;


import java.util.concurrent.Callable;

/**
 * Dumb (and awful) TMExecutor which fulfills the atomicity requirements using a central lock. This
 * implementations can only be used for simple test cases and/or in rescue situation.
 *
 * @see org.tm4j.TMExecutor
 */
public class DumbExecutor implements TMExecutor {
    private long transactionsCount = 0;

    public <T> T execute(Callable<T> c, TMContext context) throws TMWrappedException {
        synchronized (this) {
            try {
                transactionsCount++;
                return c.call();
            } catch (Exception e) {
                throw new TMWrappedException(e);
            }
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

    public TMStats getStats() {
        return new TMStats() {
            @Override
            public long getSerials() {
                return transactionsCount;
            }

            @Override
            public long getTransactions() {
                return transactionsCount;
            }

            @Override
            public long getAborts() {
                return 0;
            }
        };
    }
}
