package com.communote.server.core.common.ldap.caching;

import java.io.Serializable;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapServer implements Serializable {
    private static final long serialVersionUID = 6187393438266140906L;
    private int weight;
    private String url;

    /**
     * Constructor. Needed for serialization.
     */
    public LdapServer() {
        // Do nothing.
    }

    /**
     * Constructor.
     * 
     * @param url
     *            The url of the server.
     * @param weight
     *            The weight of the server.
     */
    public LdapServer(String url, int weight) {
        this.setUrl(url);
        this.setWeight(weight);
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @param weight
     *            the weight to set
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }
}
