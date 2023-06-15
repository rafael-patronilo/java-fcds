package linkedLists;

public interface SimplifiedList<E> extends Iterable<E> {
    void add(E value);
    void insert(int index, E value);

    boolean contains(E value);

    E replace(int index, E value);

    E get(int index);

    E removeAt(int index);

    void remove(E value);

    int count();
}
