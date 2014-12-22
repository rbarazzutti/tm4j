package org.tm4j;


import java.util.concurrent.Callable;

public class DumbExecutor implements TMExecutor {
    public <T> T execute(Callable<T> c) throws Exception {
        synchronized (this) {
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
}
