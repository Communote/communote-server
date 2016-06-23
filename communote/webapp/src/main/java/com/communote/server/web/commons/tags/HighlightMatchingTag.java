package com.communote.server.web.commons.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A Tag which highlights all substrings inside the value attribute that match
 * some pattern. The highlighting is achieved by applying a CSS class.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class HighlightMatchingTag extends TagSupport {

    /** serialization id. */
    private static final long serialVersionUID = 1L;

    /** The pattern to match. */
    private String pattern;

    /** Name of the cssClass to highlight matching parts. */
    private String cssClass;

    /** The value that should be highlighted */
    private String value;

    /**
     * {@inheritDoc}
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            if (pattern.length() == 0) {
                pageContext.getOut().print(value);
            } else {
                String newBody = value.replace(pattern, "<span class=\"" + cssClass + "\">"
                        + pattern + "</span>");
                pageContext.getOut().print(newBody);
            }
            return EVAL_PAGE;
        } catch (IOException e) {
            throw new JspTagException("IO Error: " + e.getMessage());
        } catch (Exception e) {
            throw new JspTagException("Unknown error: " + e.getMessage());
        }
    }

    /**
     * @param cssClass
     *            the cssClass to set
     */
    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    /**
     * @param pattern
     *            the pattern to set
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
