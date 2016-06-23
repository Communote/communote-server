package com.communote.server.web.fe.portal.user.client.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.communote.server.model.security.ChannelType;


/**
 * Represents all defined channels for an ip range filter
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class IpRangeFilterChannelType {

    private String channelType;
    private boolean enabled;

    /**
     * Constructor
     */
    public IpRangeFilterChannelType() {
        // Do nothing.
    }

    /**
     * Constructor
     * 
     * @param channelType
     *            Channel type
     * @param enabled
     *            <code>true</code> if the current channel type enabled
     */
    public IpRangeFilterChannelType(String channelType, boolean enabled) {
        super();
        this.channelType = channelType;
        this.enabled = enabled;
    }

    /**
     * Return channel type
     * 
     * @return Channel type as string
     */
    public String getChannelType() {
        return channelType;
    }

    /**
     * Set channel type for filter
     * 
     * @param channelType
     *            Channel type
     */
    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    /**
     * Return whether the given filter channel type is enabled
     * 
     * @return <code>true</code> if the filter is enabled
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * Set <code>true</code> if the fitler channel is enabled
     * 
     * @param enabled
     *            Enable or disable filter channel type
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Convert the given channel types for a filter to list of IpRangeFilterFormItemChannelType.
     * 
     * @param types
     *            This parameter can be null.
     * @return List of IpRangeFilterFormItemChannelType. This list contains all available channel
     *         types.
     */
    public static List<IpRangeFilterChannelType> convertToList(ChannelType[] types) {
        List<IpRangeFilterChannelType> result = new ArrayList<IpRangeFilterChannelType>();
        List<ChannelType> typeList = Arrays.asList(types != null ? types : new ChannelType[] {});
        for (String channelType : ChannelType.names()) {
            IpRangeFilterChannelType type = new IpRangeFilterChannelType();
            type.setChannelType(channelType);
            type.setEnabled(typeList.contains(ChannelType.fromString(channelType)));
            result.add(type);
        }
        return result;
    }
}
