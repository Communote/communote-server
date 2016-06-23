package com.communote.server.core.common.caching.eh;

import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.extension.CacheExtension;
import net.sf.ehcache.extension.CacheExtensionFactory;

import org.apache.commons.lang.StringUtils;

/**
 * Factory to create cache extensions which can switch a cache into a distributed cache that works
 * with a specific replication scheme. The properties passed to the create method should contain a
 * property named <i>scheme</i> that denotes the scheme to be used for replication and can contain a
 * property named <i>settings</i> that holds semicolon separated key-value pairs of settings to be
 * passed to the extension.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class DistributionSupportCacheExtensionFactory extends CacheExtensionFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheExtension createCacheExtension(Ehcache cache, Properties properties) {
        String replicationScheme = properties.getProperty("scheme");
        if (StringUtils.isBlank(replicationScheme)) {
            throw new CacheException("No scheme specified for distribution");
        }
        if (!replicationScheme.equals("RMI")) {
            throw new CacheException("The provided scheme " + replicationScheme
                    + " is not supported, only RMI can be used");
        }
        return new DistributionSupportCacheExtension(cache, extractSettings(properties));
    }

    /**
     * Extracts the settings that should be passed to the cache extension.
     * 
     * @param properties
     *            the properties to search for the settings property
     * @return the extracted settings as properties
     */
    private Properties extractSettings(Properties properties) {
        Properties settings = new Properties();
        String settingsString = properties.getProperty("settings");
        if (StringUtils.isNotBlank(settingsString)) {
            // split entries by semicolon
            String[] entries = settingsString.split(";");
            for (String entry : entries) {
                String[] keyValuePair = entry.split("=");
                if (keyValuePair.length == 2) {
                    String key = keyValuePair[0].trim();
                    if (key.length() > 0) {
                        settings.setProperty(key, keyValuePair[1].trim());
                    }
                }
            }
        }
        return settings;
    }

}
