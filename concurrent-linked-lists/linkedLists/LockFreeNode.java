package linkedLists;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockFreeNode<E> {
    public E value;
    public AtomicMarkableReference<LockFreeNode<E>> next;

    //boolean isRemoved;

    public LockFreeNode(E value){
        this.value = value;
        this.next = new AtomicMarkableReference<>(null, false);
    }

    public LockFreeNode(E value, LockFreeNode<E> next){
        this.value = value;
        this.next = new AtomicMarkableReference<>(next, false);
    }
}
