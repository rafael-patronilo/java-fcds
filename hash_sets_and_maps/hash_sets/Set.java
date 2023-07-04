package hash_sets;

public interface Set<E> {
    boolean add(E item);
    boolean contains(E item);

    boolean remove(E item);
}
