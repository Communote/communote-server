package com.communote.server.persistence.user;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.NavigationItem;
import com.communote.server.model.user.NavigationItemConstants;
import com.communote.server.model.user.NavigationItemImpl;
import com.communote.server.persistence.hibernate.HibernateDaoImpl;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Repository("navigationItemDao")
public class NavigationItemDaoImpl extends HibernateDaoImpl<NavigationItem> implements
        NavigationItemDao {

    private final static Logger LOGGER = LoggerFactory.getLogger(NavigationItemDaoImpl.class);

    /**
     * Constructor.
     */
    public NavigationItemDaoImpl() {
        super(NavigationItemImpl.class);
    }

    @Override
    public List<NavigationItem> find(Long userId, Long... navigationItemIds) {
        // shortcut when user ID is missing
        if (userId != null) {
            List<NavigationItem> result;
            String query = "SELECT nav FROM " + NavigationItemConstants.CLASS_NAME
                    + " nav LEFT JOIN nav."
                    + NavigationItemConstants.OWNER + " owner WHERE owner."
                    + CommunoteEntityConstants.ID + " = :ownerId";
            if (navigationItemIds.length == 0) {
                result = getHibernateTemplate().findByNamedParam(
                        query + " ORDER BY nav." + NavigationItemConstants.ITEMINDEX, "ownerId",
                        userId);
            } else {
                result = getHibernateTemplate().findByNamedParam(
                        query + " AND nav." + NavigationItemConstants.ID
                                + " IN( :itemIds ) ORDER BY nav."
                                + NavigationItemConstants.ITEMINDEX,
                        new String[] { "ownerId", "itemIds" },
                        new Object[] { userId, navigationItemIds });
            }
            return result;
        }
        return Collections.emptyList();
    }

}
