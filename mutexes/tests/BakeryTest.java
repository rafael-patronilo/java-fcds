package tests;

import mutexes.Bakery;
import mutexes.Mutex;

public class BakeryTest extends MutexTests{
    @Override
    public Mutex getMutex(int n) {
        return new Bakery(n);
    }
}
