package org.tm4j;

import java.util.concurrent.Callable;

public class TSXExecutor extends TSXJNI implements TMExecutor {


    public <V> V execute(Callable<V> c, TMContext context) {
        return execute(c, context.getMaxNumberOfRetries());
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
        // TODO implement me!
        return null;
    }
}
