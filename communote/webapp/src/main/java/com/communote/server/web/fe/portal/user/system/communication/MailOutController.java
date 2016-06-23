package com.communote.server.web.fe.portal.user.system.communication;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.core.mail.KenmeiJavaMailSender;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MailOutController extends BaseFormController {

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(MailOutController.class);

    /** Save. */
    public static final String ACTION_SAVE = "save";
    /** Test mail. */
    public static final String ACTION_TESTMAIL = "testmail";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        MailOutForm mailOutForm = new MailOutForm();
        mailOutForm.setLogin(ApplicationPropertyMailing.LOGIN.getValue());
        String decryptedPassword = StringUtils.EMPTY;
        try {
            decryptedPassword = EncryptionUtils.decrypt(
                    ApplicationPropertyMailing.PASSWORD.getValue(),
                    ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
        } catch (EncryptionException e) {
            LOG.debug("Was not able to encrypt a property: "
                    + ApplicationPropertyMailing.PASSWORD.getKeyString());
        }
        mailOutForm.setPassword(decryptedPassword);

        mailOutForm.setServer(ApplicationPropertyMailing.HOST.getValue());
        mailOutForm.setPort(ApplicationPropertyMailing.PORT.getValue());
        mailOutForm.setStartTls(ApplicationPropertyMailing.USE_STARTTLS.getValue(false));
        mailOutForm.setSenderName(ApplicationPropertyMailing.FROM_ADDRESS_NAME.getValue());
        mailOutForm.setSenderAddress(ApplicationPropertyMailing.FROM_ADDRESS.getValue());
        mailOutForm.setAction(ACTION_SAVE);
        return mailOutForm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        MailOutForm form = (MailOutForm) command;
        ModelAndView view = null;
        if (ACTION_SAVE.equals(form.getAction())) {
            view = saveSettings(request, form);
        } else if (ACTION_TESTMAIL.equals(form.getAction())) {
            view = sendTestmail(request, form);
        } else {
            MessageHelper.saveErrorMessageFromKey(request, "client.system.settings.error");
            return new ModelAndView(getSuccessView(), "command", form);
        }
        return view;
    }

    /**
     * Saves the settings.
     *
     * @param request
     *            Request
     * @param form
     *            Form
     * @return MaV.
     * @throws Exception
     *             Exception.
     */
    private synchronized ModelAndView saveSettings(HttpServletRequest request, MailOutForm form)
            throws Exception {
        try {
            testConnection(request, form);
        } catch (MessagingException e) {
            MessageHelper.saveErrorMessage(request, MessageHelper.getText(request,
                    "client.system.communication.mail.out.test.connection.error",
                    new Object[] { "<p>" + e.getMessage() + "</p>" }));
            return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
        }
        Map<ApplicationConfigurationPropertyConstant, String> settings = null;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyMailing.LOGIN, form.getLogin());

        if (form.isPasswordChanged()) {
            String encryptedPassword = EncryptionUtils.encrypt(form.getPassword(),
                    ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
            settings.put(ApplicationPropertyMailing.PASSWORD, encryptedPassword);
        }

        settings.put(ApplicationPropertyMailing.HOST, form.getServer());
        settings.put(ApplicationPropertyMailing.PORT, form.getPort());
        settings.put(ApplicationPropertyMailing.USE_STARTTLS, Boolean.toString(form.isStartTls()));

        settings.put(ApplicationPropertyMailing.FROM_ADDRESS, form.getSenderAddress());
        settings.put(ApplicationPropertyMailing.FROM_ADDRESS_NAME, form.getSenderName());
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.application.settings.error");
            return new ModelAndView(getSuccessView(), "command", form);
        }
        ServiceLocator.findService(MailManagement.class).resetSettings();
        MessageHelper.saveMessageFromKey(request, "client.system.content.file.upload.success");
        return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
    }

    /**
     * Sends a test mail.
     *
     * @param request
     *            request.
     * @param form
     *            Form.
     * @return MaV.
     */
    private ModelAndView sendTestmail(HttpServletRequest request, MailOutForm form) {
        String email = SecurityHelper.assertCurrentKenmeiUser().getEmail();
        try {
            Properties mailingProperties = new Properties();
            mailingProperties.setProperty("mail.smtp.starttls.enable",
                    Boolean.toString(form.isStartTls()));
            String username = null, password = null;
            if (StringUtils.isNotBlank(form.getLogin())) {
                mailingProperties.setProperty("mail.smtp.auth", "true");
                username = form.getLogin();
                password = form.getPassword();
            }
            KenmeiJavaMailSender mailor = new KenmeiJavaMailSender(form.getServer(),
                    NumberUtils.toInt(form.getPort(), 25), username, password, mailingProperties);
            mailor.getSession().setDebug(LOG.isDebugEnabled());
            mailor.send(MessageHelper.getText(request,
                    "client.system.communication.mail.out.test.mail.subject"), MessageHelper
                    .getText(request, "client.system.communication.mail.out.test.mail.message"),
                    form.getSenderAddress(), form.getSenderName(), email);
            MessageHelper.saveMessage(request, MessageHelper.getText(request,
                    "client.system.communication.mail.out.test.mail.send.success",
                    new Object[] { email }));
        } catch (MailException e) {
            MessageHelper.saveErrorMessage(
                    request,
                    MessageHelper.getText(request,
                            "client.system.communication.mail.out.test.mail.send.error")
                            + "<p>"
                            + e.getMessage() + "</p>");
        }
        return new ModelAndView(getSuccessView(), "command", form);
    }

    /**
     * Try to establish a connection to the mail server.
     *
     * @param request
     *            the http request.
     * @param form
     *            the form.
     * @throws MessagingException
     *             in case there is a problem communicating with SMTP server
     */
    private void testConnection(HttpServletRequest request, MailOutForm form)
            throws MessagingException {
        LOG.info("Test connetion to the SMTP server.");
        Properties mailingProperties = new Properties();
        String host = form.getServer();
        int port = NumberUtils.toInt(form.getPort(), 25);
        String username = null;
        String password = null;
        if (StringUtils.isNotBlank(form.getLogin())) {
            mailingProperties.setProperty("mail.smtp.auth", "true");
            username = form.getLogin();
            password = form.getPassword();
        }
        mailingProperties.setProperty("mail.smtp.starttls.enable",
                Boolean.toString(form.isStartTls()));
        KenmeiJavaMailSender mailSender = new KenmeiJavaMailSender(host, port, username, password,
                mailingProperties);
        if (!mailSender.canConnect()) {
            throw new MessagingException("It was not possible to connect to the service.");
        }
        LOG.info("Can establish a connection to the SMTP server.");
    }
}
