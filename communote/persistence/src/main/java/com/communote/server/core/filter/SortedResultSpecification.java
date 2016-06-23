package com.communote.server.core.filter;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SortedResultSpecification extends com.communote.server.core.filter.ResultSpecification
implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -9023681170161303498L;

    private com.communote.server.core.filter.SortType sortType;

    public SortedResultSpecification() {
        super();
    }

    public SortedResultSpecification(com.communote.server.core.filter.SortType sortType,
            int offset, int numberOfElements, int checkAtLeastMoreResults) {
        super(offset, numberOfElements, checkAtLeastMoreResults);
        this.sortType = sortType;
    }

    public SortedResultSpecification(int offset, int numberOfElements) {
        super(offset, numberOfElements);
    }

    /**
     * Copies constructor from other SortedResultSpecification
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public SortedResultSpecification(SortedResultSpecification otherBean) {
        this(otherBean.getSortType(), otherBean.getOffset(), otherBean.getNumberOfElements(),
                otherBean.getCheckAtLeastMoreResults());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(SortedResultSpecification otherBean) {
        if (otherBean != null) {
            this.setSortType(otherBean.getSortType());
            this.setOffset(otherBean.getOffset());
            this.setNumberOfElements(otherBean.getNumberOfElements());
            this.setCheckAtLeastMoreResults(otherBean.getCheckAtLeastMoreResults());
        }
    }

    /**
     *
     */
    public com.communote.server.core.filter.SortType getSortType() {
        return this.sortType;
    }

    public void setSortType(com.communote.server.core.filter.SortType sortType) {
        this.sortType = sortType;
    }

}