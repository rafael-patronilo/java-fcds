package stacks;

import linkedLists.LockFreeNode;
import utils.ExponentialBackoff;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeStack<E> implements Stack<E> {
    private static final int MIN_DELAY = 10, MAX_DELAY = 50;
    private AtomicMarkableReference<LockFreeNode<E>> top = new AtomicMarkableReference<>(null, false);
    ExponentialBackoff backoff = new ExponentialBackoff(MIN_DELAY, MAX_DELAY);


    private boolean pushOnStack(LockFreeNode<E> node){
        LockFreeNode<E> currentTop = top.getReference();
        node.next.set(currentTop, false);
        return top.compareAndSet(currentTop, node, false, false);
    }

    private E popFromStack(){
        LockFreeNode<E> currentTop = top.getReference();
        if(currentTop == null) return null;
        LockFreeNode<E> nextTop = currentTop.next.getReference();
        if(top.compareAndSet(currentTop, nextTop, false, false)){
            return currentTop.value;
        } else {
            return null;
        }
    }

    @Override
    public void push(E item) {
        LockFreeNode<E> node = new LockFreeNode<>(item);
        while(true){
            if(pushOnStack(node)){
                return;
            } else {
                backoff.backoff();
            }
        }
    }

    @Override
    public E pop() {
        while(true){
            E value = popFromStack();
            if(value != null){
                return value;
            } else {
                backoff.backoff();
            }
        }
    }
}
