package com.communote.common.util;

import java.util.Comparator;

/**
 * Generic Comparator for Pairs
 * 
 * @author cte
 * 
 * @param <Left>
 *            the left side
 * @param <Right>
 *            the right side
 */

public class PairComparator<Left, Right>
        implements
        Comparator<Pair<Left, Right>> {

    /**
     * Creating a constructor for a comparison on the left side of the pair
     * 
     * @param leftComparator
     *            the comparator
     * @param <L>
     *            generic left side object
     * @param <R>
     *            generic right side object
     * 
     * @return the constructor
     */
    public static <L, R> PairComparator<L, R> createLeftSidePairComparator(
            Comparator<L> leftComparator) {
        return new PairComparator<L, R>(leftComparator, null);
    }

    /**
     * Creating a constructor for a comparison on the right side of the pair
     * 
     * @param rightComparator
     *            the comparator
     * @param <L>
     *            generic left side object
     * @param <R>
     *            generic right side object
     * @return the constructor
     */
    public static <L, R> PairComparator<L, R> createRightSidePairComparator(
            Comparator<R> rightComparator) {
        return new PairComparator<L, R>(null, rightComparator);
    }

    private final Comparator<Left> leftComparator;
    private final Comparator<Right> rightComparator;

    /**
     * Constructor Template
     * 
     * @param leftComparator
     *            the left side of the pair
     * @param rightComparator
     *            the right side of the pair
     */
    private PairComparator(Comparator<Left> leftComparator, Comparator<Right> rightComparator) {
        this.leftComparator = leftComparator;
        this.rightComparator = rightComparator;
    }

    @Override
    public int compare(Pair<Left, Right> o1, Pair<Left, Right> o2) {
        if (leftComparator != null) {
            return leftComparator.compare(o1.getLeft(), o2.getLeft());
        } else {
            return rightComparator.compare(o1.getRight(), o2.getRight());
        }

    }

}