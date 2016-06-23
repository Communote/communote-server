package com.communote.server.core.filter;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResultSpecification implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2828540514529281070L;

    private int offset;

    private int numberOfElements;

    private int checkAtLeastMoreResults = 0;

    public ResultSpecification() {
        this.offset = 0;
        this.numberOfElements = 0;
    }

    public ResultSpecification(int offset, int numberOfElements) {
        this.offset = offset;
        this.numberOfElements = numberOfElements;
    }

    public ResultSpecification(int offset, int numberOfElements, int checkAtLeastMoreResults) {
        this.offset = offset;
        this.numberOfElements = numberOfElements;
        this.checkAtLeastMoreResults = checkAtLeastMoreResults;
    }

    /**
     * Copies constructor from other ResultSpecification
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public ResultSpecification(ResultSpecification otherBean) {
        this(otherBean.getOffset(), otherBean.getNumberOfElements(), otherBean
                .getCheckAtLeastMoreResults());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(ResultSpecification otherBean) {
        if (otherBean != null) {
            this.setOffset(otherBean.getOffset());
            this.setNumberOfElements(otherBean.getNumberOfElements());
            this.setCheckAtLeastMoreResults(otherBean.getCheckAtLeastMoreResults());
        }
    }

    /**
     *
     */
    public int getCheckAtLeastMoreResults() {
        return this.checkAtLeastMoreResults;
    }

    /**
     * <p>
     * Use a value <= 0 for unlimted number
     * </p>
     */
    public int getNumberOfElements() {
        return this.numberOfElements;
    }

    /**
     *
     */
    public int getOffset() {
        return this.offset;
    }

    public void setCheckAtLeastMoreResults(int checkAtLeastMoreResults) {
        this.checkAtLeastMoreResults = checkAtLeastMoreResults;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}