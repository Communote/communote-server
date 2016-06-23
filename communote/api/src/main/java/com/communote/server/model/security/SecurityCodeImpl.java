package com.communote.server.model.security;

import java.util.UUID;

/**
 * @see com.communote.server.model.security.SecurityCode
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SecurityCodeImpl extends com.communote.server.model.security.SecurityCode {
    /**
     * {@inheritDoc}
     */
    private static final long serialVersionUID = 6122036170180276484L;

    /**
     * {@inheritDoc}
     */
    public void generateNewCode() {
        setCode(UUID.randomUUID().toString());
    }

}