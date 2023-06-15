package linkedLists;

import utils.Pair;

import java.util.Iterator;
import java.util.function.Predicate;

public class LazySynchronizationList<E> implements SimplifiedList<E> {
    protected final LinkedListNode<E> headPointer = new LinkedListNode<>(null);

    @SuppressWarnings("unchecked")
    protected LinkedListNode<E> traverseUntil(Predicate<Pair<LinkedListNode<E>[], Integer>> predicate){
        LinkedListNode<E>[] node = new LinkedListNode[]{headPointer, headPointer.next};
        int i = -1;
        while(!predicate.test(new Pair<>(node, i++))){
            node[0] = node[1];
            if(node[0] == null)
                return null;
            node[1] = node[0].next;
        }
        return node[0];
    }

    protected boolean isReachable(LinkedListNode<E> node){
        return traverseUntil(pair-> pair.a()[0] == node) == node;
    }

    protected boolean areAdjacent(LinkedListNode<E> a, LinkedListNode<E> b){
        return a.next == b;
    }

    protected LinkedListNode<E> getParentOf(E value){
        return traverseUntil(pair-> {
            if(pair.a()[1]==null)
                throw new RuntimeException("No element");
            return value.equals(pair.a()[1].value);
        });
    }

    protected LinkedListNode<E> getNode(E value){
        return traverseUntil(pair-> value.equals(pair.a()[0].value));
    }

    protected LinkedListNode<E> getLastNode(){
        return traverseUntil(pair->pair.a()[1]==null);
    }

    protected LinkedListNode<E> nodeAt(int index){
        return traverseUntil(pair->pair.b()>=index);
    }

    @Override
    public void add(E value) {
        LinkedListNode<E> node = getLastNode();
        boolean done = insertAfter(node, value);
        while(!done){
            node = getLastNode();
            done = insertAfter(node, value);
        }
    }

    @Override
    public void insert(int index, E value) {
        LinkedListNode<E> parent = nodeAt(index - 1);
        boolean done = insertAfter(parent, value);
        while(!done){
            parent = nodeAt(index - 1);
            done = insertAfter(parent, value);
        }
    }

    @Override
    public boolean contains(E value) {
        LinkedListNode<E> node = getNode(value);
        return !(node == null || node.isRemoved);
    }

    protected boolean insertAfter(LinkedListNode<E> node, E value){
        boolean result = true;
        node.lock.lock();
        LinkedListNode<E> nextNode = node.next;
        if(nextNode != null){
            nextNode.lock.lock();
        }
        if(!node.isRemoved && (nextNode == null || (areAdjacent(node, nextNode) && !nextNode.isRemoved))){
            node.next = new LinkedListNode<>(value, node.next);
        }
        else {
            result = false;
        }

        if(nextNode != null){
            nextNode.lock.unlock();
        }
        node.lock.unlock();
        return result;
    }

    @Override
    public E replace(int index, E value) {
        LinkedListNode<E> node = nodeAt(index);
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
        boolean done = false;
        E old = null;
        while(!done) {
            LinkedListNode<E> parent = nodeAt(index - 1);
            parent.lock.lock();
            LinkedListNode<E> node = parent.next;
            node.lock.lock();
            if(!parent.isRemoved && areAdjacent(parent, node)){
                node.isRemoved = true;
                old = node.value;
                parent.next = node.next;
                done = true;
            } else if(node.isRemoved){
                done = true;
            }
            parent.lock.unlock();
            node.lock.unlock();
        }
        return old;
    }

    @Override
    public void remove(E value){
        boolean done = false;
        while(!done) {
            LinkedListNode<E> parent = getParentOf(value);
            parent.lock.lock();
            LinkedListNode<E> node = parent.next;
            node.lock.lock();
            Thread.yield(); //cause program to fail if not synchronized
            if(!parent.isRemoved && areAdjacent(parent, node) && node.value.equals(value)){
                node.isRemoved = true;
                parent.next = node.next;
                done = true;
            } else if(node.isRemoved && node.value.equals(value)){
                done = true;
            }
            parent.lock.unlock();
            node.lock.unlock();
        }
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
