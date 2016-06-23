package com.communote.server.external.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.hibernate.EhCache;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.core.common.caching.eh.SingletonEhCacheManagerFactory;

/**
 * <p>
 * Custom EhCacheProvider which ensures that the correct disk store path is used and the cache
 * manager can be shared between Hibernate and Communote.
 * </p>
 * Additionally this provider defines a mapping from an Hibernate cache region to the actual
 * EhCache. This mapping was included to circumvent some shortcomings of the default behavior which
 * would map each cache region to a distinctive EhCache. Especially in clustered environments this
 * is not desired since EhCache creates additional threads for each cache to manage change
 * distributions.<br>
 * We could have used other names for the Hibernate cache regions (e.g. "UserGroupCache" instead of
 * qualified class name) in the hbm.xml files (after extending AndroMDA to support it) but this has
 * a disadvantage. For each cache region Hibernate creates one cache object representing it. This
 * object is an instance of a class implementing the CacheConcurrencyStrategy interface. One of the
 * implementations is the ReadWriteCache which uses method based synchronization to support an
 * isolation level close to repeatable read. When grouping a lot of entity and collection caches in
 * a region that uses the ReadWriteCache strategy we might get a negative impact on the performance
 * since the get method to retrieve a cached item is synchronized too. By incorporating our own
 * mapping we are reducing the frequency of concurrent gets on the same Hibernate cache because
 * entities and collection got their own region.
 * <p>
 * Parts of the implementation are taken from net.sf.ehcache.hibernate.SingletonEhCacheProvider
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SingletonEhCacheProvider implements CacheProvider {

    private static final String HIBERNATE_EHCACHE_CONFIG = "/com/communote/server/core/cache/ehcache/hibernate2lc_ehcache.xml";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonEhCacheProvider.class);
    private final Map<String, String> region2CacheMapping;

    /**
     * Create a new provider and prepare the region mapping.
     */
    public SingletonEhCacheProvider() {
        region2CacheMapping = new HashMap<String, String>();
        // cache for user and group data
        region2CacheMapping
        .put("com.communote.server.model.user.CommunoteEntity", "UserGroupCache");
        region2CacheMapping.put("com.communote.server.model.user.UserProfile", "UserGroupCache");
        region2CacheMapping.put("com.communote.server.model.user.ContactImpl", "UserGroupCache");
        region2CacheMapping.put("com.communote.server.model.user.UserProperty", "UserGroupCache");
        region2CacheMapping.put("com.communote.server.model.user.ExternalUserAuthenticationImpl",
                "UserGroupCache");
        region2CacheMapping.put("com.communote.server.model.user.CommunoteEntity.tags",
                "UserGroupCache");
        region2CacheMapping.put("com.communote.server.model.user.User.externalAuthentications",
                "UserGroupCache");
        region2CacheMapping
                .put("com.communote.server.model.user.User.properties", "UserGroupCache");
        // cache for blog data
        region2CacheMapping.put("com.communote.server.model.blog.Blog", "BlogCache");
        region2CacheMapping.put("com.communote.server.model.external.ExternalObjectImpl",
                "BlogCache");
        region2CacheMapping.put("com.communote.server.model.blog.BlogProperty", "BlogCache");
        region2CacheMapping
        .put("com.communote.server.model.blog.Blog.externalObjects", "BlogCache");
        region2CacheMapping.put("com.communote.server.model.blog.Blog.tags", "BlogCache");
        region2CacheMapping.put("com.communote.server.model.blog.Blog.properties", "BlogCache");
        // cache for tag data
        region2CacheMapping.put("com.communote.server.model.tag.TagImpl", "TagCache");
        region2CacheMapping.put("com.communote.server.model.tag.TagImpl.names", "TagCache");
        region2CacheMapping.put("com.communote.server.model.tag.TagImpl.descriptions", "TagCache");
        // cache for localization data
        region2CacheMapping
        .put("com.communote.server.model.user.LanguageImpl", "LocalizationCache");
        region2CacheMapping.put("com.communote.server.model.i18n.MessageImpl", "LocalizationCache");
        // cache for note data
        region2CacheMapping.put("com.communote.server.model.note.Note", "NoteCache");
        region2CacheMapping.put("com.communote.server.model.note.Content", "NoteCache");
        region2CacheMapping.put("com.communote.server.model.note.NoteProperty", "NoteCache");
        region2CacheMapping.put("com.communote.server.model.note.Note.tags", "NoteCache");
        region2CacheMapping.put("com.communote.server.model.note.Note.usersToBeNotified",
                "NoteCache");
        region2CacheMapping.put("com.communote.server.model.note.Note.attachments", "NoteCache");
        region2CacheMapping.put("com.communote.server.model.note.Note.properties", "NoteCache");
        // cache for attachment data
        region2CacheMapping.put("com.communote.server.model.attachment.Attachment",
                "AttachmentCache");
        region2CacheMapping.put("com.communote.server.model.attachment.AttachmentProperty",
                "AttachmentCache");
        region2CacheMapping.put("com.communote.server.model.attachment.Attachment.properties",
                "AttachmentCache");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cache buildCache(String name, Properties properties) throws CacheException {
        try {
            CacheManager manager = SingletonEhCacheManagerFactory.getInstance();
            if (region2CacheMapping.containsKey(name)) {
                name = region2CacheMapping.get(name);
            }
            net.sf.ehcache.Ehcache cache = manager.getEhcache(name);
            if (cache == null) {
                LOGGER.warn("No EhCache configuration for cache named {}"
                        + " found; creating cache with default settings.", name);
                manager.addCache(name);
                cache = manager.getEhcache(name);
                LOGGER.debug("Added EhCache: {}", name);
            }
            // use unwrapped caches for performance on ST installations
            if (CommunoteRuntime.getInstance().getApplicationInformation().isStandalone()) {
                return new EhCache(cache);
            }
            return new ClientDelegateCache(new EhCache(cache));
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMinimalPutsEnabledByDefault() {
        // whatever this is, just return false
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long nextTimestamp() {
        return Timestamper.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Properties properties) throws CacheException {
        LOGGER.debug("Creating CacheManager for Hibernate caching");
        try {
            SingletonEhCacheManagerFactory.createCacheManager(HIBERNATE_EHCACHE_CONFIG);
        } catch (net.sf.ehcache.CacheException e) {
            LOGGER.error("Creating the EhCache manager for the Hibernate caching failed", e);
            throw new CacheException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        // ignore because it is shut down when the CacheManager of the application is shutdown
    }

}
