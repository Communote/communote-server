package com.communote.server.core.vo.query.user;

import com.communote.server.api.core.user.UserData;

/**
 * Query Definition to find users
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTaggingCoreQuery extends AbstractUserTaggingCoreQuery<UserData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<UserData> getResultListItem() {
        return UserData.class;
    }
}
