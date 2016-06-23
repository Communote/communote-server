package com.communote.plugins.core.classloader;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ClassLoaderAggregatorThreadContextSetter<T extends Exception> {

    public void execute(final ClassLoader... classLoadersToUse) throws T {

        ClassLoader current = Thread.currentThread().getContextClassLoader();

        try {
            ClassLoaderAggregator.setAggregatedClassloader(current, classLoadersToUse);

            run();

        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
    }

    protected abstract void run() throws T;
}