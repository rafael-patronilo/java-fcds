package mutexes;

import java.util.Arrays;

public class Filter implements Mutex {
    private volatile int[] level;
    private volatile int[] victim;

    private Runnable spinAction = ()->{};
    private final int n;

    public Filter(int n){
        this.n = n;
        level = new int[n];
        victim = new int[n];
    }

    public Filter(int n, Runnable spinAction){
        this.n = n;
        this.spinAction = spinAction;
        level = new int[n];
        victim = new int[n];
    }

    @Override
    public void lock(int i) {
        for(int L = 1; L < n; L++){
            level[i] = L;
            victim[L] = i;
            final int fL = L;
            while(Arrays.stream(level).filter(x -> x >= fL).count() > 1 && victim[L]==i){
                spinAction.run();
            }
        }
    }

    @Override
    public void unlock(int i) {
        level[i] = 0;
    }
}
