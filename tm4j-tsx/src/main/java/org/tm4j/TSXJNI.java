package org.tm4j;

import java.util.concurrent.Callable;

public class TSXJNI {
    static private boolean loaded;

    public boolean isLoaded() {
        return loaded;
    }

    protected native <V> V execute(Callable<V> c, int maxRetries);

    protected native boolean hasRTMSupport();

    static {
        try {
            System.loadLibrary("tm-tsx");
            loaded = true;
        } catch (Exception e) {
            loaded = false;
        }
    }
}
