package com.communote.server.core.vo.query.user;

import com.communote.server.core.filter.listitems.RankUserListItem;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.UserProfileConstants;


/**
 * Query definition to also rank the users
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RankUserTaggingCoreQuery extends AbstractUserTaggingCoreQuery<RankUserListItem> {

    /**
     * the parameters of the list item constructor
     */
    private String[] constructorParameters;

    private String groupByParameters;

    /**
     * default constructor
     */
    public RankUserTaggingCoreQuery() {
        super();
        assertConstructor();
    }

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
        Number rank = 100;

        RankUserListItem userListItem = new RankUserListItem(rank, userId, email, alias, firstName,
                lastName, salutation);

        assert userListItem.getRank().equals(rank) : "Rank must match in constructor for RankUserListItem!";
        assert email.equals(userListItem.getEmail()) : "email must match in constructor for RankUserListItem!";
        assert alias.equals(userListItem.getAlias()) : "alias must match in constructor for RankUserListItem!";
        assert firstName.equals(userListItem.getFirstName()) : "firstName must match in constructor "
                + "for RankUserListItem!";
        assert lastName.equals(userListItem.getLastName()) : "lastName must match in constructor for RankUserListItem!";
        assert salutation.equals(userListItem.getSalutation()) : "salutation must match in constructor "
                + "for RankUserListItem!";
        assert userId.equals(userListItem.getId()) : "userId must match in constructor for RankUserListItem!";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParameters() {
        return constructorParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupByParameters() {
        return groupByParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<RankUserListItem> getResultListItem() {
        return RankUserListItem.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        assertConstructor();

        StringBuilder groupByClause = new StringBuilder();

        String prefix = getResultObjectPrefix();
        String profile = prefix + UserConstants.PROFILE + ".";

        groupByClause.append(prefix);
        groupByClause.append("id, ");

        groupByClause.append(prefix);
        groupByClause.append(UserConstants.EMAIL + ", ");

        groupByClause.append(prefix);
        groupByClause.append(UserConstants.ALIAS + ", ");

        groupByClause.append(profile);
        groupByClause.append(UserProfileConstants.FIRSTNAME + ", ");

        groupByClause.append(profile);
        groupByClause.append(UserProfileConstants.LASTNAME + ", ");

        groupByClause.append(profile);
        groupByClause.append(UserProfileConstants.SALUTATION);

        constructorParameters = new String[] {
                "count(distinct " + getNoteAlias() + "id)",
                prefix + "id",
                prefix + UserConstants.EMAIL,
                prefix + UserConstants.ALIAS,
                profile + UserProfileConstants.FIRSTNAME,
                profile + UserProfileConstants.LASTNAME,
                profile + UserProfileConstants.SALUTATION
        };
        groupByParameters = groupByClause.toString();

        super.setupQueries();

    }
}
