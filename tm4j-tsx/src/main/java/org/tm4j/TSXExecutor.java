package org.tm4j;

import java.util.concurrent.Callable;

public class TSXExecutor extends TSXJNI implements TMExecutor
{
    public <V> V execute(Callable<V> c, TMContext context) {
        // FIXME context can be null, but should remove user-specified retries
        return execute(c, 8 /*context.getMaxNumberOfRetries()*/);
    }

    public boolean isSupported() {
        return isLoaded() && hasRTMSupport();
    }

    public String getDescription() {
        return "Intel TSX support";
    }

    public boolean isHardware() {
        return true;
    }

    public TMStats getStats() {
        return new TMStats() {
            @Override
            public long getSerials() {
                return TMStats_getSerials();
            }

            @Override
            public long getTransactions() {
                return TMStats_getTransactions();
            }

            @Override
            public long getAborts() {
                return TMStats_getAborts();
            }
            // TODO To be completed for extended stats
        };
    }
}
