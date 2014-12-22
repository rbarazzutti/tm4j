package org.tm4j;

public class TM4J {
    static final private TMExecutor executor = findExecutor();

    static private TMExecutor findExecutor() {
        String executors[] = {"org.tm4j.TSXExecutor", "org.tm4j.ScalaSTMExecutor", "org.tm4j.DumbExecutor"};

        for (String executorName : executors)
            try {
                Class<?> clazz = Class.forName(executorName);

                Object executor = clazz.newInstance();
                if (executor instanceof TMExecutor)
                    if (((TMExecutor) executor).isSupported())
                        return (TMExecutor) executor;
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                // Such exception should not be handled
            }
        return null;
    }

    public static TMExecutor getExecutor() throws TMException {
        if (executor != null)
            return executor;
        else
            throw new TMException("No transactional memory executor found in classpath");
    }
}
