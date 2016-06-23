package com.communote.server.model.attachment;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.communote.server.model.global.GlobalId;
import com.communote.server.model.global.GlobalIdentifiable;
import com.communote.server.model.note.Note;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.user.User;

/**
 * <p>
 * An attachment refers to a file in the repository connector.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Attachment implements Serializable, GlobalIdentifiable, Propertyable {
    /**
     * Constructs new instances of {@link Attachment}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link Attachment}.
         */
        public static Attachment newInstance() {
            return new Attachment();
        }

        /**
         * Constructs a new instance of {@link Attachment}, taking all required and/or read-only
         * properties as arguments.
         */
        public static Attachment newInstance(String contentIdentifier, String repositoryIdentifier,
                String name, AttachmentStatus status, Timestamp uploadDate) {
            final Attachment entity = new Attachment();
            entity.setContentIdentifier(contentIdentifier);
            entity.setRepositoryIdentifier(repositoryIdentifier);
            entity.setName(name);
            entity.setStatus(status);
            entity.setUploadDate(uploadDate);
            return entity;
        }

        /**
         * Constructs a new instance of {@link Attachment}, taking all possible properties (except
         * the identifier(s))as arguments.
         */
        public static Attachment newInstance(String contentIdentifier, String repositoryIdentifier,
                String name, String contentType, Long size, AttachmentStatus status,
                Timestamp uploadDate, Note note, GlobalId globalId, User uploader,
                Set<AttachmentProperty> properties) {
            final Attachment entity = new Attachment();
            entity.setContentIdentifier(contentIdentifier);
            entity.setRepositoryIdentifier(repositoryIdentifier);
            entity.setName(name);
            entity.setContentType(contentType);
            entity.setSize(size);
            entity.setStatus(status);
            entity.setUploadDate(uploadDate);
            entity.setNote(note);
            entity.setGlobalId(globalId);
            entity.setUploader(uploader);
            entity.setProperties(properties);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 369465732312497605L;

    private String contentIdentifier;

    private String repositoryIdentifier;

    private String name;

    private String contentType;

    private Long size;

    private AttachmentStatus status;

    private Timestamp uploadDate;

    private Long id;

    private Note note;

    private GlobalId globalId;

    private User uploader;

    private Set<AttachmentProperty> properties = new HashSet<AttachmentProperty>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("contentIdentifier='");
        sb.append(contentIdentifier);
        sb.append("', ");

        sb.append("repositoryIdentifier='");
        sb.append(repositoryIdentifier);
        sb.append("', ");

        sb.append("name='");
        sb.append(name);
        sb.append("', ");

        sb.append("contentType='");
        sb.append(contentType);
        sb.append("', ");

        sb.append("size='");
        sb.append(size);
        sb.append("', ");

        sb.append("status='");
        sb.append(status);
        sb.append("', ");

        sb.append("uploadDate='");
        sb.append(uploadDate);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Attachment instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Attachment)) {
            return false;
        }
        final Attachment that = (Attachment) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * The content identifier describes the resource for a repository uniquely. If the repository
     * identifier is null the content identifier is an URL and the content identifier and uri will
     * be equal
     * </p>
     */
    public String getContentIdentifier() {
        return this.contentIdentifier;
    }

    /**
     * <p>
     * The content type of the attachment. It can be null in case the value has not been set yet. It
     * must then be determined by the meta data.
     * </p>
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     *
     */
    @Override
    public GlobalId getGlobalId() {
        return this.globalId;
    }

    /**
     *
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * The (file) name of the attachment
     * </p>
     */
    public String getName() {
        return this.name;
    }

    /**
     *
     */
    public Note getNote() {
        return this.note;
    }

    /**
     *
     */
    @Override
    public Set<AttachmentProperty> getProperties() {
        return this.properties;
    }

    /**
     * <p>
     * The repository identifier describes the repository the content to this resource is stored at.
     * If it is NONE the repository is the world wide web
     * </p>
     */
    public String getRepositoryIdentifier() {
        return this.repositoryIdentifier;
    }

    /**
     * <p>
     * The size of the attachment in bytes. It is -1 if the size could not be determined.
     * </p>
     */
    public Long getSize() {
        return this.size;
    }

    /**
     * <p>
     * The status of the attachment
     * </p>
     */
    public AttachmentStatus getStatus() {
        return this.status;
    }

    /**
     * <p>
     * The date, the attachment was uploaded.
     * </p>
     */
    public Timestamp getUploadDate() {
        return this.uploadDate;
    }

    /**
     *
     */
    public User getUploader() {
        return this.uploader;
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

    public void setContentIdentifier(String contentIdentifier) {
        this.contentIdentifier = contentIdentifier;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setGlobalId(GlobalId globalId) {
        this.globalId = globalId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public void setProperties(Set<AttachmentProperty> properties) {
        this.properties = properties;
    }

    public void setRepositoryIdentifier(String repositoryIdentifier) {
        this.repositoryIdentifier = repositoryIdentifier;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setStatus(AttachmentStatus status) {
        this.status = status;
    }

    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

}