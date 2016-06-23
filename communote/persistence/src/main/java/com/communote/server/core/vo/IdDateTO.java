package com.communote.server.core.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Transfer object that holds ID and date information. Can for instance be used to store the last
 * modification date of an entity or property.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IdDateTO implements Serializable {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private Date date;
    private Long id;

    /**
     * Create a new TO
     * 
     * @param id
     *            the ID to set
     * @param date
     *            the date to set
     */
    public IdDateTO(Long id, Date date) {
        this.id = id;
        this.date = date;
    }

    /**
     * Create a new TO
     * 
     * @param id
     *            the ID to set
     * @param date
     *            the date to set
     */
    public IdDateTO(Long id, Timestamp date) {
        this.id = id;
        this.date = date;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the date field
     * 
     * @param date
     *            the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Set the ID field
     * 
     * @param id
     *            the ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

}
