package com.communote.server.web.fe.portal.user.system.communication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.core.service.BuiltInServiceNames;
import com.communote.server.core.service.CommunoteServiceManager;
import com.communote.server.core.service.RestartServiceEvent;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class XmppController extends BaseFormController {

    private static final String VELOCITY_TEMPLATE_HTTP_AUTH_PROPERTIES = ""
            + "com/communote/server/web/administration/xmpp/http-auth.properties.vm";

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(XmppController.class);

    /** Save. */
    public static final String ACTION_SAVE = "save";
    /** Test mail. */
    public static final String ACTION_TEST = "test";
    /** Action to start the service. */
    public static final String ACTION_START_SERVICE = "start-service";
    /** Action to stop the service. */
    public static final String ACTION_STOP_SERVICE = "stop-service";

    /**
     * Render the http auth properties file to be used with openfire
     *
     * @param request
     *            the request to use
     * @return the rendered properties
     */
    public static String getOpenfireHttpAuthProperties(HttpServletRequest request) {
        // TODO change JSPs to VM and parse the http_auth.properties template
        VelocityEngine engine = ServiceLocator.findService(VelocityEngine.class);
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("defaultHost", ApplicationProperty.WEB_SERVER_HOST_NAME.getValue());
        context.put("defaultPort", ApplicationProperty.WEB_HTTP_PORT.getValue());
        context.put("internalHost", request.getServerName());
        context.put("internalPort", request.getServerPort());
        if (request.isSecure()) {
            context.put("internalProtocol", "https");
        } else {
            context.put("internalProtocol", "http");
        }
        context.put("defaultPortHttps", ApplicationProperty.WEB_HTTPS_PORT.getValue());
        context.put("context", request.getContextPath());
        String render = VelocityEngineUtils.mergeTemplateIntoString(engine,
                VELOCITY_TEMPLATE_HTTP_AUTH_PROPERTIES, context);
        return render;
    }

    private String testResource;

    /** */
    private CommunoteServiceManager serviceManager;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        XmppForm xmppForm = new XmppForm();
        xmppForm.setAction(ACTION_SAVE);
        xmppForm.setServer(ApplicationPropertyXmpp.HOST.getValue());
        xmppForm.setPort(ApplicationPropertyXmpp.PORT.getValue());
        xmppForm.setLogin(ApplicationPropertyXmpp.LOGIN.getValue());
        try {
            xmppForm.setPassword(EncryptionUtils.decrypt(
                    ApplicationPropertyXmpp.PASSWORD.getValue(),
                    ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue()));
        } catch (EncryptionException e) {
            xmppForm.setPassword(StringUtils.EMPTY);
        }
        xmppForm.setPriority(ApplicationPropertyXmpp.PRIORITY.getValue());

        xmppForm.setRunning(getServiceManager().isRunning(BuiltInServiceNames.XMPP_MESSAGING));

        return xmppForm;
    }

    /**
     * @return the kenmei service manager
     */
    private CommunoteServiceManager getServiceManager() {
        if (serviceManager == null) {
            serviceManager = ServiceLocator.findService(CommunoteServiceManager.class);
        }
        return serviceManager;
    }

    /**
     * @return an XMPP resource for testing the connection
     */
    private String getTestResource() {
        if (testResource == null) {
            testResource = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getStartupProperties().getInstanceName()
                    + "_test";
        }
        return testResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        XmppForm form = (XmppForm) command;
        try {
            if (ACTION_SAVE.equals(form.getAction())) {
                return saveSettings(request, form);
            } else if (ACTION_TEST.equals(form.getAction())) {
                testConnection(request, form);
            } else if (ACTION_START_SERVICE.equals(form.getAction())) {
                handleServiceManagementTasks(true, request, form);
            } else if (ACTION_STOP_SERVICE.equals(form.getAction())) {
                handleServiceManagementTasks(false, request, form);
            }
        } catch (XMPPException.XMPPErrorException e) {
            XMPPError xmppError = e.getXMPPError();
            String condition = xmppError != null ? xmppError.getCondition() : null;
            if (condition == null) {
                condition = "Unknown condition";
            }
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.communication.xmpp.client.error.test.condition", condition,
                    e.getMessage());
            LOG.debug("XMPP error occurred: ", e.getMessage());
        } catch (SmackException.ConnectionException e) {
            LOG.debug("Connection failed", e);
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.communication.xmpp.client.error.test.connectionFailed");
        } catch (XMPPException | SmackException | IOException e) {
            LOG.error("Error testing XMPP server connection", e);
            if (e.getMessage() != null) {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.system.communication.xmpp.client.error.test.unknownWithDetail",
                        e.getMessage());
            } else {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.system.communication.xmpp.client.error.test.unknown");
            }
        } catch (EncryptionException e) {
            LOG.error("Was not able to decrypt a property.", e);
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.communication.xmpp.client.error.encryption");
        }
        return new ModelAndView(getSuccessView(), "command", command);
    }

    /**
     * Handles the tasks to enable or disable the service for XMPP messaging.
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
            XmppForm form) throws Exception {
        Map<ApplicationConfigurationPropertyConstant, String> settings = null;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyXmpp.ENABLED, Boolean.toString(enableService));

        // first update the property and then run service tasks
        // (service depends on the enabled property)
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
            .updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request, "client.system.settings.error");
            LOG.error("Running a service task aborted because an incorrect update of "
                    + "an application property occured. Caused by "
                    + ApplicationPropertyXmpp.ENABLED);
            return;
        }
        ServiceLocator.findService(EventDispatcher.class).fire(
                new RestartServiceEvent(BuiltInServiceNames.XMPP_MESSAGING));

        if (enableService) {
            if (getServiceManager().isRunning(BuiltInServiceNames.XMPP_MESSAGING)) {
                MessageHelper.saveMessageFromKey(request,
                        "client.system.communication.xmpp.service.start.success");
            } else {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.system.communication.xmpp.service.start.error");
            }
        } else {
            if (!getServiceManager().isRunning(BuiltInServiceNames.XMPP_MESSAGING)) {
                MessageHelper.saveMessageFromKey(request,
                        "client.system.communication.xmpp.service.stop.success");
            } else {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.system.communication.xmpp.service.stop.error");
            }
        }

        form.setRunning(getServiceManager().isRunning(BuiltInServiceNames.XMPP_MESSAGING));
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
    private synchronized ModelAndView saveSettings(HttpServletRequest request, XmppForm form)
            throws Exception {
        Map<ApplicationConfigurationPropertyConstant, String> settings = null;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyXmpp.LOGIN, form.getLogin());

        if (form.isPasswordChanged()) {
            String encryptedPassword = EncryptionUtils.encrypt(form.getPassword(),
                    ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
            settings.put(ApplicationPropertyXmpp.PASSWORD, encryptedPassword);
        }
        settings.put(ApplicationPropertyXmpp.HOST, form.getServer());
        settings.put(ApplicationPropertyXmpp.PORT, form.getPort());
        settings.put(ApplicationPropertyXmpp.PRIORITY, form.getPriority());
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
     * Sends a test mail.
     *
     * @param request
     *            request.
     * @param form
     *            Form.
     * @throws XMPPException
     *             Exception.
     * @throws IOException
     * @throws SmackException
     */
    private void testConnection(HttpServletRequest request, XmppForm form) throws XMPPException,
    SmackException, IOException {
        ConnectionConfiguration config = new ConnectionConfiguration(form.getServer(),
                Integer.parseInt(form.getPort()));
        XMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login(form.getLogin(), form.getPassword(), getTestResource());
        connection.disconnect();
        MessageHelper.saveMessageFromKey(request,
                "client.system.communication.xmpp.client.success.test");
    }
}
