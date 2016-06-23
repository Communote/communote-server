package com.communote.server.persistence.user.client;

import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.client.Client;
import com.communote.server.model.client.ClientConstants;
import com.communote.server.model.client.ClientStatus;

/**
 * @see com.communote.server.model.client.Client
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientDaoImpl extends ClientDaoBase {

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.persistence.user.client.ClientDaoBase#handleChangeClientStatus(String,
     *      ClientStatus, ClientStatus)
     */
    @Override
    protected Client handleChangeClientStatus(String clientId, ClientStatus newStatus,
            ClientStatus oldStatus) {
        getHibernateTemplate().bulkUpdate(
                "update " + ClientConstants.CLASS_NAME
                + " c set c.clientStatus = ? where c.clientStatus = ? and c.clientId = ?",
                new Object[] { newStatus, oldStatus, clientId });
        return findByClientId(clientId);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.persistence.user.client.ClientDaoBase#handleFindByClientId(String)
     */
    @Override
    protected Client handleFindByClientId(String clientId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(Client.class).add(
                Restrictions.ilike(ClientConstants.CLIENTID, clientId));
        List<?> list = getHibernateTemplate().findByCriteria(criteria);
        Client result = null;
        if (list != null && list.size() > 0) {
            if (list.size() > 1) {
                throw new IllegalDatabaseState("found more than one client with id '" + clientId
                        + "'");
            } else if (list.size() == 1) {
                result = (Client) list.iterator().next();
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.persistence.user.client.ClientDaoBase#handleFindByClientIdForWriting(String)
     */
    @Override
    protected Client handleFindByClientIdForWriting(String clientId) {
        Client client = handleFindByClientId(clientId);
        if (client != null) {
            getHibernateTemplate().lock(client, LockMode.PESSIMISTIC_WRITE);
        }
        return client;
    }

}
