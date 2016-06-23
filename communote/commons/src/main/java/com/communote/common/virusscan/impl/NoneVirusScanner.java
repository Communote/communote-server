package com.communote.common.virusscan.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.virusscan.VirusScanner;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;

/**
 * This scanner does nothing. All method are empty. Use it if you want turn off the scanner
 * functionality
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoneVirusScanner implements VirusScanner {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NoneVirusScanner.class);

    /**
     * {@inheritDoc}
     */
    public void init(Properties properties) throws InitializeException {

    }

    /**
     * {@inheritDoc}
     */
    public void scan(byte[] bytes) throws VirusScannerException, VirusFoundException {
        LOGGER.warn("No virus scan will be executed");
    }

    /**
     * {@inheritDoc}
     */
    public void scan(File file) throws VirusScannerException, VirusFoundException {
        LOGGER.warn("No virus scan will be executed");
    }

    /**
     * {@inheritDoc}
     */
    public InputStream scan(InputStream stream) throws VirusScannerException, VirusFoundException {
        LOGGER.warn("No virus scan will be executed");
        return stream;
    }

}
