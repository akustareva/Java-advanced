package ru.ifmo.ctddev.kustareva.concurrent;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * Structure with binary operation and
 * neutral element.
 * @param <T>
 *     Type of neutral element.
 */

public class SemiMonoid<T> {
    /**
     * Binary operation.
     */
    final BinaryOperator<T> operator;
    /**
     * Neutral element.
     */
    final Supplier<T> res;

    /**
     * Constructor.
     * @param op
     *     Binary operation.
     * @param r
     *     Neutral element.
     */
    public SemiMonoid (BinaryOperator<T> op, Supplier<T> r) {
        this.operator = op;
        this.res = r;
    }

    /**
     * Due to this method you can get neutral element.
     * @return
     *     Neutral element.
     */
    public T getStart() {
        return res.get();
    }

    /**
     * Due to this method you can get binary operation.
     * @return
     *      Binary operation.
     */
    public BinaryOperator<T> getOperator() {
        return operator;
    }
}
