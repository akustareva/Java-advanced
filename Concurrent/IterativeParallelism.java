package ru.ifmo.ctddev.kustareva.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *  This class calculates results of various
 *  functions using several threads.
 */
public class IterativeParallelism implements ListIP {

    /**
     * Default constructor.
     */
    public IterativeParallelism() {}

    /**
     * The method returns concatenation of the list elements.
     * @param i
     *       Number of threads.
     * @param list
     *       List for processing.
     * @return
     *       Concatenation of the list elements.
     * @throws InterruptedException
     *       If activity was interrupted.
     */
    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        return String.join("", map(i, list, Object::toString));
    }

    /**
     * The method returns elements which satisfy the predicate.
     * @param i
     *      Number of threads.
     * @param list
     *      List for processing.
     * @param predicate
     *      Given predicate.
     * @param <T>
     *     Type of elements in given list.
     * @return
     *     List of elements which satisfy the predicate.
     * @throws InterruptedException
     *     If activity was interrupted.
     */
    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        BinaryOperator<List<T>> filt = (a, b) -> {
            a.addAll(b);
            return a;
        };
        SemiMonoid<List<T>> monoid = new SemiMonoid<>(filt, ArrayList::new);
        BiFunction<List<T>, T, List<T>> bi = (a, b) -> {
            a.addAll(predicate.test(b) ? Collections.singletonList(b) : Collections.emptyList());
            return a;
        };
        return parallel(i, list, monoid, bi);
    }

    /**
     * The method returns elements that are the result of
     * the function.
     * @param i
     *        Number of threads.
     * @param list
     *        List for processing.
     * @param function
     *        Given function.
     * @param <T>
     *        Type of elements in given list.
     * @param <U>
     *        Type of elements in returned list.
     * @return
     *        List of elements that are the result of
     *        the function.
     * @throws InterruptedException
     *        If activity was interrupted.
     */
    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        BinaryOperator<List<U>> m = (a, b) -> {
            a.addAll(b);
            return a;
        };
        SemiMonoid<List<U>> monoid = new SemiMonoid<>(m, ArrayList::new);
        BiFunction<List<U>, T, List<U>> bi = (a, b) -> {
            a.addAll(Arrays.asList(function.apply(b)));
            return a;
        };
        return parallel(i, list, monoid, bi);
    }

    /**
     * The method returns maximum from given list.
     * @param i
     *        Number of threads.
     * @param list
     *        List for processing.
     * @param comparator
     *        Given comparator.
     * @param <T>
     *        Type of elements in given list.
     * @return
     *        Maximum from given list.
     * @throws InterruptedException
     *        If activity was interrupted.
     */
    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        if (list.size() == 0) {
            throw new NoSuchElementException("List is empty");
        }
        BinaryOperator<T> max = (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
        SemiMonoid<T> monoid = new SemiMonoid<>(max, () -> list.get(0));
        return parallel(i, list, monoid, max);
    }

    /**
     * The method returns minimum from given list.
     * @param i
     *        Number of threads.
     * @param list
     *        List for processing.
     * @param comparator
     *        Given comparator.
     * @param <T>
     *        Type of elements in given list.
     * @return
     *        Minimum from given list.
     * @throws InterruptedException
     *        If activity was interrupted.
     */
    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(i, list, comparator.reversed());
    }

    /**
     * The method checks that all elements satisfy the predicate.
     * @param i
     *        Number of threads.
     * @param list
     *        List for processing.
     * @param predicate
     *        Given predicate.
     * @param <T>
     *        Type of elements in given list.
     * @return
     *        Returns <code>true</code> if all elements
     *        satisfy the given predicate and <code>false</code>
     *        otherwise.
     * @throws InterruptedException
     *        If activity was interrupted.
     */
    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        BinaryOperator<Boolean> all = (a, b) -> a && b;
        SemiMonoid<Boolean> monoid = new SemiMonoid<>(all, () -> true);
        BiFunction<Boolean, T, Boolean> bi = (a, b) -> a && predicate.test(b);
        return parallel(i, list, monoid, bi);
    }

    /**
     * The method checks that exists an element that
     * satisfy the predicate.
     * @param i
     *        Number of threads.
     * @param list
     *        List for processing.
     * @param predicate
     *        Given predicate.
     * @param <T>
     *        Type of elements in given list.
     * @return
     *        Returns <code>true</code> if exists an element
     *        that satisfy the predicate and <code>false</code>
     *        otherwise.
     * @throws InterruptedException
     *        If activity was interrupted.
     */
    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !all(i, list, predicate.negate());
    }

    /**
     * The method provides opportunity to calculate
     * result with several threads.
     * @param i
     *        Number of threads.
     * @param list
     *        List for processing.
     * @param monoid
     *        Structure for processing.
     * @param biFunction
     *        Structure for processing.
     * @param <T>
     *        Type of elements in given list.
     * @param <R>
     *        Type of result element.
     * @return
     *        Result of given function.
     * @throws InterruptedException
     *        If activity was interrupted.
     */
    private <T, R> R parallel(int i, List<? extends T> list, SemiMonoid<R> monoid, BiFunction<R, ? super T, R> biFunction) throws InterruptedException{
        if (i > list.size()) {
            i = list.size();
        }
        int sizeOfGroup = list.size() / i;
        int ost = list.size() % i;
        List<R> res = new ArrayList<>(Collections.nCopies(i, null));
        List<Thread> threads = new ArrayList<>();
        int size;
        for (int j = 0, index = 0; j < list.size(); j += size, index++) {
            size = sizeOfGroup;
            if (ost > 0) {
                size++;
                ost--;
            }
            final int ind = index;
            List<? extends T> group = list.subList(j, Math.min(j + size, list.size()));
            Thread thread = new Thread(() -> {
                R r = group.stream().reduce(monoid.getStart(), biFunction, monoid.getOperator());
                synchronized (res) {
                    res.set(ind, r);
                }
            });
            thread.start();
            threads.add(thread);
        }
        for (int index = 0; index < i; index++) {
            threads.get(index).join();
        }
        return res.stream().reduce(monoid.getStart(), monoid.getOperator());
    }
}
