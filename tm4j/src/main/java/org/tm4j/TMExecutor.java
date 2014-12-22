package org.tm4j;

import java.util.concurrent.Callable;

public interface TMExecutor {
    public <T> T execute(Callable<T> c) throws Exception;

    public String getDescription();

    public boolean isHardware();

    public boolean isSupported();
}
