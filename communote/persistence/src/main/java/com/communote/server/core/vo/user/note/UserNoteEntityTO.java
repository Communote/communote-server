package com.communote.server.core.vo.user.note;

import java.io.Serializable;

/**
 * The TO for a user note entity. When using it for updating the data do not forget to set the
 * {@link #setUpdateRank(boolean)} parameter
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserNoteEntityTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private double normalizedRank;
    private final Long noteId;
    private final Long userId;

    private boolean updateRank;

    /**
     * 
     * @param userId
     *            the associated user id - cannot be null
     * @param noteId
     *            the associated note id - cannot be null
     */
    public UserNoteEntityTO(Long userId, Long noteId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null.");
        }
        if (noteId == null) {
            throw new IllegalArgumentException("noteId cannot be null.");
        }
        this.noteId = noteId;
        this.userId = userId;

    }

    /**
     * 
     * @return the normalized rank (a number between 0 and 1). the rank indicates a computed
     *         interest of the user into the note.
     */
    public double getNormalizedRank() {
        return normalizedRank;
    }

    /**
     * 
     * @return the associated note id for this entity
     */
    public Long getNoteId() {
        return noteId;
    }

    /**
     * 
     * @return the associated user id for this entity
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 
     * @return true if the rank should be updated when synchronizing with the backend
     */
    public boolean isUpdateRank() {
        return updateRank;
    }

    /**
     * 
     * @param normalizedRank
     *            the normalized rank. if not a number between 0 and 1 it will be limited to 0 or 1
     */
    public void setNormalizedRank(double normalizedRank) {
        this.normalizedRank = normalizedRank;
    }

    /**
     * 
     * @param updateRank
     *            if true the rank will be updated if passed to the management layer
     */
    public void setUpdateRank(boolean updateRank) {
        this.updateRank = updateRank;
    }

}
