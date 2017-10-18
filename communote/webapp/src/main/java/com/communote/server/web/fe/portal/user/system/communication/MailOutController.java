package com.communote.server.web.fe.portal.user.system.communication;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.converter.IdentityConverter;
import com.communote.common.encryption.EncryptionException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.GenericMailMessage;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MailOutController extends BaseFormController {

    private final static Logger LOGGER = LoggerFactory.getLogger(MailOutController.class);

    /** Save. */
    public static final String ACTION_SAVE = "save";
    /** Test mail. */
    public static final String ACTION_TESTMAIL = "testmail";

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        MailOutForm mailOutForm = new MailOutForm();
        mailOutForm.setLogin(ApplicationPropertyMailing.LOGIN.getValue());
        String decryptedPassword = StringUtils.EMPTY;
        try {
            ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                    .getConfigurationManager().getApplicationConfigurationProperties();
            decryptedPassword = props.getPropertyDecrypted(ApplicationPropertyMailing.PASSWORD);
        } catch (EncryptionException e) {
            LOGGER.debug("Decryption of mail account password failed");
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

    private Map<ApplicationPropertyMailing, String> getMailSettings(MailOutForm form,
            boolean alwaysIncludePassword) {
        HashMap<ApplicationPropertyMailing, String> settings = new HashMap<>();
        settings.put(ApplicationPropertyMailing.LOGIN, form.getLogin());

        if (alwaysIncludePassword || form.isPasswordChanged()) {
            // TODO is the old password actually set if not changed???
            settings.put(ApplicationPropertyMailing.PASSWORD, form.getPassword());
        }

        settings.put(ApplicationPropertyMailing.HOST, form.getServer());
        settings.put(ApplicationPropertyMailing.PORT, form.getPort());
        settings.put(ApplicationPropertyMailing.USE_STARTTLS, Boolean.toString(form.isStartTls()));

        settings.put(ApplicationPropertyMailing.FROM_ADDRESS, form.getSenderAddress());
        settings.put(ApplicationPropertyMailing.FROM_ADDRESS_NAME, form.getSenderName());

        return settings;
    }

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
        MailSender mailSender = ServiceLocator.findService(MailSender.class);
        if (mailSender.testSettings(getMailSettings(form, true))) {
            LOGGER.info("SMTP server connection test succeeded.");
        } else {
            MessageHelper.saveErrorMessage(request, MessageHelper.getText(request,
                    "client.system.communication.mail.out.test.connection.error"));
            return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
        }
        try {
            mailSender.updateSettings(getMailSettings(form, false));
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.application.settings.error");
            return new ModelAndView(getSuccessView(), "command", form);
        }
        // TODO use own message!!
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
        User user = ServiceLocator.findService(UserManagement.class).getUserById(
                SecurityHelper.assertCurrentUserId(), new IdentityConverter<User>());
        GenericMailMessage testMessage = new GenericMailMessage(
                "client.system.communication.mail.out.test.mail.message", user.getLanguageLocale(),
                user);
        Map<ApplicationPropertyMailing, String> newSettings = getMailSettings(form, true);
        testMessage.setFromAddress(newSettings.get(ApplicationPropertyMailing.FROM_ADDRESS));
        testMessage.setFromAddressName(newSettings.get(ApplicationPropertyMailing.FROM_ADDRESS_NAME));
        MailSender mailSender = ServiceLocator.findService(MailSender.class);
        if (mailSender.testSettings(newSettings , testMessage)) {
            MessageHelper.saveMessage(request, MessageHelper.getText(request,
                    "client.system.communication.mail.out.test.mail.send.success",
                    new Object[] { user.getEmail() }));
        } else {
            MessageHelper.saveErrorMessage(request, MessageHelper.getText(request,
                    "client.system.communication.mail.out.test.mail.send.error"));
        }
        return new ModelAndView(getSuccessView(), "command", form);
    }

}
