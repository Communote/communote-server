package com.communote.common.virusscan;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.virusscan.exception.InitializeException;

/**
 * Returns actually configured scanner
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class VirusScannerFactory {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(VirusScannerFactory.class);

    /**
     * Scanner type
     */
    public static final String PROP_SCANNER_TYPE = "scanner.type";

    private static final VirusScannerFactory INSTANCE = new VirusScannerFactory();

    /**
     * @return the instance
     */
    public static VirusScannerFactory instance() {
        return INSTANCE;
    }

    private VirusScanner virusScanner;

    private boolean isInit;

    /**
     * An empty private constructor because this class is a utility class and should not be
     * instantiated by others.
     */
    private VirusScannerFactory() {

    }

    /**
     * Returns the configured virus scanner.
     * 
     * @return the configured virus scanner instance
     * @throws InitializeException
     *             if the factory was not successfully initialized and no scanner is available
     */
    public synchronized VirusScanner getScanner() throws InitializeException {
        if (!isInit) {
            throw new InitializeException("The virus scanner factory was not initialized");
        }
        return virusScanner;
    }

    /**
     * Init the factory and virus scanner
     * 
     * @param properties
     *            Properties which contains factory and scanner setting
     * @throws InitializeException
     * @throws InitializeException
     *             {@link InitializeException}
     */
    public synchronized void init(Properties properties) throws InitializeException {
        try {
            isInit = false;
            VirusScanner scannerInstance = null;
            String scanner = properties.getProperty(PROP_SCANNER_TYPE);
            if (StringUtils.isNotBlank(scanner)) {
                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(scanner);
                scannerInstance = (VirusScanner) clazz.newInstance();
            } else {
                throw new InitializeException("No scanner defined");
            }
            scannerInstance.init(properties);

            virusScanner = scannerInstance;
            isInit = true;
        } catch (ClassNotFoundException e) {
            throw new InitializeException("Error initializing virus scanner!", e);
        } catch (InstantiationException e) {
            throw new InitializeException("Error initializing virus scanner!", e);
        } catch (IllegalAccessException e) {
            throw new InitializeException("Error initializing virus scanner!", e);
        }
    }

    /**
     * Read the property file and calls the <code>init(Properties Properties)</code> method
     * 
     * @param filePath
     *            The path to the property file
     * @throws IOException
     *             {@link IOException}
     * @throws ClassNotFoundException
     *             {@link ClassNotFoundException}
     * @throws InstantiationException
     *             {@link InstantiationException}
     * @throws IllegalAccessException
     *             {@link IllegalAccessException}
     * @throws InitializeException
     *             {@link InitializeException}
     */
    public void init(String filePath) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InitializeException, IOException {
        LOGGER.info("Init virus scanner");
        Properties properties = new Properties();
        properties.load(new FileInputStream(filePath));
        init(properties);
    }

}
