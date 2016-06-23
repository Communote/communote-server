package com.communote.server.model.note;

/**
 * <p>
 * Represents a as user tagged post interpreted mail message that was processed by the
 * MailBasedPostingManagement.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ProcessedMailNote implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.note.ProcessedMailNote}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.note.ProcessedMailNote}.
         */
        public static com.communote.server.model.note.ProcessedMailNote newInstance() {
            return new com.communote.server.model.note.ProcessedMailNoteImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.note.ProcessedMailNote},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.note.ProcessedMailNote newInstance(
                String mailMessageId) {
            final com.communote.server.model.note.ProcessedMailNote entity = new com.communote.server.model.note.ProcessedMailNoteImpl();
            entity.setMailMessageId(mailMessageId);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6695804831487723690L;

    private String mailMessageId;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("mailMessageId='");
        sb.append(mailMessageId);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an ProcessedMailNote instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ProcessedMailNote)) {
            return false;
        }
        final ProcessedMailNote that = (ProcessedMailNote) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * A unique identifier for the mail message represented by this object.
     * </p>
     */
    public String getMailMessageId() {
        return this.mailMessageId;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMailMessageId(String mailMessageId) {
        this.mailMessageId = mailMessageId;
    }
}