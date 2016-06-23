package com.communote.common.util;

/**
 * Simple Pair.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <Left>
 * @param <Right>
 */
public class Pair<Left, Right> {

    private Left left;

    private Right right;

    /**
     * Constructor.
     * 
     * @param left
     *            Left.
     * @param right
     *            Right.
     */
    public Pair(Left left, Right right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Pair other = (Pair) obj;
        if (left == null) {
            if (other.left != null) {
                return false;
            }
        } else if (!left.equals(other.left)) {
            return false;
        }
        if (right == null) {
            if (other.right != null) {
                return false;
            }
        } else if (!right.equals(other.right)) {
            return false;
        }
        return true;
    }

    /**
     * @return the left
     */
    public Left getLeft() {
        return left;
    }

    /**
     * @return the right
     */
    public Right getRight() {
        return right;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
        return result;
    }

    /**
     * @param left
     *            the left to set
     */
    public void setLeft(Left left) {
        this.left = left;
    }

    /**
     * @param right
     *            the right to set
     */
    public void setRight(Right right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return "Pair [left=" + left + ", right=" + right + "]";
    }

}
