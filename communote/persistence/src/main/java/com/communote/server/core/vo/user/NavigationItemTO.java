package com.communote.server.core.vo.user;

import java.io.Serializable;
import java.util.Date;

/**
 * We just want another boilerplate class. So here it comes...
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <DATA_TYPE>
 *            Type of Data
 */
public class NavigationItemTO<DATA_TYPE extends NavigationItemDataTO> implements Serializable {

    private static final long serialVersionUID = 927084157068669648L;

    /**
     * Clone the provided navigation item TO. The filters of the data TO will be a shallow clone.
     * 
     * @param item
     *            the item to clone
     * @return the cloned item or null if the provided item was null.
     */
    public static NavigationItemTO<NavigationItemDataTO> clone(
            NavigationItemTO<NavigationItemDataTO> item) {
        if (item == null) {
            return null;
        }
        NavigationItemTO<NavigationItemDataTO> clone = new NavigationItemTO<NavigationItemDataTO>();
        clone.setId(item.getId());
        clone.setName(item.getName());
        clone.setIndex(item.getIndex());
        clone.setLastAccessDate(new Date(item.getLastAccessDate().getTime()));
        clone.setDataAsJson(item.getDataAsJson());
        clone.setData(NavigationItemDataTO.clone(item.getData()));
        return clone;
    }

    private DATA_TYPE data;

    private String dataAsJson;
    private String name;
    private int index = 0;
    private Date lastAccessDate;
    private Long id;

    /**
     * @return The data.
     */
    public DATA_TYPE getData() {
        return this.data;
    }

    /**
     * @return The data as plain text json
     */
    public String getDataAsJson() {
        return dataAsJson;
    }

    /**
     * @return Id of the item.
     */
    public Long getId() {
        return this.id;
    }

    /**
     *
     */
    public int getIndex() {
        return this.index;
    }

    /**
     *
     */
    public Date getLastAccessDate() {
        return this.lastAccessDate;
    }

    /**
     * @return Name of this item.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the data.
     * 
     * @param data
     *            The data to set.
     */
    public void setData(DATA_TYPE data) {
        this.data = data;
    }

    /**
     * <b>Note:</b> Use setData when you want to update the entity.
     * 
     * @param dataAsJson
     *            The data as plain text json.
     */
    public void setDataAsJson(String dataAsJson) {
        this.dataAsJson = dataAsJson;
    }

    /**
     * @param id
     *            of the item.
     */
    public void setId(Long id) {
        this.id = id;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    /**
     * @param name
     *            Name of this item.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NavigationItemTO [data=" + data + ", dataAsJson=" + dataAsJson + ", name=" + name
                + ", index=" + index + ", lastAccessDate=" + lastAccessDate + ", id=" + id + "]";
    }
}
