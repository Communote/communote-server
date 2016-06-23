package com.communote.server.web.fe.portal.user.client.forms;

import java.util.List;

/**
 * Ip range filter from
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class IpRangeFilterItem {

    private String action;

    private Long id;

    private boolean enabled;

    private String name;
    private String excludes;
    private String includes;
    private List<IpRangeFilterChannelType> channelTypes;

    /**
     * Return current action
     *
     * @return submit action
     */
    public String getAction() {
        return action;
    }

    /**
     * Return all filter
     *
     * @return List of ip filter
     */
    public List<IpRangeFilterChannelType> getChannelTypes() {
        return channelTypes;
    }

    /**
     * Is filter enabled
     *
     * @return <code>true</code> if filter enabled
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * Return all defined excluded ip addresses
     *
     * @return Ip addresses as text
     */
    public String getExcludes() {
        return excludes;
    }

    /**
     * Return the internal database id of the filter
     *
     * @return Filter Id
     */
    public Long getId() {
        return id;
    }

    /**
     * Return ip range as string(text)
     *
     * @return Ip range as text
     */
    public String getIncludes() {
        return includes;
    }

    /**
     * Return the name of the current filter
     *
     * @return Name of filter
     */
    public String getName() {
        return name;
    }

    /**
     * Set Action
     *
     * @param action
     *            Submit action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Set list of channel types
     *
     * @param channelTypes
     *            List of channel types
     */
    public void setChannelTypes(List<IpRangeFilterChannelType> channelTypes) {
        this.channelTypes = channelTypes;
    }

    /**
     * Set filter enabled
     *
     * @param enabled
     *            <code>true</code> if filter should be enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Set exclude ip addresses
     *
     * @param excludes
     *            Ip range as string
     */
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    /**
     * Set the current filter id
     *
     * @param id
     *            Filter id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Set include addresses
     *
     * @param includes
     *            Ip addresses
     */
    public void setIncludes(String includes) {
        this.includes = includes;
    }

    /**
     * Set the name of the current filter
     *
     * @param name
     *            Name of filter
     */
    public void setName(String name) {
        this.name = name;
    }

}