package tests;

import org.junit.Test;
import queues.Queue;
import utils.BusyStructureException;
import utils.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assume.*;
import static org.junit.Assert.*;
import static tests.utils.ExtraAssertions.*;

public abstract class QueueTest {
    public static final int TRIALS = 1000;

    protected abstract <E> Queue<E> createQueue(int capacity);
    protected abstract boolean isBounded();

    private <E> Thread createEnqueuer(Queue<E> queue, E value){
        return new Thread(()->assertNotThrows(()->queue.enqueue(value)));
    }

    private <E> Thread createEnqueuer(Queue<E> queue, E value, AtomicInteger blockCounter){
        return new Thread(()->{
            try {
                queue.tryEnqueue(value);
            } catch (BusyStructureException e){
                assertNotThrows(()->queue.enqueue(value)); //wait for it
                blockCounter.incrementAndGet();
            }
        });
    }
    private <E> Thread createDequeuer(Queue<E> queue, Consumer<E> callback){
        return new Thread(()->callback.accept(assertNotThrows(queue::dequeue)));
    }

    private <E> Thread createDequeuer(Queue<E> queue, Consumer<E> callback, AtomicInteger blockCounter){
        return new Thread(()->{
            E result;
            try {
                result = queue.tryDequeue();
            } catch (BusyStructureException e){
                result = assertNotThrows(queue::dequeue); //wait for it
                blockCounter.incrementAndGet();
            }
            callback.accept(result);
        });
    }

    @Test
    public void testSequentialEnqueueAndDequeue() {
        final int COUNT = 100;
        Queue<Integer> queue = createQueue(COUNT);
        assertThrows(BusyStructureException.class, queue::tryDequeue);
        for (int i = 0; i < COUNT; i++) {
            queue.enqueue(i);
        }
        if(isBounded()){
            assertThrows(BusyStructureException.class, ()->queue.tryEnqueue(-1));
        }
        for (int i = 0; i < COUNT; i++) {
            assertEquals(i, queue.dequeue().intValue());
        }
        assertThrows(BusyStructureException.class, queue::tryDequeue);
    }

    @Test
    public void testConcurrentTryEnqueueAndDequeue() throws InterruptedException {
        int enqueueBlocks = 0, dequeueBlocks = 0;
        for (int i = 0; i < TRIALS; i++) {
            Pair<Integer, Integer> pair = concurrentTryEnqueueAndDequeue(true);
            enqueueBlocks += pair.a();
            dequeueBlocks += pair.b();
            pair = concurrentTryEnqueueAndDequeue(false);
            enqueueBlocks += pair.a();
            dequeueBlocks += pair.b();
        }
        System.out.println(
                "Total number of blocks: " + (enqueueBlocks + dequeueBlocks) + "\n" +
                "        Enqueue blocks: " + enqueueBlocks + "\n" +
                "        Dequeue blocks: " + dequeueBlocks + "\n"
        );
    }

    private Pair<Integer, Integer> concurrentTryEnqueueAndDequeue(boolean enqueueFirst) throws InterruptedException {
        final int CAPACITY = 10;
        final int COUNT = 100;
        final int THREAD_BATCH = CAPACITY * 2;
        AtomicInteger enqueueBlocks = new AtomicInteger();
        AtomicInteger dequeueBlocks = new AtomicInteger();
        Queue<Integer> queue = createQueue(CAPACITY);
        BitSet bitSet = new BitSet(COUNT);
        assertThrows(BusyStructureException.class, queue::tryDequeue);
        ArrayList<Thread> threads = new ArrayList<>(COUNT*2);
        ArrayList<Thread> enqueuers = new ArrayList<>(THREAD_BATCH);
        ArrayList<Thread> dequeuers = new ArrayList<>(THREAD_BATCH);
        int bound;
        for (int i = 0; i < COUNT; i = bound) {
            bound = Math.min(i + THREAD_BATCH, COUNT);
            for (int j = i; j < bound; j++) {
                Thread enqueuer = createEnqueuer(queue, j, enqueueBlocks);
                enqueuers.add(enqueuer);
                threads.add(enqueuer);

                Thread dequeuer = createDequeuer(queue, x->{
                    synchronized(bitSet){
                        assertFalse(bitSet.get(x));
                        bitSet.set(x);
                    }}, dequeueBlocks);
                dequeuers.add(dequeuer);
                threads.add(dequeuer);
            }
            ArrayList<Thread> first, second;
            if(enqueueFirst){
                first = enqueuers;
                second = dequeuers;
            } else {
                first = dequeuers;
                second = enqueuers;
            }
            first.forEach(Thread::start);
            second.forEach(Thread::start);
            enqueuers.clear();
            dequeuers.clear();
        }

        // make sure they've all finished
        for(Thread thread : threads){
            thread.join();
        }

        System.out.println(
                "Total number of blocks: " + (enqueueBlocks.get() + dequeueBlocks.get()) + "\n" +
                "        Enqueue blocks: " + enqueueBlocks.get() + "\n" +
                "        Dequeue blocks: " + dequeueBlocks.get() + "\n"
        );
        //make sure queue is empty
        assertThrows(BusyStructureException.class, queue::tryDequeue);
        //make sure all values were found in the queue
        assertAllTrue(bitSet, COUNT);
        return new Pair<>(enqueueBlocks.get(), dequeueBlocks.get());
    }

}
