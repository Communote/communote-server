package com.communote.common.virusscan.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.io.CopyInputStreamBuffer;
import com.communote.common.virusscan.VirusScanner;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;

/**
 * This class represents an anti virus scanner. The input stream or byte content will be scanned by
 * a clamav daemon(TCP/IP).
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClamAVScanner implements VirusScanner {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClamAVScanner.class);

    private static final String TEMP_FILENAME_PREFIX = "virus_temp_file";
    private static final String TEMP_FILENAME_SUFFIX = ".scan";

    /**
     * Directory for temporary files
     */
    public static final String TEMP_DIR_PROP = "clamav.scanner.temp.dir";

    /**
     * Host where the clamav daemon is listening
     */
    public static final String HOST_PROP = "clamav.daemon.host";

    /**
     * Port where daemon is listening
     */
    public static final String PORT_PROP = "clamav.daemon.port";

    /**
     * TCP / IP Connection time out
     */
    public static final String CONNECTION_TIMEOUT_PROP = "clamav.daemon.connection.timeout";

    private int connectionTimeout = 90;
    private String clamdHost;
    private int clamdPort;
    private String tempDir;

    /**
     * This constructor is necessary for reflection else you will get an ClassNotFoundException by
     * the VirusScannerFactory
     */
    public ClamAVScanner() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Properties properties) throws InitializeException {
        clamdHost = properties.getProperty(HOST_PROP);
        if (StringUtils.isEmpty(clamdHost)) {
            throw new InitializeException("The property '" + HOST_PROP + "' was not found");
        }

        tempDir = properties.getProperty(TEMP_DIR_PROP);
        if (StringUtils.isEmpty(tempDir)) {
            throw new InitializeException("The property '" + TEMP_DIR_PROP + "' was not found");
        }
        tempDir = (tempDir.endsWith("/")) ? (tempDir) : (tempDir + "/");

        String strPort = properties.getProperty(PORT_PROP);
        if (StringUtils.isEmpty(strPort)) {
            throw new InitializeException("The property '" + PORT_PROP + "' was not found");
        }
        if (!StringUtils.isNumeric(strPort)) {
            throw new InitializeException("The value '" + strPort + "' for the key '" + PORT_PROP
                    + "' is not an valide integer");
        }
        clamdPort = Integer.parseInt(strPort);

        String strTimeout = properties.getProperty(CONNECTION_TIMEOUT_PROP);
        if (StringUtils.isEmpty(strTimeout)) {
            throw new InitializeException("The property '" + CONNECTION_TIMEOUT_PROP
                    + "' was not found");
        }
        if (!StringUtils.isNumeric(strTimeout)) {
            throw new InitializeException("The value '" + strTimeout + "' for the key '"
                    + CONNECTION_TIMEOUT_PROP + "' is not an valide integer");
        }
        connectionTimeout = Integer.parseInt(strTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scan(byte[] bytes) throws VirusScannerException, VirusFoundException {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputStream newStream = scan(stream);
        try {
            newStream.close();
        } catch (IOException e) {
            throw new VirusFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scan(File file) throws VirusScannerException, VirusFoundException {
        FileInputStream inputStream = null;
        InputStream returnStream = null;
        try {
            LOGGER.debug("Scanning file for viruses {}", file.getPath());
            inputStream = new FileInputStream(file);
            returnStream = scan(inputStream);

        } catch (FileNotFoundException e) {
            throw new VirusScannerException("File not found", e);
        } finally {
            IOUtils.closeQuietly(returnStream);
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream scan(InputStream stream) throws VirusScannerException, VirusFoundException {
        ClamAVDaemonConnector connector = new ClamAVDaemonConnector(clamdHost, clamdPort,
                connectionTimeout);
        LOGGER.debug("Start scanning stream for viruses.");
        CopyInputStreamBuffer buffer = null;
        try {
            File tempFile = CopyInputStreamBuffer.getTempFile(tempDir, TEMP_FILENAME_PREFIX,
                    TEMP_FILENAME_SUFFIX);
            buffer = new CopyInputStreamBuffer(stream, tempFile);
        } catch (FileNotFoundException e) {
            throw new VirusScannerException("Temporary file not found", e);
        } catch (IOException e) {
            throw new VirusScannerException(e);
        }

        try {
            boolean result = connector.performScan(buffer);
            if (!result) {
                LOGGER.info("A virus was detected in stream");
                buffer.closeCompleteStream();
                throw new VirusFoundException("A virus was detected in stream");
            }
        } catch (VirusScannerException e) {
            throw new VirusScannerException(e);
        }

        InputStream returnInputStream = null;
        try {
            buffer.close();
            returnInputStream = buffer.getInputStream();
        } catch (IOException e) {
            throw new VirusScannerException(e);
        }
        LOGGER.debug("Finished scanning stream for viruses.");
        return returnInputStream;
    }

}
