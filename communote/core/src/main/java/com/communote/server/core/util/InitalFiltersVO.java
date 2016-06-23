package com.communote.server.core.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The inital filters value object keeps the filter data of a permalink including the necessary
 * information to be used to initalize the widgets. The VO will be JSONed so be careful with
 * associations.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InitalFiltersVO implements Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2766914102583119781L;

    private Map<String, String> tagData = new HashMap<String, String>();

    private Long noteId;
    private Long tagId;
    private String tagName;

    private String blogAlias;

    private String userAlias;

    private Long userId;

    private Long blogId;

    private String userShortName;

    private String userLongName;

    private String blogTitle;

    private String noteTitle;

    private String blogRole;

    private boolean permalinkFound = false;

    /**
     * Add info about a tag to the mapping from Tag ID to Tag name.
     *
     * @param id
     *            The ID of the tag
     * @param name
     *            the name of the tag
     */
    public void addTag(Long id, String name) {
        this.tagData.put(id.toString(), name);
    }

    /**
     * @return the blogAlias
     */
    public String getBlogAlias() {
        return blogAlias;
    }

    /**
     * @return the blogId
     */
    public Long getBlogId() {
        return blogId;
    }

    /**
     * @return the blogRole
     */
    public String getBlogRole() {
        return blogRole;
    }

    /**
     * @return the blogTitle
     */
    public String getBlogTitle() {
        return blogTitle;
    }

    /**
     * @return the noteId
     */
    public Long getNoteId() {
        return noteId;
    }

    /**
     * @return the noteTitle
     */
    public String getNoteTitle() {
        return noteTitle;
    }

    /**
     * @return the ID of the tag
     */
    public Long getTagId() {
        return tagId;
    }

    /**
     * @return the name of the tag identified by the ID returned by {@link #getTagId()}
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * @return a mapping from Tag ID to the name of the Tag.
     */
    public Map<String, String> getTags() {
        return tagData;
    }

    /**
     * @return the userAlias
     */
    public String getUserAlias() {
        return userAlias;
    }

    /**
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return the userLongName
     */
    public String getUserLongName() {
        return userLongName;
    }

    /**
     * @return the userShortName
     */
    public String getUserShortName() {
        return userShortName;
    }

    /**
     * @return the permalinkFound
     */
    public boolean isPermalinkFound() {
        return permalinkFound;
    }

    /**
     * @param blogAlias
     *            the blogAlias to set
     */
    public void setBlogAlias(String blogAlias) {
        this.blogAlias = blogAlias;
    }

    /**
     * @param blogId
     *            the blogId to set
     */
    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    /**
     * @param blogRole
     *            the blogRole to set
     */
    public void setBlogRole(String blogRole) {
        this.blogRole = blogRole;
    }

    /**
     * @param blogTitle
     *            the blogTitle to set
     */
    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }

    /**
     * @param noteId
     *            the noteId to set
     */
    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    /**
     * @param noteTitle
     *            the noteTitle to set
     */
    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    /**
     * @param permalinkFound
     *            the permalinkFound to set
     */
    public void setPermalinkFound(boolean permalinkFound) {
        this.permalinkFound = permalinkFound;
    }

    /**
     * Set the tag ID
     *
     * @param tagId
     *            the ID of the tag
     */
    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    /**
     * Set the name of the tag identified by the ID returned by {@link #getTagId()}
     *
     * @param tagName
     *            the name of the tag
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * @param tags
     *            the tags to set as a mapping from Tag ID to Tag name
     */
    public void setTags(Map<String, String> tags) {
        this.tagData = tags;
    }

    /**
     * @param userAlias
     *            the userAlias to set
     */
    public void setUserAlias(String userAlias) {
        this.userAlias = userAlias;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @param userLongName
     *            the userLongName to set
     */
    public void setUserLongName(String userLongName) {
        this.userLongName = userLongName;
    }

    /**
     * @param userShortName
     *            the userShortName to set
     */
    public void setUserShortName(String userShortName) {
        this.userShortName = userShortName;
    }

}