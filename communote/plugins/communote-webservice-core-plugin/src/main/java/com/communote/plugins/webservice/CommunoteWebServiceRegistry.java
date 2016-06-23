package com.communote.plugins.webservice;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface CommunoteWebServiceRegistry {

    public void registerService(
            CommunoteWebServiceDefinition communoteWebserviceDefinition);

    public void unregisterService(CommunoteWebServiceDefinition definition);

    public void unregisterService(String pluginName, String urlPattern);

}