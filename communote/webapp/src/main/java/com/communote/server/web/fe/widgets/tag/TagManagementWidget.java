package com.communote.server.web.fe.widgets.tag;

import java.util.Locale;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget for managing tags.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagManagementWidget extends AbstractWidget {

    /**
     * @return the locale of the current user. will never be null
     */
    protected Locale getLocale() {
        return SessionHandler.instance().getCurrentLocale(getRequest());
    }

    @Override
    @Deprecated
    public String getTile(String outputType) {
        return "core.widget.tag.management";
    }

    @Override
    public TagData handleRequest() {
        Locale locale = SessionHandler.instance().getCurrentLocale(getRequest());
        String tagIds = getParameter("tagIds");
        Long tagId = Long.parseLong(tagIds);
        TagManagement tagManagement = ServiceLocator.instance().getService(TagManagement.class);
        TagData tag = tagManagement.findTag(tagId, locale);
        if (tag != null) {
            setResponseMetadata("tagName", tag.getDefaultName());
            setAttribute("internalTag",
                    TagStoreType.Types.NOTE.getDefaultTagStoreId().equals(tag.getTagStoreAlias()));
        }
        return tag;
    }

    @Override
    protected void initParameters() {
        // nothing to do here
    }

}
