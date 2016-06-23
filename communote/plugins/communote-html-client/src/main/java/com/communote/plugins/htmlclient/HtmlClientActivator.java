package com.communote.plugins.htmlclient;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.communote.server.web.fe.portal.controller.EnhancedPageSection;
import com.communote.server.web.fe.portal.service.ServicePageHelper;
import com.communote.server.web.fe.portal.service.ToolsPageContentManagerFactory;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class HtmlClientActivator implements BundleActivator {

    private static final String PAGE_SECTION_ID = "mobile-web";

    @Override
    public void start(BundleContext context) throws Exception {
        EnhancedPageSection section = new EnhancedPageSection(PAGE_SECTION_ID);
        // position after apps
        section.setOrder(ToolsPageContentManagerFactory.SECTION_ORDER_MOBILE_APPS - 50);
        section.setShortTitleMessageKey("htmlclient.toolspage.title.short");
        section.setTitleMessageKey("htmlclient.toolspage.title");
        section.setContentMessageKey("htmlclient.toolspage.description");
        section.addActionLink("htmlclient.toolspage.button", "/portal/mobile");
        section.setImageLink("/portal/mobile");
        section.setImageUrl("/plugins/" + context.getBundle().getSymbolicName()
                + "/images/mobile-site.png?t=" + context.getBundle().getVersion().toString());
        ServicePageHelper.addToolsPageSection(section);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        ServicePageHelper.removeToolsPageSection(PAGE_SECTION_ID);
    }

}
