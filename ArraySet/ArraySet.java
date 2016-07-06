package ru.ifmo.ctddev.kustareva.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> list;
    private Comparator<? super T> comparator;

    public ArraySet() {
        list = Collections.emptyList();
    }

    public ArraySet(Collection<? extends T>  collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends T>  collection, Comparator<? super T> comparator) {
        this.comparator = comparator;
        list = new ArrayList<>();
        TreeSet<T> tmp = new TreeSet<>(comparator);
        tmp.addAll(collection);
        list.addAll(tmp);
    }

    private ArraySet(List<T> list, Comparator<? super T> comparator, boolean alreadySorted) {
        this.list = list;
        this.comparator = comparator;
    }

    private T getElement(int i) {
        if (i >= 0 && i < size()) {
            return list.get(i);
        }
        return null;
    }

    private int checkResult(int i, boolean strict) {
        if (i >= 0) {
            return strict ? i - 1 : i;
        }
        return (-i - 2);
    }

    @Override
    public T lower(T t) {
        int i = checkResult(Collections.binarySearch(list, t, comparator), true);
        return getElement(i);
    }

    @Override
    public T floor(T t) {
        int i = checkResult(Collections.binarySearch(list, t, comparator), false);
        return getElement(i);
    }

    @Override
    public T ceiling(T t) {
        int i = checkResult(Collections.binarySearch(list, t, comparator), true);
        return getElement(i + 1);
    }

    @Override
    public T higher(T t) {
        int i = checkResult(Collections.binarySearch(list, t, comparator), false);
        return getElement(i + 1);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("pollFirst()");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("pollLast()");
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(list, Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int inFrom = checkResult(Collections.binarySearch(list, fromElement, comparator), fromInclusive) + 1;
        int inTo = checkResult(Collections.binarySearch(list, toElement, comparator), !toInclusive);
        if (inFrom > inTo) {
            return new ArraySet<>();
        }
        return new ArraySet<>(list.subList(inFrom, inTo + 1), comparator, true);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        int inTo = checkResult(Collections.binarySearch(list, toElement, comparator), !inclusive);
        return new ArraySet<>(list.subList(0, inTo + 1), comparator, true);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        int inFrom = checkResult(Collections.binarySearch(list, fromElement, comparator), inclusive) + 1;
        return new ArraySet<>(list.subList(inFrom, size()), comparator, true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        if (size() == 0) {
            throw new NoSuchElementException("first()");
        }
        return getElement(0);
    }

    @Override
    public T last() {
        if (size() == 0) {
            throw new NoSuchElementException("last()");
        }
        return getElement(size() - 1);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean contains(Object o) {
        @SuppressWarnings("unchecked")
        int check = Collections.binarySearch(list, (T) o, comparator);
        return (check >= 0);
    }
}
