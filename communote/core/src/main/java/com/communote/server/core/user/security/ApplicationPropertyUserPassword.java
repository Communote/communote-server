package com.communote.server.core.user.security;

import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;

public enum ApplicationPropertyUserPassword implements ApplicationConfigurationPropertyConstant {

    /**
     * Property holding a boolean value which determines whether the password of a local user should
     * be updated during login with the hash produced by the current hash function.
     */
    LOCAL_USER_PASSWORD_UPDATE_ON_LOGIN("communote.core.security.localUserPassword.updateOnLogin"),

    /**
     * Property holding the identifier of the password hash function which should be used for
     * storing the passwords of local (i.e. not provided by an external user repository) Communote
     * users.
     */
    LOCAL_USER_PASSWORD_HASH_FUNCTION("communote.core.security.localUserPassword.hashFunction"),

    /**
     * Property holding the cost factor of the bcrypt password hash function which can be used for
     * hashing the password of local Communote users. The value of the property can be an integer in
     * the range of 4 to 31, inclusive.
     */
    LOCAL_USER_PASSWORD_HASH_BCRYPT_COST("communote.core.security.localUserPassword.hashFunction.bcrypt.cost");

    /**
     * Default value for property LOCAL_USER_PASSWORD_UPDATE_ON_LOGIN
     */
    public static final boolean DEFAULT_LOCAL_USER_PASSWORD_UPDATE_ON_LOGIN = true;

    private final String key;

    /**
     * Constructor for enum type.
     *
     * @param keyString
     *            the constant as string
     */
    private ApplicationPropertyUserPassword(String keyString) {
        this.key = keyString;
    }

    @Override
    public String getKeyString() {
        return key;
    }

}
