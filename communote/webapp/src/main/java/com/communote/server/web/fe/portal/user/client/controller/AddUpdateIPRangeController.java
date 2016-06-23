package com.communote.server.web.fe.portal.user.client.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.iprange.CurrentIpNotInRange;
import com.communote.server.core.security.iprange.InvalidIpAddressException;
import com.communote.server.core.security.iprange.InvalidIpRangeException;
import com.communote.server.core.security.iprange.IpRangeFilterManagement;
import com.communote.server.model.security.ChannelType;
import com.communote.server.persistence.security.iprange.IpRangeFilterVO;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.fe.portal.user.client.forms.IpRangeFilterChannelType;
import com.communote.server.web.fe.portal.user.client.forms.IpRangeFilterItem;

/**
 * Adds,Updates and Creation controller for ip range filter
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AddUpdateIPRangeController extends BaseFormController {

    private static final String FILTER_ID = "filter_id";
    /**
     * Defines the update action
     */
    public static final String ACTION_UPDATE = "au";
    /**
     * Defines the delete action
     */
    public static final String ACTION_DELETE = "ad";

    private static final ChannelType WEB_CHANNEL = ChannelType.WEB;

    /**
     * adds or removes channels from a filter
     * 
     * @param request
     *            the request
     * @param item
     *            the form backing object
     * @param currentIP
     *            the IP of the request
     * @throws InvalidIpAddressException
     *             in case the currentIP is not a valid IP
     * @return whether the updates were successful
     */
    private boolean changeChannelEnabledStatus(HttpServletRequest request,
            IpRangeFilterItem item, String currentIP) throws InvalidIpAddressException {
        for (IpRangeFilterChannelType channelType : item.getChannelTypes()) {
            try {
                ServiceLocator
                        .findService(IpRangeFilterManagement.class).setFilterChannelEnabled(
                                item.getId(), ChannelType
                                        .fromString(channelType.getChannelType()),
                                channelType.getEnabled(),
                                currentIP, WEB_CHANNEL);
            } catch (CurrentIpNotInRange e) {
                String msgKey;
                if (channelType.getEnabled()) {
                    msgKey = "client.iprange.update.failed.ip.blocked.channel.enable";
                } else {
                    msgKey = "client.iprange.update.failed.ip.blocked.channel.disable";
                }
                MessageHelper.saveErrorMessage(request, MessageHelper.getText(request, msgKey,
                        new Object[] { channelType.getChannelType(), currentIP }));
                return false;
            }
        }
        return true;
    }

    /**
     * disables or enables a filter
     * 
     * @param request
     *            the request
     * @param item
     *            the form backing object
     * @param currentIP
     *            the IP of the request
     * @throws InvalidIpAddressException
     *             in case the currentIP is not a valid IP
     * @return whether the update was successful
     */
    private boolean changeFilterEnabledStatus(HttpServletRequest request,
            IpRangeFilterItem item, String currentIP) throws InvalidIpAddressException {
        try {
            ServiceLocator
                    .findService(IpRangeFilterManagement.class).setFilterEnabled(item.getId(),
                            item.getEnabled(), currentIP,
                            WEB_CHANNEL);
        } catch (CurrentIpNotInRange e) {
            String msgKey;
            if (item.getEnabled()) {
                msgKey = "client.iprange.update.failed.ip.blocked.filter.enable";
            } else {
                msgKey = "client.iprange.update.failed.ip.blocked.filter.disable";
            }
            MessageHelper.saveErrorMessage(request, MessageHelper.getText(request, msgKey,
                    new Object[] { item.getName(), currentIP }));
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        IpRangeFilterItem item = new IpRangeFilterItem();
        String filterIdAsString = request.getParameter(FILTER_ID);
        if (StringUtils.isNumeric(filterIdAsString)) {
            Long filterId = Long.parseLong(filterIdAsString);
            IpRangeFilterVO filterObject = ServiceLocator
                    .findService(IpRangeFilterManagement.class).findFilterById(filterId);
            item.setChannelTypes(IpRangeFilterChannelType.convertToList(filterObject
                    .getChannels()));
            item.setEnabled(filterObject.isEnabled());
            item.setExcludes(filterObject.getExcludes());
            item.setIncludes(filterObject.getIncludes());
            item.setId(filterObject.getId());
            item.setName(filterObject.getName());
        } else {
            item.setChannelTypes(IpRangeFilterChannelType.convertToList(null));
        }
        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        IpRangeFilterItem item = (IpRangeFilterItem) command;
        String currentIP = request.getRemoteAddr();
        ModelAndView mav = null;
        if (item.getAction().equals(ACTION_UPDATE)) {
            // TODO use separate requests for update, enable/disable and channel selection OR do it
            // in one transaction
            boolean success = true;
            if (item.getId() == null) {
                IpRangeFilterVO filterVO = ServiceLocator
                        .findService(IpRangeFilterManagement.class).createFilter(item.getName(),
                                item.getIncludes(), item.getExcludes());
                item.setId(filterVO.getId());
            } else {
                success = updateFilter(request, item, currentIP);
            }
            if (success) {
                success = changeFilterEnabledStatus(request, item, currentIP);
            }
            if (success) {
                success = changeChannelEnabledStatus(request, item, currentIP);
            }
            if (success) {
                MessageHelper.saveMessage(request, MessageHelper.getText(request,
                        "client.iprange.update.success"));
            }
            mav = showForm(request, errors, getFormView());
        } else if (item.getAction().equals(ACTION_DELETE)) {
            mav = removeFilter(request, item, errors, currentIP);
        }
        return mav;
    }

    /**
     * Handles the filter remove request.
     * 
     * @param request
     *            the servlet request
     * @param item
     *            the form backing object
     * @param errors
     *            for binding errors
     * @param currentIP
     *            the IP of the request
     * @return the new model and view or null on exception
     * @throws InvalidIpAddressException
     *             in case the currentIP is not a valid IP
     * @throws Exception
     *             in case of invalid state or arguments when building the MAV
     */
    private ModelAndView removeFilter(HttpServletRequest request, IpRangeFilterItem item,
            BindException errors, String currentIP) throws InvalidIpAddressException, Exception {
        ModelAndView mav;
        try {
            ServiceLocator
                    .findService(IpRangeFilterManagement.class).removeFilter(item.getId(),
                            currentIP,
                            WEB_CHANNEL);
        } catch (CurrentIpNotInRange e) {
            MessageHelper.saveErrorMessage(request, MessageHelper.getText(request,
                    "client.iprange.update.failed.ip.blocked.filter.remove", new Object[] {
                            item.getName(), currentIP }));
            return showForm(request, errors, getFormView());
        }
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("filters", ServiceLocator
                .findService(IpRangeFilterManagement.class).listFilter());
        MessageHelper.saveMessage(request, MessageHelper.getText(request,
                "client.iprange.delete.success"));
        mav = new ModelAndView(ControllerHelper
                .replaceModuleInViewName("main.MODULE.client.ip.range"), attr);
        return mav;
    }

    /**
     * Tries to update the includes and excludes of a given filter.
     * 
     * @param request
     *            the servlet request
     * @param item
     *            the form backing object
     * @param currentIP
     *            the IP of the request
     * @return whether the update was successful
     * @throws InvalidIpAddressException
     *             in case the currentIP is not a valid IP (already checked by validator)
     * @throws InvalidIpRangeException
     *             in case the specified IP range is not valid (already checked by validator)
     */
    private boolean updateFilter(HttpServletRequest request, IpRangeFilterItem item,
            String currentIP) throws InvalidIpAddressException, InvalidIpRangeException {
        try {
            ServiceLocator
                    .findService(IpRangeFilterManagement.class).updateFilter(item.getId(),
                            item.getName(), item
                                    .getIncludes(), item.getExcludes(), currentIP, WEB_CHANNEL);
        } catch (CurrentIpNotInRange e) {
            MessageHelper.saveErrorMessage(request, MessageHelper.getText(request,
                    "client.iprange.update.failed.ip.blocked.filter.update", new Object[] {
                            item.getName(), currentIP }));
            return false;
        }
        return true;
    }

}
