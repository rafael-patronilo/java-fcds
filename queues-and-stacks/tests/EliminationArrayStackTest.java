package tests;

import stacks.EliminationArrayStack;
import stacks.Stack;

public class EliminationArrayStackTest extends StackTest {

    @Override
    protected <E> Stack<E> createStack() {
        return new EliminationArrayStack<>();
    }
}
