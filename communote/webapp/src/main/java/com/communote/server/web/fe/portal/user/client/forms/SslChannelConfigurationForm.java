package com.communote.server.web.fe.portal.user.client.forms;

import java.util.List;

import com.communote.server.model.security.ChannelConfiguration;
import com.communote.server.model.security.ChannelType;


/**
 * The Class SslChannelConfigurationForm handles the ssl settings of the client atministration
 * section.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SslChannelConfigurationForm {

    private Boolean web = Boolean.FALSE;
    private Boolean api = Boolean.FALSE;
    private Boolean rss = Boolean.FALSE;

    /**
     * Instantiates a new ssl channel configuration form.
     * 
     * @param channels
     *            The list of channels.
     */
    public SslChannelConfigurationForm(List<ChannelConfiguration> channels) {
        if (channels != null) {
            for (ChannelConfiguration channelConfiguration : channels) {
                if (ChannelType.WEB.equals(channelConfiguration.getChannelType())) {
                    setWeb(channelConfiguration.getForceSsl());
                } else if (ChannelType.API.equals(channelConfiguration.getChannelType())) {
                    setApi(channelConfiguration.getForceSsl());
                } else if (ChannelType.RSS.equals(channelConfiguration.getChannelType())) {
                    setRss(channelConfiguration.getForceSsl());
                }
            }
        }
    }

    /**
     * @return the api
     */
    public Boolean getApi() {
        return api;
    }

    /**
     * @return the rss
     */
    public Boolean getRss() {
        return rss;
    }

    /**
     * @return the web
     */
    public Boolean getWeb() {
        return web;
    }

    /**
     * @param api
     *            the api to set
     */
    public void setApi(Boolean api) {
        this.api = api;
    }

    /**
     * @param rss
     *            the rss to set
     */
    public void setRss(Boolean rss) {
        this.rss = rss;
    }

    /**
     * @param web
     *            the web to set
     */
    public void setWeb(Boolean web) {
        this.web = web;
    }

}
