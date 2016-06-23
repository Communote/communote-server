package com.communote.server.web.commons.tags;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * The class for the kenmei url tag.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class KenmeiUrlTag extends BodyTagSupport {

    /** serialization id. */
    private static final long serialVersionUID = 1L;

    private final static Logger LOG = Logger.getLogger(KenmeiUrlTag.class);

    /** The value. */
    private String value;

    /** The custom client id. If set the current client id will be ignored. */
    private String clientId = null;

    /** Render the session id in the url */
    private Boolean renderSessionId = null;

    private boolean absolute = false;
    private boolean secure = false;
    private String baseUrl = null;
    /**
     * true if the url should be rendered as a resource not to be delivered by the dispatcher
     * servlet
     */
    private boolean staticResource = false;

    /**
     * true if the url should be rendered as a resource BUT delivered by the dispatcher servlet (see
     * KenmeiForwardFilter)
     */
    private boolean staticResourceByDispatcher = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public int doEndTag() throws JspException {
        String urlPath = this.value;
        if (urlPath == null) {
            urlPath = WebServiceLocator.instance().getStartpageRegistry().getStartpage();
        }
        String url = ControllerHelper.renderUrl((HttpServletRequest) pageContext.getRequest(),
                urlPath, this.baseUrl, this.absolute, this.secure, this.renderSessionId,
                this.clientId, this.staticResource, this.staticResourceByDispatcher);
        try {

            pageContext.getOut().write(url);

            // TODO this looks awkward
            if (url.endsWith(".js") || url.endsWith(".css")) {
                // TODO cache the modification date
                String fileName = pageContext.getServletContext().getRealPath(this.value);
                File file = new File(fileName);
                if (file.exists()) {
                    long lastModified = file.lastModified();
                    if (url.contains("?")) {
                        pageContext.getOut().write("&timestamp=" + lastModified);
                    } else {
                        pageContext.getOut().write("?timestamp=" + lastModified);
                    }
                } else {
                    LOG.error("could not create timestamped resource url, file not found: '"
                            + fileName + "'");
                }
            }

        } catch (IOException e) {
            throw new JspTagException("IO Error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new JspTagException("Unknown error: " + e.getMessage(), e);
        }
        return EVAL_PAGE;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns whether to render an absolute URL. Will be ignored if {@link #getBaseUrl()} is set.
     *
     * @return true if the URL should be rendered as absolute URL
     */
    public boolean isAbsolute() {
        return absolute;
    }

    /**
     * Returns whether to render the absolute URL as a URL using HTTPS. Will be ignored if
     * {@link #isAbsolute()} returns false.
     *
     * @return TRUE if the absolute URL is to be rendered as a URL using HTTPS
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * @return true if the url should be rendered as a resource not to be delivered by the
     *         dispatcher servlet
     */
    public boolean isStaticResource() {
        return staticResource;
    }

    /**
     *
     * @return true if the url should be rendered as a resource BUT delivered by the dispatcher
     *         servlet (see KenmeiForwardFilter)
     */
    public boolean isStaticResourceByDispatcher() {
        return staticResourceByDispatcher;
    }

    /**
     * Whether to render an absolute URL. Will be ignored if {@link #getBaseUrl()} is set.
     *
     * @param absolute
     *            True to render an absolute URL
     */
    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    /**
     * The base URL. If set it will be used as prefix for the URL instead of module and or client
     * id.
     *
     * @param baseUrl
     *            the baseUrl to set
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Sets a custom client ID to render the URL for the named client instead of the current client.
     *
     * @param clientId
     *            the client ID to use
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Decide if the session should be rendered in the url (default is true).
     *
     * @param renderSessionId
     *            true to render the client id into the url
     */
    public void setRenderSessionId(boolean renderSessionId) {
        this.renderSessionId = renderSessionId;
    }

    /**
     * Whether to render the absolute URL as a URL using HTTPS. Will be ignored if
     * {@link #isAbsolute()} returns false.
     *
     * @param secure
     *            TRUE to render the absolute URL as a URL using HTTPS
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     *
     * @param staticResource
     *            true if the url should be rendered as a resource not to be delivered by the
     *            dispatcher servlet
     */
    public void setStaticResource(boolean staticResource) {
        this.staticResource = staticResource;
    }

    /**
     *
     * @param staticResourceByDispatcher
     *            true if the url should be rendered as a resource BUT delivered by the dispatcher
     *            servlet (see KenmeiForwardFilter)
     */
    public void setStaticResourceByDispatcher(boolean staticResourceByDispatcher) {
        this.staticResourceByDispatcher = staticResourceByDispatcher;
    }

    /**
     * the URL path to the resource to link to
     *
     * @param value
     *            the value
     */
    public void setValue(String value) {
        this.value = value;
    }

}
