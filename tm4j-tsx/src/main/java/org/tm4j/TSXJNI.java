package org.tm4j;

import java.util.concurrent.Callable;

public class TSXJNI  {

    protected native <V> V execute(Callable<V> c, int maxRetries );

    protected native boolean hasRTMSupport();

    static {
        System.loadLibrary("tm-tsx");
    }
}
