package tests;

import mutexes.Filter;
import mutexes.Mutex;

class FilterTest extends MutexTests{

    @Override
    public Mutex getMutex(int n) {
        return new Filter(n);
    }
}