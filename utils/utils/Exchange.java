package utils;

import java.util.IllegalFormatCodePointException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

public class Exchange<T> {
    private static final int EMPTY = 0;
    private static final int WAITING = 1;
    private static final int BUSY = 2;

    private AtomicStampedReference<T> slot = new AtomicStampedReference<>(null, EMPTY);

    public T exchange(T value, long timeout) throws TimeoutException{
        long endTime = System.nanoTime() + timeout;
        while(true){
            if(System.nanoTime() > endTime)
                throw new TimeoutException();
            int[] stamp = new int[1];
            T otherValue = slot.get(stamp);
            switch (stamp[0]){
                case EMPTY -> {
                    if(slot.compareAndSet(otherValue, value, EMPTY, WAITING)){
                        while(System.nanoTime() <= endTime){
                            otherValue = slot.get(stamp);
                            if(stamp[0] == BUSY){
                                //successful exchange
                                slot.set(null, EMPTY);
                                return otherValue;
                            }
                            Thread.yield();
                        }
                        if(slot.compareAndSet(value, null, WAITING, EMPTY)){
                            //value unset, timeout successful
                            throw new TimeoutException();
                        } else {
                            //slot was changed right after timeout, exchange is successful
                            otherValue = slot.get(stamp);
                            slot.set(null, EMPTY);
                            return otherValue;
                        }
                    }
                }
                case WAITING -> {
                    if(slot.compareAndSet(otherValue, value, WAITING, BUSY)){
                        //successful exchange
                        return otherValue;
                    }
                }
                case BUSY -> Thread.onSpinWait();
                default -> throw new IllegalStateException("Unexpected state " + stamp[0] + " found in stamp.");
            }
        }
    }
}
