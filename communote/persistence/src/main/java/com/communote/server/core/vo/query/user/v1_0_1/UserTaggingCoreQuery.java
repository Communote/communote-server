package com.communote.server.core.vo.query.user.v1_0_1;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.vo.query.TagConstraintConnectorEnum;
import com.communote.server.core.vo.query.TaggingCoreItemQueryDefinition;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.UserProfileConstants;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.UserDao;

/**
 * Query Definition to find users
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTaggingCoreQuery extends
        TaggingCoreItemQueryDefinition<DetailedUserData, UserTaggingCoreQueryParameters> {

    /**
     * the parameters of the list item constructor
     */
    private String[] constructorParameters;

    /**
     * Assert that the constructor parameters are generated correctly
     */
    private void assertConstructor() {
        Long userId = 12l;
        String email = "email@email.com";
        String alias = "alias";
        String firstName = "firstName";
        String lastName = "lastName";
        String salutation = "salutation";
        UserStatus status = UserStatus.ACTIVE;
        Date lastModificationDate = new Date();
        Date lastPhotoModificationDate = new Date(23);

        DetailedUserData userListItem = new DetailedUserData(lastModificationDate,
                lastPhotoModificationDate, userId, email, alias, firstName, lastName, salutation,
                status);

        assert StringUtils.equals(userListItem.getEmail(), email) : ""
                + "email must much in constructor for UserData!";
        assert StringUtils.equals(userListItem.getAlias(), alias) : ""
                + "alias must much in constructor for UserData!";
        assert StringUtils.equals(userListItem.getFirstName(), firstName) : ""
                + "firstName must much in constructor for UserData!";
        assert StringUtils.equals(userListItem.getLastName(), lastName) : ""
                + "lastName must much in constructor for UserData!";
        assert StringUtils.equals(userListItem.getSalutation(), salutation) : ""
                + "salutation must much in constructor for UserData!";
        assert userListItem.getLastModificationDate().equals(lastModificationDate) : ""
                + "lastModificationDate must much in constructor for UserData!";
        assert userListItem.getLastPhotoModificationDate().equals(lastPhotoModificationDate) : ""
                + "lastPhotoModificationDate must much in constructor for UserData!";
        assert userListItem.getStatus().equals(status) : ""
                + "status must much in constructor for UserData!";
        assert userListItem.getId().equals(userId) : ""
                + "userId must much in constructor for UserData!";
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
     * {@inheritDoc}
     */
    @Override
    public Class<DetailedUserData> getResultListItem() {
        return DetailedUserData.class;
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
    protected boolean needDistinct(UserTaggingCoreQueryParameters queryInstance) {
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
    @Deprecated
    public PageableList postQueryExecution(UserTaggingCoreQueryParameters queryParameters,
            PageableList result) {
        UserDao kenmeiUserDao = ServiceLocator.findService(UserDao.class);

        // that is not nice, load the user and user profile and set the first and last name since
        // the name is lowered in the select query.
        // however since this all is deprecated and will be removed hopefully soon it seems to be
        // the simplest working solution
        for (DetailedUserData userListItem : (PageableList<DetailedUserData>) result) {

            User user = kenmeiUserDao.load(userListItem.getId());

            if (user.getProfile() != null) {
                userListItem.setLastName(user.getProfile().getLastName());
                userListItem.setFirstName(user.getProfile().getFirstName());
            }
        }

        return super.postQueryExecution(queryParameters, result);
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
                profile + UserProfileConstants.LASTMODIFICATIONDATE,
                profile + UserProfileConstants.LASTPHOTOMODIFICATIONDATE,
                prefix + "id",
                prefix + UserConstants.EMAIL,
                prefix + UserConstants.ALIAS,
                "lower(" + profile + UserProfileConstants.FIRSTNAME + ")",
                "lower(" + profile + UserProfileConstants.LASTNAME + ")",
                profile + UserProfileConstants.SALUTATION,
                prefix + UserConstants.STATUS,
        };

        super.setupQueries();

    }
}
