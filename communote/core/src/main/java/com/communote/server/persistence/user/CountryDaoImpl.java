package com.communote.server.persistence.user;

import java.util.List;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.user.Country;
import com.communote.server.model.user.CountryConstants;
import com.communote.server.persistence.user.CountryDaoBase;


/**
 * @see com.communote.server.model.user.Country
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CountryDaoImpl extends CountryDaoBase {
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Country handleFindCountryByCode(String countryCode) {
        StringBuilder query = new StringBuilder();
        query.append("select u from ");
        query.append(CountryConstants.CLASS_NAME);
        query.append(" u where u.");
        query.append(CountryConstants.COUNTRYCODE);
        query.append("=?");
        List<Country> countries = getHibernateTemplate().find(query.toString(), countryCode);
        if (countries.size() == 0) {
            return null;
        }
        if (countries.size() > 1) {
            throw new IllegalDatabaseState(
                    "Cannot have more than one Country with the same country code. CountryCode: "
                            + countryCode);
        }
        return countries.iterator().next();
    }
}
