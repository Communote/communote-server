package com.communote.server.core.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.restlet.ext.jaxrs.JaxRsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.communote.common.util.VersionComparator;

/**
 * Manager or register, unregister and getting {@link JaxRsApplication} for an specific version (for
 * example a simple major.minor (like 1.3) or prisma-1.3).
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            instance of extended {@link JaxRsApplication}
 */
// TODO this class is not thread safe
@Service
public class RestletApplicationManager<T extends JaxRsApplication> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RestletApplicationManager.class);

    private final Map<String, T> restletApplications = new HashMap<String, T>();

    private final Map<String, CommunoteRestletServletAdapter> restletAdapter = new HashMap<String, CommunoteRestletServletAdapter>();

    private List<String> versionsList = new ArrayList<String>();

    /**
     * Default constructor for restlet application manager
     */
    public RestletApplicationManager() {
        // Do nothing.
    }

    /**
     * Getting an registered {@link JaxRsApplication} for specific version
     *
     * @param version
     *            version of the application in the format major.minor like 1.3
     * @return Application
     */
    public T getApplication(String version) {
        return restletApplications.get(version);
    }

    /**
     * Getter for the {@link CommunoteRestletServletAdapter}. If
     * {@link CommunoteRestletServletAdapter} not exists, than create an instance, combine this with
     * the corresponding restlet application using the version and add this to an map.
     *
     * @param version
     *            version of api
     * @param context
     *            {@link ServletContext}
     * @return {@link CommunoteRestletServletAdapter}
     */
    public CommunoteRestletServletAdapter getServletAdapter(String version, ServletContext context) {
        CommunoteRestletServletAdapter adapter = restletAdapter.get(version);
        if (adapter == null) {
            adapter = new CommunoteRestletServletAdapter(context);
            if (adapter != null) {
                adapter.setNext(restletApplications.get(version));
                restletAdapter.put(version, adapter);
            }
        }
        return adapter;
    }

    /**
     * Getting all supported versions of the REST API sorted
     *
     * @return an array of supported versions
     */
    public List<String> getVersions() {
        return versionsList;
    }

    /**
     * Register {@link JaxRsApplication} for specific version. The restlet application is setting to
     * an map using the version. The old {@link CommunoteRestletServletAdapter} is removed (when
     * exists) from specific map.
     *
     *
     * @param version
     *            version of the application in the format major.minor like 1.3
     * @param application
     *            instance of extended {@link JaxRsApplication}
     */
    public void registerApplication(String version, T application) {
        restletApplications.put(version, application);
        restletAdapter.remove(version);
        ArrayList<String> arrVersionsList = new ArrayList<String>(restletApplications.keySet());
        Collections.sort(arrVersionsList, new VersionComparator());
        Collections.reverse(arrVersionsList);
        versionsList = arrVersionsList;
        LOGGER.debug("Restlet application with version {} registered.", version);
    }

    /**
     * Unregister {@link JaxRsApplication} for specific version. The
     * {@link CommunoteRestletServletAdapter} is removed when it exists.
     *
     * @param version
     *            version of the application in the format major.minor like 1.3
     */
    public void unregisterApplication(String version) {
        restletApplications.remove(version);
        restletAdapter.remove(version);
        versionsList.remove(version);
        LOGGER.debug("Restlet application with version {} unregistered.", version);
    }

}
