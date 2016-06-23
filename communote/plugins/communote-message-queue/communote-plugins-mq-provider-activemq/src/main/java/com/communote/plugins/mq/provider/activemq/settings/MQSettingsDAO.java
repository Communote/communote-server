package com.communote.plugins.mq.provider.activemq.settings;

import java.util.Set;

import com.communote.server.api.core.security.AuthorizationException;

/**
 * Interface for accessing the configuration of the broker.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface MQSettingsDAO {

    /**
     * Use this this method to list all urls that the broker should be started on, hence the vm url
     * will not be included, since it will be available always.
     * 
     * @return broker connector urls
     */
    public Set<String> getBrokerConnectorURLs();

    /**
     * Use this method to list all urls that can be used to connect <b>to</b> the broker
     * 
     * @return broker connector urls including the VM url (if {@link #isEnableVM()} is true)
     */
    public Set<String> getBrokerConnectorURLsWithVM();

    /**
     * @return broker name
     */
    public String getBrokerName();

    /**
     * @return data directory path
     */
    public String getDataDirectory();

    /**
     * @return the JMX domain name for the MQ broker
     */
    public String getJmxDomainName();

    /**
     * 
     * @return the port to be used for jmx connection
     */
    public int getJmxPort();

    /**
     * @return the name of the communote message queue
     */
    public String getQueueName();

    /**
     * @return the name of queue to reply messages to
     */
    public String getReplyQueueNamePrefix();

    /**
     * 
     * @return the ssl port to use
     */
    public String getSSLPort();

    /**
     * 
     * @return the tcp port to use
     */
    public String getTcpPort();

    /**
     * 
     * @return the hostname of the url to use
     */
    public String getUrlHostname();

    /**
     * 
     * @return the vm url for clients to connect to
     */
    public String getVMConnectorURL();

    /**
     * @return broker's state
     */
    public boolean isBrokerStarted();

    /**
     * @return true if SSL is enabled. default ist false
     */
    public boolean isEnableSSL();

    /**
     * 
     * @return true to enable tcp connector. default ist false.
     */
    boolean isEnableTCP();

    /**
     * This flag is only relevant for the adapter connecting to the broker, since the VM connection
     * is always available once the broker is started. It can be used to tell the adaper, not to use
     * the vm and connect to another host.
     * 
     * @return true to enable vm connection. default ist true.
     */
    boolean isEnableVM();

    /**
     * @return the forceSSL. default ist false.
     */
    public boolean isForceSSL();

    /**
     * 
     * @return true if a client certificate is required (if it is a SSL connection, will be ignored
     *         for non SSL connections)
     */
    public boolean isForceSSLClientAuthentication();

    /**
     * 
     * @return true to enable the jmx monitoring
     */
    public boolean isJmxMonitoringEnabled();

    /**
     * 
     * @return true if jmx is enabled for remote connections and should be started on the
     *         {@link #getJmxPort()} port
     */
    public boolean isJmxRemoteEnabled();

    /**
     * sets broker's state flag
     * 
     * @param isStarted
     *            the flag value
     */
    public void setBrokerStarted(boolean isStarted) throws AuthorizationException;

    /**
     * sets data directory path
     * 
     * @param dataDirectoryPath
     *            path to set
     */
    public void setDataDirectory(String dataDirectoryPath) throws AuthorizationException;

    /**
     * @param enableJmx
     *            the enableJmx to set
     */
    public void setEnableJmxRemote(boolean enableJmx) throws AuthorizationException;

    /**
     * @param enableSSL
     *            true to enable the ssl connector
     */
    public void setEnableSSL(boolean enableSSL) throws AuthorizationException;

    /**
     * @param enableTCP
     *            true to enable tcp
     */
    public void setEnableTCP(boolean enableTCP) throws AuthorizationException;

    /**
     * VM = virtual machine connector
     * 
     * @param enableVM
     *            true to enable vm (which actually only relevant for the embedded adapter)
     */
    public void setEnableVM(boolean enableVM) throws AuthorizationException;

    /**
     * @param forceSSL
     *            the forceSSL to set
     */
    public void setForceSSL(boolean forceSSL) throws AuthorizationException;

    /**
     * 
     * @param needClientAuth
     *            true if a client auth by certificate is expected on connecting to the broker
     */
    public void setForceSSLClientAuthentication(boolean needClientAuth) throws AuthorizationException;

}