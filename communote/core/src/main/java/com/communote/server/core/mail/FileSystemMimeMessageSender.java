package com.communote.server.core.mail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import com.communote.common.io.IOHelper;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.application.Runtime;
import com.communote.server.api.core.config.ConfigurationInitializationException;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;

/**
 * MimeMessageSender which stores the message on the filesystem in a sub-directory of Communote's
 * data directory. Mainly useful for development and tests.
 * 
 * @author Communote team - <a href="http://communote.github.io/">http://communote.github.io/</a>
 */
public class FileSystemMimeMessageSender implements MimeMessageSender {

    public static final String MESSAGE_STORAGE_SUB_DIR = "DevMimeMessageStore";

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemMimeMessageSender.class);

    private final File storageDir;

    private Session session;

    private boolean messageIdBasedFilename;

    /**
     * 
     * @param messageIdBasedFilename
     *            whether to create file name from message ID or the subject
     */
    public FileSystemMimeMessageSender(boolean messageIdBasedFilename) {
        storageDir = initStorageDir();
        this.messageIdBasedFilename = messageIdBasedFilename;
    }

    private String createFilename(MimeMessage message) throws MessagingException {
        String mailFilename;
        if (messageIdBasedFilename) {
            mailFilename = message.getMessageID().replace("<", "").replace(">", "");
        } else {
            mailFilename = String.valueOf(System.currentTimeMillis()) + "-" + message.getSubject()
                    + "-" + UUID.randomUUID().hashCode() + ".txt";
            // replace chars which are forbidden on some platforms
            mailFilename = mailFilename.replace('$', '_');
            mailFilename = mailFilename.replace('{', '_');
            mailFilename = mailFilename.replace('}', '_');
            mailFilename = mailFilename.replace('\\', '_');
            mailFilename = mailFilename.replace(':', '_');
            mailFilename = mailFilename.replace("..", ".");
            mailFilename = mailFilename.replace(' ', '_');
            mailFilename = mailFilename.replace('<', '_');
            mailFilename = mailFilename.replace('>', '_');
        }
        return mailFilename;
    }

    @Override
    public MimeMessage createMimeMessage() {
        return new MessageIdPreservingMimeMessage(getSession());
    }

    private Session getSession() {
        if (session == null) {
            session = Session.getInstance(new Properties());
        }
        return session;
    }

    private File initStorageDir() {
        Runtime runtime = CommunoteRuntime.getInstance();
        File dataDir = runtime.getConfigurationManager().getStartupProperties().getDataDirectory();
        File dir = new File(dataDir, MESSAGE_STORAGE_SUB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.isDirectory()) {
            throw new ConfigurationInitializationException(
                    "Initialization of mail storage directory failed");
        }
        return dir;
    }

    @Override
    public void send(MimeMessage message) throws MailException {
        File messageFile;
        try {
            message.saveChanges();
            String filename = createFilename(message);
            messageFile = new File(storageDir, filename);
            if (messageFile.exists()) {
                throw new MailingException(
                        "File '" + filename + "' for message with ID " + message.getMessageID()
                                + " already exists");
            }
        } catch (MessagingException e) {
            throw new MailingException("Creating file for storing message failed", e);
        }
        BufferedOutputStream out = null;
        boolean success = false;
        try {
            messageFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(messageFile));
            message.writeTo(out);
            success = true;
        } catch (IOException e) {
            throw new MailingException("Creating file for storing message failed", e);
        } catch (MessagingException e) {
            throw new MailingException("Storing message failed", e);
        } finally {
            IOHelper.close(out);
            if (!success && messageFile.exists()) {
                try {
                    Files.delete(messageFile.toPath());
                } catch (IOException e) {
                    LOGGER.warn(
                            "Removing incomplete message file {} failed. This might cause further exceptions.",
                            messageFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings) {
        return testSettings(settings, null);
    }

    @Override
    public boolean testSettings(Map<ApplicationPropertyMailing, String> settings,
            MimeMessage message) {
        // test for minimal configuration an SMTP based sender would need
        if (StringUtils.isBlank(settings.get(ApplicationPropertyMailing.HOST))) {
            LOGGER.info("Mail settings not valid: host is missing");
            return false;
        }
        if (message != null) {
            try {
                send(message);
            } catch (MailException e) {
                LOGGER.info("Sending test message failed: {}", e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateSettings(Map<ApplicationPropertyMailing, String> settings) {
        // nothing
    }

}
