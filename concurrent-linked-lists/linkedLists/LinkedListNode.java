package linkedLists;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LinkedListNode<E> {

    public Lock lock = new ReentrantLock();
    public E value;
    public LinkedListNode<E> next;

    public boolean isRemoved;

    public LinkedListNode(E value){
        this.value = value;
        this.next = null;
    }

    public LinkedListNode(E value, LinkedListNode<E> next){
        this.value = value;
        this.next = next;
    }
}
