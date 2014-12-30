package org.tm4j;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.tm4j.TM4J.*;

public class DumbExecutorTest {

    static private void safeJoin(final Thread a) {
        try {
            a.join();
        } catch (Exception e) {
            // do nothing
        }
    }

    static private void safeSleep(final long m, final int n) {
        try {
            Thread.sleep(m, n);
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void concurrentMutations() throws InterruptedException {
        int n = 500;
        int t = 200;

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
                    // wait 100 µs to avoid to increase contention
                    safeSleep(0, 100);
                }
            }


        }

        List<Mutator> mutators = IntStream.range(0, t).mapToObj(i -> new Mutator(2 * i - t + 1)).collect(Collectors.toList());

        mutators.forEach(Thread::start);

        mutators.forEach(DumbExecutorTest::safeJoin);

        assertEquals(n * t, TM4J.getStats().getSerials());
        assertEquals(0, TM4J.getStats().getAborts());
        assertEquals(n * t, TM4J.getStats().getTransactions());
    }
}
