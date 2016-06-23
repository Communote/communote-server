package com.communote.server.web.commons.tags;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;

import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * Tag to render the tooltip out.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TipTag extends TagSupport {

    private static final long serialVersionUID = 7276317848945363153L;

    /** Logger. */
    private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger
            .getLogger(TipTag.class);

    private String key;
    private Object[] args;

    /**
     * {@inheritDoc}
     */
    @Override
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        Locale locale = getLocale();
        try {
            out.write("<span class=\"tooltip-wrapper\"><a class=\"tooltip\"");
            String title;
            if (ResourceBundleManager.instance().knowsMessageKey(key + ".title", new Object[0],
                    locale)) {
                title = ResourceBundleManager.instance().getText(key + ".title", locale);
            } else {
                title = ResourceBundleManager.instance().getText("tip.title.default", locale);
            }
            out.write(" title=\"" + title + "\"");

            String message = ResourceBundleManager.instance().getText(key, locale, args);
            if (StringUtils.isNotBlank(message)) {
                out.write(" rel=\"" + message + "\"");
            }
            out.write(">[?]</a></span>");
        } catch (IOException e) {
            LOG.warn("Couldn't write out tip.", e);
        }
        return EVAL_PAGE;
    }

    /**
     * @return the args
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the locale of the current user.
     * 
     * @return Current locale or {@link Locale#ENGLISH} as default.
     */
    private Locale getLocale() {
        ServletRequest request = pageContext.getRequest();
        if (request instanceof HttpServletRequest) {
            return SessionHandler.instance().getCurrentLocale(
                    (HttpServletRequest) request);
        }
        Locale locale = request.getLocale();
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        return locale;
    }

    /**
     * @param args
     *            the args to set
     */
    public void setArgs(Object[] args) {
        this.args = args;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

}
