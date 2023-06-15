package linkedLists;

import utils.Pair;

import java.util.Iterator;
import java.util.function.Predicate;

// Always lock
public class CoarseGrainedLockingList<E> implements SimplifiedList<E> {
    protected final LinkedListNode<E> headPointer = new LinkedListNode<>(null);

    protected LinkedListNode<E> traverseUntil(Predicate<Pair<LinkedListNode<E>, Integer>> predicate){
        LinkedListNode<E> node = headPointer;
        int i = -1;
        while(node != null && !predicate.test(new Pair<>(node, i++))){
            node = node.next;
        }
        return node;
    }

    protected LinkedListNode<E> getParentOf(E value){
        return traverseUntil(pair-> {
            if(pair.a().next==null)
                throw new RuntimeException("No element");
            return value.equals(pair.a().next.value);
        });
    }

    protected LinkedListNode<E> getLastNode(){
        return traverseUntil(pair->pair.a().next==null);
    }

    protected LinkedListNode<E> nodeAt(int index){
        return traverseUntil(pair->pair.b()>=index);
    }

    @Override
    public synchronized void add(E value) {
        LinkedListNode<E> node = getLastNode();
        insertAfter(node, value);
    }

    @Override
    public synchronized void insert(int index, E value) {
        LinkedListNode<E> parent = nodeAt(index - 1);
        insertAfter(parent, value);
    }

    @Override
    public synchronized boolean contains(E value) {
        return traverseUntil(x->value.equals(x.a().value)) != null;
    }

    protected void insertAfter(LinkedListNode<E> node, E value){
        node.next = new LinkedListNode<>(value, node.next);
    }

    @Override
    public synchronized E replace(int index, E value) {
        LinkedListNode<E> node = nodeAt(index);
        E old = node.value;
        node.value = value;
        return old;
    }

    @Override
    public E get(int index) {
        return nodeAt(index).value;
    }

    @Override
    public synchronized E removeAt(int index) {
        LinkedListNode<E> parent = nodeAt(index-1);
        LinkedListNode<E> node = parent.next;
        E old = node.value;
        parent.next = node.next;
        return old;
    }

    @Override
    public synchronized void remove(E value){
        LinkedListNode<E> parent = getParentOf(value);
        LinkedListNode<E> node = parent.next;
        Thread.yield(); //cause program to fail if not synchronized
        parent.next = node.next;
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedNodeIterator<E>(headPointer.next);
    }

    @Override
    public int count(){
        int i = 0;
        for(E ignored : this){
            i++;
        }
        return i;
    }
}
