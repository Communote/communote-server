package com.communote.server.model.security;

/**
 * @see com.communote.server.model.security.IpRangeChannel
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeChannelImpl extends com.communote.server.model.security.IpRangeChannel {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 1655064499163494050L;
    private ChannelType channel;

    /**
     * Gets the channel.
     * 
     * @return the channel
     * @see com.communote.server.model.security.IpRangeChannel#getChannel()
     */
    @Override
    public com.communote.server.model.security.ChannelType getChannel() {
        return channel;
    }

    /**
     * Sets the channel.
     * 
     * @param channel
     *            the channel
     * @see com.communote.server.model.security.IpRangeChannel#setChannel(com.communote.server.model.security.ChannelType)
     */
    @Override
    public void setChannel(com.communote.server.model.security.ChannelType channel) {
        this.channel = channel;
        if (channel == null) {
            super.setType(null);
        } else {
            super.setType(channel.getValue());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.model.security.IpRangeChannel#setType(String)
     */
    @Override
    public void setType(String type) {
        super.setType(type);
        if (type == null) {
            this.channel = null;
        } else {
            this.channel = ChannelType.fromString(type);
        }
    }

}
