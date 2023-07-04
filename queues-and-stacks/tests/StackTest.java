package tests;

import org.junit.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import stacks.Stack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static tests.utils.ExtraAssertions.assertNotThrows;

public abstract class StackTest {
    public static final int TRIALS = 1000;

    protected abstract <E> Stack<E> createStack();

    private <E> Thread createPusher(Stack<E> stack, E value){
        return new Thread(()->assertNotThrows(()->stack.push(value)));
    }

    private <E> Thread createPopper(Stack<E> stack, Consumer<E> callback){
        return new Thread(()->callback.accept(assertNotThrows(stack::pop)));
    }

    @Test
    public void testSequentialPushAndPop() {
        final int COUNT = 100;
        Stack<Integer> stack = createStack();
        for (int i = 0; i < COUNT; i++) {
            stack.push(i);
        }
        for (int i = COUNT-1; i >= 0; i--) {
            assertEquals(i, stack.pop().intValue());
        }
    }

    @TestFactory
    public Collection<DynamicTest> massTestConcurrentPushAndPop() throws InterruptedException {
        List<DynamicTest> list = new ArrayList<>(TRIALS*2);
        for (int i = 0; i < TRIALS; i++) {
            list.add(DynamicTest.dynamicTest(i + " - pushFirst", () -> concurrentPushAndPop(true)));
            list.add(DynamicTest.dynamicTest(i + " -  popFirst", () -> concurrentPushAndPop(false)));
        }
        return list;
    }

    private void concurrentPushAndPop(boolean pushFirst) throws InterruptedException {
        final int COUNT = 100;
        final int THREAD_BATCH = 20;
        Stack<Integer> stack = createStack();
        BitSet bitSet = new BitSet(COUNT);
        ArrayList<Thread> threads = new ArrayList<>(COUNT*2);
        ArrayList<Thread> pushers = new ArrayList<>(THREAD_BATCH);
        ArrayList<Thread> poppers = new ArrayList<>(THREAD_BATCH);
        int bound;
        for (int i = 0; i < COUNT; i = bound) {
            bound = Math.min(i + THREAD_BATCH, COUNT);
            for (int j = i; j < bound; j++) {
                Thread pusher = createPusher(stack, j);
                pushers.add(pusher);
                threads.add(pusher);

                Thread popper = createPopper(stack, x->{
                    synchronized(bitSet){
                        assertFalse(bitSet.get(x));
                        bitSet.set(x);
                    }});
                poppers.add(popper);
                threads.add(popper);
            }
            ArrayList<Thread> first, second;
            if(pushFirst){
                first = pushers;
                second = poppers;
            } else {
                first = poppers;
                second = pushers;
            }
            first.forEach(Thread::start);
            second.forEach(Thread::start);
            pushers.clear();
            poppers.clear();
        }

        // make sure they've all finished
        for(Thread thread : threads){
            thread.join();
        }
    }

}
