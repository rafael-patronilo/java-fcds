package utils;

import java.util.Random;

public class ExponentialBackoff {
    private int min, max;
    private int limit;
    private Random random;
    public ExponentialBackoff(int min, int max){
        this.min = min;
        this.max = max;
        limit = min;
        random = new Random();
    }

    public void backoff() {
        int delay = random.nextInt(limit);
        limit = Math.min(max, limit*2);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) { }
    }
}
