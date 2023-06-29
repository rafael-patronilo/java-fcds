package queues;

import utils.BusyStructureException;

public interface Queue<E> {
    void enqueue(E item);
    E dequeue();

    void tryEnqueue(E item) throws BusyStructureException;
    E tryDequeue() throws BusyStructureException;
}
