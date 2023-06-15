package tests;

import mutexes.Mutex;
import org.junit.platform.commons.annotation.Testable;

public abstract class SpinsTest {

    protected abstract Mutex getMutexWithSpin(int n, Runnable spinAction);

    public class DumbBusyWait extends MutexTests{

        @Override
        public Mutex getMutex(int n) {
            return getMutexWithSpin(n, ()->{});
        }
    }

    public class YieldWait extends MutexTests{

        @Override
        public Mutex getMutex(int n) {
            return getMutexWithSpin(n, Thread::yield);
        }
    }

    public class OnSpinWait extends MutexTests{

        @Override
        public Mutex getMutex(int n) {
            return getMutexWithSpin(n, Thread::onSpinWait);
        }
    }
}
