package com.communote.server.core.security.authentication.confluence;

import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.security.authentication.TokenFactory;

/**
 * Factory to create a confluence token
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ConfluenceTokenFactory implements TokenFactory<ConfluenceAuthenticationToken> {

    /**
     * {@inheritDoc}
     * 
     * @Override
     */
    @Override
    public ConfluenceAuthenticationToken createToken(String token) {
        return new ConfluenceAuthenticationToken(token,
                ClientProperty.GENERIC_AUTHENTICATOR_ENABLED.getValue(false));
    }

    /**
     * {@inheritDoc}
     * 
     * @Override
     */
    @Override
    public String getTokenName() {
        return ClientProperty.GENERIC_AUTHENTICATOR_URL_TOKEN_NAME
                .getValue("confluence_token");
    }
}
