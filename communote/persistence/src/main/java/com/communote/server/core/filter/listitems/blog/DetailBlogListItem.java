package com.communote.server.core.filter.listitems.blog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.communote.server.api.core.tag.TagData;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DetailBlogListItem
        extends com.communote.server.api.core.blog.BlogData
        implements java.io.Serializable {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 544219795541139884L;
    private Collection<TagData> tagItems = new ArrayList<TagData>();
    private boolean allCanRead;
    private boolean allCanWrite;
    private boolean toplevelTopic;
    private boolean rootTopic;
    private String readingUserIds;
    private String writingUserIds;
    private String managingUserIds;
    private String blogEmail;
    private boolean createSystemNotes;
    private List<DetailBlogListItem> children = new ArrayList<DetailBlogListItem>();
    private List<DetailBlogListItem> parents = new ArrayList<DetailBlogListItem>();

    /**
     * Default constructor.
     */
    public DetailBlogListItem() {
        super();
        this.allCanRead = false;
        this.allCanWrite = false;
        this.readingUserIds = null;
        this.writingUserIds = null;
        this.managingUserIds = null;
        this.blogEmail = null;
        this.createSystemNotes = false;
    }

    public DetailBlogListItem(boolean allCanRead, boolean allCanWrite, String readingUserIds,
            String writingUserIds,
            String managingUserIds, String blogEmail,
            boolean createSystemNotes, String nameIdentifier,
            String description, Long blogId, String title,
            java.util.Date lastModificationDate) {
        super(nameIdentifier, description, blogId, title, lastModificationDate);
        this.allCanRead = allCanRead;
        this.allCanWrite = allCanWrite;
        this.readingUserIds = readingUserIds;
        this.writingUserIds = writingUserIds;
        this.managingUserIds = managingUserIds;
        this.blogEmail = blogEmail;
        this.createSystemNotes = createSystemNotes;
    }

    /**
     * Copies constructor from other DetailBlogListItem
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public DetailBlogListItem(DetailBlogListItem otherBean) {
        this(otherBean.isAllCanRead(), otherBean.isAllCanWrite(), otherBean
                .getReadingUserIds(), otherBean.getWritingUserIds(),
                otherBean.getManagingUserIds(), otherBean.getBlogEmail(), otherBean
                        .isCreateSystemNotes(), otherBean.getNameIdentifier(), otherBean
                        .getDescription(), otherBean.getId(), otherBean.getTitle(), otherBean
                        .getLastModificationDate());
        this.tagItems = otherBean.tagItems;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(DetailBlogListItem otherBean) {
        if (otherBean != null) {
            this.setAllCanRead(otherBean.isAllCanRead());
            this.setAllCanWrite(otherBean.isAllCanWrite());
            this.setToplevelTopic(otherBean.isToplevelTopic());
            this.setRootTopic(otherBean.isRootTopic());
            this.setReadingUserIds(otherBean.getReadingUserIds());
            this.setWritingUserIds(otherBean.getWritingUserIds());
            this.setManagingUserIds(otherBean.getManagingUserIds());
            this.setBlogEmail(otherBean.getBlogEmail());
            this.setCreateSystemNotes(otherBean.isCreateSystemNotes());
            this.setNameIdentifier(otherBean.getNameIdentifier());
            this.setDescription(otherBean.getDescription());
            this.setId(otherBean.getId());
            this.setTitle(otherBean.getTitle());
            this.setLastModificationDate(otherBean.getLastModificationDate());
            this.setTagItems(otherBean.getTagItems());
        }
    }

    /**
     * Email address of this blog
     */
    public String getBlogEmail() {
        return this.blogEmail;
    }

    /**
     * @return The children of this topic. This will never return null.
     */
    public List<DetailBlogListItem> getChildren() {
        return children;
    }

    /**
     * Comma separated list of user ids who are allowed to manage
     */
    public String getManagingUserIds() {
        return this.managingUserIds;
    }

    /**
     * @return The parents of this topic. This will never return null.
     */
    public List<DetailBlogListItem> getParents() {
        return parents;
    }

    /**
     * Comma separated list of user ids who are allowed to read
     */
    public String getReadingUserIds() {
        return this.readingUserIds;
    }

    /**
     * @return the tagItems
     */
    public Collection<TagData> getTagItems() {
        return tagItems;
    }

    /**
     * Comma separated list of user ids who are allowed to write
     */
    public String getWritingUserIds() {
        return this.writingUserIds;
    }

    /**
     * @return True if all can read
     */
    public boolean isAllCanRead() {
        return this.allCanRead;
    }

    /**
     * @return True if all can write
     */
    public boolean isAllCanWrite() {
        return this.allCanWrite;
    }

    /**
     * whether notes with creation source &aposSYSTEM&apos will be created in this blog.
     */
    public boolean isCreateSystemNotes() {
        return this.createSystemNotes;
    }

    /**
     * @return True, if this is a root topic, means has no parents.
     */
    public boolean isRootTopic() {
        return rootTopic;
    }

    /**
     * @return True, if this is a top level topic.
     */
    public boolean isToplevelTopic() {
        return toplevelTopic;
    }

    public void setAllCanRead(boolean allCanRead) {
        this.allCanRead = allCanRead;
    }

    public void setAllCanWrite(boolean allCanWrite) {
        this.allCanWrite = allCanWrite;
    }

    public void setBlogEmail(String blogEmail) {
        this.blogEmail = blogEmail;
    }

    /**
     * @param children
     *            The children of this topic.
     */
    public void setChildren(List<DetailBlogListItem> children) {
        if (children != null) {
            this.children = children;
        }
    }

    public void setCreateSystemNotes(boolean createSystemNotes) {
        this.createSystemNotes = createSystemNotes;
    }

    public void setManagingUserIds(String managingUserIds) {
        this.managingUserIds = managingUserIds;
    }

    /**
     * @param parents
     *            The parents of this topic.
     */
    public void setParents(List<DetailBlogListItem> parents) {
        if (parents != null) {
            this.parents = parents;
        }
    }

    public void setReadingUserIds(String readingUserIds) {
        this.readingUserIds = readingUserIds;
    }

    /**
     * @param rootTopic
     *            True, if this is a root topic.
     */
    public void setRootTopic(boolean rootTopic) {
        this.rootTopic = rootTopic;
    }

    /**
     * @param tagItems
     *            the tagItems to set
     */
    public void setTagItems(Collection<TagData> tagItems) {
        this.tagItems = tagItems;
    }

    /**
     * @param toplevelTopic
     *            True for top level topic.
     */
    public void setToplevelTopic(boolean toplevelTopic) {
        this.toplevelTopic = toplevelTopic;
    }

    public void setWritingUserIds(String writingUserIds) {
        this.writingUserIds = writingUserIds;
    }
}
