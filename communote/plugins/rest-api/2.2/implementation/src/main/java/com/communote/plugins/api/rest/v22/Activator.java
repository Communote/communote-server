package com.communote.plugins.api.rest.v22;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.restlet.Component;

import com.communote.plugins.api.rest.v22.service.CommunoteJaxRsApplicationConfig;
import com.communote.plugins.api.rest.v22.servlet.CommunoteJaxRsApplication;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.rest.RestletApplicationManager;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Activator implements BundleActivator {

    private static final String PROPERTY_KEY_VERSION = "Communote-Version-Rest-Api";

    /**
     * Getter for the {@link RestletApplicationManager}
     * 
     * @return {@link RestletApplicationManager}
     */
    private RestletApplicationManager<CommunoteJaxRsApplication> getRestletApplicationManager() {
        return ServiceLocator.instance().getService(RestletApplicationManager.class);
    }

    /**
     * Retrieves the version of the REST-Api.
     * 
     * @param bundleContext
     *            The context.
     * @return String of major and minor version like "1.3"
     */
    private String getVersion(BundleContext bundleContext) {
        Object version = bundleContext.getBundle().getHeaders().get(PROPERTY_KEY_VERSION);
        return version != null ? version.toString() : null;
    }

    /**
     * Register the application in {@link RestletApplicationManager} using the current bundle
     * version.
     * 
     * @param context
     *            The context of this bundle.
     * @throws Exception
     *             Exception.
     */
    @Override
    public void start(BundleContext context) throws Exception {

        final Component component = new Component();
        component.start();

        CommunoteJaxRsApplication application = new CommunoteJaxRsApplication(component,
                new CommunoteJaxRsApplicationConfig());
        application.getTunnelService().setExtensionsTunnel(false);
        getRestletApplicationManager().registerApplication(getVersion(context), application);
    }

    /**
     * Unregister the application in {@link RestletApplicationManager} using the current bundle
     * version.
     * 
     * @param context
     *            The context of this bundle.
     * @throws Exception
     *             Exception.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        getRestletApplicationManager().unregisterApplication(getVersion(context));
    }

}
