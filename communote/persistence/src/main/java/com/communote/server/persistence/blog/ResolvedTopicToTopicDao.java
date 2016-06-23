package com.communote.server.persistence.blog;

import com.communote.server.model.blog.ResolvedTopicToTopic;
import com.communote.server.persistence.hibernate.HibernateDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ResolvedTopicToTopicDao extends HibernateDao<ResolvedTopicToTopic> {

    /**
     * This method connects the both topics, means builds the needed structure.
     * 
     * @param parentTopicId
     *            The parents topic id.
     * @param childTopicId
     * 
     * @return Number of connections created.
     */
    public int connect(Long parentTopicId, Long childTopicId);

    /**
     * This method removes all connections for the given topic.
     * 
     * @param topicId
     *            Id of the topic.
     * @return The total number of removed connections.
     */
    public int disconnect(Long topicId);

    /**
     * This method removes the given child topic as child from the given parent topic.
     * 
     * @param parentTopicId
     *            Id of the parent topic.
     * @param childTopicId
     *            Id of the child topic.
     * @return The total number of removed connections.
     */
    public int disconnect(Long parentTopicId, Long childTopicId);

    /**
     * @param parentTopicId
     *            Id of the parent.
     * @param childTopicId
     *            Id of the child.
     * @return True, if the given child is an direct or indirect child of the given parent.
     */
    public boolean isChild(Long parentTopicId, Long childTopicId);
}
