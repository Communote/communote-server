package com.communote.server.core.vo.query.user;

import com.communote.server.core.filter.listitems.UserManagementListItem;

/**
 * The Class UserManagementQueryDefinition.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserManagementQuery extends
        AbstractUserQuery<UserManagementListItem, UserManagementQueryParameters> {

    @Override
    public UserManagementQueryParameters createInstance() {
        return new UserManagementQueryParameters();
    }
}
