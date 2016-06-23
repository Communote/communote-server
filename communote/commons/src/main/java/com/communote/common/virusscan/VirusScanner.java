package com.communote.common.virusscan;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;


/**
 * Virus scanner interface.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface VirusScanner {

    /**
     * Initialize the Scanner
     * 
     * @param properties
     *            Init Properties. This will be used for individual settings.
     * @throws InitializeException
     *             Thrown when initialization fails
     */
    void init(Properties properties) throws InitializeException;

    /**
     * Scan a byte array for viruses
     * 
     * @param bytes
     *            Byte content
     * @throws VirusScannerException
     *             This Exception encapsulate any exception
     * @throws VirusFoundException
     *             {@link VirusScannerException}
     */
    void scan(byte[] bytes) throws VirusScannerException, VirusFoundException;

    /**
     * Scan file for virus
     * 
     * @param file
     *            Scanning file
     * @throws VirusScannerException
     *             {@link com.communote.common.virusscan.exception.VirusScannerException}
     * @throws VirusFoundException
     *             {@link com.communote.common.virusscan.exception.VirusFoundException}
     */
    void scan(File file) throws VirusScannerException, VirusFoundException;

    /**
     * Scan an input stream for virus.
     * 
     * @param stream
     *            Data stream
     * @return Return <code>true</code> if a virus was detected else <code>false</code>, when
     *         no virus was detected
     * @throws VirusScannerException
     *             This Exception encapsulate any exception
     * @throws VirusFoundException
     *             This only thrown when a virus was found
     */
    InputStream scan(InputStream stream) throws VirusScannerException, VirusFoundException;
}
