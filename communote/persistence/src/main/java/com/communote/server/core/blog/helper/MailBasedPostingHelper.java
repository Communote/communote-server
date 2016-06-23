package com.communote.server.core.blog.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.model.note.Note;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Helper methods for using mail based posting.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailBasedPostingHelper {
    private static final String MAIL_TO_PREFIX = "mailto:";
    private static final String MAIL_TO_SUBJECT_SUFFIX = "?subject=";
    private final static Logger LOG = Logger.getLogger(MailBasedPostingHelper.class);

    /**
     * Creates a message identifier that uniquely identifies a note and can be used as part of a
     * Message-ID email header.
     *
     * @param note
     *            the note for which the identifier is to be created
     * @return the identifier
     */
    public static String createMessageIdentifier(Note note) {
        StringBuilder sb = new StringBuilder();
        sb.append(note.getId());
        sb.append(".");
        String clientId = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.UNIQUE_CLIENT_IDENTIFER);
        sb.append(clientId);
        sb.append(".");
        sb.append(System.currentTimeMillis());
        return sb.toString();
    }

    /**
     * Extracts the ID of a note from a message identifier that was previously created with
     * {@link #createMessageIdentifier(Note)}.
     *
     * @param messageIdentifier
     *            the identifier to parse
     * @return the note ID or null if the identifier does not correspond to the expected format
     */
    public static Long extractNoteIdFromMessageIdentifier(String messageIdentifier) {
        String[] parts = messageIdentifier.split("\\.");
        if (parts.length == 3) {
            String clientId = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getClientConfigurationProperties()
                    .getProperty(ClientProperty.UNIQUE_CLIENT_IDENTIFER);
            if (clientId.equals(parts[1])) {
                try {
                    return Long.parseLong(parts[0]);
                } catch (NumberFormatException e) {
                    LOG.info("Identifier within Messeage-ID header value does not contain a"
                            + " parsable note ID: " + e.getMessage());
                }
            } else {
                LOG.info("Identifier within Messeage-ID header value does not contain the correct"
                        + " unique client ID");
            }
        } else {
            LOG.debug("Identifier within Message-ID header value does not match the expected"
                    + " format");
        }
        return null;
    }

    /**
     * Returns the blog email address of a blog identified by its name identifier. In case the
     * system is configured to use only one email address for all blogs then this address will be
     * returned.
     *
     * @param blogIdentifier
     *            the name identifier of the blog
     * @return the email address or null if the mail fetcher is disabled blogIdentifier is null or
     *         {@link #getCurrentEmailServerDomain()} returns null
     */
    public static String getBlogEmailAddress(String blogIdentifier) {
        String singleAddress = CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyMailfetching.SINGLE_ADDRESS);
        String emailAddress = null;
        if (StringUtils.isBlank(singleAddress)) {
            String suffix = getClientWideBlogEmailAddressSuffix();
            if (suffix != null && blogIdentifier != null) {
                emailAddress = blogIdentifier + suffix;
            }
        } else {
            if (isMailFetchingEnabled()) {
                emailAddress = singleAddress;
            }
        }
        return emailAddress;
    }

    /**
     * Returns the mailto string to be inserted as href value to render a link of the email address
     * of a blog.
     *
     * @param alias
     *            the alias of the blog
     * @return the mailto string or null if the mail fetcher is not enabled
     */
    public static String getBlogMailTo(String alias) {
        String emailAddress = MailBasedPostingHelper.getBlogEmailAddress(alias);
        return getBlogMailToFromEmailAddress(emailAddress, alias);
    }

    /**
     * Returns the mailto string to be inserted as href value to render a link of the email address
     * of a blog.
     *
     * @param emailAddress
     *            the email address of the blog
     * @param alias
     *            the alias of the blog
     * @return the mailto string or null if the mail fetcher is not enabled
     */
    public static String getBlogMailToFromEmailAddress(String emailAddress, String alias) {
        if (emailAddress == null) {
            return null;
        }
        StringBuilder mailTo = new StringBuilder(MAIL_TO_PREFIX);
        mailTo.append(emailAddress);
        if (MailBasedPostingHelper.isRunningInSingleAddressMode() && alias != null) {
            mailTo.append(MAIL_TO_SUBJECT_SUFFIX + "[");
            mailTo.append(alias);
            mailTo.append("]");
        }
        return mailTo.toString();
    }

    /**
     * Returns the blog email address without the leading blog name identifier.
     *
     * @return the email address suffix or null if {@link #getCurrentEmailServerDomain()} returns
     *         null
     */
    public static String getClientWideBlogEmailAddressSuffix() {
        boolean noClientId = CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyMailfetching.NO_CLIENTID_IN_ADDRESS_FOR_GLOBAL,
                        false);
        if (noClientId && ClientHelper.isCurrentClientGlobal()) {
            return getClientWideBlogEmailAddressSuffix("");
        }
        return getClientWideBlogEmailAddressSuffix("." + ClientHelper.getCurrentClientId());
    }

    /**
     * Returns the blog email address without the leading blog name identifier.
     *
     * @param clientId
     *            The client id.
     * @return the email address suffix or null if {@link #getCurrentEmailServerDomain()} returns
     *         null
     */
    public static String getClientWideBlogEmailAddressSuffix(String clientId) {
        String domain = getCurrentEmailServerDomain();
        if (domain == null) {
            return null;
        }
        StringBuilder address = new StringBuilder();
        address.append(clientId);
        String staticSuffix = getStaticMailAddressSuffix();
        if (StringUtils.isNotBlank(staticSuffix)) {
            address.append(".");
            address.append(staticSuffix);
        }
        address.append("@");
        address.append(domain);
        return address.toString();
    }

    /**
     * Returns the email server domain (e.g. communote.com). The method will fallback to the
     * mailfetching.host property if the property mailfetching.domain is not found.
     *
     * @return the domain or null if not specified in mailfetching configuration or the mail
     *         fetching is disabled or singleMode is activated
     */
    public static String getCurrentEmailServerDomain() {
        String domain = null;
        if (isMailFetchingEnabled() && !isRunningInSingleAddressMode()) {
            ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                    .getConfigurationManager().getApplicationConfigurationProperties();
            domain = props.getProperty(ApplicationPropertyMailfetching.DOMAIN);
            if (StringUtils.isEmpty(domain)) {
                domain = props.getProperty(ApplicationPropertyMailfetching.HOST);
                if (StringUtils.isEmpty(domain)) {
                    return null;
                }
            }
        }
        return domain;
    }

    /**
     * The static part at the end of the local name of an email address (e.g. microblog) which is
     * used for mail based posting. This part is separated from the rest of the address by the '.'
     * character. The value can be the empty string which results in not using a static suffix.
     *
     * @return the static part from configuration or the empty string if configuration property is
     *         missing
     */
    public static String getStaticMailAddressSuffix() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyMailfetching.STATIC_SUFFIX, "");
    }

    /**
     * Returns whether the mail fetcher is enable by checking the configuration properties.
     *
     * @return true if the mailfetching.enabled property is set to 'true' (case insensitive)
     */
    public static boolean isMailFetchingEnabled() {
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties();
        return props.getProperty(ApplicationPropertyMailfetching.ENABLED, false);
    }

    /**
     * Returns true if the mail fetcher is configured to run in single address mode, which means
     * that there is only one address to receive mail based postings and the blog name ID is
     * searched inside the email's subject. Note: it's not checked whether mail fetching is enabled
     * or not.
     *
     * @return true if single address mode is enabled
     */
    public static boolean isRunningInSingleAddressMode() {
        String singleAddress = ApplicationPropertyMailfetching.SINGLE_ADDRESS.getValue();
        return StringUtils.isNotBlank(singleAddress);
    }

    /**
     * Helper class does not need to be constructed
     */
    private MailBasedPostingHelper() {

    }

}
