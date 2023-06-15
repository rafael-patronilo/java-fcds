package utils;

import java.util.Objects;

public class AtomicPair<A, B> {
    private A a;
    private B b;

    public AtomicPair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public synchronized A getA() {
        return a;
    }

    public synchronized void setA(A a){
        this.a = a;
    }

    public synchronized B getB(){
        return b;
    }

    public synchronized void setB(B b){
        this.b = b;
    }

    public synchronized Pair<A,B> get(){
        return new Pair<>(a, b);
    }

    public synchronized void set(A a, B b){
        this.a = a;
        this.b = b;
    }

    public synchronized boolean compareAndSet(A expectedA, A newA, B expectedB, B newB){
        if(Objects.equals(a, expectedA) && Objects.equals(b, expectedB)){
            this.a = newA;
            this.b = newB;
            return true;
        } else return false;
    }

    public synchronized boolean compareAndSetA(A expectedA, A newA){
        if(Objects.equals(a, expectedA)){
            this.a = newA;
            return true;
        } else return false;
    }

    public synchronized boolean compareAndSetB(B expectedB, B newB){
        if(Objects.equals(b, expectedB)){
            this.b = newB;
            return true;
        } else return false;
    }

}
