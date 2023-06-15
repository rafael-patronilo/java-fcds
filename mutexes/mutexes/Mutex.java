package mutexes;

public interface Mutex {
    void lock(int index);
    void unlock(int index);
}
