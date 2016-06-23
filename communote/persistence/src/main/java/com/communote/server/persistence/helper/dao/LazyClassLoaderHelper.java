package com.communote.server.persistence.helper.dao;

import org.hibernate.proxy.HibernateProxy;

/**
 * Helper methods for handling lazy loading objects.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class LazyClassLoaderHelper {

    /**
     * Dereference the proxy objects on demand.
     * 
     * @param <T>
     *            Same Object class as given by baseClass
     * @param maybeProxy
     *            Object, which needs to be dereferenced.
     * @param baseClass
     *            Class type the maybeProxy should be an instance of
     * @return the dereferenced maybeProxy
     * @throws ClassCastException
     *             If the maybeProxy is not null, but the baseClass not assignable to it.
     */
    public static <T> T deproxy(Object maybeProxy, Class<T> baseClass)
            throws ClassCastException {
        if (maybeProxy instanceof HibernateProxy) {
            return baseClass.cast(((HibernateProxy) maybeProxy).getHibernateLazyInitializer()
                    .getImplementation());
        } else {
            return baseClass.cast(maybeProxy);
        }
    }

    /**
     * Private constructor, because utility classes should not have a public or default constructor.
     */
    private LazyClassLoaderHelper() {
        // Do nothing.
    }
}
