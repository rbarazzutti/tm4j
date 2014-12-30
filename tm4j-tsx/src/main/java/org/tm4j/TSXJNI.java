package org.tm4j;

import java.util.concurrent.Callable;

public class TSXJNI {
    static private boolean loaded;

    public boolean isLoaded() {
        return loaded;
    }

    protected native <V> V execute(Callable<V> c, int maxRetries);

    protected native boolean hasRTMSupport();

    protected native long TMStats_getTransactions();
    protected native long TMStats_getSerials();
    protected native long TMStats_getAborts();

    static {
        try {
            System.loadLibrary("Tm4jTsx");
            loaded = true;
        } catch (LinkageError e) {
            loaded = false;
        }
    }
}
