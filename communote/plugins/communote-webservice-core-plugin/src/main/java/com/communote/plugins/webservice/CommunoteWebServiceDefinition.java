package com.communote.plugins.webservice;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteWebServiceDefinition {
    /**
     * For example "http://schemas.microsoft.com/sharepoint/remoteapp/"
     */
    private String nameSpaceUri;
    /**
     * For example MyRemoteEventService.class
     */
    private Class<?> serviceClass;
    /**
     * For example "MyRemoteEventService"
     */
    private String localPartName;
    /**
     * For example "RemoteEventReceiver"
     */
    private String endpointName;

    /**
     * For example com.communote.plugin.myplugin
     */
    private String pluginName;
    /**
     * for example "/Services/RemoteEventReceiver.svc"
     */
    private String relativeUrlPattern;

    public String getEndpointName() {
        return endpointName;
    }

    public String getLocalPartName() {
        return localPartName;
    }

    public String getNameSpaceUri() {
        return nameSpaceUri;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getRelativeUrlPattern() {
        return relativeUrlPattern;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public void setLocalPartName(String localPartName) {
        this.localPartName = localPartName;
    }

    /**
     * @param nameSpaceUri
     */
    public void setNameSpaceUri(String nameSpaceUri) {
        this.nameSpaceUri = nameSpaceUri;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public void setRelativeUrlPattern(String relativeUrlPattern) {
        this.relativeUrlPattern = relativeUrlPattern;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    @Override
    public String toString() {
        return "CommunoteWebServiceDefinition [nameSpaceUri=" + nameSpaceUri + ", serviceClass="
                + serviceClass + ", localPartName=" + localPartName + ", endpointName="
                + endpointName + ", pluginName=" + pluginName + ", relativeUrlPattern="
                + relativeUrlPattern + "]";
    }
}