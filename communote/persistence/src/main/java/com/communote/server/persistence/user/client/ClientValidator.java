package com.communote.server.persistence.user.client;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.client.InvalidClientIdException;

/**
 * The Class ClientValidator validates a client id.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class ClientValidator {
    /** Part of the pattern containing all valid symbols for the client id. */
    public final static String CLIENT_ID_VALID_SYMBOLD = "a-z0-9_";
    /** The Constant CLIENT_ID_REGEX_PATTERN defines the pattern for a client id. */
    public final static String CLIENT_ID_REGEX_PATTERN = "[" + CLIENT_ID_VALID_SYMBOLD + "]+";

    /** The Constant CLIENT_ID_MAX_LENGTH defines the maximal length of a client id. */
    public final static Integer CLIENT_ID_MAX_LENGTH = 20;

    /** The Constant CLIENT_ID_MIN_LENGTH defines the minimal length of a client id. */
    public final static Integer CLIENT_ID_MIN_LENGTH = 1;

    /**
     * Test that the client ID is valid.
     *
     * @param clientId
     *            the client ID to test
     * @throws InvalidClientIdException
     *             in case the ID is not valid
     */
    public static void assertValidClientId(String clientId) throws InvalidClientIdException {
        if (StringUtils.isNotBlank(clientId)) {
            if (!ClientValidator.validateClientId(clientId)) {
                throw new InvalidClientIdException(clientId, "the client id '" + clientId
                        + "' is invalid");
            }
            if (!ClientValidator.validateClientIdLength(clientId)) {
                throw new InvalidClientIdException(clientId, "the client id '" + clientId
                        + "' is too long");
            }
        } else {
            throw new InvalidClientIdException("", "client id can not be empy");
        }
    }

    /**
     * Validate client id against the regex pattern.
     *
     * @param id
     *            the id
     * @return true, if successful
     */
    public static boolean validateClientId(String id) {
        return id != null && id.matches(CLIENT_ID_REGEX_PATTERN);
    }

    /**
     * Validate the client id length.
     *
     * @param id
     *            the id
     * @return true, if successful
     */
    public static boolean validateClientIdLength(String id) {
        return id != null && id.length() >= CLIENT_ID_MIN_LENGTH
                && id.length() <= CLIENT_ID_MAX_LENGTH;
    }

    /**
     * Instantiates a new client validator.
     */
    private ClientValidator() {
    }
}
