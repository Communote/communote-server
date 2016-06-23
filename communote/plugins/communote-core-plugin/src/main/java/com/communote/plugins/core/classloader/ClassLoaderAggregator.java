package com.communote.plugins.core.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregate ClassLoaders. On finding resources and classes the aggegrator will iterate over all
 * known classloaders to get a result.
 *
 * The priority of the classloader is based on the order of using
 * {@link #addClassloader(ClassLoader)}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClassLoaderAggregator extends ClassLoader {

    /**
     * The LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderAggregator.class);

    public static ClassLoaderAggregator setAggregatedClassloader(final ClassLoader current,
            final ClassLoader... classLoadersToUse) {

        ClassLoaderAggregator aggregatorCl = null;

        for (ClassLoader classLoader : classLoadersToUse) {
            if (current != classLoader && classLoader != null) {

                if (aggregatorCl == null) {
                    aggregatorCl = new ClassLoaderAggregator(classLoader);
                }
                else {
                    aggregatorCl.addClassloader(classLoader);
                }
            }
        }
        if (aggregatorCl != null) {
            aggregatorCl.addClassloader(current);
            Thread.currentThread().setContextClassLoader(aggregatorCl);
        }

        return aggregatorCl;
    }

    private final List<ClassLoader> classloaders = new LinkedList<ClassLoader>();

    /**
     *
     * @param cl
     *            the main initial class loader
     */
    public ClassLoaderAggregator(ClassLoader cl) {
        if (cl == null) {
            throw new IllegalArgumentException("ClassLoader cannot be null.");
        }
        this.classloaders.add(cl);
    }

    /**
     * The first classloader will be the main one, used for all non finding methods
     *
     * @param cl
     *            the classloader to add
     */
    public void addClassloader(ClassLoader cl) {
        this.classloaders.add(cl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void clearAssertionStatus() {
        getMain().clearAssertionStatus();
    }

    /**
     *
     * @return the main class loader
     */
    private ClassLoader getMain() {
        return this.classloaders.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource(String name) {
        for (ClassLoader cl : classloaders) {
            try {
                URL res = cl.getResource(name);

                if (res != null) {
                    return res;
                }
            } catch (Throwable th) {
                LOG.debug("Error calling #getResource", th);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> resources = new ArrayList<URL>();
        for (ClassLoader cl : classloaders) {
            try {
                Enumeration<URL> res = cl.getResources(name);

                while (res.hasMoreElements()) {
                    resources.add(res.nextElement());
                }
            } catch (Throwable th) {
                LOG.debug("Error calling #getResources", th);
            }
        }
        return new IteratorEnumeration<URL>(resources.iterator());
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> loadClass(String name, boolean log) throws ClassNotFoundException {
        ClassNotFoundException error = null;
        NoClassDefFoundError error2 = null;
        Class<?> clazz = null;
        for (ClassLoader cl : classloaders) {
            try {
                if (cl instanceof ClassLoaderAggregator) {
                    clazz = ((ClassLoaderAggregator) cl).loadClass(name, false);
                } else {
                    clazz = cl.loadClass(name);
                }
            } catch (ClassNotFoundException th) {
                error = th;
            } catch (NoClassDefFoundError ncd) {
                error2 = ncd;
            }
            if (clazz != null) {
                return clazz;
            }
        }

        if (!StringUtils.endsWith(name, "_")
                && !StringUtils.startsWith(name,
                        "com.sun.xml.internal.messaging.saaj.soap.LocalStrings")) {
            LOG.debug("ClassNotFound: " + name + " ClassLoaders: " + this.toString(), error);
        }
        if (error != null) {
            throw error;
        }
        if (error2 != null) {
            throw error2;
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setClassAssertionStatus(String className, boolean enabled) {
        getMain().setClassAssertionStatus(className, enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setDefaultAssertionStatus(boolean enabled) {
        getMain().setDefaultAssertionStatus(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setPackageAssertionStatus(String packageName, boolean enabled) {
        getMain().setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public String toString() {
        String res = this.getClass().getName() + ":\n";
        for (ClassLoader cl : this.classloaders) {
            res += cl.toString() + "\n";
        }
        return res;
    }
}
