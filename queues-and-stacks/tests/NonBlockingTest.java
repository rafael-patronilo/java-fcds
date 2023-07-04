package tests;


import org.junit.Ignore;
import queues.NonBlockingQueue;
import queues.Queue;

public class NonBlockingTest extends QueueTest {
    @Override
    protected <E> Queue<E> createQueue(int capacity) {
        return new NonBlockingQueue<>();
    }

    @Override
    protected boolean isBounded() {
        return false;
    }
}
