package com.communote.server.core.vo.tag;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CategorizedTagVO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6531621312005891463L;

    private String name;

    public CategorizedTagVO() {
        this.name = null;
    }

    /**
     * Copies constructor from other CategorizedTagVO
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public CategorizedTagVO(CategorizedTagVO otherBean) {
        this(otherBean.getName());
    }

    public CategorizedTagVO(String name) {
        this.name = name;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(CategorizedTagVO otherBean) {
        if (otherBean != null) {
            this.setName(otherBean.getName());
        }
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}