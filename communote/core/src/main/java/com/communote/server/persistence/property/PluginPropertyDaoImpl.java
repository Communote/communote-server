package com.communote.server.persistence.property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.communote.server.model.property.PluginProperty;
import com.communote.server.model.property.PluginPropertyConstants;
import com.communote.server.model.property.PropertyConstants;
import com.communote.server.persistence.property.PluginPropertyDaoBase;

/**
 * @see com.communote.server.model.property.PluginProperty
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PluginPropertyDaoImpl extends PluginPropertyDaoBase {

    /**
     * {@inheritDoc}
     */
    @Override
    protected PluginProperty handleFind(String symbolicName, String propertyKey) {
        List<?> result = getHibernateTemplate().find(
                "SELECT pluginProperty FROM " + PluginPropertyConstants.CLASS_NAME
                        + " pluginProperty WHERE  pluginProperty."
                        + PropertyConstants.KEYGROUP + "=? AND pluginProperty."
                        + PropertyConstants.PROPERTYKEY + "=?",
                symbolicName, propertyKey);
        return (PluginProperty) (result.size() > 0 ? result.get(0) : null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> handleGetAllProperties(String symbolicName,
            boolean applicationProperty) {
        List<?> results = getHibernateTemplate().find(
                "SELECT pluginProperty FROM " + PluginPropertyConstants.CLASS_NAME
                        + " pluginProperty WHERE  pluginProperty."
                        + PropertyConstants.KEYGROUP + "=? AND pluginProperty."
                        + PluginPropertyConstants.APPLICATIONPROPERTY + "=?",
                symbolicName, applicationProperty);
        Map<String, String> resultMap = new HashMap<String, String>();
        if (results.size() > 0) {
            for (Object propertyAsObject : results) {
                PluginProperty property = (PluginProperty) propertyAsObject;
                resultMap.put(property.getPropertyKey(), property.getPropertyValue());
            }
        }
        return resultMap;
    }
}