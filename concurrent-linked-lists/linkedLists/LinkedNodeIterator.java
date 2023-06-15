package linkedLists;

import java.util.Iterator;

class LinkedNodeIterator<E> implements Iterator<E> {
    private LinkedListNode<E> node;
    public LinkedNodeIterator(LinkedListNode<E> head){
        node = head;
    }
    @Override
    public boolean hasNext() {
        return node != null;
    }

    @Override
    public E next() {
        E value = node.value;
        node = node.next;
        return value;
    }
}
