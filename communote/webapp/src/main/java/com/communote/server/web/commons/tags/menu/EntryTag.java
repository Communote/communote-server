package com.communote.server.web.commons.tags.menu;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.communote.server.web.commons.MessageHelper;


/**
 * Simple entry within a panel.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EntryTag extends TagSupport {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private String key;

    @Override
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().write("\" > ");
            pageContext.getOut().write(
                    MessageHelper.getText((HttpServletRequest) pageContext.getRequest(), key));
            pageContext.getOut().write("</a>");
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {
        Object selectedItem = pageContext.getAttribute("selected_item");
        try {
            pageContext.getOut().write("<a class=\"link");
            if (selectedItem != null && name.equals(selectedItem)) {
                pageContext.getOut().write(" active");
            }
            pageContext.getOut().write("\" href=\"");
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
