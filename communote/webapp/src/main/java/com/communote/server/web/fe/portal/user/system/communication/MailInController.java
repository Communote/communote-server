package com.communote.server.web.fe.portal.user.system.communication;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyMailfetching;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.core.mail.fetching.MailInProtocolType;
import com.communote.server.core.service.BuiltInServiceNames;
import com.communote.server.core.service.RestartServiceEvent;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.sun.mail.iap.ConnectionException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MailInController extends BaseFormController {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(MailInController.class);

    /** Mailfetching is in single address mode. */
    public static final String MODE_SINGLE = "single";
    /** Mailfetching is multi address mode. */
    public static final String MODE_MULTI = "multi";

    /** Action to save form input. */
    public static final String ACTION_SAVE = "save";
    /** Action to test mail fetching. */
    public static final String ACTION_TEST = "testmail";
    /** Action to start the service. */
    public static final String ACTION_START_SERVICE = "start-service";
    /** Action to stop the service. */
    public static final String ACTION_STOP_SERVICE = "stop-service";

    private String forcedMode;

    /**
     * Asserts that a connection to the given mailbox is possible.
     *
     * @param protocol
     *            The protocol.
     * @param isStartTls
     *            True, if StartTLS ist possible.
     * @param host
     *            The host.
     * @param port
     *            The port.
     * @param login
     *            The login.
     * @param password
     *            The password.
     * @param mailbox
     *            The mailbox.
     * @throws ConnectionException
     *             Exception.
     * @throws MessagingException
     *             Exception.
     */
    private void assertConnection(String protocol, boolean isStartTls, String host, String port,
            String login, String password, String mailbox) throws ConnectionException,
            MessagingException {
        String propertiesProtocolPrefix = "mail." + protocol + ".";
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.put(propertiesProtocolPrefix + "starttls.enable", isStartTls);
        Session session = Session.getInstance(properties);
        Store store = session.getStore(protocol);
        try {
            store.connect(host, NumberUtils.toInt(port, -1), login, password);
            Folder folder = store.getFolder(mailbox);
            if (!folder.exists()) {
                throw new FolderNotFoundException();
            }
        } finally {
            store.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        MailInForm form = new MailInForm();
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties();
        form.setAction(ACTION_SAVE);
        form.setServer(props.getProperty(ApplicationPropertyMailfetching.HOST));
        form.setStartTls(props.getProperty(ApplicationPropertyMailfetching.USE_STARTTLS, false));

        String protocol = props.getProperty(ApplicationPropertyMailfetching.PROTOCOL,
                MailInForm.DEFAULT_PROTOCOL.getName());
        for (MailInProtocolType element : MailInProtocolType.values()) {
            if (StringUtils.equalsIgnoreCase(protocol, element.getName())) {
                form.setProtocol(element);
                break;
            }
        }

        form.setPort(props.getProperty(ApplicationPropertyMailfetching.PORT));
        form.setMailbox(props.getProperty(ApplicationPropertyMailfetching.MAILBOX));
        form.setFetchingTimeout(props.getProperty(ApplicationPropertyMailfetching.FETCH_TIMEOUT,
                MailInForm.DEFAULT_FETCH_TIMEOUT));
        form.setReconnectionTimeout(props.getProperty(
                ApplicationPropertyMailfetching.RECONNECT_TIMEOUT,
                MailInForm.DEFAULT_PRECONNECT_TIMEOUT));
        form.setLogin(props.getProperty(ApplicationPropertyMailfetching.USER_LOGIN));
        String encryptedPwd = props.getProperty(ApplicationPropertyMailfetching.USER_PASSWORD);
        form.setPassword(EncryptionUtils.decrypt(encryptedPwd,
                props.getProperty(ApplicationProperty.INSTALLATION_UNIQUE_ID)));
        form.setSingleModeAddress(props.getProperty(ApplicationPropertyMailfetching.SINGLE_ADDRESS));
        form.setMultiModeDomain(props.getProperty(ApplicationPropertyMailfetching.DOMAIN));
        form.setMultiModeSuffix(props.getProperty(ApplicationPropertyMailfetching.STATIC_SUFFIX));
        form.setMultiModeUseAccount(!Boolean.parseBoolean(props
                .getProperty(ApplicationPropertyMailfetching.NO_CLIENTID_IN_ADDRESS_FOR_GLOBAL)));

        // TODO should be the running state of the service, but doesn't work on slaves
        form.setRunning(props.getProperty(ApplicationPropertyMailfetching.ENABLED, false));
        boolean onlySingleMode = false;
        boolean onlyMultiMode = false;
        if (forcedMode != null) {
            form.setMode(forcedMode);
            if (forcedMode.equals(MODE_MULTI)) {
                onlyMultiMode = true;
            } else {
                onlySingleMode = true;
            }
        } else if (StringUtils.isNotBlank(form.getSingleModeAddress())) {
            form.setMode(MODE_SINGLE);
        } else {
            form.setMode(MODE_MULTI);
        }
        request.setAttribute("onlySingleMode", onlySingleMode);
        request.setAttribute("onlyMultiMode", onlyMultiMode);
        return form;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        MailInForm form = (MailInForm) command;
        try {
            if (ACTION_SAVE.equals(form.getAction())) {
                return saveSettings(request, form);
            } else if (ACTION_TEST.equals(form.getAction())) {
                assertConnection(form.getProtocol().getName(), form.isStartTls(), form.getServer(),
                        form.getPort(), form.getLogin(), form.getPassword(), form.getMailbox());
                MessageHelper.saveMessageFromKey(request,
                        "client.system.communication.mail.in.mode.success.test");
            } else if (ACTION_START_SERVICE.equals(form.getAction())) {
                handleServiceManagementTasks(true, request, form);
            } else if (ACTION_STOP_SERVICE.equals(form.getAction())) {
                handleServiceManagementTasks(false, request, form);
            }
        } catch (AuthenticationFailedException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.communication.mail.in.error.authentication");
        } catch (FolderNotFoundException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.communication.mail.in.error.folder");
        } catch (EncryptionException e) {
            LOG.error("Was not able to decrypt a property.", e);
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.communication.mail.in.error.encryption");
        } catch (MessagingException e) {
            LOG.debug("Unknown error.", e);
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.communication.mail.in.error.unknown",
                    new Object[] { e.getMessage() });
        }
        return new ModelAndView(getSuccessView(), "command", command);
    }

    /**
     * Handles the tasks to enable or disable the service to fetch e-mails.
     *
     * @param enableService
     *            {@code true} if the service should be enabled
     * @param request
     *            the current servlet request
     * @param form
     *            the form object with request parameters bound onto it
     * @throws Exception
     *             thrown in case of errors
     */
    private void handleServiceManagementTasks(boolean enableService, HttpServletRequest request,
            MailInForm form) throws Exception {
        Map<ApplicationConfigurationPropertyConstant, String> settings = null;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyMailfetching.ENABLED, Boolean.toString(enableService));

        // first update the property and then run service tasks
        // (service depends on the enabled property)
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request, "client.system.settings.error");
            LOG.error("Running a service task aborted because an incorrect update of "
                    + "an application property occured. Caused by "
                    + ApplicationPropertyMailfetching.ENABLED);
            return;
        }
        ServiceLocator.findService(EventDispatcher.class).fire(
                new RestartServiceEvent(BuiltInServiceNames.MAIL_FETCHING));
        saveServiceRestartFeedbackMessage(enableService, request);

        // TODO should be the running state of the service, but doesn't work on slaves
        form.setRunning(enableService);
    }

    /**
     * Saves an appropriate error/success message.
     *
     * @param enableService
     *            whether the service was enabled or disabled
     * @param request
     *            the request
     */
    private void saveServiceRestartFeedbackMessage(boolean enableService, HttpServletRequest request) {
        // TODO this is crap!! The actual running state should be reflected
        if (enableService) {
            MessageHelper.saveMessageFromKey(request,
                    "client.system.communication.mail.in.service.start.slave");
        } else {
            MessageHelper.saveMessageFromKey(request,
                    "client.system.communication.mail.in.service.stop.slave");
        }
    }

    /**
     * @param request
     *            The request.
     * @param form
     *            The form.
     * @return The MaV.
     * @throws Exception
     *             Exception.
     */
    private ModelAndView saveSettings(HttpServletRequest request, MailInForm form) throws Exception {
        Map<ApplicationConfigurationPropertyConstant, String> settings = null;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyMailfetching.USER_LOGIN, form.getLogin());

        if (form.isPasswordChanged()) {
            String encryptedPassword = EncryptionUtils.encrypt(form.getPassword(),
                    ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
            settings.put(ApplicationPropertyMailfetching.USER_PASSWORD, encryptedPassword);
        }
        settings.put(ApplicationPropertyMailfetching.HOST, form.getServer());
        settings.put(ApplicationPropertyMailfetching.PORT, form.getPort());
        settings.put(ApplicationPropertyMailfetching.USE_STARTTLS,
                Boolean.toString(form.isStartTls()));
        settings.put(ApplicationPropertyMailfetching.PROTOCOL, form.getProtocol().getName());
        settings.put(ApplicationPropertyMailfetching.FETCH_TIMEOUT, form.getFetchingTimeout());
        settings.put(ApplicationPropertyMailfetching.RECONNECT_TIMEOUT,
                form.getReconnectionTimeout());
        settings.put(ApplicationPropertyMailfetching.MAILBOX, form.getMailbox());
        settings.put(ApplicationPropertyMailfetching.STATIC_SUFFIX, StringUtils.EMPTY);
        settings.put(ApplicationPropertyMailfetching.NO_CLIENTID_IN_ADDRESS_FOR_GLOBAL,
                StringUtils.EMPTY);
        settings.put(ApplicationPropertyMailfetching.DOMAIN, StringUtils.EMPTY);
        settings.put(ApplicationPropertyMailfetching.SINGLE_ADDRESS, StringUtils.EMPTY);
        if (MODE_SINGLE.equals(form.getMode())) {
            settings.put(ApplicationPropertyMailfetching.SINGLE_ADDRESS,
                    form.getSingleModeAddress());
        } else if (MODE_MULTI.equals(form.getMode())) {
            settings.put(ApplicationPropertyMailfetching.STATIC_SUFFIX, form.getMultiModeSuffix());
            settings.put(ApplicationPropertyMailfetching.NO_CLIENTID_IN_ADDRESS_FOR_GLOBAL,
                    Boolean.toString(!form.getMultiModeUseAccount()));
            settings.put(ApplicationPropertyMailfetching.DOMAIN, form.getMultiModeDomain());
        }
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request, "client.system.settings.error");
            return new ModelAndView(getSuccessView(), "command", form);
        }
        MessageHelper.saveMessageFromKey(request, "client.system.settings.success");
        return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
    }

    /**
     * Restrict the available modes in the form to single or multi mode.
     *
     * @param mode
     *            one of the MODE_ constants
     */
    public void setForcedMode(String mode) {
        if (MODE_MULTI.equals(mode) || MODE_SINGLE.equals(mode)) {
            forcedMode = mode;
        }
    }
}
