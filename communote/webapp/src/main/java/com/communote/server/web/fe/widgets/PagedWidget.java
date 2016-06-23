package com.communote.server.web.fe.widgets;

import java.util.Map;

import com.communote.common.paging.PageInformation;
import com.communote.server.widgets.Widget;

/**
 * Interface for a paged widget
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <I>
 *            type of the resulting class
 */
public interface PagedWidget<I> extends Widget {

    /**
     * request parameter identifying a message key suffix (appended to 'widget.paging.tooltip.')
     * which used to generate a tooltip message for the paging navigation. In case the parameter is
     * not found in the request the message key is created by evaluating the return value of
     * {@link #getDefaultPagingMessageKeySuffix()}.
     */
    public static final String PARAM_PAGING_MESSAGE_KEY_SUFFIX = "pagingMessageKeySuffix";

    /**
     * Prefix for the paging tooltip message key (widget.paging.tooltip.).
     */
    public static final String PAGING_MASSAGE_KEY_PREFIX = "widget.paging.tooltip.";

    /**
     * Returns the default message key suffix for creating a tooltip to the paging navigation. This
     * value is evaluated if the {@link #PARAM_PAGING_MESSAGE_KEY_SUFFIX} is not in the request.
     * 
     * @return the message key suffix
     */
    String getDefaultPagingMessageKeySuffix();

    /**
     * Get the page information of the widget
     * 
     * @return the page information
     */
    public PageInformation getPageInformation();

    /**
     * Get the parameter descriptions: This is a map of the parameter names to a human understanable
     * value, e.g. the full name of the user or the name of a user group. Only the parameter names
     * are set which have been used in the filter. The name can be determined by
     * {@link #getSuppliedParameters()}
     * 
     * @return the parameter descriptions
     */
    public Map<String, String> getParameterDescriptions();
}
