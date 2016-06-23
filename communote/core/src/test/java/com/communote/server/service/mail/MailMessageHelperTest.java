package com.communote.server.service.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.vo.content.AttachmentTO;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MailMessageHelperTest {
    private static final String SRC_TEST_RESOURCES_MAILING_TEST = "src/test/resources/mailing-test";

    /**
     * @param attachmentPaths
     *            Paths to attachment files.
     * @throws Exception
     *             Exception.
     * @return Message with attachments.
     */
    private Message prepareMessage(String[] attachmentPaths) throws Exception {
        Message message = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("Test.");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        for (String file : attachmentPaths) {
            BodyPart attachmentPart = new MimeBodyPart();
            File attachment = new File(SRC_TEST_RESOURCES_MAILING_TEST + "/" + file);
            DataSource source = new FileDataSource(attachment);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(attachment.getName());
            multipart.addBodyPart(attachmentPart);
        }
        message.setContent(multipart);
        message.removeHeader("Content-Type");
        message.setHeader("Content-Type", "multipart/mixed");
        Assert.assertTrue(message.isMimeType("multipart/*"));
        return message;
    }

    /**
     * Test for extracting attachments.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testGetAttachments() throws Exception {
        File attachmentFolder = new File(SRC_TEST_RESOURCES_MAILING_TEST);
        List<String> attachmentPaths = new ArrayList<String>();
        for (String file : attachmentFolder.list()) {
            if (new File(SRC_TEST_RESOURCES_MAILING_TEST + "/" + file).isFile()) {
                attachmentPaths.add(file);
            }
        }
        Message message = prepareMessage(attachmentPaths
                .toArray(new String[attachmentPaths.size()]));
        Collection<AttachmentTO> attachments = MailMessageHelper.getAttachments(message);
        Assert.assertNotNull(attachments);
        Assert.assertEquals(attachments.size(), attachmentPaths.size());
        for (String attachment : attachmentPaths) {
            boolean found = false;
            for (AttachmentTO attachmentTO : attachments) {
                found = found || attachmentTO.getMetadata().getFilename().equals(attachment);
            }
            Assert.assertTrue(found);
        }
    }
}
