package tests;

import linkedLists.LazySynchronizationList;
import linkedLists.SimplifiedList;

public class LazySynchronizationTest extends ListTest {
    @Override
    <E> SimplifiedList<E> createList() {
        return new LazySynchronizationList<>();
    }
}
