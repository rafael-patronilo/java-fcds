package tests;
import mutexes.Mutex;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public abstract class MutexTests {
    private class Counter{
        int count = 0;
    }
    public abstract Mutex getMutex(int n);

    public void count(int numberOfThreads, int threadCounter) throws InterruptedException {
        final Mutex mutex = getMutex(numberOfThreads);
        final Counter counter = new Counter();
        List<Thread> threads = new ArrayList<>(numberOfThreads);
        for(int i = 0; i < numberOfThreads; i++){
            final int threadId = i;
            Thread thread = new Thread(()->{
                for(int j = 0; j < threadCounter; j++) {
                    mutex.lock(threadId);
                    counter.count++;
                    mutex.unlock(threadId);
                }
            });
            threads.add(thread);
            thread.start();
        }
        for(Thread thread : threads){ thread.join(); }
        assertEquals(numberOfThreads*threadCounter, counter.count);
    }

    @Test
    public void count10x10() throws InterruptedException {
        count(10, 10);
    }

    @Test
    public void count10x100() throws InterruptedException {
        count(10, 10);
    }
}
