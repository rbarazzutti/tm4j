package org.tm4j;

/**
 * Transactional Memory statistics
 */
public interface TMStats {

    public long getSerialCommitsCount();

    public long getTransactionCount();

    public long getAbortsCount();
}
