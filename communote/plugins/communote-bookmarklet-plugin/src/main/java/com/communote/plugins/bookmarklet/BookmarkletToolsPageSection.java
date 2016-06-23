package com.communote.plugins.bookmarklet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.Bundle;

import com.communote.common.util.Pair;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.portal.controller.EnhancedPageSection;
import com.communote.server.web.fe.portal.service.ToolsPageContentManagerFactory;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class BookmarkletToolsPageSection extends EnhancedPageSection {

    public BookmarkletToolsPageSection(Bundle bundle) {
        super("bookmarklet");
        setOrder(ToolsPageContentManagerFactory.SECTION_ORDER_DESKTOP_CLIENT + 100);
        setTitleMessageKey("bookmarklet.toolspage.title");
        setShortTitleMessageKey("bookmarklet.toolspage.title.short");
        setContentMessageKey("bookmarklet.toolspage.description");
        setImageUrl("/plugins/" + bundle.getSymbolicName() + "/images/bookmarklet.png?t="
                + bundle.getVersion().toString());
    }

    @Override
    public List<Pair<String, String>> getActionLinks(Locale locale, HttpServletRequest request) {
        ArrayList<Pair<String, String>> links = new ArrayList<>();
        String url = "javascript:(function(){var u='"
                + ControllerHelper.renderAbsoluteUrl(request, null, "/bookmarklet", false, false,
                        false)
                        + "?u='+encodeURIComponent(window.location.href)+'&t='+encodeURIComponent(document.title)+'&c='+encodeURIComponent((window.getSelection?window.getSelection():document.getSelection?document.getSelection():document.selection.createRange().text));"
                        + "u=u.substring(0,1900);var o=function(){if(!window.open(u,'cnt_bookmarklet','location=yes,links=no,scrollbars=yes,toolbar=no,width=718,height=620')){if(navigator.userAgent.indexOf('MSIE 9.')==-1)location.href=u}};o()})()";
        String label = ResourceBundleManager.instance().getText("bookmarklet.toolspage.button",
                locale);
        links.add(new Pair<>(label, url));
        return links;
    }

    @Override
    public boolean hasActionLinks() {
        return true;
    }

}
