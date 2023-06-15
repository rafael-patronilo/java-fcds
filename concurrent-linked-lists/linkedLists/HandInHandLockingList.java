package linkedLists;

import utils.Pair;

import java.util.Iterator;
import java.util.function.Predicate;

public class HandInHandLockingList<E> implements SimplifiedList<E> {
    protected final LinkedListNode<E> headPointer = new LinkedListNode<>(null);

    protected LinkedListNode<E> traverseUntil(Predicate<Pair<LinkedListNode<E>, Integer>> predicate){
        LinkedListNode<E> node = headPointer;
        int i = -1;
        while(node != null && !predicate.test(new Pair<>(node, i++))){
            node = node.next;
        }
        return node;
    }

    protected LinkedListNode<E> traverseUntilLocking(Predicate<Pair<LinkedListNode<E>, Integer>> predicate){
        LinkedListNode<E> node = headPointer;
        int i = -1;
        node.lock.lock();
        while(!predicate.test(new Pair<>(node, i++))){
            LinkedListNode<E> old = node;
            node = node.next;
            if(node == null){
                old.lock.unlock();
                return null;
            }
            node.lock.lock();
            old.lock.unlock();
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

    protected LinkedListNode<E> getParentOfLocking(E value){
        return traverseUntilLocking(pair-> {
            if(pair.a().next==null)
                throw new RuntimeException("No element");
            return value.equals(pair.a().next.value);
        });
    }

    protected LinkedListNode<E> getLastNode(){
        return traverseUntil(pair->pair.a().next==null);
    }

    protected LinkedListNode<E> getLastNodeLocking(){
        return traverseUntilLocking(pair->pair.a().next==null);
    }

    protected LinkedListNode<E> nodeAt(int index){
        return traverseUntil(pair->pair.b()>=index);
    }

    protected LinkedListNode<E> nodeAtLocking(int index){
        return traverseUntilLocking(pair->pair.b()>=index);
    }

    @Override
    public void add(E value) {
        LinkedListNode<E> node = getLastNodeLocking();
        insertAfter(node, value);
    }

    @Override
    public void insert(int index, E value) {
        LinkedListNode<E> parent = nodeAtLocking(index - 1);
        insertAfter(parent, value);
    }

    protected void insertAfter(LinkedListNode<E> node, E value){
        LinkedListNode<E> nextNode = node.next;
        if(nextNode != null){
            nextNode.lock.lock();
        }
        node.next = new LinkedListNode<>(value, nextNode);
        if(nextNode != null){
            nextNode.lock.unlock();
        }
        node.lock.unlock();
    }

    @Override
    public E replace(int index, E value) {
        LinkedListNode<E> node = nodeAtLocking(index);
        E old = node.value;
        node.value = value;
        node.lock.unlock();
        return old;
    }

    @Override
    public E get(int index) {
        return nodeAt(index).value;
    }

    @Override
    public E removeAt(int index) {
        LinkedListNode<E> parent = nodeAtLocking(index-1);
        LinkedListNode<E> node = parent.next;
        node.lock.lock();
        E old = node.value;
        parent.next = node.next;
        parent.lock.unlock();
        node.lock.unlock();
        return old;
    }

    @Override
    public void remove(E value){
        LinkedListNode<E> parent = getParentOfLocking(value);
        LinkedListNode<E> node = parent.next;
        node.lock.lock();
        Thread.yield(); //cause program to fail if not synchronized
        parent.next = node.next;
        parent.lock.unlock();
        node.lock.unlock();
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedNodeIterator<E>(headPointer.next);
    }

    @Override
    public boolean contains(E value) {
        LinkedListNode<E> node = traverseUntilLocking(x->value.equals(x.a().value));
        try{
            return node != null;
        } finally {
            if(node!=null){
                node.lock.unlock();
            }
        }
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
