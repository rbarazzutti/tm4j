package org.tm4j;

public interface TMStats {

    public long getSerialCommitsCount();

    public long getTransactionalCommitsCount();

    public long getAbortsCount();
}
