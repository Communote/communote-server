package com.communote.server.core.service;

/**
 * Provides the names of the built-in services.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface BuiltInServiceNames {
    /**
     * The name of the mail-fetching service.
     */
    String MAIL_FETCHING = "DefaultMailFetchingService";
    /**
     * The name of the XMPP service.
     */
    String XMPP_MESSAGING = "DefaultXMPPService";
    /**
     * The name of the virus scanner service.
     */
    String VIRUS_SCANNER = "DefaultVirusScannerService";
}
