package com.communote.server.core.common.caching.eh;

import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;

import com.communote.server.core.common.caching.CacheManagerInitializationException;

/**
 * Provides the standard ehCache configuration.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class DefaultEhCacheConfigurer implements EhCacheConfigurer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration(Properties properties) throws CacheException {
        Configuration config = ConfigurationFactory.parseConfiguration(resolveEhcacheConfigFile());
        postProcessConfiguration(config, properties);
        return config;
    }

    /**
     * Returns the location of the default Ehcache configuration XML file as a URL. The URL could
     * point to a class path resource like '/com/acme/cache/ehcache.xml' or a resource on the file
     * system. Subclasses should override this method to provide another configuration file.
     *
     * @return the location of the ehCache configuration file
     */
    protected URL getEhCacheConfigFile() {
        return this.getClass().getResource("/com/communote/server/core/cache/ehcache/ehcache.xml");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMainCacheName() {
        return "communoteLocalCache";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getProtectedCacheNames() {
        return Collections.emptyList();
    }

    /**
     * Helper method that overrides the factory configuration property string with other settings.
     *
     * @param factoryConfig
     *            the cache manager factory configuration
     * @param propertyMap
     *            a map with properties to override or add
     */
    protected void overrideFactoryConfigProperties(FactoryConfiguration factoryConfig,
            Map<String, String> propertyMap) {
        String props = factoryConfig.getProperties();
        StringBuilder changedProps = new StringBuilder();
        String[] splitted = props.split(factoryConfig.getPropertySeparator());
        for (int i = 0; i < splitted.length; i++) {
            boolean added = false;
            Iterator<String> it = propertyMap.keySet().iterator();
            while (it.hasNext()) {
                String propKey = it.next();
                if (splitted[i].contains(propKey)) {
                    changedProps.append(propKey);
                    changedProps.append("=");
                    changedProps.append(propertyMap.get(propKey));
                    added = true;
                    it.remove();
                }
            }
            if (!added) {
                changedProps.append(splitted[i]);
            }
            changedProps.append(factoryConfig.getPropertySeparator());
        }

        // add remaining pairs
        for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
            changedProps.append(entry.getKey());
            changedProps.append("=");
            changedProps.append(entry.getValue());
            changedProps.append(factoryConfig.getPropertySeparator());
        }
        String changedPropsString = changedProps.toString();
        if (changedPropsString.endsWith(factoryConfig.getPropertySeparator())) {
            factoryConfig.setProperties(changedPropsString.substring(0,
                    changedPropsString.length() - 1));
        } else {
            factoryConfig.setProperties(changedPropsString);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postConfiguration(CacheManager cacheManager) {
        // nothing
    }

    /**
     * Called by the {@link #getConfiguration(Properties)} method to allow additional processing of
     * the loaded configuration.
     *
     * @param config
     *            the loaded configuration to process
     * @param properties
     *            additional configuration properties
     */
    protected void postProcessConfiguration(Configuration config, Properties properties) {
        // nothing
    }

    /**
     * Returns a URL pointing to the Ehcache configuration file.
     *
     * @return the URL to the file
     * @throws CacheManagerInitializationException
     *             if the {@link #ehcacheConfigFile} property is not set or the specified file does
     *             not exist
     */
    private URL resolveEhcacheConfigFile() throws CacheManagerInitializationException {
        URL confFileURL = getEhCacheConfigFile();
        if (confFileURL == null) {
            throw new CacheManagerInitializationException("The path to the Ehcache "
                    + "configuration file is not defined.");
        }
        return confFileURL;
    }

}
