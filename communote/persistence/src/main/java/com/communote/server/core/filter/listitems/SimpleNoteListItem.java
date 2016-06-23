package com.communote.server.core.filter.listitems;

import com.communote.server.api.core.common.IdentifiableEntityData;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SimpleNoteListItem extends IdentifiableEntityData {

    private static final long serialVersionUID = 1962296913830895706L;

    private java.util.Date creationDate;

    public SimpleNoteListItem() {
        super();
        this.creationDate = null;
    }

    public SimpleNoteListItem(Long id, java.util.Date creationDate) {
        super();
        setId(id);
        this.creationDate = creationDate;
    }

    /**
     * 
     */
    public java.util.Date getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(java.util.Date creationDate) {
        this.creationDate = creationDate;
    }

}