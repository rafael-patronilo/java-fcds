package tests;

import queues.Queue;
import queues.SplitCounterBlockingQueue;


public class SplitCounterBlockingQueueTest extends QueueTest {

    @Override
    protected <E> Queue<E> createQueue(int capacity) {
        return new SplitCounterBlockingQueue<>(capacity);
    }

    @Override
    protected boolean isBounded() {
        return true;
    }
}