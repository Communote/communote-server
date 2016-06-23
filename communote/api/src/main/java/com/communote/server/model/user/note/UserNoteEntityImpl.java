package com.communote.server.model.user.note;

/**
 * @see com.communote.server.model.user.note.UserNoteEntity
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserNoteEntityImpl extends UserNoteEntity {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -369645065433524696L;

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.model.user.note.UserNoteEntity#getRankNormalized()
     */
    @Override
    public double getRankNormalized() {
        return UserNoteEntityHelper.convertToNormalizedRank(getRank());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRank(int rank) {
        // just check that the rank is in range
        if (!UserNoteEntityHelper.checkValidRank(rank)) {
            throw new IllegalArgumentException(
                    "The given rank is invalid. The rank must match the interval 0.."
                            + UserNoteEntityHelper.RANK_PRECISION + ", but is  '" + rank + "'");
        }
        super.setRank(rank);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.model.user.note.UserNoteEntity#setRankNormalized(double)
     */
    @Override
    public void setRankNormalized(double normalizedRank) {
        this.setRank(UserNoteEntityHelper.convertNormalizedRank(normalizedRank));
    }

}