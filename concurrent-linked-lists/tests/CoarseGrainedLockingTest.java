package tests;

import linkedLists.CoarseGrainedLockingList;
import linkedLists.SimplifiedList;

public class CoarseGrainedLockingTest extends ListTest{
    @Override
    <E> SimplifiedList<E> createList() {
        return new CoarseGrainedLockingList<>();
    }
}
