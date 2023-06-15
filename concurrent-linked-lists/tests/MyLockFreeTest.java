package tests;


import linkedLists.MyLockFreeList;
import linkedLists.SimplifiedList;

public class MyLockFreeTest extends ListTest {
    @Override
    <E> SimplifiedList<E> createList() {
        return new MyLockFreeList<>();
    }
}
