package com.communote.server.web.fe.admin;

import com.communote.common.menu.EntryIdIncludeExcludeFilter;
import com.communote.common.menu.MenuEntry;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Filter to include or exclude entries if the current client is the global client.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            type of the menu entry this filter handles
 */
public class GlobalClientFilter<T extends MenuEntry> extends EntryIdIncludeExcludeFilter<T> {

    /**
     * @return true if the current client is the global client
     */
    @Override
    protected boolean testCondition(T entry) {
        return ClientHelper.isCurrentClientGlobal();
    }

}
