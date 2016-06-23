package com.communote.server.core.vo.query.config;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.MatchMode;

import com.communote.common.util.ParameterHelper;
import com.communote.server.core.vo.query.tag.UserTagQueryParameters;
import com.communote.server.core.vo.query.user.UserQueryParameters;
import com.communote.server.model.user.UserStatus;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserQueryParametersConfigurator extends QueryParametersConfigurator {
    /**
     * @param parameterNameProvider
     *            Provider for names.
     */
    public UserQueryParametersConfigurator(
            QueryParametersParameterNameProvider parameterNameProvider) {
        super(parameterNameProvider, 20);
    }

    /**
     * Method to start the configuration process.
     * 
     * @param parameters
     *            Map which holds the parameters to be used to configure the query instance.
     * @param queryParameters
     *            Class of type QueryInstance.
     * @param locale
     *            The language to use.
     */
    public void configure(Map<String, ? extends Object> parameters,
            UserQueryParameters queryParameters, Locale locale) {
        queryParameters.setResultSpecification(getResultSpecification(parameters));
        String searchString = ParameterHelper.getParameterAsString(parameters,
                getParameterNameProvider().getNameForUserSearchString());
        if (StringUtils.isNotEmpty(searchString)) {
            queryParameters.setMatchMode(MatchMode.ANYWHERE);
            queryParameters.setUserSearchFilters(searchString.split(" "));
        }
        queryParameters.setTagPrefix(ParameterHelper.getParameterAsString(parameters,
                getParameterNameProvider().getNameForTagPrefix()));
        queryParameters.setIncludeStatusFilter(new UserStatus[] { UserStatus.ACTIVE });
        queryParameters.setRetrieveOnlyFollowedUsers(ParameterHelper.getParameterAsBoolean(
                parameters, getParameterNameProvider().getNameForFollowedNotes(), false));
        if (queryParameters instanceof UserTagQueryParameters) {
            ((UserTagQueryParameters) queryParameters).sortByTagCount();
        } else {
            queryParameters.sortByLastNameAsc();
            queryParameters.sortByFirstNameAsc();
            queryParameters.sortByEmailAsc();
        }
        queryParameters.addUserTagIds(ParameterHelper.getParameterAsLongArray(parameters,
                getParameterNameProvider().getNameForTagIds()));
    }
}
