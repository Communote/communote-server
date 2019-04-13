package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * <p>
 * Extension to {@link DelegatingFilterProxy} which is aware of refreshs of the application context
 * that manages the filter delegate. After a refresh of the context it will delegate to the new
 * instance of the filter bean. For the refresh observation to work the
 * {@link WebApplicationContext} needs to extend {@link AbstractApplicationContext}.
 * </p>
 * Most of the code is directly taken from {@link DelegatingFilterProxy}.
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 * @see com.communote.server.web.bootstrap.DispatcherServletInitializer
 */
public class RefreshAwareDelegatingFilterProxy extends DelegatingFilterProxy {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RefreshAwareDelegatingFilterProxy.class);

    private volatile Filter delegate;

    private final Object delegateMonitor = new Object();
    private boolean refreshListenerRegistered;

    private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            LOGGER.debug("Web application was refreshed - resetting filter delegate");
            synchronized (delegateMonitor) {
                // reset the delegate, doFilter will init it again with the bean from the refreshed
                // web app context
                delegate = null;
            }
        }
    }

    @Override
    protected void initFilterBean() throws ServletException {
        synchronized (this.delegateMonitor) {
            if (this.delegate == null) {
                // If no target bean name specified, use filter name.
                if (this.getTargetBeanName() == null) {
                    this.setTargetBeanName(getFilterName());
                }
                // Fetch Spring root application context and initialize the delegate early,
                // if possible. If the root application context will be started after this
                // filter proxy, we'll have to resort to lazy initialization.
                WebApplicationContext wac = findWebApplicationContext();
                if (wac != null) {
                    LOGGER.debug("Initializing filter delegate during bean initialization");
                    this.delegate = init(wac);
                } else {
                    LOGGER.debug("Web application context not available. Filter delegate will be initialized lazily.");
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lazily initialize the delegate if necessary.
        Filter delegateToUse = this.delegate;
        if (delegateToUse == null) {
            LOGGER.debug("Lazily initializing filter delegate");
            synchronized (this.delegateMonitor) {
                if (this.delegate == null) {
                    WebApplicationContext wac = findWebApplicationContext();
                    if (wac == null) {
                        throw new IllegalStateException(
                                "No WebApplicationContext found: no ContextLoaderListener registered?");
                    }
                    this.delegate = init(wac);
                }
                delegateToUse = this.delegate;
            }
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(delegateToUse, request, response, filterChain);
    }

    /**
     * Register a listener for the context refresh event and init the filter delegate as defined in
     * {@link #initDelegate(WebApplicationContext)}.
     * 
     * @param wac
     *            the web application context
     * @return the filter delegate
     * @throws ServletException
     *             if thrown by the filter
     */
    private Filter init(WebApplicationContext wac) throws ServletException {
        if (!refreshListenerRegistered) {
            LOGGER.debug("Registering listener to observe refreshes of web application context");
            if (wac instanceof AbstractApplicationContext) {
                // add refresh listener. Add to static listeners otherwise it would get lost during
                // refresh.
                ((AbstractApplicationContext) wac).getApplicationListeners().add(
                        new SourceFilteringListener(wac, new ContextRefreshListener()));
                refreshListenerRegistered = true;
            } else {
                LOGGER.warn(
                        "Refreshs of the web application context cannot be tracked because the context implementation does not support this");
            }
        }
        return initDelegate(wac);
    }
}
