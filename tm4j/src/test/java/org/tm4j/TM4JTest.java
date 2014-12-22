package org.tm4j;

import static org.junit.Assert.*;

import org.junit.Test;

public class TM4JTest {
    @Test
    public void noDefaultExecutor() {
        try {
            TM4J.getExecutor();
            fail();
        } catch (TMException e) {
            // supposed to throw an exception, no default executor is provided with TM4J
        }
    }
}
