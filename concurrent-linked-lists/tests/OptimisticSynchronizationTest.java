package tests;

import linkedLists.OptimisticSynchronizationList;
import linkedLists.SimplifiedList;

public class OptimisticSynchronizationTest extends ListTest {
    @Override
    <E> SimplifiedList<E> createList() {
        return new OptimisticSynchronizationList<>();
    }
}
