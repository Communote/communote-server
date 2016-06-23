package com.communote.server.core.vo.query.user;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.user.UserData;
import com.communote.server.core.vo.query.TagConstraintConnectorEnum;
import com.communote.server.core.vo.query.TaggingCoreItemQueryDefinition;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.UserProfileConstants;
import com.communote.server.model.user.UserStatus;


/**
 * Query Definition to find users
 * 
 * @param <R>
 *            Type of the result for this query.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractUserTaggingCoreQuery<R extends UserData> extends
        TaggingCoreItemQueryDefinition<R, UserTaggingCoreQueryParameters> {

    private static final long USER_ID = 12l;
    private static final String SALUTATION = "salutation";
    private static final String LAST_NAME = "lastName";
    private static final String FIRST_NAME = "firstName";
    private static final String ALIAS = "alias";
    private static final String EMAIL_EMAIL_COM = "email@email.com";
    private static final UserStatus STATUS = UserStatus.ACTIVE;
    /**
     * the parameters of the list item constructor
     */
    private String[] constructorParameters;

    /**
     * Assert that the constructor parameters are generated correctly
     */
    private void assertConstructor() {
        UserData userListItem = new UserData(USER_ID, EMAIL_EMAIL_COM, ALIAS, FIRST_NAME,
                LAST_NAME, SALUTATION, STATUS);

        assert StringUtils.equals(userListItem.getEmail(), EMAIL_EMAIL_COM) : "email "
                + "must much in constructor for UserData!";
        assert StringUtils.equals(userListItem.getAlias(), ALIAS) : "alias must much in constructor for UserData!";
        assert StringUtils.equals(userListItem.getFirstName(), FIRST_NAME) : "firstName must much "
                + "in constructor for UserData!";
        assert StringUtils.equals(userListItem.getLastName(), LAST_NAME) : "lastName must much in "
                + "constructor for UserData!";
        assert StringUtils.equals(userListItem.getSalutation(), SALUTATION) : "salutation must much "
                + "in constructor for UserData!";
        assert userListItem.getStatus().equals(STATUS) : "status must much in constructor for UserData!";
        assert userListItem.getId().equals(USER_ID) : "userId must much in constructor for UserData!";
    }

    /**
     * Build a query depending if its to get the overall count or not
     * 
     * @param queryInstance
     *            the query instance
     * @return the query string with named parameters
     */
    @Override
    public String buildQuery(UserTaggingCoreQueryParameters queryInstance) {
        StringBuilder mainQuery = new StringBuilder();
        StringBuilder whereQuery = new StringBuilder();

        renderSelectClause(queryInstance, mainQuery);

        subQueryFindNoteWithTags(mainQuery, whereQuery, queryInstance,
                TagConstraintConnectorEnum.OR);

        if (whereQuery.length() > 0) {
            mainQuery.append(" where ");
            mainQuery.append(whereQuery);
        }

        mainQuery.append(" group by ");
        mainQuery.append(getGroupByParameters());

        renderOrderbyClause(mainQuery, queryInstance);

        return mainQuery.toString();
    }

    /**
     * Create a new empty query instance
     * 
     * @return the instance
     */
    @Override
    public UserTaggingCoreQueryParameters createInstance() {
        return new UserTaggingCoreQueryParameters(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParameters() {
        return constructorParameters;
    }

    /**
     * The parameters for the group by clause
     * 
     * @return the group by parameters
     */
    protected String getGroupByParameters() {
        return StringUtils.join(constructorParameters, ", ");
    }

    /**
     * the result prefix is the qualifying name of the object in the select clause (e.g. 'utr.')
     * 
     * @return the result prefix
     */
    public String getResultObjectPrefix() {
        return ALIAS_USER + ".";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean includeUsersWithoutTags(UserTaggingCoreQueryParameters instance) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean needUserInQuery(UserTaggingCoreQueryParameters queryInstance) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        assertConstructor();

        String prefix = getResultObjectPrefix();
        String profile = prefix + UserConstants.PROFILE + ".";

        constructorParameters = new String[] {
                prefix + "id",
                prefix + UserConstants.EMAIL,
                prefix + UserConstants.ALIAS,
                profile + UserProfileConstants.FIRSTNAME,
                profile + UserProfileConstants.LASTNAME,
                profile + UserProfileConstants.SALUTATION,
                prefix + UserConstants.STATUS
        };

        super.setupQueries();

    }
}
