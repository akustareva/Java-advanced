package ru.ifmo.ctddev.kustareva.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 *  This class calculates results of various
 *  functions using several threads.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final Thread[] threadsArr;
    private final Deque<Runnable> queue = new ArrayDeque<>();

    /**
     * Constructor of this class.
     * @param threads
     *        Count of threads.
     */
    public ParallelMapperImpl(int threads) {
        threadsArr = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            threadsArr[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Runnable data;
                        while (!Thread.interrupted()) {
                            synchronized (queue) {
                                while (queue.isEmpty()) {
                                    queue.wait();
                                }
                                data = queue.poll();
                            }
                            if (data != null) {
                                data.run();
                            }
                        }
                    } catch (InterruptedException ignored) {

                    } finally {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            threadsArr[i].start();
        }
    }

    /**
     * The method provides opportunity to calculate
     * result with several threads.
     * @param function Function to operate
     * @param list List for processing
     * @param <T> Type of elements in given list.
     * @param <R> Type of result element.
     * @return Result of given function.
     * @throws InterruptedException If activity was interrupted.
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        List<R> res = new ArrayList<>(Collections.nCopies(list.size(), null));
        AtomicInteger check = new AtomicInteger(0);
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            synchronized (queue) {
                queue.push(() -> {
                    T element = list.get(index);
                    R result = function.apply(element);
                    synchronized (queue) {
                        res.set(index, result);
                        check.incrementAndGet();
                        if (check.get() == res.size()) {
                            queue.notifyAll();
                        }
                    }
                });
                queue.notifyAll();
            }
        }
        synchronized (queue) {
            if (check.get() < res.size()) {
                queue.wait();
            }
        }
        return res;
    }

    /**
     * Auto close.
     * @throws InterruptedException If activity was interrupted.
     */
    @Override
     public void close() throws InterruptedException {
        for (Thread thread : threadsArr) {
            thread.interrupt();
            thread.join();
        }
    }
}
