package tests;

import linkedLists.HandInHandLockingList;
import linkedLists.SimplifiedList;

public class HandInHandLockingTest extends ListTest{
    @Override
    <E> SimplifiedList<E> createList() {
        return new HandInHandLockingList<>();
    }
}
