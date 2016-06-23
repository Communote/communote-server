package com.communote.server.api.core.config.database;

/**
 * Describes a database management system.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DatabaseType {
    private final String driverClassName;
    private final String protocol;
    private final String dialectClassName;
    private final String[] protocolSeparators;
    private final String identifier;
    private final int defaultPort;

    /**
     * Constructor for database type.
     *
     * @param identifier
     *            a string that uniquely identifies the type. The string should only contain
     *            alphanumeric ASCII characters.
     * @param defaultPort
     *            default port of the database server
     * @param driverClassName
     *            the driver as string
     * @param protocol
     *            the protocol as string
     * @param dialectClassName
     *            name of the appropriate dialect class
     */
    public DatabaseType(String identifier, int defaultPort, String driverClassName,
            String protocol, String dialectClassName) {
        this(identifier, defaultPort, driverClassName, protocol, dialectClassName,
                new String[] { "://" });
    }

    /**
     * Constructor for database type.
     *
     * @param identifier
     *            a string that uniquely identifies the type. The string should only contain
     *            alphanumeric ASCII characters.
     * @param defaultPort
     *            default port of the database server
     * @param driverClassName
     *            the driver as string
     * @param protocol
     *            the protocol as string
     * @param dialectClassName
     *            name of the appropriate dialect class
     * @param protocolSeparator
     *            The separator for the database protocol.
     */
    public DatabaseType(String identifier, int defaultPort, String driverClassName,
            String protocol, String dialectClassName, String[] protocolSeparator) {
        this.identifier = identifier;
        this.defaultPort = defaultPort;
        this.driverClassName = driverClassName;
        this.protocol = protocol;
        this.dialectClassName = dialectClassName;
        this.protocolSeparators = protocolSeparator;
    }

    /**
     * @return the default port of the database server
     */
    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * @return The default separator for this db type.
     */
    public String getDefaultProtocolSeperator() {
        return protocolSeparators[0];
    }

    /**
     * The hibernate dialect class name as string assigned to the constant.
     *
     * @return the dialect class name as string
     */
    public String getDialectClassName() {
        return dialectClassName;
    }

    /**
     * The driver class name as string assigned to the constant.
     *
     * @return the driver class name as string
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * @return a string that uniquely identifies the type. The string should only contain
     *         alphanumeric ASCII characters.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * The protocol name as string assigned to the constant.
     *
     * @return the protocol name as string
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param separator
     *            Separator to match.
     * @return True, if this type supports the given separator.
     */
    public boolean supportsProtocolSeparator(String separator) {
        for (String protocolSeparator : protocolSeparators) {
            if (protocolSeparator.equals(separator)) {
                return true;
            }
        }
        return false;
    }

}
