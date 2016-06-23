package com.communote.plugins.atlr;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorManager;

/**
 * OSGi Activator for Atlr.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AtlrActivator implements BundleActivator {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AtlrActivator.class);

    private AliasAndTagLinkRenderer atlr;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BundleContext context) throws Exception {
        LOGGER.info("Atlr plugin started");
        atlr = new AliasAndTagLinkRenderer();
        ServiceLocator.findService(NoteRenderingPreProcessorManager.class).addProcessor(
                atlr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        ServiceLocator.findService(NoteRenderingPreProcessorManager.class).removeProcessor(
                atlr);
        atlr = null;
    }
}
