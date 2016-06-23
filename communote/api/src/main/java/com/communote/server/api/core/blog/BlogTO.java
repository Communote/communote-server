package com.communote.server.api.core.blog;

import java.util.List;
import java.util.Set;

import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.tag.TagTO;

/**
 * Transfer object holding the details of a blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogTO implements java.io.Serializable {

    private static final long serialVersionUID = -344210789537787130L;
    private String[] unparsedTags;
    private boolean allCanRead = false;
    private boolean allCanWrite = false;
    private boolean toplevelTopic = false;
    private Long creatorUserId;
    private String description;
    private String nameIdentifier;
    private String title;
    private boolean createSystemNotes;
    private List<StringPropertyTO> properties;
    private Set<TagTO> tags;
    private Long[] childTopicIds = new Long[0];

    /**
     * Create a TO with default values.
     */
    public BlogTO() {
        this.allCanRead = false;
        this.allCanWrite = false;
        this.title = null;
        this.createSystemNotes = false;
    }

    /**
     * Create a new TO and set the provided members
     * 
     * @param allCanRead
     *            true if all users are allowed to read, false otherwise
     * @param allCanWrite
     *            true if all users are allowed to write, false otherwise
     * @param title
     *            the title of the blog
     * @param createSystemNotes
     *            whether notes with creation source 'SYSTEM' can be created in the blog
     */
    public BlogTO(boolean allCanRead, boolean allCanWrite, String title, boolean createSystemNotes) {
        this.allCanRead = allCanRead;
        this.allCanWrite = allCanWrite;
        this.title = title;
        this.createSystemNotes = createSystemNotes;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     * 
     * @param otherBean
     *            the other object to copy from
     */
    public void copy(BlogTO otherBean) {
        if (otherBean != null) {
            this.setUnparsedTags(otherBean.getUnparsedTags());
            this.setAllCanRead(otherBean.isAllCanRead());
            this.setAllCanWrite(otherBean.isAllCanWrite());
            this.setCreatorUserId(otherBean.getCreatorUserId());
            this.setDescription(otherBean.getDescription());
            this.setNameIdentifier(otherBean.getNameIdentifier());
            this.setTitle(otherBean.getTitle());
            this.setCreateSystemNotes(otherBean.isCreateSystemNotes());
            this.setProperties(otherBean.getProperties());
            this.setTags(otherBean.getTags());
            this.setToplevelTopic(otherBean.isToplevelTopic());
        }
    }

    /**
     * @return Array of id of child topics. This never returns null.
     */
    public Long[] getChildTopicIds() {
        return childTopicIds;
    }

    /**
     * @return the ID of the user that creates the blog, this user will be the manager of the blog.
     *         This member should be ignored when using the TO for updating an existing blog.
     */
    public Long getCreatorUserId() {
        return this.creatorUserId;
    }

    /**
     * @return the description for the blog.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the unique alias of the blog.
     */
    public String getNameIdentifier() {
        return this.nameIdentifier;
    }

    /**
     * @return the properties to add or added to the blog
     */
    public List<StringPropertyTO> getProperties() {
        return this.properties;
    }

    /**
     * Find and return the property with matching values
     * 
     * @param keyGroup
     *            the key group
     * @param key
     *            the key
     * @return the property if found
     */
    public StringPropertyTO getProperty(String keyGroup, String key) {
        return PropertyHelper.getProperty(properties, keyGroup, key);
    }

    /**
     * @return the tags of the blog as a collection of TOs describing the tags
     */
    public Set<TagTO> getTags() {
        return this.tags;
    }

    /**
     * @return the title of the blog
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return the tags of the blog as a an array of tag names
     */
    public String[] getUnparsedTags() {
        return this.unparsedTags;
    }

    /**
     * @return whether all users are allowed to read the blog
     */
    public boolean isAllCanRead() {
        return this.allCanRead;
    }

    /**
     * @return whether all users are allowed to write to the blog
     */
    public boolean isAllCanWrite() {
        return this.allCanWrite;
    }

    /**
     * @return whether notes with creation source 'SYSTEM' can be created in the blog
     */
    public boolean isCreateSystemNotes() {
        return this.createSystemNotes;
    }

    /**
     * @return whether the topic is a top level topic
     */
    public boolean isToplevelTopic() {
        return this.toplevelTopic;
    }

    /**
     * Set whether all users are allowed to read the blog
     * 
     * @param allCanRead
     *            true if all users are allowed to read, false otherwise
     */
    public void setAllCanRead(boolean allCanRead) {
        this.allCanRead = allCanRead;
    }

    /**
     * Set whether all users are allowed to write to the blog
     * 
     * @param allCanWrite
     *            true if all users are allowed to write, false otherwise
     */
    public void setAllCanWrite(boolean allCanWrite) {
        this.allCanWrite = allCanWrite;
    }

    /**
     * @param childTopicIds
     *            Array of child topic ids.
     */
    public void setChildTopicIds(Long[] childTopicIds) {
        if (childTopicIds != null) {
            this.childTopicIds = childTopicIds;
        }
    }

    /**
     * Set whether notes with creation source 'SYSTEM' can be created in the blog
     * 
     * @param createSystemNotes
     *            true if the notes can be created, false otherwise
     */
    public void setCreateSystemNotes(boolean createSystemNotes) {
        this.createSystemNotes = createSystemNotes;
    }

    /**
     * Set the ID of the user that creates the blog, this user will be the manager of the blog.
     * 
     * @param creatorUserId
     *            the ID of the creator
     */
    public void setCreatorUserId(Long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    /**
     * Set the description of the blog.
     * 
     * @param description
     *            a short description of the blog
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set the unique alias of the blog
     * 
     * @param nameIdentifier
     *            the alias of the blog
     */
    public void setNameIdentifier(String nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
    }

    /**
     * The properties of the blog
     * 
     * @param properties
     *            the blog properties
     */
    public void setProperties(List<StringPropertyTO> properties) {
        this.properties = properties;
    }

    /**
     * Set the tags of the blog
     * 
     * @param tags
     *            the tags as a collection of TOs describing the tags
     */
    public void setTags(Set<TagTO> tags) {
        this.tags = tags;
    }

    /**
     * Set the title of the blog
     * 
     * @param title
     *            the blog title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set whether the topic is a top level topic
     * 
     * @param toplevelTopic
     *            true if the topic is a top level topic, otherwise false
     */
    public void setToplevelTopic(boolean toplevelTopic) {
        this.toplevelTopic = toplevelTopic;
    }

    /**
     * Set the tags of the blog.
     * 
     * @param unparsedTags
     *            the tags as an array of tag names
     */
    public void setUnparsedTags(String[] unparsedTags) {
        this.unparsedTags = unparsedTags;
    }
}
