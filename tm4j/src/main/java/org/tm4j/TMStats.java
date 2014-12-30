package org.tm4j;

/**
 * Transactional Memory statistics
 */
public interface TMStats {

    public long getSerials();

    public long getTransactions();

    public long getAborts();
}
