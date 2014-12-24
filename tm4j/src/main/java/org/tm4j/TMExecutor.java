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
     * @return
     * @throws Exception
     */
    public <T> T execute(Callable<T> callable, TMContext context) throws Exception;

    /**
     *
     * @return a
     */
    public String getDescription();

    public boolean isHardware();

    public boolean isSupported();

    public TMStats getStats();
}
