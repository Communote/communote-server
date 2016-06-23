package com.communote.server.core.vo.query.logical;

/**
 * An abstract class which allows combining of tags by boolean logic. For example: (("TagA" and
 * "TagB") or ("TagC" and not "TagD"))
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LogicalTagFormula {

    /**
     * determines whether the formula is negated
     */
    private boolean negated;

    /**
     * 
     * @return the number of of atomic tags to filter for
     */
    public abstract int countAtomicTags();

    /**
     * {@inheritDoc}
     */
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
        final LogicalTagFormula other = (LogicalTagFormula) obj;
        if (negated != other.negated) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        return result;
    }

    /**
     * Returns whether the formula is negated.
     * 
     * @return the negation state
     */
    public boolean isNegated() {
        return negated;
    }

    /**
     * Changes the negated property.
     * 
     * @param negated
     *            the negation state
     */
    public void setNegated(boolean negated) {
        this.negated = negated;
    }

}
