package com.communote.server.web.fe.installer.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.common.util.Base64Utils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.client.InvalidClientIdException;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationInitializationException;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.CoreConfigurationPropertyConstant;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.config.type.CorePropertyDatabase;
import com.communote.server.api.core.installer.CommunoteInstaller;
import com.communote.server.api.core.installer.CommunoteInstallerException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.helper.ValidationHelper;
import com.communote.server.core.mail.KenmeiJavaMailSender;
import com.communote.server.core.user.MasterDataManagement;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.client.ClientValidator;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.installer.forms.InstallerForm;
import com.communote.server.web.fe.installer.validator.InstallerAdminAccountValidator;
import com.communote.server.web.fe.installer.validator.InstallerApplicationValidator;
import com.communote.server.web.fe.installer.validator.InstallerDatabaseValidator;
import com.communote.server.web.fe.installer.validator.InstallerMailValidator;

/**
 * The Class <code>InstallationController</code> controls the workflow for a standalone
 * installation.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InstallationController extends AbstractWizardFormController {

    private static final String STANDARD_SMTP_PORT = "25";

    /** The Constant LOG. */
    private final static Logger LOG = LoggerFactory.getLogger(InstallationController.class);

    /** The finish view. */
    private String finishView = StringUtils.EMPTY;

    /** The error view. */
    private String errorView = StringUtils.EMPTY;

    private void checkForExistingApplication(HttpServletRequest request, InstallerForm form) {
        CommunoteInstaller installer = CommunoteRuntime.getInstance().getInstaller();
        try {
            if (loadExistingApplicationDetails(installer, form)) {
                // client exists, set locale
                initLanguageFromRequest(request, form);
                form.setCurrentProgress(3);
                loadExistingMailSettings(form);
                testConnection(request, form);
                form.setCurrentProgress(4);
                LOG.info("Load initial administrator account.");
                if (loadAdminAccount(installer, form)) {
                    LOG.info("Found initial administrator account.");
                    form.setCurrentProgress(5);
                } else {
                    LOG.info("There is still no initial administrator account.");
                }
            }
        } catch (MessagingException e) {
            // a previous installation was probably canceled in step 3
            // current mail settings are invalid
            clearExistingMailSettings(form);
            LOG.info("No valid SMTP configuration found.");
            LOG.debug(e.getMessage());
        } catch (Exception e) {
            LOG.info("Unknown exception while checking for an existing installation.");
            LOG.debug(e.getMessage(), e);
        }
    }

    /**
     * @param request
     *            the current http request
     * @param command
     *            the command object
     */
    private void checkForExistingInstallation(CommunoteInstaller installer,
            HttpServletRequest request, Object command) {
        // check if data exist from a previous call

        InstallerForm form = (InstallerForm) command;

        LOG.info("Check for an existing installation that was aborted.");
        LOG.info("Search for database settings and test connection.");
        loadExistingDatabaseSettings(installer.getDatabaseSettings(), form);
        if (!installer.canConnectToDatabase()) {
            // a previous installation was probably canceled in step 0
            LOG.info("No database settings found or connection not possible.");
            return;
        }
        LOG.info("Found database settings and can establish a connection.");
        // valid database connection settings
        form.setCurrentProgress(1);

        if (isDatabaseInitialized()) {
            LOG.info("Database is initialized.");
            form.setCurrentProgress(2);
            checkForExistingApplication(request, form);
        } else {
            LOG.info("Database was not initialized.");
        }
    }

    /**
     * @param form
     *            form object with request parameters bound onto it
     */
    private void clearExistingMailSettings(InstallerForm form) {
        form.setSmtpHost(null);
        form.setSmtpPort(null);
        form.setSmtpUser(null);
        form.setSmtpPassword(null);
        form.setSenderName(null);
        form.setSenderAddress(null);
        form.setSupportAddress(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        CommunoteInstaller installer = CommunoteRuntime.getInstance().getInstaller();
        InstallerForm form = new InstallerForm(installer);
        checkForExistingInstallation(installer, request, form);
        return form;
    }

    /**
     * Gets the changed database settings for updating startup properties.
     *
     * @param form
     *            the installer form object
     * @param startupProps
     *            the startup properties
     * @return map containing the changed startup properties
     */
    private Map<CoreConfigurationPropertyConstant, String> getDatabaseSettings(InstallerForm form,
            StartupProperties startupProps) {
        Map<CoreConfigurationPropertyConstant, String> settings = null;
        settings = new HashMap<CoreConfigurationPropertyConstant, String>();

        if (!StringUtils.equals(startupProps.getDatabaseHost(), form.getDatabaseHost())) {
            settings.put(CorePropertyDatabase.DATABASE_HOST, form.getDatabaseHost());
        }
        if (startupProps.getDatabasePort() == null || !StringUtils
                .equals(startupProps.getDatabasePort().toString(), form.getDatabasePort())) {
            settings.put(CorePropertyDatabase.DATABASE_PORT, form.getDatabasePort());
        }
        if (!StringUtils.equals(startupProps.getDatabaseName(), form.getDatabaseName())) {
            settings.put(CorePropertyDatabase.DATABASE_NAME, form.getDatabaseName());
        }
        if (!StringUtils.equals(startupProps.getDatabaseUserName(), form.getDatabaseUser())) {
            settings.put(CorePropertyDatabase.DATABASE_USER_NAME, form.getDatabaseUser());
        }
        if (!StringUtils.equals(startupProps.getDatabaseUserPassword(),
                form.getDatabasePassword())) {
            settings.put(CorePropertyDatabase.DATABASE_USER_PASSWORD, form.getDatabasePassword());
        }
        settings.put(CorePropertyDatabase.DATABASE_SPECIFIC_FULL_TEXT_SEARCH,
                Boolean.TRUE.toString());
        return settings;
    }

    /**
     * @return the errorView
     */
    public String getErrorView() {
        return errorView;
    }

    /**
     * @return the finishView
     */
    public String getFinishView() {
        return finishView;
    }

    /**
     * Saves the administrator account.
     *
     * @param request
     *            the http servlet request
     * @param command
     *            form object with the current wizard state
     * @param errors
     *            validation errors holder
     * @throws ConfigurationUpdateException
     *             thrown if the update of configuration settings failed
     */
    private void handleAdminAccount(HttpServletRequest request, Object command, Errors errors)
            throws ConfigurationUpdateException {
        // only save if no errors exists and moving forward
        int currentPage = getCurrentPage(request);
        if (errors.hasErrors() || getTargetPage(request, currentPage) < currentPage) {
            return;
        }

        InstallerForm form = (InstallerForm) command;

        UserVO userVo = new UserVO();
        userVo.setEmail(form.getUserEmail());
        userVo.setFirstName(form.getUserFirstName());
        userVo.setLastName(form.getUserLastName());

        userVo.setLanguage(new Locale(form.getUserLanguageCode()));
        userVo.setAlias(form.getUserAlias());

        userVo.setPassword(form.getUserPassword());

        String webProtocol = request.getParameter("webProtocol");
        String webPort = request.getParameter("webPort");
        String webHost = request.getParameter("webHost");

        Map<ApplicationProperty, String> params = new HashMap<ApplicationProperty, String>();
        params.put(ApplicationProperty.WEB_SERVER_HOST_NAME, webHost);

        if (StringUtils.isNotBlank(webProtocol) && StringUtils.equals(webProtocol, "https")) {
            params.put(ApplicationProperty.WEB_HTTPS_PORT, webPort);
            params.put(ApplicationProperty.WEB_HTTPS_SUPPORTED, Boolean.TRUE.toString());
        } else {
            params.put(ApplicationProperty.WEB_HTTP_PORT, webPort);
            params.put(ApplicationProperty.WEB_HTTPS_SUPPORTED, Boolean.FALSE.toString());
        }

        try {
            CommunoteRuntime.getInstance().getInstaller().initializeCommunoteAccount(userVo,
                    params);
        } catch (EmailValidationException e) {
            errors.rejectValue("userEmail", "error.email.not.valid");
        } catch (ConfigurationInitializationException e) {
            // TODO huh, why is this field the cause?
            errors.reject("error.email.not.valid");
        } catch (Exception e) {
            errors.reject("installer.error.global.unknown", new Object[] { e.getMessage() }, null);
        }

    }

    /**
     * Saves the application details.
     *
     * @param request
     *            the http servlet request
     * @param command
     *            form object with the current wizard state
     * @param errors
     *            validation errors holder
     * @throws ConfigurationUpdateException
     *             thrown if the update of configuration settings failed
     */
    private void handleApplicationDetails(HttpServletRequest request, Object command, Errors errors)
            throws ConfigurationUpdateException {
        // only save if no errors exists and moving forward
        int currentPage = getCurrentPage(request);
        if (errors.hasErrors() || getTargetPage(request, currentPage) <= currentPage) {
            return;
        }

        InstallerForm form = (InstallerForm) command;

        try {
            CommunoteRuntime.getInstance().getInstaller()
                    .createCommunoteAccount(form.getAccountName(), form.getAccountTimeZoneId());
            // client is available, can set the locale
            initLanguageFromRequest(request, form);
        } catch (InvalidClientIdException e) {
            Object[] args = new Object[] {
                    ValidationHelper.getRegexForDisplay(ClientValidator.CLIENT_ID_REGEX_PATTERN),
                    ClientValidator.CLIENT_ID_MIN_LENGTH };
            errors.reject("installer.step.application.error.illegalid", args, null);
        } catch (CommunoteInstallerException e) {
            errors.reject("installer.error.global.unknown", new Object[] { e.getMessage() }, null);
            LOG.error("Unkown error during application initialization", e);
        }
    }

    /**
     * Saves the database settings.
     *
     * @param request
     *            the http servlet request
     * @param command
     *            form object with the current wizard state
     * @param errors
     *            validation errors holder
     */
    private void handleDatabaseSettings(HttpServletRequest request, Object command, Errors errors) {
        // only save if no errors exists and moving forward
        int currentPage = getCurrentPage(request);
        if (errors.hasErrors() || getTargetPage(request, currentPage) <= currentPage) {
            return;
        }

        InstallerForm form = (InstallerForm) command;
        CommunoteInstaller installer = CommunoteRuntime.getInstance().getInstaller();
        StartupProperties startupProps = installer.getDatabaseSettings();

        Map<CoreConfigurationPropertyConstant, String> settings = getDatabaseSettings(form,
                startupProps);

        if (settings.size() > 0) {
            try {
                startupProps = installer.updateDatabaseSettings(form.getDatabaseType(), settings);

                String databaseUrl = startupProps.getDatabaseUrl();
                form.setDatabaseUrl(databaseUrl);

                // reset progress, because database schema needs to be recreated
                form.setCurrentProgress(0);

            } catch (ConfigurationUpdateException e) {
                String errorCode = MessageHelper.getText(request, "installer.error.save.settings");
                errors.reject(errorCode);
            }
        } else {
            // do not create the database again -> go to step 4 (_target3)
            // isDatabaseInitialized();
        }
    }

    /**
     * Saves the mail out settings.
     *
     * @param request
     *            the http servlet request
     * @param command
     *            form object with the current wizard state
     * @param errors
     *            validation errors holder
     * @throws EncryptionException
     *             Exception.
     */
    private void handleMailSettings(HttpServletRequest request, Object command, Errors errors)
            throws EncryptionException {
        // only save if no errors exists and moving forward
        int currentPage = getCurrentPage(request);
        if (errors.hasErrors() || getTargetPage(request, currentPage) <= currentPage) {
            return;
        }

        InstallerForm form = (InstallerForm) command;
        ConfigurationManager conf = CommunoteRuntime.getInstance().getConfigurationManager();
        ApplicationConfigurationProperties applicationProps = conf
                .getApplicationConfigurationProperties();

        Map<ApplicationConfigurationPropertyConstant, String> settings = setMailSettings(form,
                applicationProps);

        if (settings.size() > 0) {
            try {
                // update configuration
                conf.updateApplicationConfigurationProperties(settings);
            } catch (ConfigurationUpdateException e) {
                LOG.error("Storing the new mailing settings failed", e);
                String errorCode = MessageHelper.getText(request, "installer.error.save.settings");
                if (e.getMessageKey() != null) {
                    errorCode = MessageHelper.getText(request, e.getMessageKey());
                }
                errors.reject(errorCode);
            }
        }
        if (!errors.hasErrors()) {
            // support address
            if (!StringUtils.equals(conf.getClientConfigurationProperties()
                    .getProperty(ClientProperty.SUPPORT_EMAIL_ADDRESS), form.getSupportAddress())) {
                conf.updateClientConfigurationProperty(ClientProperty.SUPPORT_EMAIL_ADDRESS,
                        form.getSupportAddress());
            }

            try {
                // always test configuration
                testConnection(request, form);
            } catch (MessagingException e) {
                LOG.error("While trying to establish a connection an error occurred!", e);
                String errorCode = MessageHelper.getText(request,
                        "installer.step.mail.test.connection.error");
                errors.reject(errorCode);
            }
        }
    }

    /**
     * Set the locale of the admin user of the form to the locale of the current request. If the
     * locale of the request is not a registered locale, English will be used.
     *
     * Note: should not be called before the services are available
     *
     * Note 2: the languages provided by the localization plugins are not yet available because
     * these plugins are registered after the the last step
     *
     * @param request
     *            the current request
     * @param form
     *            the form
     */
    private void initLanguageFromRequest(HttpServletRequest request, InstallerForm form) {
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        if (!ServiceLocator.findService(MasterDataManagement.class).isAvailableLanguage(locale)) {
            locale = Locale.ENGLISH;
        }
        form.getUser().setLanguage(locale);
    }

    /**
     * Returns whether the global database is initialized, which is the schema exists.
     *
     * @return {@code true} if the database is initialized
     */
    private boolean isDatabaseInitialized() {
        return CommunoteRuntime.getInstance().getInstaller().isDatabaseInitialized();
    }

    /**
     * Retrieves the client admin if there is one and prepares the form with that data.
     *
     * @param installer
     *            the Communote installer
     * @param form
     *            form object with request parameters bound onto it
     * @return true if the manager could be loaded, false otherwise
     */
    private boolean loadAdminAccount(CommunoteInstaller installer, InstallerForm form) {
        User manager = installer.getAdminAccount();

        if (manager != null) {

            form.setUserAlias(manager.getAlias());
            form.setUserEmail(manager.getEmail());
            form.setUserFirstName(manager.getProfile().getFirstName());
            form.setUserLastName(manager.getProfile().getLastName());
            form.setUserLanguageCode(manager.getLanguageCode());
            return true;
        }

        return false;
    }

    /**
     * Loads application details, if already exist.
     *
     * @param form
     *            form object with request parameters bound onto it
     */
    private boolean loadExistingApplicationDetails(CommunoteInstaller installer,
            InstallerForm form) {
        LOG.info("Search for some application details (application name and time zone).");
        ClientTO client = installer.getCommunoteAccount();
        if (client != null) {
            form.setAccountName(client.getName());
            form.setAccountTimeZoneId(CommunoteRuntime.getInstance().getConfigurationManager()
                    .getClientConfigurationProperties().getClientTimeZoneId());
            LOG.info("Found application details.");
            return true;
        }
        LOG.info("No application details found.");
        return false;
    }

    /**
     * Loads database connection settings if defined.
     *
     * @param startupProps
     *            the startup properties
     * @param form
     *            form object with request parameters bound onto it
     */
    private void loadExistingDatabaseSettings(StartupProperties startupProps, InstallerForm form) {
        form.setDatabaseType(CommunoteRuntime.getInstance().getInstaller().getDatabaseType());

        form.setDatabaseHost(startupProps.getDatabaseHost());
        if (startupProps.getDatabasePort() != null) {
            form.setDatabasePort(startupProps.getDatabasePort().toString());
        }
        form.setDatabaseName(startupProps.getDatabaseName());
        form.setDatabaseUser(startupProps.getDatabaseUserName());
        form.setDatabasePassword(startupProps.getDatabaseUserPassword());
        form.setDatabaseUrl(startupProps.getDatabaseUrl());
    }

    /**
     * @param form
     *            form object with request parameters bound onto it
     * @throws EncryptionException
     *             Exception.
     */
    private void loadExistingMailSettings(InstallerForm form) throws EncryptionException {
        LOG.info("Load SMTP configuration.");
        ConfigurationManager conf = CommunoteRuntime.getInstance().getConfigurationManager();
        ApplicationConfigurationProperties applicationProps = conf
                .getApplicationConfigurationProperties();

        form.setSmtpHost(applicationProps.getProperty(ApplicationPropertyMailing.HOST));
        form.setSmtpPort(applicationProps.getProperty(ApplicationPropertyMailing.PORT));
        form.setSmtpStartTls(
                applicationProps.getProperty(ApplicationPropertyMailing.USE_STARTTLS, false));

        form.setSmtpUser(applicationProps.getProperty(ApplicationPropertyMailing.LOGIN));
        String decryptedPassword = EncryptionUtils.decrypt(
                applicationProps.getProperty(ApplicationPropertyMailing.PASSWORD),
                ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
        form.setSmtpPassword(decryptedPassword);

        form.setSenderName(
                applicationProps.getProperty(ApplicationPropertyMailing.FROM_ADDRESS_NAME));
        form.setSenderAddress(
                applicationProps.getProperty(ApplicationPropertyMailing.FROM_ADDRESS));
        form.setSupportAddress(conf.getClientConfigurationProperties()
                .getProperty(ClientProperty.SUPPORT_EMAIL_ADDRESS, null));
    }

    @Override
    protected void postProcessPage(HttpServletRequest request, Object command, Errors errors,
            int page) throws Exception {

        switch (page) {
        case 0:
            break;
        case 1:
            // save database settings
            handleDatabaseSettings(request, command, errors);
            break;
        case 2:
            resetDatabaseSetupStatus(request);
            // database config might have changed, so check for an existing installed application
            // and load its data. If user switched back to DB setup after entering application
            // details his previous input will be preserved if there is no existing data in the
            // database.
            checkForExistingApplication(request, (InstallerForm) command);
            break;
        case 3:
            handleApplicationDetails(request, command, errors);
            break;
        case 4:
            handleMailSettings(request, command, errors);
            break;
        case 5:
            // see processFinish()
            break;
        default:
            break;
        }

        // set some front end used values
        setProgressStatus(request, command, errors, page);

    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#processFinish(javax.servlet
     *      .http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object,
     *      org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView processFinish(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {

        ModelAndView mav = null;

        // user account (the first admin)
        handleAdminAccount(request, command, errors);

        if (errors.hasErrors()) {
            // return to last installation step
            mav = showPage(request, errors, getCurrentPage(request));

        } else {
            // show finish view
            mav = showForm(request, errors, getFinishView());
        }

        // set some front end used values
        setProgressStatus(request, command, errors, getCurrentPage(request));

        return mav;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#referenceData(javax.servlet
     *      .http.HttpServletRequest, Object, org.springframework.validation.Errors, int)
     */
    @Override
    protected Map<?, ?> referenceData(HttpServletRequest request, Object command, Errors errors,
            int page) throws Exception {
        if (!isFormSubmission(request)) {
            // TODO is this really necessary to call this method again (see formBackingObject). On
            // first request/page reload it is called twice!
            checkForExistingInstallation(CommunoteRuntime.getInstance().getInstaller(), request,
                    command);
        }
        return super.referenceData(request, command, errors, page);
    }

    /**
     * Removes all previously generated session attributes
     *
     * @param request
     *            the http servlet request
     */
    private void resetDatabaseSetupStatus(HttpServletRequest request) {

        // remove all previous generated session attributes
        HttpSession session = request.getSession();

        session.removeAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY);
        session.removeAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY);
        session.removeAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY);
    }

    /**
     * @param errorView
     *            the errorView to set
     */
    public void setErrorView(String errorView) {
        this.errorView = errorView;
    }

    /**
     * @param finishView
     *            the finishView to set
     */
    public void setFinishView(String finishView) {
        this.finishView = finishView;
    }

    /**
     * Sets changed mail settings for updating application properties.
     *
     * @param form
     *            the installer form object
     * @param applicationProps
     *            the application properties
     * @return map containing the changed mail properties
     * @throws EncryptionException
     *             Exception.
     */
    private Map<ApplicationConfigurationPropertyConstant, String> setMailSettings(
            InstallerForm form, ApplicationConfigurationProperties applicationProps)
            throws EncryptionException {
        Map<ApplicationConfigurationPropertyConstant, String> settings = null;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();

        if (!StringUtils.equals(applicationProps.getProperty(ApplicationPropertyMailing.HOST),
                form.getSmtpHost())) {
            settings.put(ApplicationPropertyMailing.HOST, form.getSmtpHost());
        }
        if (!StringUtils.equals(applicationProps.getProperty(ApplicationPropertyMailing.PORT),
                form.getSmtpPort())) {
            String port;
            if (StringUtils.isBlank(form.getSmtpPort())) {
                port = STANDARD_SMTP_PORT;
            } else {
                port = form.getSmtpPort();
            }
            settings.put(ApplicationPropertyMailing.PORT, port);
        }
        if (applicationProps.getProperty(ApplicationPropertyMailing.USE_STARTTLS, false) != form
                .isSmtpStartTls()) {
            settings.put(ApplicationPropertyMailing.USE_STARTTLS,
                    Boolean.toString(form.isSmtpStartTls()));
        }
        if (!StringUtils.equals(applicationProps.getProperty(ApplicationPropertyMailing.LOGIN),
                form.getSmtpUser())) {
            settings.put(ApplicationPropertyMailing.LOGIN, form.getSmtpUser());
        }

        String newPassword = form.getSmtpPassword();
        String oldPassword = applicationProps.getProperty(ApplicationPropertyMailing.PASSWORD);
        String iid = ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue();
        if (StringUtils.isBlank(oldPassword)) {
            newPassword = EncryptionUtils.encrypt(form.getSmtpPassword(), iid);
        } else {
            String salt = EncryptionUtils.getSalt(oldPassword);
            newPassword = EncryptionUtils.encrypt(form.getSmtpPassword(), iid,
                    Base64Utils.decode(salt));
        }
        if (!StringUtils.equals(oldPassword, newPassword)) {
            settings.put(ApplicationPropertyMailing.PASSWORD, newPassword);
        }

        if (!StringUtils.equals(
                applicationProps.getProperty(ApplicationPropertyMailing.FROM_ADDRESS_NAME),
                form.getSenderName())) {
            settings.put(ApplicationPropertyMailing.FROM_ADDRESS_NAME, form.getSenderName());
        }
        if (!StringUtils.equals(
                applicationProps.getProperty(ApplicationPropertyMailing.FROM_ADDRESS),
                form.getSenderAddress())) {
            settings.put(ApplicationPropertyMailing.FROM_ADDRESS, form.getSenderAddress());
        }
        return settings;
    }

    /**
     * Set some progress information to use to build the front end progress bar.
     *
     * @param request
     *            the current HTTP request
     * @param command
     *            form object with request parameters bound onto it
     * @param errors
     *            validation errors holder
     * @param page
     *            number of page to post-process
     */
    private void setProgressStatus(HttpServletRequest request, Object command, Errors errors,
            int page) {
        InstallerForm form = (InstallerForm) command;

        int currentPage = page;
        int targetPage = getTargetPage(request, command, errors, currentPage);

        // see AbstractWizardFormController.processFormSubmission()
        if (targetPage != currentPage) {
            if (!errors.hasErrors() || (isAllowDirtyBack() && targetPage < currentPage)
                    || (isAllowDirtyForward() && targetPage > currentPage)) {
                // allowed to go to target page.
                currentPage = targetPage;
            }
        }

        form.setCurrentPage(currentPage);

        if (!errors.hasErrors() && isFinishRequest(request)) {
            form.setCurrentPage(currentPage + 1);
            form.setFinishRequest(true);
        }

        // set progress state only if this step is successful
        // and only if the user moves forward
        if (!errors.hasErrors() && form.getCurrentProgress() < page && targetPage > page) {
            form.setCurrentProgress(page);
        }
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
    // TODO refactor to use KenmeiJavaMailSender which should provide a test and a sendTestEmail
    // method that should be used in all places where the mail connection is testet
    // (InstallationJsonController, MailInController). Moreover setting of mail settings should also
    // be done in that class, thus MailManagementImpl must be refactored too.
    private void testConnection(HttpServletRequest request, InstallerForm form)
            throws MessagingException {
        LOG.info("Test connetion to the SMTP server.");
        Properties mailingProperties = new Properties();
        String host = form.getSmtpHost();
        int port = NumberUtils.toInt(form.getSmtpPort(), 25);
        String username = null;
        String password = null;
        if (StringUtils.isNotBlank(form.getSmtpUser())) {
            mailingProperties.setProperty("mail.smtp.auth", "true");
            username = form.getSmtpUser();
            // the password can be an empty string but must be null if the user is not set because
            // the mail api has some fall back mechanisms to determine a user name (e.g. system
            // property user.name) which finally would result in trying an authentication if the
            // password is not null
            password = form.getSmtpPassword();
        }
        mailingProperties.setProperty("mail.smtp.starttls.enable",
                Boolean.toString(form.isSmtpStartTls()));

        KenmeiJavaMailSender mailSender = new KenmeiJavaMailSender(host, port, username, password,
                mailingProperties);
        if (!mailSender.canConnect()) {
            throw new MessagingException("It was not possible to connect to the service.");
        }
        LOG.info("Can establish a connection to the SMTP server.");
    }

    /**
     * Validate administration account form.
     *
     * @param command
     *            the command
     * @param errors
     *            the errors
     * @param finish
     *            the finish
     */
    private void validateAdminAccountSettings(Object command, Errors errors, boolean finish) {
        InstallerAdminAccountValidator validator = new InstallerAdminAccountValidator();
        InstallerForm form = (InstallerForm) command;
        validator.validate(form, errors);
    }

    /**
     * Validate application settings form.
     *
     * @param command
     *            the command
     * @param errors
     *            the errors
     * @param finish
     *            the finish
     */
    private void validateApplicationSettings(Object command, Errors errors, boolean finish) {
        InstallerApplicationValidator validator = new InstallerApplicationValidator();
        InstallerForm form = (InstallerForm) command;
        validator.validate(form, errors);
    }

    /**
     * Validate database selection form.
     *
     * @param command
     *            the command
     * @param errors
     *            the errors
     * @param finish
     *            the finish
     */
    private void validateDatabaseSelection(Object command, Errors errors, boolean finish) {
        InstallerDatabaseValidator validator = new InstallerDatabaseValidator();
        InstallerForm form = (InstallerForm) command;
        validator.validate(form, errors);
    }

    /**
     * Validate mail setting form.
     *
     * @param command
     *            the command
     * @param errors
     *            the errors
     * @param finish
     *            the finish
     */
    private void validateMailSettings(Object command, Errors errors, boolean finish) {
        InstallerMailValidator validator = new InstallerMailValidator();
        InstallerForm form = (InstallerForm) command;
        validator.validate(form, errors);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#validatePage(Object ,
     *      org.springframework.validation.Errors, int, boolean)
     */
    @Override
    protected void validatePage(Object command, Errors errors, int page, boolean finish) {
        switch (page) {
        case 0:
            break;
        case 1:
            validateDatabaseSelection(command, errors, finish);
            break;
        case 2:
            // nothing to do
            break;
        case 3:
            validateApplicationSettings(command, errors, finish);
            break;
        case 4:
            validateMailSettings(command, errors, finish);
            break;
        case 5:
            validateAdminAccountSettings(command, errors, finish);
            break;
        case 6:
            // nothing to do
            break;
        default:
            LOG.warn("no validator found for page " + page);
            break;
        }
    }
}