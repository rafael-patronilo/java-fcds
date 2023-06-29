package queues;

import org.junit.Ignore;
import utils.BusyStructureException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingBoundedQueue<E> implements Queue<E> {
    private final E[] array;
    private volatile int head;
    private volatile int tail;
    private final AtomicInteger permits;
    private final Object enqLock = new Object();
    private final Object deqLock = new Object();

    @SuppressWarnings("unchecked")
    public BlockingBoundedQueue(int capacity){
        array = (E[])new Object[capacity];
        head = 0;
        tail = 0;
        permits = new AtomicInteger(capacity);
    }

    @Override
    public void enqueue(E item) {
        synchronized (enqLock){
            while(permits.get() == 0){
                try {
                    enqLock.wait();
                } catch (InterruptedException ignored) {}
            }
            array[(tail++) % array.length] = item;
            permits.decrementAndGet();
        }

        synchronized (deqLock){
            deqLock.notify();
        }
    }

    @Override
    public E dequeue() {
        E value;
        synchronized (deqLock){
            while(permits.get() == array.length){
                try {
                    deqLock.wait();
                } catch (InterruptedException ignored) {}
            }
            value = array[(head++) % array.length];
            permits.incrementAndGet();
        }
        synchronized (enqLock){
            enqLock.notify();
        }
        return value;
    }

    @Override
    public void tryEnqueue(E item) throws BusyStructureException{
        synchronized (enqLock){
            if(permits.get() == 0){
                throw new BusyStructureException();
            }
            array[(tail++) % array.length] = item;
            permits.decrementAndGet();
        }
        synchronized (deqLock){
            deqLock.notify();
        }
    }

    @Override
    public E tryDequeue() throws BusyStructureException{
        E value;
        synchronized (deqLock){
            if(permits.get() == array.length){
                throw new BusyStructureException();
            }
            value = array[(head++) % array.length];
            permits.incrementAndGet();
        }
        synchronized (enqLock){
            enqLock.notify();
        }
        return value;
    }
}
