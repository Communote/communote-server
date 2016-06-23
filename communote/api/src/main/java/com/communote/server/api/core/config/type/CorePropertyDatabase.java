package com.communote.server.api.core.config.type;

import com.communote.server.api.core.config.CoreConfigurationPropertyConstant;

/**
 * Property constants that represent database settings which are required for the start of the
 * application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum CorePropertyDatabase implements CoreConfigurationPropertyConstant {
    /** Driver class name. */
    DATABASE_DRIVER_CLASS_NAME("communote.database.driver.classname"),
    /** The host of the database. */
    DATABASE_HOST("communote.database.host"),

    /** The name of the Database. */
    DATABASE_NAME("communote.database.name"),

    /** The port the database is listening on. */
    DATABASE_PORT("communote.database.port"),

    /** Database protocol. */
    DATABASE_PROTOCOL("communote.database.protocol"),

    /** Database protocol separator. */
    DATABASE_PROTOCOL_SEPARATOR("communote.database.protocol.host.separator"),

    /** Property user name. */
    DATABASE_USER_NAME("communote.database.user.name"),

    /** Property for user password. */
    DATABASE_USER_PASSWORD("communote.database.user.password"),

    /**
     * Property denoting whether the database specific fulltext search feature should be enabled if
     * supported.
     */
    DATABASE_SPECIFIC_FULL_TEXT_SEARCH("communote.database.use.fulltextsearch"),

    /** Property for the hibernate dialect */
    HIBERNATE_DIALECT("communote.hibernate.dialect"),

    /** Separator to separate the host url from the schema, default is "/". */
    SCHEMA_SEPARATOR("communote.database.schema.separator");

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private CorePropertyDatabase(String keyString) {
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
}