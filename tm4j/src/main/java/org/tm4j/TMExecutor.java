package org.tm4j;

import java.util.concurrent.Callable;

/**
 * Transactional Memory Executor
 */
public interface TMExecutor {
    /**
     * Execute a callable in a thread-safe manner.
     *
     * It has too guarantee that two concurrent runs will not touch the same variables or
     * just do read access on them.
     *
     * A conflict occurs when a variable is modified in a run, while the same variable is
     * read or modified in a concurrent run. In such a case, only run will be able to end
     * properly (commit), the other ones will be roll-backed.
     *
     *
     * @param callable
     * @param <T>
     * @param context
     * @return the result of callable
     * @throws RuntimeException
     */
    public <T> T execute(Callable<T> callable, TMContext context) throws RuntimeException;

    /**
     *
     * @return the description of this implementation of transactional memory executor
     */
    public String getDescription();

    /**
     *
     * @return true if this executor is powered by a hardware feature (i.e. Intel TSX, IBM Power8, ...)
     */
    public boolean isHardware();

    /**
     *
     * @return true if this executor is supported (more likely related with the availability of an
     * hardware feature, but could also be related with JVM features).
     */
    public boolean isSupported();

    /**
     *
     * @return the current statistics of this executor
     */
    public TMStats getStats();
}
