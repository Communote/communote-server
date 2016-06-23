package com.communote.plugins.mq.provider.activemq.adminconsole;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleContext;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.plugins.core.services.PluginPropertyServiceException;
import com.communote.plugins.core.views.AdministrationViewController;
import com.communote.plugins.core.views.ViewControllerException;
import com.communote.plugins.core.views.annotations.Page;
import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.plugins.mq.provider.activemq.ConfigurableBroker;
import com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO;
import com.communote.plugins.mq.provider.activemq.user.MQUsersDAO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.web.commons.MessageHelper;

/**
 * The Class AdminController.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate(name = "ActiveMQAdminController")
@UrlMapping(value = "/*/admin/application/mq/settings")
@Page(menu = "extensions", submenu = "mq", jsCategories = { "communote-core",
        "admin" }, menuMessageKey = "administration.title.submenu.mq")
public class AdminController extends AdministrationViewController implements
        Controller {

    /** The Constant BROKER_ACTIVE. */
    private final static String BROKER_ACTIVE = "broker_active";

    /** The Constant MQ_USERS. */
    private final static String MQ_USERS = "mq_users";

    /** The Constant MQ_CONNECTORS. */
    private final static String MQ_CONNECTORS = "mq_connectors";

    /** The data directory constant */
    private final static String MQ_DATA_DIRECTORY = "mq_data_directory";

    @Requires
    private ConfigurableBroker broker;

    @Requires
    private MQUsersDAO usersDao;

    /** The settings dao. */
    @Requires
    private MQSettingsDAO settingsDao;

    private final Pattern userNamePattern = Pattern.compile("[\\w]+");

    /**
     * @param bundleContext
     *            OSGi bundle context
     */
    public AdminController(BundleContext bundleContext) {
        super(bundleContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
        try {
            model.put(MQ_DATA_DIRECTORY, settingsDao.getDataDirectory());
            model.put(BROKER_ACTIVE, broker.isActive());
            model.put(MQ_USERS, usersDao.getMQUsers());
            model.put(MQ_CONNECTORS, settingsDao.getBrokerConnectorURLsWithVM());
            model.put("jmxPort", settingsDao.getJmxPort());
            model.put("isEnableJmx", settingsDao.isJmxRemoteEnabled());
            model.put("tcpPort", settingsDao.getTcpPort());
            model.put("sslPort", settingsDao.getSSLPort());
            model.put("isEnableTCP", settingsDao.isEnableTCP());
            model.put("isEnableSSL", settingsDao.isEnableSSL());
            model.put("isForceSSL", settingsDao.isForceSSL());
            model.put("isForceSSLClientAuthentication",
                    settingsDao.isForceSSLClientAuthentication());
        } catch (PluginPropertyServiceException e) {
            throw new ViewControllerException(500, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doPost(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> model)
            throws ViewControllerException {
        String action = request.getParameter("action");
        if ("broker-activate".equals(action)) {
            try {
                broker.activateBroker();
            } catch (Exception e) {
                setErrorMessage(request, e);
            }
        } else if ("broker-deactivate".equals(action)) {
            try {
                broker.deactivateBroker();
            } catch (Exception e) {
                setErrorMessage(request, e);
            }
        } else if ("broker-user-delete".equals(action)) {
            try {
                usersDao.removeUser(request.getParameter("removeUser").trim());
                String message = MessageHelper
                        .getText(request,
                                "plugins.mq.provider.configuration.user.remove.success");
                MessageHelper.saveMessage(request, message);
            } catch (PluginPropertyServiceException e) {
                throw new ViewControllerException(400, e.getMessage(), e);
            } catch (AuthorizationException e) {
                throw new ViewControllerException(403, "You are not allowed to add a user", e);
            }
        } else if ("broker-user-add".equals(action)) {
            processUserAddition(request);
        } else if ("update-settings".equals(action)) {
            try {
                if (processDataDirectorySetting(request)) {
                    settingsDao.setEnableJmxRemote(BooleanUtils.toBoolean(request.getParameter("enableJmx")));
                    settingsDao.setEnableTCP(BooleanUtils.toBoolean(request.getParameter("enableTCP")));
                    processSslSettings(request);
    
                    MessageHelper.saveMessageFromKey(request,
                            "plugins.mq.provider.configuration.settings.success");
                    MessageHelper.saveMessageFromKey(request,
                            "plugins.mq.provider.configuration.settings.success.hint",
                            MessageHelper.WARNING_MESSAGES_KEY);
                }
            } catch (AuthorizationException e) {
                throw new ViewControllerException(403, "You are not allowed to update the settings", e);
            }
        }

        doGet(request, response, model);
    }

    @Override
    public String getContentTemplate() {
        return "/vm/admin-console-content.html.vm";
    }

    /**
     * sets new value of the data directory, checking its syntactic validity
     * 
     * @param request
     *            the request object
     * @return true if the update of the data directory has been performed successfully
     */
    private boolean processDataDirectorySetting(HttpServletRequest request) throws AuthorizationException {
        String dataDirectoryPath = request.getParameter("dataDirectory").trim();
        boolean updateSuccess = false;
        if (dataDirectoryPath != null
                && dataDirectoryPath.equals(settingsDao.getDataDirectory())) {
            return true;
        }
        File dataDirectory = new File(dataDirectoryPath);
        if (dataDirectory.isDirectory() || dataDirectory.mkdirs()) {
            settingsDao.setDataDirectory(dataDirectory.getAbsolutePath());
            updateSuccess = true;
        } else {
            MessageHelper.saveErrorMessageFromKey(request,
                    "plugins.mq.provider.configuration.answer.data.dir.invalid");
            updateSuccess = false;
        }
        return updateSuccess;
    }

    /**
     * sets new value of all SSL options, checking its validity
     * 
     * @param request
     *            the request object
     */
    private void processSslSettings(HttpServletRequest request) throws AuthorizationException {
        settingsDao.setEnableSSL(BooleanUtils.toBoolean(request.getParameter("enableSSL")));
        if (settingsDao.isEnableSSL()) {
            settingsDao.setForceSSL(BooleanUtils.toBoolean(request.getParameter("forceSSL")));
            settingsDao.setForceSSLClientAuthentication(BooleanUtils.toBoolean(request
                    .getParameter("forceSSLClientAuthentication")));
        } else {
            settingsDao.setForceSSL(false);
            settingsDao.setForceSSLClientAuthentication(false);
        }
    }

    /**
     * processes addition of the user to MQ, checking its validity
     * 
     * @param request
     *            the request object
     * @throws ViewControllerException
     *             exception
     */
    private void processUserAddition(HttpServletRequest request)
            throws ViewControllerException {
        // XSS avoiding -> since the user name is returned to the web page,
        // it might be used as "persistent XSS" hole

        String userName = request.getParameter("userName").trim();
        String password = request.getParameter("password");

        Matcher matcher = userNamePattern.matcher(userName);
        if (matcher.matches()) {
            try {
                usersDao.addUser(userName, password);
                String message = MessageHelper.getText(request,
                        "plugins.mq.provider.configuration.user.add.success");
                MessageHelper.saveMessage(request, message);
            } catch (PluginPropertyServiceException e) {
                throw new ViewControllerException(400, e.getMessage(), e);
            } catch (AuthorizationException e) {
                throw new ViewControllerException(403, "You are not allowed to add a user", e);
            }
        } else {
            String message = MessageHelper
                    .getText(request,
                            "plugins.mq.provider.configuration.answer.user.name.invalid");
            MessageHelper.saveErrorMessage(request, message);
        }
    }

    /**
     * Set a default error message with the exception message as argument into the request
     * 
     * @param request
     *            the request
     * @param e
     *            the exception to use the message from
     */
    private void setErrorMessage(HttpServletRequest request, Exception e) {
        MessageHelper.saveErrorMessage(request,
                MessageHelper.getText(request, "error.application.exception.detailed",
                        new Object[] { e.getMessage() }));
    }

}
