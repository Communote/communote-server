package com.communote.server.core.vo.query.logical;

import org.apache.commons.lang.StringUtils;

/**
 * Represents an atom within a logical tag formula. An atom is described by the value of the
 * associated tag and a flag denoting whether it is negated.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AtomicTagFormula extends LogicalTagFormula {

    private String tag;

    public AtomicTagFormula(String tag, boolean negated) {
        if (StringUtils.isBlank(tag)) {
            throw new IllegalArgumentException("The tag must not be blank");
        }
        this.tag = tag;
        setNegated(negated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countAtomicTags() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AtomicTagFormula other = (AtomicTagFormula) obj;
        if (tag == null) {
            if (other.tag != null) {
                return false;
            }
        } else if (!tag.equals(other.tag)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the value of the associated tag.
     * 
     * @return the tag value
     */
    public String getTag() {
        return tag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (tag == null ? 0 : tag.hashCode());
        return result;
    }

    /**
     * Sets the tag value.
     * 
     * @param tag
     *            the tag value
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
}
