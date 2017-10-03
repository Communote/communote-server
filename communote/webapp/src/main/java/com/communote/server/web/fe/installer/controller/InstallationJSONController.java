package com.communote.server.web.fe.installer.controller;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.communote.common.util.MapUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.bootstrap.ApplicationInitializationException;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.GenericMailMessage;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.installer.helper.DatabaseStatusCallback;

/**
 * JSON Controller to handle ajax requests.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InstallationJSONController extends MultiActionController {

    private static final String JSON_RESPONSE_FIELD_MESSAGE = "message";

    private static final String JSON_RESPONSE_STATUS_TYPE_OK = "OK";

    private static final String JSON_RESPONSE_STATUS_TYPE_ERROR = "ERROR";

    private static final String JSON_RESPONSE_FIELD_STATUS = "status";

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(InstallationJSONController.class);

    /** the name of the session attribute for setup status */
    public final static String DATABASE_SETUP_STATUS_SESSION_KEY = "databaseSetupStatus";

    /** this value represents the start of a process */
    public final static String DATABASE_SETUP_STATUS_STARTED = "STARTED";

    /** this value represents a succeeded step */
    public final static String DATABASE_SETUP_STATUS_SUCCEEDED = "SUCCEEDED";

    /** this value represents a failed step */
    public final static String DATABASE_SETUP_STATUS_FAILED = "FAILED";

    /** the name of the session attribute for setup progress */
    public final static String DATABASE_SETUP_PROGRESS_SESSION_KEY = "databaseSetupProgress";

    /** this number represents the progress 'establish a connection' */
    public final static String DATABASE_SETUP_PROGRESS_CONNECTION = "0";

    /** this number represents the progress 'preparing the installation' */
    public final static String DATABASE_SETUP_PROGRESS_PREPARING = "1";

    /** this number represents the progress 'creating the database schema' */
    public final static String DATABASE_SETUP_PROGRESS_SCHEMA = "2";

    /** this number represents the progress 'initial some application data' */
    public final static String DATABASE_SETUP_PROGRESS_DATA = "3";

    /** the name of the session attribute for setup progress */
    public final static String DATABASE_SETUP_MESSAGE_SESSION_KEY = "databaseSetupMessage";

    /**
     * Ajax method to check the current status of database initialization.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void checkDatabaseSetupStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // only support POST
        if (!StringUtils.equalsIgnoreCase(request.getMethod(), METHOD_POST)) {
            return;
        }

        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        String progress = StringUtils.EMPTY;
        String status = StringUtils.EMPTY;
        String message = StringUtils.EMPTY;

        HttpSession session = request.getSession();

        if (session.getAttribute(DATABASE_SETUP_STATUS_SESSION_KEY) != null) {
            status = (String) session.getAttribute(DATABASE_SETUP_STATUS_SESSION_KEY);
        }

        if (session.getAttribute(DATABASE_SETUP_PROGRESS_SESSION_KEY) != null) {
            progress = (String) session.getAttribute(DATABASE_SETUP_PROGRESS_SESSION_KEY);
        }

        if (session.getAttribute(DATABASE_SETUP_MESSAGE_SESSION_KEY) != null) {
            message = (String) session.getAttribute(DATABASE_SETUP_MESSAGE_SESSION_KEY);
        }

        jsonResponse.put("progress", progress);
        jsonResponse.put(JSON_RESPONSE_FIELD_STATUS, status);
        jsonResponse.put(JSON_RESPONSE_FIELD_MESSAGE, message);
        response.getWriter().write(JsonHelper.writeJsonTreeAsString(jsonResponse));

        return;
    }

    /**
     * Ajax method to initialize the application after completing the installation.
     *
     * @param request
     *            the current request
     * @param response
     *            the response to write to
     * @throws IOException
     *             in case writing the response failed
     */
    public void completeInstallation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // only support POST
        if (!StringUtils.equalsIgnoreCase(request.getMethod(), METHOD_POST)) {
            return;
        }
        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        try {
            CommunoteRuntime.getInstance().getInstaller().initializeApplicationAfterInstallation();
            jsonResponse.put(JSON_RESPONSE_FIELD_STATUS, JSON_RESPONSE_STATUS_TYPE_OK);
        } catch (ApplicationInitializationException e) {
            LOGGER.error("Completing the installation failed", e);
            jsonResponse.put(JSON_RESPONSE_FIELD_STATUS, JSON_RESPONSE_STATUS_TYPE_ERROR);
        }
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }


    /**
     * Sends a test mail.
     *
     * @param request
     *            the http request.
     * @param response
     *            the http response.
     *
     * @throws IOException
     *             in case of an IO exception
     */
    public void sendTestMessage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // only support POST
        if (!StringUtils.equalsIgnoreCase(request.getMethod(), METHOD_POST)) {
            return;
        }

        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        StringBuilder jsonMessage = new StringBuilder();
        String senderAddress = request.getParameter("senderAddress");
        String senderName = request.getParameter("senderName");
        Map<ApplicationPropertyMailing, String> settings = new HashMap<>();
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.HOST, request.getParameter("smtpHost"));
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.PORT, request.getParameter("smtpPort"));
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.USE_STARTTLS, request.getParameter("startTls"));
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.LOGIN, request.getParameter("smtpUser"));
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.PASSWORD, request.getParameter("smtpPassword"));
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.FROM_ADDRESS, senderAddress);
        MapUtils.putNonNull(settings, ApplicationPropertyMailing.FROM_ADDRESS_NAME, senderName);

        if (StringUtils.isBlank(settings.get(ApplicationPropertyMailing.HOST)) || StringUtils.isBlank(senderAddress)
                || StringUtils.isBlank(senderName)) {
            jsonResponse.put(JSON_RESPONSE_FIELD_STATUS, JSON_RESPONSE_STATUS_TYPE_ERROR);
            jsonMessage.append(MessageHelper.getText(request,
                    "installer.step.mail.test.error.empty.fields"));

            LOGGER.info("Sending the test email failed, because required input fields are empty.");
        } else {

            GenericMailMessage testMessage = new GenericMailMessage("installer.step.mail.test.mail", 
                    SessionHandler.instance().getCurrentLocale(request));
            testMessage.addTo(senderAddress, senderName);
            testMessage.setFromAddress(senderAddress);
            testMessage.setFromAddressName(senderName);
            
            if (ServiceLocator.findService(MailSender.class).testSettings(settings, testMessage)) {
                jsonResponse.put(JSON_RESPONSE_FIELD_STATUS, JSON_RESPONSE_STATUS_TYPE_OK);
                jsonMessage.append(MessageHelper.getText(request,
                        "installer.step.mail.test.success", new Object[] { senderAddress }));
            } else {
                jsonResponse.put(JSON_RESPONSE_FIELD_STATUS, JSON_RESPONSE_STATUS_TYPE_ERROR);
                jsonMessage.append(MessageHelper.getText(request,
                        "installer.step.mail.test.error.send.unknown"));
            }
        }

        jsonResponse.put(JSON_RESPONSE_FIELD_MESSAGE, jsonMessage.toString());

        response.getWriter().write(JsonHelper.writeJsonTreeAsString(jsonResponse));

        return;
    }

    /**
     * Ajax method to start the database setup.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void startDatabaseSetup(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // only support POST
        if (!StringUtils.equalsIgnoreCase(request.getMethod(), METHOD_POST)) {
            return;
        }

        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        DatabaseStatusCallback callback = new DatabaseStatusCallback(request.getSession(),
                SessionHandler.instance().getCurrentLocale(request));

        try {
            // start to initialize the database setup
            LOGGER.info("CommunoteInstallerImpl -> start to initialize global database");

            if (CommunoteRuntime.getInstance().getInstaller().initializeDatabase(callback)) {
                jsonResponse.put(JSON_RESPONSE_FIELD_STATUS, JSON_RESPONSE_STATUS_TYPE_OK);
                LOGGER.info("CommunoteInstallerImpl -> finish initialization global database");
            } else {
                jsonResponse.put(JSON_RESPONSE_FIELD_STATUS, JSON_RESPONSE_STATUS_TYPE_ERROR);
            }
        } catch (Exception e) {
            // get setup status
            HttpSession session = request.getSession();
            String status = (String) session
                    .getAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY);

            // preparation failed
            callback.preparingInstallationFailed();

            // if something went wrong and status did not change to FAILED set unknown error
            if (!StringUtils
                    .equals(status, InstallationJSONController.DATABASE_SETUP_STATUS_FAILED)) {
                session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                        InstallationJSONController.DATABASE_SETUP_STATUS_FAILED);
                session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                        MessageHelper.getText(request,
                                "installer.step.database.setup.report.error.unknown"));
            }
            jsonResponse.put(JSON_RESPONSE_FIELD_STATUS, JSON_RESPONSE_STATUS_TYPE_ERROR);
            LOGGER.info("CommunoteInstallerImpl -> error while initializing global database", e);
        }

        response.getWriter().write(JsonHelper.writeJsonTreeAsString(jsonResponse));

        return;
    }
}
