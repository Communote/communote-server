package com.communote.server.persistence.config;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import com.communote.server.model.config.ExternalSystemConfigurationConstants;

/**
 * @see com.communote.server.model.config.ExternalSystemConfiguration
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalSystemConfigurationDaoImpl extends
        com.communote.server.persistence.config.ExternalSystemConfigurationDaoBase {

    private static final String QUERY_BY_SYSTEM_ID = "select id from "
            + ExternalSystemConfigurationConstants.CLASS_NAME + " where "
            + ExternalSystemConfigurationConstants.SYSTEMID + "=?";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long handleFindBySystemId(String systemId) {
        @SuppressWarnings("rawtypes")
        List queryResult = getHibernateTemplate().find(QUERY_BY_SYSTEM_ID, systemId);
        if (queryResult == null || queryResult.size() == 0) {
            return null;
        }
        if (queryResult.size() > 1) {
            throw new DataIntegrityViolationException(
                    "Found more than one external configurations for system ID " + systemId);
        }
        return (Long) queryResult.get(0);
    }
}