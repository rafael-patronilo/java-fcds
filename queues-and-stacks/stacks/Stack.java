package stacks;

import utils.BusyStructureException;

public interface Stack<E> {
    void push(E item);
    E pop();
}
