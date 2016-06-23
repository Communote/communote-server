package com.communote.server.model.note;

import java.io.Serializable;

/**
 * Content of a note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Content implements Serializable {
    /**
     * Constructs new instances of {@link Content}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link Content}.
         */
        public static Content newInstance() {
            return new Content();
        }

        /**
         * Constructs a new instance of {@link Content}, taking all required and/or read-only
         * properties as arguments.
         */
        public static Content newInstance(String content) {
            final Content entity = new Content();
            entity.setContent(content);
            return entity;
        }

        /**
         * Constructs a new instance of {@link Content}, taking all possible properties (except the
         * identifier(s))as arguments.
         */
        public static Content newInstance(String content, String shortContent) {
            final Content entity = new Content();
            entity.setContent(content);
            entity.setShortContent(shortContent);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2644989726302045301L;

    private String content;

    private String shortContent;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("content='");
        sb.append(content);
        sb.append("', ");

        sb.append("shortContent='");
        sb.append(shortContent);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Content instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Content)) {
            return false;
        }
        final Content that = (Content) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * The textual content of a note with HTML markup.
     * </p>
     */
    public String getContent() {
        return this.content;
    }

    /**
     *
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * A shortened version of the content which is generated during note creation if shortening is
     * necessary. If not necessary, for instance if the note content is short enough or an autosave,
     * this value is null.
     * </p>
     */
    public String getShortContent() {
        return this.shortContent;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setShortContent(String shortContent) {
        this.shortContent = shortContent;
    }
}