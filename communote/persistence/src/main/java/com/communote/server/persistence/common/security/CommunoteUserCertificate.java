package com.communote.server.persistence.common.security;

import java.security.cert.X509Certificate;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulates the subject name of a certificate and provides method to check if is a
 * valid communote user certificate and provides method to get the clientId and user alias. *
 * 
 * A valid communote user certificate is one there the OU (organization unit) of the subject named
 * ends with "communote.user". Then the CN is the communote user alias and the clientId is the part
 * of OU without 'communote.user'
 * 
 * Example for a subject name 'CN=sharepoint.system, OU=global.communote.user, O=Communote,
 * L=Dresden, ST=Sachsen, C=DE' Here the user alias is 'sharepoint.system' and the client id is
 * 'global'
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CommunoteUserCertificate {

    private static final String CN = "CN";

    private static final String OU = "OU";

    private static final String COMMUNOTE_USER = "communote.user";

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunoteUserCertificate.class);

    /**
     * Uses the list of certificates and create a communote certificate and check if all properties
     * are set and valid. Return the first one valid.
     * 
     * @param certs
     *            some certificates to pick one from
     * @return The first valid communote certificate or null if none is available
     */
    public static CommunoteUserCertificate pickValid(X509Certificate[] certs) {
        if (certs == null) {
            return null;
        }
        for (X509Certificate cert : certs) {
            try {
                CommunoteUserCertificate certificate = new CommunoteUserCertificate(cert);
                if (certificate.isValidCommunoteUserCertificate()) {
                    return certificate;
                }
            } catch (Exception e) {
                LOGGER.warn("Error extracting certificate. Will ignore it. cert=" + cert
                        + e.getMessage());
                LOGGER.debug(
                        "Error extracting certificate. Will ignore it. cert=" + cert
                                + e.getMessage(), e);
            }
        }
        return null;
    }

    private final String subjectName;

    private final LdapName ldapName;

    /**
     * Use the given subject name. Example: 'CN=sharepoint.system, OU=global.communote.user,
     * O=Communote, L=Dresden, ST=Sachsen, C=DE'
     * 
     * @param subject
     *            the name, cannot be null or empty and must be a valid {@link LdapName}
     */
    public CommunoteUserCertificate(String subject) {
        if (StringUtils.isEmpty(subject)) {
            throw new IllegalArgumentException("subject cannot be null or empty");
        }
        this.subjectName = subject;
        try {
            ldapName = new LdapName(this.subjectName);
        } catch (InvalidNameException e) {
            throw new IllegalArgumentException("subject seems not to be a valid name, subject="
                    + subject, e);
        }
    }

    /**
     * 
     * @param cert
     *            initialize with the X509Certificate
     */
    public CommunoteUserCertificate(X509Certificate cert) {
        this(cert.getSubjectX500Principal().getName());

    }

    /**
     * Checks if the certificate / subject name contains all information for identifying the user.
     * It does not mean the user actually exists, it only states that all attributes (clientId,
     * communtoe alias) are set end extractable.
     * 
     * @return true if the subject name can identify a communote user
     */
    public boolean containsValidCommunoteUser() {
        return isValidCommunoteUserCertificate() && getCommunoteUserAlias() != null
                && getClientId() != null;
    }

    /**
     * 
     * @return the client id extracted, null if not available
     */
    public String getClientId() {
        String ou = null;
        if (isValidCommunoteUserCertificate()) {
            ou = getEntry(OU);

            ou.substring(0, ou.length() - COMMUNOTE_USER.length() + 1);
        }
        return ou;
    }

    /**
     * 
     * @return the user alias extracted, null if not available
     */
    public String getCommunoteUserAlias() {

        return getEntry(CN);
    }

    /**
     * Get the entry of the subject name using the ldap name.
     * 
     * Example: Assume the subject name is 'CN=sharepoint.system, OU=global.communote.user,
     * O=Communote, L=Dresden, ST=Sachsen, C=DE'
     * 
     * it will return the following value:<br>
     * <br>
     * OU, ou=, OU=: global.communote.user <br>
     * CN, cn: sharepoint.system<br>
     * <br>
     * 
     * @param entry
     *            the entry, e.g. 'ou', 'OU', 'ou='
     * @return the value of the entry without the key, or null if entry does not exist.
     */
    private String getEntry(String entry) {
        entry = entry.endsWith("=") ? entry : entry + "=";
        entry = entry.toLowerCase();
        for (int i = 0; i < ldapName.size(); i++) {
            if (ldapName.get(i).toLowerCase().startsWith(entry)) {
                return ldapName.get(i).substring(entry.length());
            }
        }
        return null;
    }

    /**
     * Example.
     * "CN=sharepoint.system, OU=global.communote.user, O=Communote, L=Dresden, ST=Sachsen, C=DE"
     * 
     * @return the subject name of the certificate
     */
    public String getSubjectName() {
        return this.subjectName;
    }

    /**
     * 
     * @return checks if the OU of the subject name ends with communote.user
     */
    private boolean isValidCommunoteUserCertificate() {
        String ou = getEntry(OU);
        return ou != null && ou.endsWith(COMMUNOTE_USER);
    }
}
