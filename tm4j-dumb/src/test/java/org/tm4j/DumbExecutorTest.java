package org.tm4j;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.tm4j.TM4J.*;

public class DumbExecutorTest {

    @Test
    public void concurrentMutations() throws InterruptedException {
        int n = 500000;

        class State {
            int a = 0;
            int b = 0;
        }

        State state = new State();

        class Mutator extends Thread {
            final private int delta;
            public boolean corrupted = false;

            public Mutator(int delta) {
                this.delta = delta;
            }

            @Override
            public void run() {

                for (int i = 0; i < n; i++) {
                    if (transaction(() -> {
                        boolean corrupted = state.a != state.b;
                        state.a += delta;
                        state.b += delta;

                        return corrupted;
                    })) corrupted = true;
                }
            }


        }

        Mutator x = new Mutator(-1);
        Mutator y = new Mutator(+1);

        x.start();
        y.start();

        x.join();
        y.join();

        if (x.corrupted || y.corrupted) fail("Corrupted counters detected");

        assertEquals(n * 2, TM4J.getStats().getSerialCommitsCount());
        assertEquals(0, TM4J.getStats().getAbortsCount());
        assertEquals(0, TM4J.getStats().getTransactionalCommitsCount());

    }
}
