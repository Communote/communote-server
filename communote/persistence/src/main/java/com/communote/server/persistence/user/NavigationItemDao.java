package com.communote.server.persistence.user;

import java.util.List;

import com.communote.server.model.user.NavigationItem;
import com.communote.server.persistence.hibernate.HibernateDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

public interface NavigationItemDao extends HibernateDao<NavigationItem> {
    /**
     * @param userId
     *            The user to load the items for.
     * @param navigationItemIds
     *            Id's of items to load or null, if all.
     * @return List of loaded navigation items.
     */
    public List<NavigationItem> find(Long userId, Long... navigationItemIds);
}
