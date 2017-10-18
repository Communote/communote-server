package com.communote.server.core.mail;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.util.MapUtils;
import com.communote.common.validation.EmailValidator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ConfigurationInitializationException;
import com.communote.server.api.core.config.DevelopmentProperties;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;

/**
 * Factory which evaluates the development properties and creates a suitable MimeMessageSender.
 * 
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 */
public class MimeMessageSenderFactory {

    private enum MailMode {
        CATCH_ALL, FILESYSTEM, SMTP
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MimeMessageSenderFactory.class);
    private static final String MAILOUT_MODE = "mailout.mode";
    private static final String MAILOUT_CATCHALL_ADDRESS = "mailout.catchall.address";
    private static final String MAILOUT_FILESYSTEM_NAME_FROM_ID = "mailout.filesystem.nameFromMessageId";

    public MimeMessageSender createInstance() throws Exception {
        DevelopmentProperties devProps = CommunoteRuntime.getInstance().getConfigurationManager()
                .getDevelopmentProperties();
        MailMode mode = getMailMode(devProps.getProperty(MAILOUT_MODE));
        MimeMessageSender sender;
        if (mode.equals(MailMode.FILESYSTEM)) {
            LOGGER.info("Creating filesystem mail sender");
            sender = new FileSystemMimeMessageSender(devProps.getProperty(MAILOUT_FILESYSTEM_NAME_FROM_ID, false));
        } else if (mode.equals(MailMode.CATCH_ALL)) {
            String catchAllAddress = devProps.getProperty(MAILOUT_CATCHALL_ADDRESS);
            if (EmailValidator.validateEmailAddressByRegex(catchAllAddress)) {
                LOGGER.info("Creating catch-all SMTP mail sender with address {}", catchAllAddress);
                sender = new CatchAllSmtpMimeMessageSender(getMailoutSettings(), catchAllAddress);
            } else {
                throw new ConfigurationInitializationException(
                        "Catch-all email address '" + catchAllAddress + "' is not valid");
            }
        } else {
            LOGGER.info("Creating SMTP mail sender");
            sender = new SmtpMimeMessageSender(getMailoutSettings());
        }
        return sender;
    }

    private MailMode getMailMode(String mailModePropertyValue) {
        if (StringUtils.isBlank(mailModePropertyValue)) {
            return MailMode.SMTP;
        }
        mailModePropertyValue = mailModePropertyValue.toLowerCase(Locale.ENGLISH);
        MailMode mode;
        switch (mailModePropertyValue) {
        case "smtp":
            mode = MailMode.SMTP;
            break;
        case "catchall":
            mode = MailMode.CATCH_ALL;
            break;
        case "filesystem":
            mode = MailMode.FILESYSTEM;
            break;
        default:
            throw new ConfigurationInitializationException("Unsupported vaule '"
                    + mailModePropertyValue + "' for development property " + MAILOUT_MODE);
        }
        return mode;
    }

    private Map<ApplicationPropertyMailing, String> getMailoutSettings()
            throws EncryptionException {
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties();
        HashMap<ApplicationPropertyMailing, String> settings = new HashMap<>();
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.HOST,
                props.getProperty(ApplicationPropertyMailing.HOST));
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.PORT,
                props.getProperty(ApplicationPropertyMailing.PORT));
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.LOGIN,
                props.getProperty(ApplicationPropertyMailing.LOGIN));
        if (StringUtils.isNotEmpty(settings.get(ApplicationPropertyMailing.LOGIN))) {
            MapUtils.putNonNull(settings, ApplicationPropertyMailing.PASSWORD,
                    props.getPropertyDecrypted(ApplicationPropertyMailing.PASSWORD));
        }
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.USE_STARTTLS,
                props.getProperty(ApplicationPropertyMailing.USE_STARTTLS));
        return settings;
    }

}
