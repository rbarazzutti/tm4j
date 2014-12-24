package org.tm4j;

import static org.junit.Assert.*;
import static org.tm4j.TM4J.*;

import org.junit.Test;

public class TM4JTest {
    @Test
    public void noDefaultExecutor() {
        try {
            TM4J.getExecutor();
            transaction(() -> {
                return null;
            });
            fail();
        } catch (TMException e) {
            // supposed to throw an exception, no default executor is provided with TM4J
        } catch (Exception e){
            fail("The transaction code itself isn't supposed to generate an exception in this case");
        }
    }
}
