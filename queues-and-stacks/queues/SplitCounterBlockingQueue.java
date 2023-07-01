package queues;

import linkedLists.LinkedListNode;
import utils.BusyStructureException;

import java.util.concurrent.atomic.AtomicInteger;

public class SplitCounterBlockingQueue<E> implements Queue<E> {
    private LinkedListNode<E> head;
    private LinkedListNode<E> tail;
    private volatile int enqPermits, deqPermits;
    private final int capacity;
    private final Object enqLock = new Object();
    private final Object deqLock = new Object();

    private int getPermits(){
        return enqPermits + deqPermits;
    }


    private void incrementPermits(){
        synchronized (deqLock) {
            if (deqPermits == Integer.MAX_VALUE - 1) {
                synchronized (enqLock) {
                    enqPermits += deqPermits;
                    deqPermits = 0;
                }
            }
            deqPermits++;
        }
    }

    private void decrementPermits(){
        synchronized (enqLock) {
            if (enqPermits == Integer.MIN_VALUE + 1) {
                synchronized (deqLock) {
                    deqPermits += enqPermits;
                    enqPermits = 0;
                }
            }
            enqPermits--;
        }
    }

    public SplitCounterBlockingQueue(int capacity){
        this.capacity = capacity;
        head = new LinkedListNode<>(null);
        tail = head;
        enqPermits = capacity;
        deqPermits = 0;
    }

    @Override
    public void enqueue(E item) {
        synchronized (enqLock){
            while(getPermits() == 0){
                try {
                    enqLock.wait();
                } catch (InterruptedException ignored) {}
            }
            LinkedListNode<E> node = new LinkedListNode<>(item);
            tail.next = node;
            tail = node;
            decrementPermits();
        }
        synchronized (deqLock){
            deqLock.notifyAll();
        }
    }

    @Override
    public E dequeue() {
        E value;
        synchronized (deqLock){
            while(getPermits() == capacity){
                try {
                    deqLock.wait();
                } catch (InterruptedException ignored) {}
            }
            value = head.next.value;
            head = head.next;
            head.value = null;
            incrementPermits();
        }
        synchronized (enqLock){
            enqLock.notifyAll();
        }
        return value;
    }

    @Override
    public void tryEnqueue(E item) throws BusyStructureException {
        synchronized (enqLock){
            if(getPermits() == 0){
                throw new BusyStructureException();
            }
            LinkedListNode<E> node = new LinkedListNode<>(item);
            tail.next = node;
            tail = node;
            decrementPermits();
        }
        synchronized (deqLock){
            deqLock.notifyAll();
        }
    }

    @Override
    public E tryDequeue() throws BusyStructureException{
        E value;
        synchronized (deqLock){
            if(getPermits() == capacity){
                throw new BusyStructureException();
            }
            value = head.next.value;
            head = head.next;
            head.value = null;
            incrementPermits();
        }
        synchronized (enqLock){
            enqLock.notifyAll();
        }
        return value;
    }
}
