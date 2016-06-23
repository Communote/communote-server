package com.communote.server.core.vo.tag;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GlobalTagCategoryVO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5445020584142616940L;

    private String name;

    private String prefix;

    private String description;

    private boolean multipleTags;

    public GlobalTagCategoryVO() {
        this.name = null;
        this.prefix = null;
        this.multipleTags = false;
    }

    /**
     * Copies constructor from other GlobalTagCategoryVO
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public GlobalTagCategoryVO(GlobalTagCategoryVO otherBean) {
        this(otherBean.getName(), otherBean.getPrefix(), otherBean.getDescription(), otherBean
                .isMultipleTags());
    }

    public GlobalTagCategoryVO(String name, String prefix, boolean multipleTags) {
        this.name = name;
        this.prefix = prefix;
        this.multipleTags = multipleTags;
    }

    public GlobalTagCategoryVO(String name, String prefix, String description, boolean multipleTags) {
        this.name = name;
        this.prefix = prefix;
        this.description = description;
        this.multipleTags = multipleTags;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(GlobalTagCategoryVO otherBean) {
        if (otherBean != null) {
            this.setName(otherBean.getName());
            this.setPrefix(otherBean.getPrefix());
            this.setDescription(otherBean.getDescription());
            this.setMultipleTags(otherBean.isMultipleTags());
        }
    }

    /**
     * 
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }

    /**
     * 
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * 
     */
    public boolean isMultipleTags() {
        return this.multipleTags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMultipleTags(boolean multipleTags) {
        this.multipleTags = multipleTags;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}