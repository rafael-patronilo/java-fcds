package linkedLists;

import utils.Pair;

import java.util.Iterator;
import java.util.function.Predicate;

public class MyLockFreeList<E> implements SimplifiedList<E> {
    protected final MyLockFreeNode<E> headPointer = new MyLockFreeNode<>(null);

    @SuppressWarnings("unchecked")
    protected MyLockFreeNode<E> traverseUntil(Predicate<Pair<MyLockFreeNode<E>[], Integer>> predicate){
        MyLockFreeNode<E>[] node = new MyLockFreeNode[]{headPointer, headPointer.next.getA()};
        int i = -1;
        while (!predicate.test(new Pair<>(node, i))) {
            if (node[1] != null && node[1].next.getB()) {
                MyLockFreeNode<E> suc = node[1].next.getA();
                if(node[0].next.compareAndSetA(node[1], suc)) {
                    node[1] = suc;
                } else {
                    node[1] = node[0].next.getA();
                }
                continue;
            }
            node[0] = node[1];
            if(node[0]==null)
                return null;
            node[1] = node[0].next.getA();
            i++;
        }
        return node[0];
    }

    protected MyLockFreeNode<E> getParentOf(E value){
        return traverseUntil(pair-> {
            if(pair.a()[1]==null)
                throw new RuntimeException("No element");
            return value.equals(pair.a()[1].value);
        });
    }

    protected MyLockFreeNode<E> getNode(E value){
        return traverseUntil(pair-> value.equals(pair.a()[0].value));
    }

    protected MyLockFreeNode<E> getLastNode(){
        return traverseUntil(pair->pair.a()[1]==null);
    }

    protected MyLockFreeNode<E> nodeAt(int index){
        return traverseUntil(pair->pair.b()>=index);
    }

    @Override
    public void add(E value) {
        boolean done = false;
        while(!done){
            MyLockFreeNode<E> node = getLastNode();
            done = insertAfter(node, value);
        }
    }

    @Override
    public void insert(int index, E value) {
        boolean done = false;
        while(!done){
            MyLockFreeNode<E> parent = nodeAt(index - 1);
            done = insertAfter(parent, value);
        }
    }

    @Override
    public boolean contains(E value) {
        MyLockFreeNode<E> node = getNode(value);
        return !(node == null || node.next.getB());
    }

    protected boolean insertAfter(MyLockFreeNode<E> node, E value){
        MyLockFreeNode<E> nextNode = node.next.getA();
        return node.next.compareAndSet(nextNode, new MyLockFreeNode<>(value, nextNode), false, false);
    }

    @Override
    public E replace(int index, E value) {
        MyLockFreeNode<E> node = nodeAt(index);
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
        System.out.println("yeah it's called");
        while(!done) {
            MyLockFreeNode<E> parent = nodeAt(index - 1);
            MyLockFreeNode<E> node = parent.next.getA();
            old = node.value;
            MyLockFreeNode<E> child = node.next.getA();
            Thread.yield(); //cause program to fail if not synchronized
            done = node.next.compareAndSet(child, child, false, true) &&
                    parent.next.compareAndSet(node, child, false, false);

        }
        return old;
    }

    @Override
    public void remove(E value){
        boolean done = false;
        while(!done) {
            MyLockFreeNode<E> parent = getParentOf(value);
            if(parent==null)
                return;
            MyLockFreeNode<E> node = parent.next.getA();
            MyLockFreeNode<E> child = node.next.getA();
            Thread.yield(); //cause program to fail if not synchronized
            if(!value.equals(node.value))
                continue;
            done = node.next.compareAndSet(child, child, false, true);
            if(done) parent.next.compareAndSetA(node, child);
        }
    }

    @Override
    public Iterator<E> iterator() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int count(){
        int i = 0;
        MyLockFreeNode<E> node = headPointer.next.getA();
        while (node != null){
            if(!node.next.getB())
                i++;
            node = node.next.getA();
        }
        return i;
    }
}
