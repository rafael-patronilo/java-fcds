package linkedLists;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class LinkedListNode<E> {

    Lock lock = new ReentrantLock();
    E value;
    LinkedListNode<E> next;

    boolean isRemoved;

    public LinkedListNode(E value){
        this.value = value;
        this.next = null;
    }

    public LinkedListNode(E value, LinkedListNode<E> next){
        this.value = value;
        this.next = next;
    }
}
