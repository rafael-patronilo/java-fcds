package mutexes;

import java.util.Arrays;

public class Bakery implements Mutex{
    volatile boolean[] flag;
    volatile int[] label;

    private Runnable spinAction = ()->{};

    private final int n;
    public Bakery(int n){
        this.n = n;
        flag = new boolean[n];
        label = new int[n];
    }

    public Bakery(int n, Runnable spinAction){
        this.n = n;
        this.spinAction = spinAction;
        flag = new boolean[n];
        label = new int[n];
    }

    private boolean shouldWait(int threadId){
        for(int i = 0; i < n; i++){
            if(i != threadId && flag[i]){
                if(label[threadId] > label[i] ||
                        (label[threadId] == label[i] && threadId > i)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void lock(int index) {
        flag[index] = true;
        label[index] = Arrays.stream(label).max().orElse(0) + 1;
        while (shouldWait(index)){
            Thread.onSpinWait();
        }
    }

    @Override
    public void unlock(int index) {
        flag[index] = false;
    }
}
