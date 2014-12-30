package org.tm4j;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.tm4j.TM4J.*;

public class RTMTest
{
    @Test
    public void RTMvsHTMTest() {
        boolean hardware = false;
        boolean rtm = false;
        try {
          TMExecutor exec = TM4J.getExecutor();
          hardware = exec.isHardware();
          rtm = ((TSXExecutor)exec).hasRTMSupport();
        } catch (TMException e) {
          System.err.println("Failed executor: "+e);
          // XXX should that make a test failure?
        }

        if (!hardware)
          System.out.print("No ");
        System.out.println("Hardware Transactional Memory support");
        if (!rtm)
          System.out.print("No ");
        System.out.println("RTM/TSX support");

        assertEquals(rtm, hardware);
    }

    private static long count;
    @Test
    public void RTMStatsTest() {
        count = 0;
        transaction(() -> {
          return count++;
        });
        assertEquals(1, count);
        assertEquals(1, TM4J.getStats().getTransactions());
    }
}
