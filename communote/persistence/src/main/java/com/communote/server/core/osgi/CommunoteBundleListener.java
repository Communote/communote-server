package com.communote.server.core.osgi;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * Marker interface for bundle listeners specific for Communote.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class CommunoteBundleListener implements BundleListener {

    /**
     * A list of Bundles which should be ignored by this listeners. The list mainly contains system
     * bundles.
     */
    private final static Set<String> BUNDLES_TO_IGNORE = new HashSet<String>();

    static {
        BUNDLES_TO_IGNORE.add("org.apache.felix.ipojo");
        BUNDLES_TO_IGNORE.add("org.apache.felix.ipojo.annotations");
        BUNDLES_TO_IGNORE.add("org.apache.felix.framework");
        BUNDLES_TO_IGNORE.add("org.apache.felix.ipojo.arch");
        BUNDLES_TO_IGNORE.add("org.apache.felix.shell");
        BUNDLES_TO_IGNORE.add("org.apache.log4j");
        BUNDLES_TO_IGNORE.add("org.apache.felix.org.apache.felix.shell.remote");
    }

    /**
     * Dispatches the Bundle events.
     * 
     * @param event
     *            The event.
     */
    public void bundleChanged(BundleEvent event) {
        if (BUNDLES_TO_IGNORE.contains(event.getBundle().getSymbolicName())) {
            return;
        }
        switch (event.getType()) {
        case BundleEvent.STARTED:
            fireBundleStarted(event.getBundle());
            break;
        case BundleEvent.STOPPED:
            fireBundleStopped(event.getBundle());
        }
    }

    /**
     * Called, when the bundle was started.
     * 
     * @param bundle
     *            The bundle.
     */
    protected abstract void fireBundleStarted(Bundle bundle);

    /**
     * Called, when the bundle was stopped.
     * 
     * @param bundle
     *            The bundle.
     */
    protected abstract void fireBundleStopped(Bundle bundle);

}
