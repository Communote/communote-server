package com.communote.server.web.fe.admin;

import java.util.Locale;

import com.communote.common.menu.MenuEntry;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * Menu entry with ID and a label based on a message key
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BasicMenuEntry implements MenuEntry {

    private final String labelMessageKey;
    private final String id;

    /**
     * Create a menu entry with ID and a label based on a message key
     *
     * @param id
     *            the ID of the entry
     * @param labelMessageKey
     *            the message key of the localizable label
     */
    public BasicMenuEntry(String id, String labelMessageKey) {
        this.id = id;
        this.labelMessageKey = labelMessageKey;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return getLocalizedLabel(Locale.ENGLISH);
    }

    /**
     * @return the message key of the localizable label
     */
    public String getLabelMessageKey() {
        return labelMessageKey;
    }

    @Override
    public String getLocalizedLabel(Locale locale, Object... arguments) {
        return ResourceBundleManager.instance().getText(labelMessageKey, locale, arguments);
    }

}
