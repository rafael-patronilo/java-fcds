package stacks;

import linkedLists.LockFreeNode;
import utils.ExponentialBackoff;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class EliminationArrayStack<E> implements Stack<E> {
    private static final int TIMEOUT = 50;
    private static final int ELIMINATION_ARRAY_SIZE = 10;
    private AtomicMarkableReference<LockFreeNode<E>> top = new AtomicMarkableReference<>(null, false);
    private EliminationArray<E> eliminationArray = new EliminationArray<>(ELIMINATION_ARRAY_SIZE, TIMEOUT);



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
                try{
                    E exchanged = eliminationArray.visit(item, ELIMINATION_ARRAY_SIZE);
                    if(exchanged == null){
                        //exchanged with a pop, elimination successful
                        return;
                    }
                } catch (TimeoutException exception){
                    // timeout, no exchange occurred, try again
                }
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
                try{
                    E exchanged = eliminationArray.visit(null, ELIMINATION_ARRAY_SIZE);
                    if(exchanged != null){
                        //exchanged with a push, elimination successful
                        return exchanged;
                    }
                } catch (TimeoutException exception){
                    // timeout, no exchange occurred, try again
                }
            }
        }
    }
}
