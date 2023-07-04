package queues;

import linkedLists.LockFreeNode;
import utils.BusyStructureException;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.Lock;

public class NonBlockingQueue<E> implements Queue<E> {
    private AtomicMarkableReference<LockFreeNode<E>> head;
    private AtomicMarkableReference<LockFreeNode<E>> tail;
    private final Object enqLock = new Object();
    private final Object deqLock = new Object();

    public NonBlockingQueue(){
        LockFreeNode<E> sentinel = new LockFreeNode<>(null);
        head = new AtomicMarkableReference<>(sentinel, false);
        tail = new AtomicMarkableReference<>(sentinel, false);
    }

    @Override
    public void enqueue(E item) {
        LockFreeNode<E> node = new LockFreeNode<>(item);
        LockFreeNode<E> currentTail = tail.getReference();
        while(!currentTail.next.compareAndSet(null, node, false, false)) {
            LockFreeNode<E> actualTail = currentTail.next.getReference();
            tail.compareAndSet(currentTail, actualTail, false, false);
            currentTail = tail.getReference();
        }
        tail.compareAndSet(null, node, false, false);
    }

    @Override
    public E dequeue() {
        retry: while(true) {
            E value;
            LockFreeNode<E> currentHead = head.getReference();
            LockFreeNode<E> newHead = currentHead.next.getReference();
            if (newHead == null) {
                Thread.onSpinWait();
                continue retry;
            }
            while (!head.compareAndSet(currentHead, newHead, false, false)) {
                currentHead = head.getReference();
                newHead = currentHead.next.getReference();
                if (newHead == null) {
                    Thread.onSpinWait();
                    continue retry;
                }
            }
            value = newHead.value;
            newHead.value = null;
            return value;
        }
    }

    @Override
    public void tryEnqueue(E item) {
        LockFreeNode<E> node = new LockFreeNode<>(item);
        LockFreeNode<E> currentTail = tail.getReference();
        while(!currentTail.next.compareAndSet(null, node, false, false)) {
            LockFreeNode<E> actualTail = currentTail.next.getReference();
            tail.compareAndSet(currentTail, actualTail, false, false);
            currentTail = tail.getReference();
        }
        tail.compareAndSet(null, node, false, false);
    }

    @Override
    public E tryDequeue() throws BusyStructureException{
        E value;
        LockFreeNode<E> currentHead = head.getReference();
        LockFreeNode<E> newHead = currentHead.next.getReference();
        if (newHead == null)
            throw new BusyStructureException();
        while (!head.compareAndSet(currentHead, newHead, false, false)) {
            currentHead = head.getReference();
            newHead = currentHead.next.getReference();
            if (newHead == null)
                throw new BusyStructureException();
        }
        value = newHead.value;
        newHead.value = null;
        return value;
    }
}
