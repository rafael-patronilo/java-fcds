package tests;

import queues.BlockingBoundedQueue;
import queues.Queue;

public class BlockingBoundedQueueTest extends QueueTest{

    @Override
    protected <E> Queue<E> createQueue(int capacity) {
        return new BlockingBoundedQueue<>(capacity);
    }

    @Override
    protected boolean isBounded() {
        return true;
    }
}
