package net.greenfieldmc.core;

/**
 * A container for three elements.
 * @param <F> type of the first element
 * @param <S> type of the second element
 * @param <T> type of the third element
 */
public class Triple<F, S, T> {
    private final F first;
    private final S second;
    private final T third;

    /**
     * Creates a new triple with the given elements.
     * @param first the first element
     * @param second the second element
     * @param third the third element
     */
    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Gets the first element.
     * @return the first element
     */
    public F getFirst() {
        return first;
    }

    /**
     * Gets the second element.
     * @return the second element
     */
    public S getSecond() {
        return second;
    }

    /**
     * Gets the third element.
     * @return the third element
     */
    public T getThird() {
        return third;
    }

    /**
     * Creates a new triple with the given elements.
     * @param first the first element
     * @param second the second element
     * @param third the third element
     * @return a new triple
     * @param <F> type of the first element
     * @param <S> type of the second element
     * @param <T> type of the third element
     */
    public static <F, S, T> Triple<F, S, T> of(F first, S second, T third) {
        return new Triple<>(first, second, third);
    }

}