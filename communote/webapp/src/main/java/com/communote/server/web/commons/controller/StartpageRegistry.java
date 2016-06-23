package com.communote.server.web.commons.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Registry to define the current start page.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StartpageRegistry implements InitializingBean {
    /** */
    private static final Logger LOG = Logger.getLogger(StartpageRegistry.class);

    /** */
    private String defaultStartpage;
    /** */
    private String startpage;
    /** */
    private Controller startpageController;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(this.defaultStartpage, "defaultStartpage must be specified");
    }

    /**
     * Corrects the URL by prepending a slash if necessary
     * 
     * @param url
     *            the relative URL to correct
     * @return the corrected URL
     */
    private String correctUrl(String url) {
        if (url != null && !url.startsWith("/")) {
            return "/" + url;
        }
        return url;
    }

    /**
     * @return the current start page
     */
    public String getStartpage() {
        return this.startpage;
    }

    /**
     * Registers a start page providing controller. The start page of the controller will be set as
     * the current start page which can for instance be used as default redirection target or as
     * target page after successful authentication.
     * 
     * @param controller
     *            the controller to register
     * @param startpage
     *            the start page to register as relative URL
     */
    public void registerStartpageController(Controller controller, String startpage) {
        startpage = correctUrl(startpage);
        if (startpage != null) {
            synchronized (this) {
                this.startpage = startpage;
                this.startpageController = controller;
                LOG.info("Setting start page to " + startpage);
            }
        }
    }

    /**
     * Sets the default start page which will be used as fallback if no start page is set.
     * 
     * @param defaultStartpage
     *            a relative URL
     */
    public void setDefaultStartpage(String defaultStartpage) {
        defaultStartpage = correctUrl(defaultStartpage);
        this.defaultStartpage = defaultStartpage;
        if (this.startpage == null) {
            this.startpage = defaultStartpage;
        }
    }

    /**
     * Unregister a controller that provided a start page. If the controller is the last controller
     * that was added with {@link #registerStartpageController(Controller, String)} the start page
     * is reset to the default start page.
     * 
     * @param controller
     *            the controller to unregister
     */
    public void unregisterStartpageController(Controller controller) {
        synchronized (this) {
            if (controller != null && controller.equals(this.startpageController)) {
                this.startpage = this.defaultStartpage;
                this.startpageController = null;
                LOG.info("Setting start page back to default " + this.startpage);
            }
        }
    }
}
