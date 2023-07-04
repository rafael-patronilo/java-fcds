package stacks;

import utils.Exchange;

import java.util.Random;
import java.util.concurrent.TimeoutException;

public class EliminationArray<T> {
    private final int timeout;
    private final Exchange<T>[] slots;
    private final Random random;

    @SuppressWarnings("unchecked")
    public EliminationArray(int capacity, int timeout){
        slots = (Exchange<T>[]) new Exchange[capacity];
        for (int i = 0; i < capacity; i++) {
            slots[i] = new Exchange<>();
        }
        random = new Random();
        this.timeout = timeout;
    }

    public T visit(T value, int range) throws TimeoutException {
        int slot = random.nextInt(range);
        return slots[slot].exchange(value, timeout);
    }
}
