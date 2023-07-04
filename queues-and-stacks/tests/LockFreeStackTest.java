package tests;

import stacks.LockFreeStack;
import stacks.Stack;

public class LockFreeStackTest extends StackTest {

    @Override
    protected <E> Stack<E> createStack() {
        return new LockFreeStack<>();
    }
}
