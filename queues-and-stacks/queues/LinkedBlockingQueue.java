package queues;

import linkedLists.LinkedListNode;
import utils.BusyStructureException;

import java.util.concurrent.atomic.AtomicInteger;

public class LinkedBlockingQueue<E> implements Queue<E> {
    private LinkedListNode<E> head;
    private LinkedListNode<E> tail;
    private final AtomicInteger permits;
    private final int capacity;
    private final Object enqLock = new Object();
    private final Object deqLock = new Object();

    public LinkedBlockingQueue(int capacity){
        this.capacity = capacity;
        head = new LinkedListNode<>(null);
        tail = head;
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
            LinkedListNode<E> node = new LinkedListNode<>(item);
            tail.next = node;
            tail = node;
            permits.decrementAndGet();
        }
        synchronized (deqLock){
            deqLock.notifyAll();
        }
    }

    @Override
    public E dequeue() {
        E value;
        synchronized (deqLock){
            while(permits.get() == capacity){
                try {
                    deqLock.wait();
                } catch (InterruptedException ignored) {}
            }
            value = head.next.value;
            head = head.next;
            head.value = null;
            permits.incrementAndGet();
        }
        synchronized (enqLock){
            enqLock.notifyAll();
        }
        return value;
    }

    @Override
    public void tryEnqueue(E item) throws BusyStructureException{
        synchronized (enqLock){
            if(permits.get() == 0){
                throw new BusyStructureException();
            }
            LinkedListNode<E> node = new LinkedListNode<>(item);
            tail.next = node;
            tail = node;
            permits.decrementAndGet();
        }
        synchronized (deqLock){
            deqLock.notifyAll();
        }
    }

    @Override
    public E tryDequeue() throws BusyStructureException{
        E value;
        synchronized (deqLock){
            if(permits.get() == capacity){
                throw new BusyStructureException();
            }
            value = head.next.value;
            head = head.next;
            head.value = null;
            permits.incrementAndGet();
        }
        synchronized (enqLock){
            enqLock.notifyAll();
        }
        return value;
    }
}
