package com.communote.plugins.webservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.xml.sax.EntityResolver;

import com.communote.plugins.core.classloader.ClassLoaderAggregatorThreadContextSetter;
import com.communote.plugins.core.classloader.ClassLoaderAggregatorThreadContextSetterUnchecked;
import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.plugins.webservice.CommunoteWebServiceDefinition;
import com.communote.plugins.webservice.CommunoteWebServiceRegistry;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.server.EndpointFactory;
import com.sun.xml.ws.transport.http.HttpAdapterList;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component(immediate = true)
@Provides
@Instantiate
@UrlMapping("/*/api/ws/**")
public class CommunoteWebServiceController implements Controller, ServletContextAware,
        CommunoteWebServiceRegistry {

    private static final String PREFIX_WEBSERVICE = "/api/ws";

    private final static Logger LOGGER = LoggerFactory
            .getLogger(CommunoteWebServiceController.class);

    private WSServletDelegate delegate;

    private final Map<CommunoteWebServiceDefinition, ServletAdapter> adapters = new HashMap<>();
    private HttpAdapterList<ServletAdapter> adapterFactory;

    private final HashSet<SDDocumentSource> emptyDocValues = new HashSet<SDDocumentSource>();

    private ServletContext servletContext;

    private boolean initialised;

    // private Set<ClassLoader> classloaders = new HashSet<>();

    /**
     * Create adapter from web service definition
     * 
     * @param communoteWebServiceDefinition
     * @return
     */
    private ServletAdapter createAdapter(CommunoteWebServiceDefinition communoteWebServiceDefinition) {
        QName serviceName = new QName(communoteWebServiceDefinition.getNameSpaceUri(),
                communoteWebServiceDefinition.getLocalPartName());
        QName portName = EndpointFactory.getDefaultPortName(serviceName,
                communoteWebServiceDefinition.getServiceClass());
        SDDocumentSource primaryWSDL = null;

        Container container = null;
        WSBinding binding = createBinding(communoteWebServiceDefinition.getServiceClass());

        WSEndpoint<?> endpoint = WSEndpoint.create(
                communoteWebServiceDefinition.getServiceClass(),
                true,
                null,
                serviceName,
                portName,
                container,
                binding,
                primaryWSDL,
                emptyDocValues,
                (EntityResolver) null,
                false);

        ServletAdapter adapter = adapterFactory.createAdapter(
                communoteWebServiceDefinition.getEndpointName(),
                getWebserviceUrlPattern(communoteWebServiceDefinition), endpoint);
        return adapter;
    }

    /**
     * @param ddBindingId
     *            binding id explicitly specified in the DeploymentDescriptor or parameter
     * @param implClass
     *            Endpoint Implementation class
     * @param mtomEnabled
     *            represents mtom-enabled attribute in DD
     * @param mtomThreshold
     *            threshold value specified in DD
     * @return is returned with only MTOMFeature set resolving the various precendece rules
     */
    private WSBinding createBinding(Class<?> implClass) {
        // Features specified through DD
        WebServiceFeatureList features;

        BindingID bindingID;

        bindingID = BindingID.parse(implClass);

        features = new WebServiceFeatureList();
        features.addAll(bindingID.createBuiltinFeatureList());

        return bindingID.createBinding(features.toArray());
    }

    private String getWebserviceUrlPattern(CommunoteWebServiceDefinition definition) {
        return getWebServiceUrlPattern(definition.getPluginName(),
                definition.getRelativeUrlPattern());

    }

    private String getWebServiceUrlPattern(String pluginName, String urlPattern) {
        pluginName = StringUtils.trimToEmpty(pluginName);
        urlPattern = StringUtils.trimToEmpty(urlPattern);

        if (!pluginName.startsWith("/")) {
            pluginName = "/" + pluginName;
        }
        if (pluginName.endsWith("/")) {
            pluginName = pluginName.substring(0, pluginName.length() - 1);
        }
        if (!urlPattern.startsWith("/")) {
            urlPattern = "/" + urlPattern;
        }
        if (urlPattern.endsWith("/")) {
            urlPattern = urlPattern.substring(0, urlPattern.length() - 1);
        }

        String fullUrlPattern = PREFIX_WEBSERVICE + pluginName + urlPattern;
        return fullUrlPattern;
    }

    @Override
    public ModelAndView handleRequest(final HttpServletRequest request,
            final HttpServletResponse response)
            throws ServletException {

        ClassLoaderAggregatorThreadContextSetter<ServletException> classLoaderAggregatorSetter = new ClassLoaderAggregatorThreadContextSetter<ServletException>() {

            @Override
            protected void run() throws ServletException {

                if (!initialised) {
                    LOGGER.warn("Webservice Controller not initalised. Is servlet context set?");
                }
                else if (delegate == null) {
                    if (adapters.size() == 0) {
                        LOGGER.info("Webservice delegate not set. No adapters are defined.");
                    } else {
                        LOGGER.warn("Webservice delegate should be set but is not. adapters: "
                                + adapters);
                    }
                } else {
                    final HttpServletRequest wrappedRequest = new CommunoteClientRemoveHttpServletRequestWrapper(
                            request);

                    String method = request.getMethod();
                    if ("GET".equals(method)) {
                        delegate.doGet(wrappedRequest, response, servletContext);
                    } else if ("POST".equals(method)) {
                        delegate.doPost(wrappedRequest, response, servletContext);
                    } else if ("PUT".equals(method)) {
                        delegate.doPut(wrappedRequest, response, servletContext);
                    } else if ("HEAD".equals(method)) {
                        delegate.doHead(wrappedRequest, response, servletContext);
                    } else if ("DELETE".equals(method)) {
                        delegate.doDelete(wrappedRequest, response, servletContext);
                    } else {
                        LOGGER.warn("Invalid request method: " + method);
                    }
                }

            }
        };

        classLoaderAggregatorSetter.execute(this.getClass().getClassLoader());

        return null;
    }

    private void init() {
        if (!initialised) {
            synchronized (this) {
                if (!initialised) {

                    adapterFactory = new ServletAdapterList(servletContext);
                    initialised = true;
                    LOGGER.info("Web Service Plugin initalised. ");

                    ClassLoaderAggregatorThreadContextSetterUnchecked classLoaderAggregatorSetter = new ClassLoaderAggregatorThreadContextSetterUnchecked() {

                        @Override
                        protected void run() {

                            for (CommunoteWebServiceDefinition definition : new HashSet<>(
                                    adapters.keySet())) {
                                ServletAdapter adapter = createAdapter(definition);
                                adapters.put(definition, adapter);
                            }
                            LOGGER.debug("Web Service Plugin initalised {} adapters. ",
                                    adapters.size());
                            restartDelegate();

                        }
                    };
                    // Set<ClassLoader> cls = new HashSet<>(this.classloaders);
                    // cls.add(this.getClass().getClassLoader());

                    classLoaderAggregatorSetter.execute(this.getClass().getClassLoader());

                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.plugins.webservice.service.CommunoteWebServiceRegistry#registerService(com.
     * communote.plugins.webservice.service.CommunoteWebServiceDefinition)
     */
    @Override
    public synchronized void registerService(
            final CommunoteWebServiceDefinition communoteWebserviceDefinition) {

        // classloaders.add(Thread.currentThread().getContextClassLoader());

        ClassLoaderAggregatorThreadContextSetterUnchecked classLoaderAggregatorSetter = new ClassLoaderAggregatorThreadContextSetterUnchecked() {

            @Override
            protected void run() {

                adapters.put(communoteWebserviceDefinition, null);

                if (initialised) {
                    ServletAdapter adapter = createAdapter(communoteWebserviceDefinition);
                    adapters.put(communoteWebserviceDefinition, adapter);

                    LOGGER.info("Added web service with adapter: {} ",
                            communoteWebserviceDefinition);

                    restartDelegate();
                } else {
                    LOGGER.info("Added web service without adapter: {} ",
                            communoteWebserviceDefinition);
                }
            }
        };

        classLoaderAggregatorSetter.execute(this.getClass().getClassLoader());

    }

    private synchronized void restartDelegate() {
        LOGGER.trace("Restarting web service delegate. ");
        if (delegate != null) {

            delegate.destroy();
        }
        delegate = null;

        if (initialised && this.adapters.size() > 0) {

            delegate = new WSServletDelegate(
                    new ArrayList<>(adapters.values()),
                    this.servletContext);
        }
        LOGGER.debug("Restarted web service delegate with {} adapters. ", adapters.size());
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        if (servletContext == null) {
            throw new IllegalArgumentException("servletContext cannot be null.");
        }
        this.servletContext = servletContext;
        init();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.plugins.webservice.service.CommunoteWebServiceRegistry#unregisterService(com
     * .communote.plugins.webservice.service.CommunoteWebServiceDefinition)
     */
    @Override
    public void unregisterService(CommunoteWebServiceDefinition definition) {
        this.unregisterService(definition.getPluginName(), definition.getRelativeUrlPattern());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.plugins.webservice.service.CommunoteWebServiceRegistry#unregisterService(java
     * .lang.String, String)
     */
    @Override
    public synchronized void unregisterService(String pluginName, String urlPattern) {
        boolean restart = false;
        for (CommunoteWebServiceDefinition definition : new HashSet<>(this.adapters.keySet())) {
            if (StringUtils.equals(getWebserviceUrlPattern(definition),
                    getWebServiceUrlPattern(pluginName, urlPattern))) {
                this.adapters.remove(definition);
                restart = true;
            }
        }
        if (restart) {
            restartDelegate();
        }
    }

}