package com.communote.plugins.bookmarklet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.external.spring.security.CommunoteAuthenticationSuccessHandler;
import com.communote.server.web.fe.portal.service.ServicePageHelper;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class BookmarkletActivator implements BundleActivator {

    private BookmarkletToolsPageSection toolsPageSection;
    private String redirectionTarget;

    @Override
    public void start(BundleContext context) throws Exception {
        this.toolsPageSection = new BookmarkletToolsPageSection(context.getBundle());
        ServicePageHelper.addToolsPageSection(toolsPageSection);
        this.redirectionTarget = WebServiceLocator.findService(
                CommunoteAuthenticationSuccessHandler.class).addRedirectionTarget("/bookmarklet",
                false);
    }

    /**
     * Does nothing.
     *
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.toolsPageSection != null) {
            ServicePageHelper.removeToolsPageSection(this.toolsPageSection.getId());
        }
        if (this.redirectionTarget != null) {
            WebServiceLocator.findService(CommunoteAuthenticationSuccessHandler.class)
                    .removeRedirectionTarget(this.redirectionTarget);
        }
    }

}
