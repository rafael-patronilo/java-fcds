package tests;

import queues.LinkedBlockingQueue;
import queues.Queue;


public class LinkedBlockingQueueTest extends QueueTest{

    @Override
    protected <E> Queue<E> createQueue(int capacity) {
        return new LinkedBlockingQueue<>(capacity);
    }

    @Override
    protected boolean isBounded() {
        return true;
    }
}