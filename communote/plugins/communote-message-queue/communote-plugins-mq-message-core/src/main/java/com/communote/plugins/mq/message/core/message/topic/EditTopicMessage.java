package com.communote.plugins.mq.message.core.message.topic;

/**
 * Message for editing a topic.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EditTopicMessage extends TopicMesssage {

    private boolean setTags;

    private boolean mergeTags;

    private boolean deleteAllTags;

    private boolean mergeProperties;

    private boolean updateDescription;

    private boolean updateAlias;

    private boolean updateTopicRights;

    private boolean setExternalObjects;

    private boolean mergeExternalObjects;

    private boolean deleteAllExternalObjects;

    private boolean updateTitle;

    /**
     * @return the deleteAllExternalObjects
     */
    public boolean isDeleteAllExternalObjects() {
        return deleteAllExternalObjects;
    }

    /**
     * @return the deleteAllTags
     */
    public boolean isDeleteAllTags() {
        return deleteAllTags;
    }

    /**
     * @return the mergeExternalObjects
     */
    public boolean isMergeExternalObjects() {
        return mergeExternalObjects;
    }

    /**
     * @return the mergeProperties
     */
    public boolean isMergeProperties() {
        return mergeProperties;
    }

    /**
     * @return the mergeTags
     */
    public boolean isMergeTags() {
        return mergeTags;
    }

    /**
     * @return the setExternalObjects
     */
    public boolean isSetExternalObjects() {
        return setExternalObjects;
    }

    /**
     * @return the setTags
     */
    public boolean isSetTags() {
        return setTags;
    }

    /**
     * @return the updateAlias
     */
    public boolean isUpdateAlias() {
        return updateAlias;
    }

    /**
     * @return the updateDescription
     */
    public boolean isUpdateDescription() {
        return updateDescription;
    }

    /**
     * @return the updateTitle
     */
    public boolean isUpdateTitle() {
        return updateTitle;
    }

    /**
     * @return the updateBlogRights
     */
    public boolean isUpdateTopicRights() {
        return updateTopicRights;
    }

    /**
     * @param deleteAllExternalObjects
     *            the deleteAllExternalObjects to set
     */
    public void setDeleteAllExternalObjects(boolean deleteAllExternalObjects) {
        this.deleteAllExternalObjects = deleteAllExternalObjects;
    }

    /**
     * @param deleteAllTags
     *            the deleteAllTags to set
     */
    public void setDeleteAllTags(boolean deleteAllTags) {
        this.deleteAllTags = deleteAllTags;
    }

    /**
     * @param mergeExternalObjects
     *            the mergeExternalObjects to set
     */
    public void setMergeExternalObjects(boolean mergeExternalObjects) {
        this.mergeExternalObjects = mergeExternalObjects;
    }

    /**
     * @param mergeProperties
     *            the mergeProperties to set
     */
    public void setMergeProperties(boolean mergeProperties) {
        this.mergeProperties = mergeProperties;
    }

    /**
     * @param mergeTags
     *            the mergeTags to set
     */
    public void setMergeTags(boolean mergeTags) {
        this.mergeTags = mergeTags;
    }

    /**
     * @param setExternalObjects
     *            the setExternalObjects to set
     */
    public void setSetExternalObjects(boolean setExternalObjects) {
        this.setExternalObjects = setExternalObjects;
    }

    /**
     * @param setTags
     *            the setTags to set
     */
    public void setSetTags(boolean setTags) {
        this.setTags = setTags;
    }

    /**
     * @param updateAlias
     *            the updateAlias to set
     */
    public void setUpdateAlias(boolean updateAlias) {
        this.updateAlias = updateAlias;
    }

    /**
     * @param updateDescription
     *            the updateDescription to set
     */
    public void setUpdateDescription(boolean updateDescription) {
        this.updateDescription = updateDescription;
    }

    /**
     * @param updateTitle
     *            the updateTitle to set
     */
    public void setUpdateTitle(boolean updateTitle) {
        this.updateTitle = updateTitle;
    }

    /**
     * @param updateBlogRights
     *            the updateBlogRights to set
     */
    public void setUpdateTopicRights(boolean updateBlogRights) {
        this.updateTopicRights = updateBlogRights;
    }

}
