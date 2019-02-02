package com.communote.server.core.user.security;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;

/**
 * <p>
 * bcrypt password hash function which produces a string in
 * <a href="http://passlib.readthedocs.io/en/stable/modular_crypt_format.html">modular crypt
 * format</a>.
 * </p>
 * Note: not using PHC string format as there is no definition for ID and parameters.
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 * @since 3.5
 */
public class BcryptPasswordHashFunction implements PasswordHashFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BcryptPasswordHashFunction.class);
    private static final String ID = "bcrypt";
    // a cost factor between 10 and 12 is currently (~2015) considered appropriate
    private static final int DEFAULT_COST_FACTOR = 11;
    private static final int MIN_COST_FACTOR = 4;
    private static final int MAX_COST_FACTOR = 31;

    @Override
    public boolean canHandle(String passwordHash) {
        // hash is always 60 chars long. Only supporting version 2a
        return passwordHash.length() == 60 && passwordHash.startsWith("$2a$")
                && passwordHash.charAt(6) == '$' && getCostFactorFromHash(passwordHash) != -1;
    }

    @Override
    public boolean check(String passwordHash, String password) {
        return OpenBSDBCrypt.checkPassword(passwordHash, password.toCharArray());
    }

    @Override
    public String generate(String password) {
        // bcrypt needs exactly 128 bits of salt
        byte[] salt = EncryptionUtils.generateSalt(16);
        return OpenBSDBCrypt.generate(password.toCharArray(), salt, getCostFactor());
    }

    private int getCostFactor() {
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties();
        int costFactor = props.getProperty(
                ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_HASH_BCRYPT_COST,
                DEFAULT_COST_FACTOR);
        if (costFactor < MIN_COST_FACTOR) {
            LOGGER.info(
                    "Configured cost factor {} is less than allowed minimum ({}). Using default {}",
                    costFactor, MIN_COST_FACTOR, DEFAULT_COST_FACTOR);
            costFactor = DEFAULT_COST_FACTOR;
        } else if (costFactor > MAX_COST_FACTOR) {
            LOGGER.info(
                    "Configured cost factor {} is larger than allowed maximum ({}). Using default {}",
                    costFactor, MAX_COST_FACTOR, DEFAULT_COST_FACTOR);
            costFactor = DEFAULT_COST_FACTOR;
        }
        return costFactor;
    }

    public int getCostFactorFromHash(String passwordHash) {
        String costFactorPart = passwordHash.substring(4, 6);
        try {
            int cost = Integer.parseInt(costFactorPart);
            if (cost >= MIN_COST_FACTOR && cost <= MAX_COST_FACTOR) {
                return cost;
            } else {
                LOGGER.debug("Cost factor is not in allowed range: {}", costFactorPart);
            }
        } catch (NumberFormatException e) {
            LOGGER.debug("Cost factor is not a valid: {}", costFactorPart);
        }
        return -1;
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public boolean needsUpdate(String passwordHash) {
        int costFactor = getCostFactor();
        int usedCostFactor = getCostFactorFromHash(passwordHash);
        return costFactor > usedCostFactor;
    }

}
