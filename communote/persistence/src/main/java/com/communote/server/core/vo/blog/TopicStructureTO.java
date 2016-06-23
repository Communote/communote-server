package com.communote.server.core.vo.blog;

import java.util.List;

/**
 * Transfer object to define the changes to the topic structure.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicStructureTO {
    private Boolean toplevel;
    private List<Long> childTopicsToRemove;
    private List<Long> childTopicsToAdd;

    /**
     * @return a list of topic IDs to be added as child topics. Can be null.
     */
    public List<Long> getChildTopicsToAdd() {
        return childTopicsToAdd;
    }

    /**
     * @return a list of topic IDs to be removed from the child topics. Can be null.
     */
    public List<Long> getChildTopicsToRemove() {
        return childTopicsToRemove;
    }

    /**
     * @return whether the topic should be marked as top-level topic or null if no changes should be
     *         conducted
     */
    public Boolean getToplevel() {
        return toplevel;
    }

    /**
     * Set the child topics to be removed
     * 
     * @param childTopicsToAdd
     *            IDs of the topics to be added from the child topics.
     */
    public void setChildTopicsToAdd(List<Long> childTopicsToAdd) {
        this.childTopicsToAdd = childTopicsToAdd;
    }

    /**
     * Set the child topics to be removed
     * 
     * @param childTopicsToRemove
     *            IDs of the topics to be removed from the child topics.
     */
    public void setChildTopicsToRemove(List<Long> childTopicsToRemove) {
        this.childTopicsToRemove = childTopicsToRemove;
    }

    /**
     * Set whether the topic should be marked as top-level topic.
     * 
     * @param toplevel
     *            whether to mark the topic as top-level topic. Can be null to not change the
     *            top-level flag
     */
    public void setToplevel(Boolean toplevel) {
        this.toplevel = toplevel;
    }
}
