package com.communote.server.model.blog;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.communote.server.model.external.ExternalObject;
import com.communote.server.model.follow.Followable;
import com.communote.server.model.global.GlobalId;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.tag.Taggable;

/**
 * <p>
 * A blog is just a blog with single user tagged items.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Blog implements Serializable, Followable, Propertyable, Taggable {
    /**
     * Constructs new instances of {@link Blog}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link Blog}.
         */
        public static Blog newInstance() {
            return new Blog();
        }

        /**
         * Constructs a new instance of {@link Blog}, taking all possible properties (except the
         * identifier(s))as arguments.
         */
        public static Blog newInstance(String title, String description, Timestamp creationDate,
                String nameIdentifier, Timestamp lastModificationDate,
                Timestamp crawlLastModificationDate, boolean allCanRead, boolean allCanWrite,
                boolean publicAccess, boolean createSystemNotes, boolean toplevelTopic,
                Set<Tag> tags, GlobalId globalId, Set<BlogMember> members,
                Set<ExternalObject> externalObjects, Set<BlogProperty> properties,
                Set<Blog> parents, Set<Blog> children) {
            final Blog entity = new Blog();
            entity.setTitle(title);
            entity.setDescription(description);
            entity.setCreationDate(creationDate);
            entity.setNameIdentifier(nameIdentifier);
            entity.setLastModificationDate(lastModificationDate);
            entity.setCrawlLastModificationDate(crawlLastModificationDate);
            entity.setAllCanRead(allCanRead);
            entity.setAllCanWrite(allCanWrite);
            entity.setPublicAccess(publicAccess);
            entity.setCreateSystemNotes(createSystemNotes);
            entity.setToplevelTopic(toplevelTopic);
            entity.setTags(tags);
            entity.setGlobalId(globalId);
            entity.setMembers(members);
            entity.setExternalObjects(externalObjects);
            entity.setProperties(properties);
            entity.setParents(parents);
            entity.setChildren(children);
            return entity;
        }

        /**
         * Constructs a new instance of {@link Blog}, taking all required and/or read-only
         * properties as arguments.
         */
        public static Blog newInstance(String title, Timestamp creationDate, String nameIdentifier,
                Timestamp lastModificationDate, Timestamp crawlLastModificationDate,
                boolean allCanRead, boolean allCanWrite, boolean publicAccess,
                boolean createSystemNotes, boolean toplevelTopic) {
            final Blog entity = new Blog();
            entity.setTitle(title);
            entity.setCreationDate(creationDate);
            entity.setNameIdentifier(nameIdentifier);
            entity.setLastModificationDate(lastModificationDate);
            entity.setCrawlLastModificationDate(crawlLastModificationDate);
            entity.setAllCanRead(allCanRead);
            entity.setAllCanWrite(allCanWrite);
            entity.setPublicAccess(publicAccess);
            entity.setCreateSystemNotes(createSystemNotes);
            entity.setToplevelTopic(toplevelTopic);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7308570661723098257L;

    private String title;

    private String description;

    private Timestamp creationDate;

    private String nameIdentifier;

    private Timestamp lastModificationDate;

    private Timestamp crawlLastModificationDate;

    private boolean allCanRead;

    private boolean allCanWrite;

    private boolean publicAccess;

    private boolean createSystemNotes;

    private boolean toplevelTopic;

    private Long id;

    private Set<Tag> tags = new HashSet<Tag>();

    private com.communote.server.model.global.GlobalId globalId;

    private Set<BlogMember> members = new HashSet<BlogMember>();

    private Set<ExternalObject> externalObjects = new HashSet<ExternalObject>();

    private Set<BlogProperty> properties = new HashSet<BlogProperty>();

    private Set<Blog> parents = new HashSet<Blog>();

    private Set<Blog> children = new HashSet<Blog>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("title='");
        sb.append(title);
        sb.append("', ");

        sb.append("description='");
        sb.append(description);
        sb.append("', ");

        sb.append("creationDate='");
        sb.append(creationDate);
        sb.append("', ");

        sb.append("nameIdentifier='");
        sb.append(nameIdentifier);
        sb.append("', ");

        sb.append("lastModificationDate='");
        sb.append(lastModificationDate);
        sb.append("', ");

        sb.append("crawlLastModificationDate='");
        sb.append(crawlLastModificationDate);
        sb.append("', ");

        sb.append("allCanRead='");
        sb.append(allCanRead);
        sb.append("', ");

        sb.append("allCanWrite='");
        sb.append(allCanWrite);
        sb.append("', ");

        sb.append("publicAccess='");
        sb.append(publicAccess);
        sb.append("', ");

        sb.append("createSystemNotes='");
        sb.append(createSystemNotes);
        sb.append("', ");

        sb.append("toplevelTopic='");
        sb.append(toplevelTopic);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Blog instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Blog)) {
            return false;
        }
        final Blog that = (Blog) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    public Set<Blog> getChildren() {
        return this.children;
    }

    /**
     * <p>
     * The last modification date of the blog.
     * </p>
     */
    public Timestamp getCrawlLastModificationDate() {
        return this.crawlLastModificationDate;
    }

    /**
     * <p>
     * The creation date of the blog.
     * </p>
     */
    public Timestamp getCreationDate() {
        return this.creationDate;
    }

    /**
     * <p>
     * The description of the blog.
     * </p>
     */
    public String getDescription() {
        return this.description;
    }

    /**
     *
     */
    public Set<ExternalObject> getExternalObjects() {
        return this.externalObjects;
    }

    @Override
    public GlobalId getFollowId() {
        return getGlobalId();
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
     * The last modification date of the blog.
     * </p>
     */
    public Timestamp getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     *
     */
    public Set<BlogMember> getMembers() {
        return this.members;
    }

    /**
     * <p>
     * The identifier of the blog.
     * </p>
     */
    public String getNameIdentifier() {
        return this.nameIdentifier;
    }

    /**
     *
     */
    public Set<Blog> getParents() {
        return this.parents;
    }

    /**
     *
     */
    @Override
    public Set<BlogProperty> getProperties() {
        return this.properties;
    }

    /**
     *
     */
    @Override
    public Set<Tag> getTags() {
        return this.tags;
    }

    /**
     * <p>
     * The title of the blog.
     * </p>
     */
    public String getTitle() {
        return this.title;
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

    /**
     * <p>
     * If true, all communote users are allowed to read the blog.
     * </p>
     */
    public boolean isAllCanRead() {
        return this.allCanRead;
    }

    /**
     * <p>
     * If true, all communote users are allowed to read and write notes to a blog.
     * </p>
     */
    public boolean isAllCanWrite() {
        return this.allCanWrite;
    }

    /**
     * <p>
     * whether notes with creation source 'SYSTEM' will be created in this blog.
     * </p>
     */
    public boolean isCreateSystemNotes() {
        return this.createSystemNotes;
    }

    /**
     * <p>
     * This option determines whether a blog can be accesses by public or not. Default is false.
     * </p>
     */
    public boolean isPublicAccess() {
        return this.publicAccess;
    }

    /**
     *
     */
    public boolean isToplevelTopic() {
        return this.toplevelTopic;
    }

    public void setAllCanRead(boolean allCanRead) {
        this.allCanRead = allCanRead;
    }

    public void setAllCanWrite(boolean allCanWrite) {
        this.allCanWrite = allCanWrite;
    }

    public void setChildren(Set<Blog> children) {
        this.children = children;
    }

    public void setCrawlLastModificationDate(Timestamp crawlLastModificationDate) {
        this.crawlLastModificationDate = crawlLastModificationDate;
    }

    public void setCreateSystemNotes(boolean createSystemNotes) {
        this.createSystemNotes = createSystemNotes;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExternalObjects(Set<ExternalObject> externalObjects) {
        this.externalObjects = externalObjects;
    }

    @Override
    public void setGlobalId(GlobalId globalId) {
        this.globalId = globalId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLastModificationDate(Timestamp lastModificationDate) {
        this.lastModificationDate = lastModificationDate;

        // also set the crawl last modification date if the given last modification is younger
        if (getCrawlLastModificationDate() == null) {
            this.setCrawlLastModificationDate(lastModificationDate);
        } else if (lastModificationDate != null
                && lastModificationDate.getTime() > getCrawlLastModificationDate().getTime()) {
            this.setCrawlLastModificationDate(lastModificationDate);
        }
    }

    public void setMembers(Set<BlogMember> members) {
        this.members = members;
    }

    public void setNameIdentifier(String nameIdentifier) {
        // force lower case identifiers
        this.nameIdentifier = StringUtils.lowerCase(nameIdentifier);
    }

    public void setParents(Set<Blog> parents) {
        this.parents = parents;
    }

    public void setProperties(Set<BlogProperty> properties) {
        this.properties = properties;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    @Override
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setToplevelTopic(boolean toplevelTopic) {
        this.toplevelTopic = toplevelTopic;
    }

}