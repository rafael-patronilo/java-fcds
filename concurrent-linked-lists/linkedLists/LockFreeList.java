package linkedLists;

import utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class LockFreeList<E> implements SimplifiedList<E> {
    protected final LockFreeNode<E> headPointer = new LockFreeNode<>(null);

    @SuppressWarnings("unchecked")
    protected LockFreeNode<E> traverseUntil(Predicate<Pair<LockFreeNode<E>[], Integer>> predicate){
        retry: while (true) {
            LockFreeNode<E>[] node = new LockFreeNode[]{headPointer, headPointer.next.getReference()};
            int i = -1;
            while (!predicate.test(new Pair<>(node, i))) {
                if (node[1] != null && node[1].next.isMarked()) {
                    LockFreeNode<E> suc = node[1].next.getReference();
                    if(!node[0].next.compareAndSet(node[1], suc, false, false)){
                        continue retry;
                    }
                    node[1] = suc;
                    continue;
                }
                node[0] = node[1];
                if(node[0] == null)
                    return null;
                node[1] = node[0].next.getReference();
                i++;
            }
            return node[0];
        }
    }

    protected LockFreeNode<E> getParentOf(E value){
        return traverseUntil(pair-> {
            if(pair.a()[1]==null)
                throw new RuntimeException("No element " + value);
            return value.equals(pair.a()[1].value);
        });
    }

    protected LockFreeNode<E> getNode(E value){
        return traverseUntil(pair-> value.equals(pair.a()[0].value));
    }

    protected LockFreeNode<E> getLastNode(){
        return traverseUntil(pair->pair.a()[0].next.getReference()==null);
    }

    protected LockFreeNode<E> nodeAt(int index){
        return traverseUntil(pair->pair.b()>=index);
    }

    @Override
    public void add(E value) {
        LockFreeNode<E> node = getLastNode();
        boolean done = insertAfter(node, value);
        while(!done){
            node = getLastNode();
            done = insertAfter(node, value);
        }
    }

    @Override
    public void insert(int index, E value) {
        LockFreeNode<E> parent = nodeAt(index - 1);
        boolean done = insertAfter(parent, value);
        while(!done){
            parent = nodeAt(index - 1);
            done = insertAfter(parent, value);
        }
    }

    @Override
    public boolean contains(E value) {
        LockFreeNode<E> node = getNode(value);
        return !(node == null || node.next.isMarked());
    }

    protected boolean insertAfter(LockFreeNode<E> node, E value){
        Thread.yield();
        LockFreeNode<E> nextNode = node.next.getReference();
        return node.next.compareAndSet(nextNode, new LockFreeNode<>(value, nextNode), false, false);
    }

    @Override
    public E replace(int index, E value) {
        LockFreeNode<E> node = nodeAt(index);
        E old = node.value;
        node.value = value;
        return old;
    }

    @Override
    public E get(int index) {
        return nodeAt(index).value;
    }

    @Override
    public E removeAt(int index) {
        boolean done = false;
        E old = null;
        while(!done) {
            LockFreeNode<E> parent = nodeAt(index - 1);
            LockFreeNode<E> node = parent.next.getReference();
            old = node.value;
            LockFreeNode<E> child = node.next.getReference();
            Thread.yield(); //cause program to fail if not synchronized
            done = node.next.compareAndSet(child, child, false, true) &&
                    parent.next.compareAndSet(node, child, false, false);
        }
        return old;
    }

    @Override
    public void remove(E value){
        boolean done = false;
        int counter = 0;
        while(!done) {
            counter++;
            LockFreeNode<E> parent = getParentOf(value);
            if(parent==null)
                return;
            LockFreeNode<E> node = parent.next.getReference();
            LockFreeNode<E> child = node.next.getReference();
            Thread.yield(); //cause program to fail if not synchronized
            if(!value.equals(node.value))
                continue;
            done = node.next.compareAndSet(child, child, false, true);
            if(done)
                parent.next.compareAndSet(node, child, false, false);
        }
    }

    @Override
    public Iterator<E> iterator() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int count(){
        int i = 0;
        LockFreeNode<E> node = headPointer.next.getReference();
        while (node != null){
            if(!node.next.isMarked())
                i++;
            node = node.next.getReference();
        }
        return i;
    }
}
