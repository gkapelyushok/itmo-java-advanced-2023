package info.kgeorgiy.ja.kapelyushok.arrayset;

import java.util.*;

public class ArraySet<E extends Comparable<E>> extends AbstractSet<E> implements SortedSet<E>{
    private final List<E> data;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(List.of(), Comparator.naturalOrder());
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, Comparator.naturalOrder());
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(List.of(), comparator);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        TreeSet<E> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        data = new ArrayList<>(treeSet);
        this.comparator = comparator;
    }

    private ArraySet(List<E> list, Comparator<? super E> comparator) {
        data = list;
        this.comparator = comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        if (comparator.equals(Comparator.naturalOrder())) {
            return null;
        }
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return subSet(getIndex(fromElement), getIndex(toElement));
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return subSet(0, getIndex(toElement));
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return subSet(getIndex(fromElement), size());
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return get(size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return binarySearch((E) o) >= 0;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    //:note: useless

    private int binarySearch(E element) {
        return Collections.binarySearch(data, element, comparator());
    }

    private int getIndex(E element) {
        int index = binarySearch(element);
        return index >= 0 ? index : -index - 1;
    }

    private int compare(E element1, E element2) {
        return comparator.compare(element1, element2);
    }

    private ArraySet<E> subSet(int fromIndex, int toIndex) {
        return new ArraySet<>(data.subList(fromIndex, toIndex), comparator());
    }

    private E get(int index) {
        return data.get(index);
    }
}
