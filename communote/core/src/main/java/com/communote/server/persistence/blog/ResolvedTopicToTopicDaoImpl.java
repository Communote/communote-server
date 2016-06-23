package com.communote.server.persistence.blog;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.communote.server.model.blog.ResolvedTopicToTopic;
import com.communote.server.model.blog.ResolvedTopicToTopicConstants;
import com.communote.server.model.blog.ResolvedTopicToTopicImpl;
import com.communote.server.persistence.hibernate.HibernateDaoImpl;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Repository("resolvedTopicToTopicDao")
public class ResolvedTopicToTopicDaoImpl extends HibernateDaoImpl<ResolvedTopicToTopic> implements
        ResolvedTopicToTopicDao {

    private final Logger LOGGER = LoggerFactory.getLogger(ResolvedTopicToTopicDaoImpl.class);

    /**
     * Constructor.
     */
    public ResolvedTopicToTopicDaoImpl() {
        super(ResolvedTopicToTopicImpl.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int connect(Long parentTopicId, Long childTopicId) {
        if (isChild(childTopicId, parentTopicId)) {
            throw new ParentIsAlreadyChildDataIntegrityViolationException(
                    "The given parent is already a child of the given child.", parentTopicId,
                    childTopicId);
        }
        if (isChild(parentTopicId, childTopicId)) {
            LOGGER.debug(
                    "Skipping connecting parent {} and child {} as they are already connected.",
                    parentTopicId, childTopicId);
            return 0;
        }
        int numberOfConnections = 1;
        // The direct connection.
        create(ResolvedTopicToTopic.Factory.newInstance(parentTopicId, childTopicId, "/"
                + parentTopicId + "/" + childTopicId + "/"));
        // All transitive connections
        List<ResolvedTopicToTopic> parentTopicConnections = getHibernateTemplate().find(
                "FROM " + ResolvedTopicToTopicConstants.CLASS_NAME + " WHERE "
                        + ResolvedTopicToTopicConstants.TOPICPATH + " LIKE ?",
                "%/" + parentTopicId + "/");
        for (ResolvedTopicToTopic parentTopicConnection : parentTopicConnections) {
            create(ResolvedTopicToTopic.Factory.newInstance(
                    parentTopicConnection.getParentTopicId(), childTopicId,
                    parentTopicConnection.getTopicPath() + childTopicId + "/"));
            numberOfConnections++;
        }

        // Handle all existing connections where child is root
        List<ResolvedTopicToTopic> childRootConnections = getHibernateTemplate().find(
                "FROM " + ResolvedTopicToTopicConstants.CLASS_NAME + " WHERE "
                        + ResolvedTopicToTopicConstants.TOPICPATH + " LIKE ?",
                "/" + childTopicId + "/%");
        LOGGER.debug("Resolving {} root connections for child {}", childRootConnections.size(),
                childTopicId);
        for (ResolvedTopicToTopic childRootConnection : childRootConnections) {
            if (childRootConnection.getTopicPath().matches("/" + childTopicId + "/[0-9]+/")) {
                disconnect(childTopicId, childRootConnection.getChildTopicId());
                numberOfConnections += connect(childTopicId, childRootConnection.getChildTopicId());
            }
        }

        LOGGER.debug("Connected parent {} and child {} on {} pathes.", parentTopicId, childTopicId,
                numberOfConnections);
        return numberOfConnections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int disconnect(Long topicId) {
        int removedLines = getHibernateTemplate().bulkUpdate(
                "DELETE FROM " + ResolvedTopicToTopicConstants.CLASS_NAME + " WHERE "
                        + ResolvedTopicToTopicConstants.TOPICPATH + " LIKE ?",
                "%/" + topicId + "/%");
        LOGGER.debug("Removed {} entries for topic {}", removedLines, topicId);
        return removedLines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int disconnect(Long parentTopicId, Long childTopicId) {
        int removedLines = getHibernateTemplate().bulkUpdate(
                "DELETE FROM " + ResolvedTopicToTopicConstants.CLASS_NAME + " WHERE "
                        + ResolvedTopicToTopicConstants.TOPICPATH + " LIKE ?",
                "%/" + parentTopicId + "/" + childTopicId + "/%");
        LOGGER.debug("Removed {} entries for parent {} and child {}", removedLines, parentTopicId,
                childTopicId);
        return removedLines;
    }

    /**
     * @{inheritDoc
     */
    @Override
    public boolean isChild(Long parentTopicId, Long childTopicId) {
        List<?> result = getHibernateTemplate().find(
                "FROM " + ResolvedTopicToTopicConstants.CLASS_NAME + " WHERE "
                        + ResolvedTopicToTopicConstants.PARENTTOPICID + " = ? AND "
                        + ResolvedTopicToTopicConstants.CHILDTOPICID + "= ?",
                parentTopicId, childTopicId);
        return result.size() > 0;
    }
}
