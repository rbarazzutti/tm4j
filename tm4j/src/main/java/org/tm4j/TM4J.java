package org.tm4j;

import java.util.concurrent.Callable;

/**
 *
 */
public class TM4J {
    static final private TMExecutor executor = findExecutor();

    public static final TMContext defaultContext;

    static {
        defaultContext=new TMContext() {
            @Override
            public int getMaxNumberOfRetries() {
                return 50;
            }
        };
    }

    static private TMExecutor findExecutor() {
        String executors[] = {"TSXExecutor", "Power8Executor", "ScalaSTMExecutor", "DumbExecutor"};

        for (String executorName : executors)
            try {
                Class<?> clazz = Class.forName("org.tm4j." + executorName);

                Object executor = clazz.newInstance();
                if (executor instanceof TMExecutor)
                    if (((TMExecutor) executor).isSupported())
                        return (TMExecutor) executor;
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                // Such exception should not be handled
            }
        return null;
    }

    /**
     * @return
     * @throws TMException if no transactional memory executor has been found.
     */
    public static TMExecutor getExecutor() throws TMException {
        if (executor != null)
            return executor;
        else
            throw new TMException("No transactional memory executor found in classpath");
    }

    static public <T> T transaction(Callable<T> c, TMContext context) throws RuntimeException {
        return getExecutor().execute(c, context);
    }

    static public <T> T transaction(Callable<T> c) throws RuntimeException {
        return transaction(c, null);
    }

    /**
     * @return
     * @throws TMException
     */
    public static TMStats getStats() throws TMException {
        return getExecutor().getStats();
    }
}
