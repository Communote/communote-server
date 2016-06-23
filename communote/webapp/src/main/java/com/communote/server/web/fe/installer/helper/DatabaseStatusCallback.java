package com.communote.server.web.fe.installer.helper;

import java.sql.SQLException;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.config.database.DatabaseConnectionException;
import com.communote.server.api.core.installer.DatabaseInitializationStatusCallback;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.fe.installer.controller.InstallationController;
import com.communote.server.web.fe.installer.controller.InstallationJSONController;

/**
 * The Class <code>DatabaseStatusCallback</code> implements
 * <code>GlobalDatabaseInitializationStatusCallback</code> to define the current state of database
 * initialization.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DatabaseStatusCallback implements DatabaseInitializationStatusCallback {

    /** The Constant LOG. */
    private final static Logger LOG = LoggerFactory.getLogger(InstallationController.class);

    /** the HttpSession */
    private HttpSession session = null;

    /** the current user locale */
    private Locale local = null;

    /**
     * Callback to inform the caller of the global database initialization routine about the current
     * step.
     *
     * @param session
     *            the current http session
     * @param local
     *            the current user local
     */
    public DatabaseStatusCallback(HttpSession session, Locale local) {
        this.session = session;
        this.local = local;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void creatingSchema() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_STARTED);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_PROGRESS_SCHEMA);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                StringUtils.EMPTY);

        LOG.info("SETUP DATABASE -> creating schema");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void creatingSchemaFailed() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_FAILED);

        String message = ResourceBundleManager.instance().getText(
                "installer.step.database.setup.report.error.schema", local);

        session
        .setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                message);

        LOG.info("SETUP DATABASE -> failed to create schema");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void creatingSchemaSucceeded() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_SUCCEEDED);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                StringUtils.EMPTY);

        LOG.info("SETUP DATABASE -> successfully created database schema");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseInitialization() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_STARTED);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_PROGRESS_SCHEMA);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                StringUtils.EMPTY);

        LOG.info("SETUP DATABASE -> database initialization started");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseInitializationAlreadyDone() {
        if (session == null) {
            return;
        }
        String status = (String) session
                .getAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY);

        String progress = (String) session
                .getAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY);

        if (status == null
                || !StringUtils.equals(status,
                        InstallationJSONController.DATABASE_SETUP_STATUS_SUCCEEDED)) {
            session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                    InstallationJSONController.DATABASE_SETUP_STATUS_SUCCEEDED);
        }

        if (progress == null
                || !StringUtils.equals(progress,
                        InstallationJSONController.DATABASE_SETUP_PROGRESS_DATA)) {
            session.setAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY,
                    InstallationJSONController.DATABASE_SETUP_PROGRESS_DATA);
        }

        String message = ResourceBundleManager.instance().getText(
                "installer.step.database.setup.report.success.initial.exists", local);

        session
        .setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                message);

        LOG.info("SETUP DATABASE -> database initialization already done");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseInitializationFinished(boolean success) {
        if (session == null) {
            return;
        }

        String status = (String) session
                .getAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY);

        String progress = (String) session
                .getAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY);

        if (success) {
            if (status == null
                    || !StringUtils.equals(status,
                            InstallationJSONController.DATABASE_SETUP_STATUS_SUCCEEDED)) {
                session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                        InstallationJSONController.DATABASE_SETUP_STATUS_SUCCEEDED);
            }

            if (progress == null
                    || !StringUtils.equals(progress,
                            InstallationJSONController.DATABASE_SETUP_PROGRESS_DATA)) {
                session.setAttribute(
                        InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY,
                        InstallationJSONController.DATABASE_SETUP_PROGRESS_DATA);
            }

            String message = ResourceBundleManager.instance().getText(
                    "installer.step.database.setup.report.success.initial.done", local);

            session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                    message);

            LOG.info("SETUP DATABASE -> database initialization successfully finished");
        } else {
            if (!StringUtils
                    .equals(status, InstallationJSONController.DATABASE_SETUP_STATUS_FAILED)) {
                session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                        InstallationJSONController.DATABASE_SETUP_STATUS_FAILED);
            }

            if (session
                    .getAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY) == null) {
                session.setAttribute(
                        InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY,
                        InstallationJSONController.DATABASE_SETUP_PROGRESS_CONNECTION);

                String message = ResourceBundleManager.instance().getText(
                        "installer.step.database.setup.report.error.unknown", local);

                session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                        message);
            }

            LOG.info("SETUP DATABASE -> database initialization terminated with errors");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void establishingConnection() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_STARTED);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_PROGRESS_CONNECTION);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                StringUtils.EMPTY);

        LOG.info("SETUP DATABASE -> establishing a connection");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void establishingConnectionFailed(DatabaseConnectionException cause) {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_FAILED);

        StringBuilder message = new StringBuilder();
        message.append(ResourceBundleManager.instance().getText(
                "installer.step.database.setup.report.error.connection", local));

        if (cause != null) {
            SQLException ex = null;
            if (cause.getCause() instanceof SQLException) {
                ex = (SQLException) cause.getCause();
            }
            // if status 28000 the user is not associated with a trusted SQL Server connection
            if (ex != null && "28000".equals(ex.getSQLState())) {
                message.append("<br /><br />");
                message.append(ResourceBundleManager.instance().getText(
                        "installer.step.database.setup.report.error.28000", local));
            }

            message.append("<pre>");
            message.append(cause.getMessage());
            message.append("<br /><br />");

            if (ex != null) {
                message.append("<b>SQLException</b><br />");
                message.append("   <em>Error Code</em> = ");
                message.append(ex.getErrorCode());
                message.append("<br />");
                message.append("   <em>SQL State</em>  = ");
                message.append(ex.getSQLState());
            }

            message.append("</pre>");
        }
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY, message
                .toString());

        LOG.info("SETUP DATABASE -> failed to establish a connection");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void establishingConnectionSucceeded() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_SUCCEEDED);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                StringUtils.EMPTY);

        LOG.info("SETUP DATABASE -> successfully established connection");
    }

    /**
     * @return the session
     */
    public HttpSession getSession() {
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preparingInstallation() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_STARTED);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_PROGRESS_PREPARING);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                StringUtils.EMPTY);

        LOG.info("SETUP DATABASE -> preparing the installation");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preparingInstallationFailed() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_FAILED);

        String message = ResourceBundleManager.instance().getText(
                "installer.step.database.setup.report.error.data", local);

        session
        .setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                message);

        LOG.info("SETUP DATABASE -> preparing the installation failed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preparingInstallationSucceeded() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_SUCCEEDED);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                StringUtils.EMPTY);

        LOG.info("SETUP DATABASE -> preparing the installation successfully completed");
    }

    /**
     * @param session
     *            the session to set
     */
    public void setSession(HttpSession session) {
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writingInitialData() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_STARTED);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_PROGRESS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_PROGRESS_DATA);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                StringUtils.EMPTY);

        LOG.info("SETUP DATABASE -> write initial data");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writingInitialDataFailed() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_FAILED);

        String message = ResourceBundleManager.instance().getText(
                "installer.step.database.setup.report.error.data", local);

        session
        .setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                message);

        LOG.info("SETUP DATABASE -> failed to write initial data");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writingInitialDataSucceeded() {
        if (session == null) {
            return;
        }

        session.setAttribute(InstallationJSONController.DATABASE_SETUP_STATUS_SESSION_KEY,
                InstallationJSONController.DATABASE_SETUP_STATUS_SUCCEEDED);
        session.setAttribute(InstallationJSONController.DATABASE_SETUP_MESSAGE_SESSION_KEY,
                StringUtils.EMPTY);

        LOG.info("SETUP DATABASE -> successfully written initial data");
    }
}
