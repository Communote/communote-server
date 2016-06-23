package com.communote.server.web.fe.admin;

import com.communote.common.menu.EntryIdIncludeExcludeFilter;
import com.communote.common.menu.MenuEntry;
import com.communote.server.api.core.config.type.ApplicationProperty;

/**
 * Include or exclude entries if HTTPS is support by the Communote installation.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            type of the menu entry this filter handles
 */
public class HttpsSupportFilter<T extends MenuEntry> extends EntryIdIncludeExcludeFilter<T> {

    @Override
    protected boolean testCondition(T entry) {
        return Boolean.parseBoolean(ApplicationProperty.WEB_HTTPS_SUPPORTED.getValue());
    }

}
