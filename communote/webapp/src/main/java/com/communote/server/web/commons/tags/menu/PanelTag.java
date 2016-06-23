package com.communote.server.web.commons.tags.menu;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;

import com.communote.server.web.commons.MessageHelper;


/**
 * Simple panel for the administration area.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PanelTag extends BodyTagSupport {

    private static final long serialVersionUID = 1L;
    private String name;
    private String key;
    private String isLast;
    private boolean subPanel;

    @Override
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().write("</div></div>");
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {
        StringBuilder output = new StringBuilder();
        Object selectedMenu = pageContext.getAttribute("selected_menu");
        boolean menuIsSelected = selectedMenu != null && name.equals(selectedMenu);
        output.append("<div id=\"" + name.replace(".", "-") + "\"");
        if (menuIsSelected) {
            output.append(" class=\"display\"");
        }
        output.append(">");
        if (isSubPanel()) {
            output.append("<div class=\"sub-panel\" onclick=\"toggleSlide('"
                    + name.replace(".", "-") + "');\">");
        } else {
            output.append("<div class=\"panel\">");
        }
        output.append("<a ");
        if (menuIsSelected) {
            output.append("class=\"panel-icon-opened\"");
        } else {
            output.append("class=\"panel-icon-closed\"");
        }
        output.append("href=\"javascript:;\" title=\"");
        output.append(MessageHelper.getText((HttpServletRequest) pageContext.getRequest(),
                "portal.panel.toggle"));
        output.append("\"><!-- ie --></a><h4>");
        output.append(MessageHelper.getText((HttpServletRequest) pageContext.getRequest(), key));
        output.append("</h4><span class=\"clear\"><!-- ie --></span>");
        output.append("</div>");

        if (isSubPanel()) {
            output.append("<div class=\"sub-wrapper ");
        } else {
            output.append("<div class=\"wrapper ");
        }

        if (StringUtils.isNotBlank(isLast)) {
            output.append(" last");
        }
        output.append("\"");
        if (!menuIsSelected) {
            output.append(" style=\"display: none;\"");
        }
        output.append(">");

        try {
            pageContext.getOut().write(output.toString());
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @return the isLast
     */
    public String getIsLast() {
        return isLast;
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
     * @return the subPanel
     */
    public boolean isSubPanel() {
        return subPanel;
    }

    /**
     * @param isLast
     *            the isLast to set
     */
    public void setIsLast(String isLast) {
        this.isLast = isLast;
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

    /**
     * @param subPanel
     *            the subPanel to set
     */
    public void setSubPanel(boolean subPanel) {
        this.subPanel = subPanel;
    }
}
