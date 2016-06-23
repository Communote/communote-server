package com.communote.server.persistence.user.client;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.client.ClientStatistic;
import com.communote.server.model.client.ClientStatisticConstants;
import com.communote.server.persistence.user.client.ClientStatisticDao;


/**
 * @see ClientStatisticDao
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientStatisticDaoImpl extends
        com.communote.server.persistence.user.client.ClientStatisticDaoBase {

    /** Logger */
    private final static Logger LOG = LoggerFactory.getLogger(ClientStatistic.class);

    /**
     * Gets the client statistic, throws exception if it not exists
     * 
     * @return the client statistic
     */
    private ClientStatistic getClientStatistic() {
        Collection<ClientStatistic> result = loadAll();
        if (result == null || result.size() == 0) {
            throw new IllegalDatabaseState(
                    "client statistic is not initialized, could not find an entity");
        }
        if (result.size() > 1) {
            throw new IllegalDatabaseState("found more than one client statistic entity");
        }
        return result.iterator().next();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.user.client.ClientStatisticDaoBase#handleDecrementRepositorySize
     *      (long)
     */
    @Override
    protected long handleDecrementRepositorySize(long value) {
        Assert.isTrue(value >= 0, "value must be positive");
        // decrement
        getHibernateTemplate().bulkUpdate(
                "update " + ClientStatisticConstants.CLASS_NAME + " c set c."
                        + ClientStatisticConstants.REPOSITORYSIZE + " = c."
                        + ClientStatisticConstants.REPOSITORYSIZE + " - ?", value);
        // set to zero if negative
        getHibernateTemplate().bulkUpdate(
                "update " + ClientStatisticConstants.CLASS_NAME + " set "
                        + ClientStatisticConstants.REPOSITORYSIZE + " = 0 where "
                        + ClientStatisticConstants.REPOSITORYSIZE + " < 0");
        return getClientStatistic().getRepositorySize();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.user.client.ClientStatisticDaoBase#handleGetRepositorySize()
     */
    @Override
    protected long handleGetRepositorySize() {
        return getClientStatistic().getRepositorySize();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.user.client.ClientStatisticDaoBase#handleIncrementRepositorySize
     *      (long)
     */
    @Override
    protected long handleIncrementRepositorySize(long value) {
        Assert.isTrue(value >= 0, "value must be positive");
        getHibernateTemplate().bulkUpdate(
                "update " + ClientStatisticConstants.CLASS_NAME + " c set c."
                        + ClientStatisticConstants.REPOSITORYSIZE + " = c."
                        + ClientStatisticConstants.REPOSITORYSIZE + " + ?", value);
        return getClientStatistic().getRepositorySize();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.user.client.ClientStatisticDaoBase#handleInitialise()
     */
    @Override
    protected void handleInitialise() {

        Long count = (Long) getHibernateTemplate().find(
                "select count(*) from " + ClientStatisticConstants.CLASS_NAME).get(0);
        if (count == 0) {
            ClientStatistic stats = ClientStatistic.Factory.newInstance();
            stats.setRepositorySize(0);
            getHibernateTemplate().save(stats);
        } else {
            LOG
                    .warn("client statistic entity already exists, client is already initialised, statistic: "
                            + getClientStatistic().attributesToString());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.user.client.ClientStatisticDaoBase#handleResetRepositorySize
     *      ()
     */
    @Override
    protected void handleResetRepositorySize() {
        getHibernateTemplate().bulkUpdate(
                "update " + ClientStatisticConstants.CLASS_NAME + " set "
                        + ClientStatisticConstants.REPOSITORYSIZE + " = 0");
    }

}
