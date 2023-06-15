package tests;

import linkedLists.LockFreeList;
import linkedLists.SimplifiedList;

public class LockFreeTest extends ListTest {

    @Override
    <E> SimplifiedList<E> createList() {
        return new LockFreeList<>();
    }
}
