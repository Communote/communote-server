package com.communote.server.widgets.jsf;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.server.widgets.SimpleWidgetController;

/**
 * This class represents a widget controller that can be used with MyFaces. The {
 * {@link #getWidget()} method use the FacesContext to determine the request<br>
 * <br>
 * Note: The attribute {@link #getUseRequestParametersForWidget()} is set to true by default since
 * the JSF implementation is not supporting the url mapping
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MyFacesWidgetController extends SimpleWidgetController {

    /**
     * Default constructor setting {@link #setUseRequestParametersForWidget(boolean)} to true
     */
    public MyFacesWidgetController() {
        this.setUseRequestParametersForWidget(true);
    }

    /**
     * Uses the FacesContext to determine the instance
     * 
     * @return the path to the jsp of the widget (as returned by {@link Widget#getViewIdentifier())
     */
    public String getWidget() {

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();

        return handleWidgetRequest(request, response);
    }

}
