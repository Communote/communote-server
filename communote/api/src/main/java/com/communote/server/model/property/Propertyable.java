package com.communote.server.model.property;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface Propertyable {

    /**
     * <p>
     * Properties
     * </p>
     */
    public java.util.Set<? extends com.communote.server.model.property.StringProperty> getProperties();

}