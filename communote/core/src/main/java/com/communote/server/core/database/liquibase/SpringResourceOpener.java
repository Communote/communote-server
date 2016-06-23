package com.communote.server.core.database.liquibase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import liquibase.FileOpener;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Opener for liquibase changelogs that supports a spring resource loader.
 *
 * <br>
 * Inspired by liquibase.integration.spring.SpringLiquibase
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SpringResourceOpener implements FileOpener {
    private final String parentFile;
    private final ResourceLoader resourceLoader;

    /**
     * @param resourceLoader
     *            the resource loader
     * @param parentFile
     *            the parent file
     */
    public SpringResourceOpener(ResourceLoader resourceLoader, String parentFile) {
        this.parentFile = parentFile;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Adjust the classpath using the parentfile if file is relative
     *
     * @param file
     *            the file to adjust
     * @return the adjusted class path
     */
    private String adjustClasspath(String file) {
        if (isClasspathPrefixPresent(parentFile) && !isClasspathPrefixPresent(file)) {
            return ResourceLoader.CLASSPATH_URL_PREFIX + file;
        }
        return file;
    }

    /**
     * @param file
     *            the file to get
     * @return the resource
     */
    public Resource getResource(String file) {
        return resourceLoader.getResource(adjustClasspath(file));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResourceAsStream(String file) throws IOException {
        Resource resource = getResource(file);

        return resource.getInputStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<URL> getResources(String packageName) throws IOException {
        Vector<URL> tmp = new Vector<URL>();

        tmp.add(getResource(packageName).getURL());

        return tmp.elements();
    }

    /**
     * @param file
     *            the file to check
     * @return true of the file is a classpath resource
     */
    public boolean isClasspathPrefixPresent(String file) {
        return file.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader toClassLoader() {
        return resourceLoader.getClassLoader();
    }
}
