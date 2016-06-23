package com.communote.server.persistence.security.iprange;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeFilterVO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5605531857105938101L;

    private String name;

    private boolean enabled;

    private String includes;

    private String excludes;

    private com.communote.server.model.security.ChannelType[] channels;

    private Long id;

    public IpRangeFilterVO() {
        this.name = null;
        this.enabled = false;
        this.includes = null;
        this.excludes = null;
        this.channels = null;
        this.id = null;
    }

    /**
     * Copies constructor from other IpRangeFilterVO
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public IpRangeFilterVO(IpRangeFilterVO otherBean) {
        this(otherBean.getName(), otherBean.isEnabled(), otherBean.getIncludes(), otherBean
                .getExcludes(), otherBean.getChannels(), otherBean.getId());
    }

    public IpRangeFilterVO(String name, boolean enabled, String includes, String excludes,
            com.communote.server.model.security.ChannelType[] channels, Long id) {
        this.name = name;
        this.enabled = enabled;
        this.includes = includes;
        this.excludes = excludes;
        this.channels = channels;
        this.id = id;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(IpRangeFilterVO otherBean) {
        if (otherBean != null) {
            this.setName(otherBean.getName());
            this.setEnabled(otherBean.isEnabled());
            this.setIncludes(otherBean.getIncludes());
            this.setExcludes(otherBean.getExcludes());
            this.setChannels(otherBean.getChannels());
            this.setId(otherBean.getId());
        }
    }

    /**
     * 
     */
    public com.communote.server.model.security.ChannelType[] getChannels() {
        return this.channels;
    }

    /**
     * 
     */
    public String getExcludes() {
        return this.excludes;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * 
     */
    public String getIncludes() {
        return this.includes;
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
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setChannels(com.communote.server.model.security.ChannelType[] channels) {
        this.channels = channels;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public void setName(String name) {
        this.name = name;
    }

}