package com.communote.server.web.fe.widgets.management.security.iprange;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.general.RunInTransactionWithResult;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.security.iprange.CurrentIpNotInRange;
import com.communote.server.core.security.iprange.InvalidIpAddressException;
import com.communote.server.core.security.iprange.InvalidIpRangeException;
import com.communote.server.core.security.iprange.IpRangeFilterManagement;
import com.communote.server.core.security.iprange.IpRangeHelper;
import com.communote.server.model.security.ChannelType;
import com.communote.server.persistence.security.iprange.IpRangeFilterVO;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.portal.user.client.controller.AddUpdateIPRangeController;
import com.communote.server.web.fe.portal.user.client.forms.IpRangeFilterChannelType;
import com.communote.server.web.fe.portal.user.client.forms.IpRangeFilterItem;
import com.communote.server.widgets.AbstractWidget;

/**
 * This widget adds or updates an ip range filter.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeWidget extends AbstractWidget {

    /** filter name. */
    public static final String FILTER_NAME = "name";
    /** filter enabled. */
    public static final String FILTER_ENABLED = "enabled";
    /** filter id. */
    public static final String FILTER_ID = "filterId";
    /** filter excludes. */
    public static final String FILTER_EXCLUDES = "excludes";

    /** filter includes. */
    public static final String FILTER_INCLUDES = "includes";

    /** channel type prefix. */
    public static final String FILTER_CHANNEL_TYPE_PREFIX = "channelType_";

    /** Logger. */
    private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger
            .getLogger(IpRangeWidget.class);

    /**
     * Defines the update action
     */
    public static final String ACTION_UPDATE = "updateFilter";
    /**
     * Defines the delete action
     */
    public static final String ACTION_DELETE = "deleteFilter";

    private final IpRangeFilterManagement ipRangeFilterManagement = ServiceLocator.instance()
            .getService(IpRangeFilterManagement.class);

    /**
     * Adds the new filter.
     * 
     * @param item
     *            The filter.
     * @return True if successful.
     */
    private boolean addFilter(final IpRangeFilterItem item) {
        SecurityHelper.assertCurrentUserIsClientManager();
        RunInTransactionWithResult<Boolean> transactional = new RunInTransactionWithResult<Boolean>() {
            @Override
            public Boolean execute() throws TransactionException {
                try {
                    IpRangeFilterVO filterVo = ipRangeFilterManagement.createFilter(
                            item.getName(), item.getIncludes(), item.getExcludes());
                    ipRangeFilterManagement.setFilterEnabled(
                            filterVo.getId(), item.getEnabled(), getRequest().getRemoteAddr(),
                            ChannelType.WEB);
                    for (IpRangeFilterChannelType channelType : item.getChannelTypes()) {
                        ipRangeFilterManagement
                                .setFilterChannelEnabled(
                                        filterVo.getId(), ChannelType
                                                .fromString(channelType.getChannelType()),
                                        channelType.getEnabled(),
                                        getRequest().getRemoteAddr(), ChannelType.WEB);
                    }
                    item.setId(filterVo.getId());
                    return true;
                } catch (InvalidIpAddressException e) {
                    MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(
                            getRequest(),
                            "client.iprange.ip.validation.error"));
                } catch (InvalidIpRangeException e) {
                    MessageHelper.saveErrorMessageFromKey(getRequest(),
                            "client.iprange.ip.validation.error");
                } catch (CurrentIpNotInRange e) {
                    MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(
                            getRequest(),
                            "client.iprange.update.failed.ip.blocked.filter.enable", new Object[] {
                                    getParameter(FILTER_NAME), getRequest().getRemoteAddr() }));
                }
                return false;
            }
        };
        try {
            return ServiceLocator.findService(TransactionManagement.class).execute(transactional);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    /**
     * This removes the given filter.
     * 
     * @return True if successful.
     */
    private boolean deleteFilter() {
        try {
            IpRangeFilterItem item = getItemFromRequest();
            ipRangeFilterManagement.removeFilter(item.getId(),
                    getRequest().getRemoteAddr(), ChannelType.WEB);
            MessageHelper.saveMessage(getRequest(), MessageHelper.getText(getRequest(),
                    "client.iprange.delete.success"));
            return true;
        } catch (InvalidIpAddressException e) {
            LOG.debug("Was not able to remove an IP range filter.", e);
            MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(getRequest(),
                    "client.iprange.ip.validation.error"));
        } catch (CurrentIpNotInRange e) {
            LOG.debug("Was not able to remove an IP range filter.", e);
            MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(getRequest(),
                    "client.iprange.update.failed.ip.blocked.filter.remove", new Object[] {
                            getParameter(FILTER_NAME), getRequest().getRemoteAddr() }));
        }
        return false;
    }

    /**
     * 
     * @return filter as form object.
     */
    private IpRangeFilterItem getItemFromRequest() {
        IpRangeFilterItem item = new IpRangeFilterItem();
        item.setEnabled(StringUtils.isNotEmpty(getParameter(FILTER_ENABLED)));
        item.setExcludes(getParameter(FILTER_EXCLUDES));
        item.setIncludes(getParameter(FILTER_INCLUDES));
        item.setId(ParameterHelper.getParameterAsLong(getParameters(), FILTER_ID));
        item.setName(getParameter(FILTER_NAME));
        List<IpRangeFilterChannelType> channels = new ArrayList<IpRangeFilterChannelType>();
        for (String channelType : ChannelType.names()) {
            IpRangeFilterChannelType channel = new IpRangeFilterChannelType();
            channel.setChannelType(channelType);
            channel.setEnabled(StringUtils.isNotEmpty(getParameter(
                    FILTER_CHANNEL_TYPE_PREFIX + channelType)));
            channels.add(channel);
        }
        item.setChannelTypes(channels);
        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "widget.client.management.security.iprange." + outputType;
    }

    /**
     * This method dispatches the forms actions.
     * 
     * @param action
     *            The action
     * @return {@link IpRangeFilterItem} or null if no action was invoked.
     */
    private IpRangeFilterItem handleActions(String action) {
        IpRangeFilterItem item = null;
        boolean success = false;
        if (ACTION_UPDATE.equals(action)) {
            item = getItemFromRequest();
            if (validate(item)) {
                if (item.getId() != null) {
                    success = updateFilter(item);
                } else {
                    success = addFilter(item);
                    if (success) {
                        getRequest().setAttribute("openFilterId", item.getId());
                        getRequest().setAttribute("openFilterName", item.getName());
                        item = new IpRangeFilterItem();
                    }
                }
            }
            if (success) {
                MessageHelper.saveMessage(getRequest(), MessageHelper.getText(getRequest(),
                        "client.iprange.update.success"));
            }
        } else if (ACTION_DELETE.equals(action)) {
            deleteFilter();
        }
        return item;
    }

    /**
     * Returns the form.
     * 
     * @return {@link IpRangeFilterItem}.
     */
    @Override
    public Object handleRequest() {
        SecurityHelper.assertCurrentUserIsClientManager();
        getRequest().setAttribute("alwaysOpen", getParameter("alwaysOpen"));
        String action = getParameter("action");
        IpRangeFilterItem item = handleActions(action);
        String filterIdAsString = getParameter(FILTER_ID);
        if (item != null) {
            item.setChannelTypes(IpRangeFilterChannelType.convertToList(null));
            return item;
        }
        item = new IpRangeFilterItem();
        if (!ACTION_DELETE.equals(action) && StringUtils.isNotEmpty(filterIdAsString)
                && StringUtils.isNumeric(filterIdAsString)) {
            Long filterId = Long.parseLong(filterIdAsString);
            IpRangeFilterVO filterObject = ipRangeFilterManagement.findFilterById(filterId);
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

    @Override
    protected void initParameters() {
        // Do nothing.
    }

    /**
     * Updates the filter.
     * 
     * @param item
     *            item to update.
     * @return True if successful.
     */
    private boolean updateFilter(final IpRangeFilterItem item) {
        RunInTransactionWithResult<Boolean> transactional = new RunInTransactionWithResult<Boolean>() {
            @Override
            public Boolean execute() throws TransactionException {
                try {
                    ipRangeFilterManagement.updateFilter(
                            item.getId(),
                            item.getName(), item.getIncludes(), item.getExcludes(),
                            getRequest().getRemoteAddr(), ChannelType.WEB);
                    ipRangeFilterManagement.setFilterEnabled(
                            item.getId(), item.getEnabled(), getRequest().getRemoteAddr(),
                            ChannelType.WEB);
                    for (IpRangeFilterChannelType channelType : item.getChannelTypes()) {
                        ipRangeFilterManagement
                                .setFilterChannelEnabled(
                                        item.getId(), ChannelType
                                                .fromString(channelType.getChannelType()),
                                        channelType.getEnabled(),
                                        getRequest().getRemoteAddr(), ChannelType.WEB);
                    }
                    return true;
                } catch (InvalidIpAddressException e) {
                    MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(
                            getRequest(),
                            "client.iprange.ip.validation.error"));
                } catch (InvalidIpRangeException e) {
                    MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(
                            getRequest(),
                            "client.iprange.ip.validation.error"));
                } catch (CurrentIpNotInRange e) {
                    MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(
                            getRequest(),
                            "client.iprange.update.failed.ip.blocked.filter.enable", new Object[] {
                                    getParameter(FILTER_NAME), getRequest().getRemoteAddr() }));
                }
                return false;
            }
        };
        try {
            return ServiceLocator.findService(TransactionManagement.class).execute(transactional);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean validate(IpRangeFilterItem item) {
        SecurityHelper.assertCurrentUserIsClientManager();
        boolean validated = true;
        if (StringUtils.isNotEmpty(item.getAction())
                && item.getAction().equals(AddUpdateIPRangeController.ACTION_DELETE)) {
            return true;
        }
        if (StringUtils.isEmpty(item.getName())) {
            MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(getRequest(),
                    "client.iprange.update.error.name.empty"));
            validated = false;
        }
        if (StringUtils.isNotEmpty(item.getExcludes())) {
            try {
                IpRangeHelper.stringToRanges(item.getExcludes(), ",");
            } catch (InvalidIpRangeException e) {
                MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(getRequest(),
                        "client.iprange.ip.validation.error"));
                validated = false;
            } catch (InvalidIpAddressException e) {
                MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(getRequest(),
                        "client.iprange.ip.validation.error"));
                validated = false;
            }
        }
        if (StringUtils.isNotEmpty(item.getIncludes())) {
            try {
                IpRangeHelper.stringToRanges(item.getIncludes(), ",");
            } catch (InvalidIpRangeException e) {
                MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(getRequest(),
                        "client.iprange.ip.validation.error"));
                validated = false;
            } catch (InvalidIpAddressException e) {
                MessageHelper.saveErrorMessage(getRequest(), MessageHelper.getText(getRequest(),
                        "client.iprange.ip.validation.error"));
                validated = false;
            }
        }
        return validated;
    }
}
