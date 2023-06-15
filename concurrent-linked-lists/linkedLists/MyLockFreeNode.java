package linkedLists;

import utils.AtomicPair;

import java.util.concurrent.atomic.AtomicMarkableReference;

class MyLockFreeNode<E> {
    E value;
    AtomicPair<MyLockFreeNode<E>, Boolean> next;

    //boolean isRemoved;

    public MyLockFreeNode(E value){
        this.value = value;
        this.next = new AtomicPair<>(null, false);
    }

    public MyLockFreeNode(E value, MyLockFreeNode<E> next){
        this.value = value;
        this.next = new AtomicPair<>(next, false);
    }
}
