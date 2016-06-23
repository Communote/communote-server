package com.communote.server.web.commons.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.apache.taglibs.standard.tag.rt.fmt.MessageTag;

import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * Custom Message tag.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
// TODO this creates a dependency to a certain JSTL implementation! Better just inherit from
// BodyTagSupport and expose this tag under a custom namespace like cfmt, especially since we
// completely override the parent logic. Note: If doing so the param tag also has to be implemented.
public class FmtMessageTag extends MessageTag {

    private static final long serialVersionUID = -4218972875475731234L;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    private final List<Object> params = new ArrayList<Object>();

    /**
     * Adds an argument (for parametric replacement) to this tag's message.
     *
     * @see org.apache.taglibs.standard.tag.common.fmt.ParamSupport
     *
     * @param parameter
     *            The parameter.
     */
    @Override
    public void addParam(Object parameter) {
        params.add(parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int doEndTag() throws JspException {
        String key = null;
        if (keySpecified) {
            key = keyAttrValue;
        } else if (bodyContent != null && bodyContent.getString() != null) {
            key = bodyContent.getString().trim();
        }

        String message = ResourceBundleManager.instance().getText(key, getLocale(),
                params.toArray());
        if (var != null) {
            pageContext.setAttribute(var, message, scope);
        } else {
            try {
                pageContext.getOut().print(message);
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString(), ioe);
            }
        }

        return EVAL_PAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int doStartTag() throws JspException {
        params.clear();
        return super.doStartTag();
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
     * {@inheritDoc}
     */
    @Override
    public void release() {
        var = null;
        scope = PageContext.PAGE_SCOPE;
        super.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVar(String var) {
        this.var = var;
    }
}
