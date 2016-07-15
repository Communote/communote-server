package com.communote.server.api.core.config.type;

import org.apache.commons.lang.StringUtils;

import com.communote.common.virusscan.VirusScannerFactory;
import com.communote.common.virusscan.impl.ClamAVScanner;
import com.communote.common.virusscan.impl.CommandlineScanner;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;

/**
 * Property constants for the virus scanner settings of the application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum ApplicationPropertyVirusScanning implements ApplicationConfigurationPropertyConstant {
    /** @see CommandlineScanner#COMMAND_LINE_PROP */
    COMMAND_LINE_STRING(CommandlineScanner.COMMAND_LINE_PROP),
    /** @see CommandlineScanner#EXIT_CODE_PROP */
    COMMAND_LINE_EXIT_CODE(CommandlineScanner.EXIT_CODE_PROP),
    /** @see CommandlineScanner#TEMP_DIR_PROP */
    COMMAND_LINE_TEMP_DIR(CommandlineScanner.TEMP_DIR_PROP),
    /** @see CommandlineScanner#TEMP_FILE_PREFIX_PROP */
    COMMAND_LINE_TEMP_FILE_PREFIX(CommandlineScanner.TEMP_FILE_PREFIX_PROP),
    /** @see CommandlineScanner#TEMP_FILE_SUFFIX_PROP */
    COMMAND_LINE_TEMP_FILE_SUFFIX(CommandlineScanner.TEMP_FILE_SUFFIX_PROP),
    /** @see CommandlineScanner#COMMAND_LINE_PROCESS_TIMEOUT_PROP */
    COMMAND_LINE_PROCESS_TIMEOUT(CommandlineScanner.COMMAND_LINE_PROCESS_TIMEOUT_PROP),
    /** @see ClamAVScanner#TEMP_DIR_PROP */
    CLAMAV_SCANNER_TEMP_DIR(ClamAVScanner.TEMP_DIR_PROP),
    /** @see ClamAVScanner#HOST_PROP */
    CLAMAV_SCANNER_HOST(ClamAVScanner.HOST_PROP),
    /** @see ClamAVScanner#PORT_PROP */
    CLAMAV_SCANNER_PORT(ClamAVScanner.PORT_PROP),
    /** @see ClamAVScanner#CONNECTION_TIMEOUT_PROP */
    CLAMAV_SCANNER_CONNECTION_TIMEOUT(ClamAVScanner.CONNECTION_TIMEOUT_PROP),
    /** @see VirusScannerFactory#PROP_SCANNER_TYPE */
    VIRUS_SCANNER_FACTORY_TYPE(VirusScannerFactory.PROP_SCANNER_TYPE),
    /**
     * defines whether the virus scanner is enabled
     */
    ENABLED("virus.scanner.enabled");

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private ApplicationPropertyVirusScanning(String keyString) {
        this.key = keyString;
    }

    /**
     * String representation of the constant to be used as key in Properties objects.
     *
     * @return the constant as string
     */
    @Override
    public String getKeyString() {
        return key;
    }

    /**
     * @return The actual value for this property of the current client.
     */
    public String getValue() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties().getProperty(this);
    }

    /**
     * @param defaultValue
     *            fallback to this default value if the actual value is whitespace, empty ("") or
     *            {@code null}.
     * @return The actual value for this property of the current client.
     */
    public String getValue(String defaultValue) {
        String value = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties().getProperty(this);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return defaultValue;
    }
}