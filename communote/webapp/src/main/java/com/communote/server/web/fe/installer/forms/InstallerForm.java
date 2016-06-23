package com.communote.server.web.fe.installer.forms;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import com.communote.server.api.core.config.database.DatabaseType;
import com.communote.server.api.core.installer.CommunoteInstaller;
import com.communote.server.api.core.user.UserVO;

/**
 * The form for installing a new application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InstallerForm implements Serializable {

    private static final long serialVersionUID = 8645477429388744008L;

    /** the type of the database management system */
    private DatabaseType databaseType;

    /** the database server to connect to */
    private String databaseHost;

    /** the port number of the server to connect to */
    private String databasePort;

    /** the name of the database */
    private String databaseName;

    /** the user name to log in with */
    private String databaseUser;

    /** the password to log in with */
    private String databasePassword;

    /** the URL to connect to the database */
    private String databaseUrl;

    /** the SMTP server to connect to */
    private String smtpHost;

    /** the SMTP port number of the server to connect to */
    private String smtpPort;

    /** whether to use the command STARTTLS for smtp */
    private boolean smtpStartTls;

    /** the user name to log in with */
    private String smtpUser;

    /** the password to log in with */
    private String smtpPassword;

    /** the name of the sender in cases of system e-mails */
    private String senderName;

    /** the sender e-mail address */
    private String senderAddress;

    /** the displayed support e-email */
    private String supportAddress;

    /** the title for the account name */
    private String accountName;

    /** the time zone id of the account */
    private String accountTimeZoneId;

    /** the user */
    private UserVO user = new UserVO();

    /** the login password */
    private String userPassword;

    /** the password confirmation */
    private String userPasswordConfirmation;

    /** the current progress of the installation */
    private int currentProgress = 0;

    /** the current request page */
    private int currentPage = 0;

    /** the inform the front end about finish request */
    private boolean finishRequest = false;

    private final CommunoteInstaller installer;

    public InstallerForm(CommunoteInstaller installer) {
        this.installer = installer;
    }

    /**
     * @return the accountName
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * @return the accountTimeZoneId
     */
    public String getAccountTimeZoneId() {
        return accountTimeZoneId;
    }

    /**
     * @return the currentPage
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * @return the currentProgress
     */
    public int getCurrentProgress() {
        return currentProgress;
    }

    /**
     * @return the databaseHost
     */
    public String getDatabaseHost() {
        return databaseHost;
    }

    /**
     * @return the databaseName
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @return the databasePassword
     */
    public String getDatabasePassword() {
        return databasePassword;
    }

    /**
     * @return the databasePort
     */
    public String getDatabasePort() {
        return databasePort;
    }

    /**
     * @return the databaseType
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public String getDatabaseTypeIdentifier() {
        if (this.databaseType != null) {
            return this.databaseType.getIdentifier();
        }
        return null;
    }

    /**
     * @return the databaseUrl
     */
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    /**
     * @return the databaseUser
     */
    public String getDatabaseUser() {
        return databaseUser;
    }

    /**
     * @return the senderAddress
     */
    public String getSenderAddress() {
        return senderAddress;
    }

    /**
     * @return the senderName
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * @return the smtpHost
     */
    public String getSmtpHost() {
        return smtpHost;
    }

    /**
     * @return the smtpPassword
     */
    public String getSmtpPassword() {
        return smtpPassword;
    }

    /**
     * @return the smtpPort
     */
    public String getSmtpPort() {
        return smtpPort;
    }

    /**
     * @return the smtpUser
     */
    public String getSmtpUser() {
        return smtpUser;
    }

    /**
     * @return the supportAddress
     */
    public String getSupportAddress() {
        return supportAddress;
    }

    public List<DatabaseType> getSupportedDatabaseTypes() {
        return installer.getSupportedDatabaseTypes();
    }

    /**
     * @return the user
     */
    public UserVO getUser() {
        return user;
    }

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    public String getUserAlias() {
        return user.getAlias();
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getUserEmail() {
        return user.getEmail();
    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String getUserFirstName() {
        return user.getFirstName();
    }

    /**
     * Gets the language code.
     *
     * @return the languageCode
     */
    public String getUserLanguageCode() {
        String code = null;
        if (user.getLanguage() != null) {
            code = user.getLanguage().getLanguage();
        }
        return code;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getUserLastName() {
        return user.getLastName();
    }

    /**
     * @return the userPassword
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * @return the userPasswordConfirmation
     */
    public String getUserPasswordConfirmation() {
        return userPasswordConfirmation;
    }

    /**
     * @return the finishRequest
     */
    public boolean isFinishRequest() {
        return finishRequest;
    }

    /**
     * @return the smtpStartTls
     */
    public boolean isSmtpStartTls() {
        return smtpStartTls;
    }

    /**
     * @param accountName
     *            the accountName to set
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName == null ? null : accountName.trim();
    }

    /**
     * @param accountTimeZoneId
     *            the accountTimeZoneId to set
     */
    public void setAccountTimeZoneId(String accountTimeZoneId) {
        this.accountTimeZoneId = accountTimeZoneId == null ? null : accountTimeZoneId.trim();
    }

    /**
     * @param currentPage
     *            the currentPage to set
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * @param currentProgress
     *            the currentProgress to set
     */
    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    /**
     * @param databaseHost
     *            the databaseHost to set
     */
    public void setDatabaseHost(String databaseHost) {
        if (databaseHost != null) {
            databaseHost = databaseHost.trim();
        }
        this.databaseHost = databaseHost;
    }

    /**
     * @param databaseName
     *            the databaseName to set
     */
    public void setDatabaseName(String databaseName) {
        if (databaseName != null) {
            databaseName = databaseName.trim();
        }
        this.databaseName = databaseName;
    }

    /**
     * @param databasePassword
     *            the databasePassword to set
     */
    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword == null ? null : databasePassword.trim();
    }

    /**
     * @param databasePort
     *            the databasePort to set
     */
    public void setDatabasePort(String databasePort) {
        if (databasePort != null) {
            databasePort = databasePort.trim();
        }
        this.databasePort = databasePort;
    }

    /**
     * @param databaseType
     *            the databaseType to set
     */
    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public void setDatabaseTypeIdentifier(String identifier) {
        this.setDatabaseType(installer.getSupportedDatabaseType(identifier));
    }

    /**
     * @param databaseUrl
     *            the databaseUrl to set
     */
    public void setDatabaseUrl(String databaseUrl) {
        if (databaseUrl != null) {
            databaseUrl = databaseUrl.trim();
        }
        this.databaseUrl = databaseUrl;
    }

    /**
     * @param databaseUser
     *            the databaseUser to set
     */
    public void setDatabaseUser(String databaseUser) {
        if (databaseUser != null) {
            databaseUser = databaseUser.trim();
        }
        this.databaseUser = databaseUser;
    }

    /**
     * @param finishRequest
     *            the finishRequest to set
     */
    public void setFinishRequest(boolean finishRequest) {
        this.finishRequest = finishRequest;
    }

    /**
     * @param senderAddress
     *            the senderAddress to set
     */
    public void setSenderAddress(String senderAddress) {
        if (senderAddress != null) {
            senderAddress = senderAddress.trim();
        }
        this.senderAddress = senderAddress;
    }

    /**
     * @param senderName
     *            the senderName to set
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName == null ? null : senderName.trim();
    }

    /**
     * @param smtpHost
     *            the smtpHost to set
     */
    public void setSmtpHost(String smtpHost) {
        if (smtpHost != null) {
            smtpHost = smtpHost.trim();
        }
        this.smtpHost = smtpHost;
    }

    /**
     * @param smtpPassword
     *            the smtpPassword to set
     */
    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    /**
     * @param smtpPort
     *            the smtpPort to set
     */
    public void setSmtpPort(String smtpPort) {
        if (smtpPort != null) {
            smtpPort = smtpPort.trim();
        }
        this.smtpPort = smtpPort;
    }

    /**
     * @param smtpStartTls
     *            the smtpStartTls to set
     */
    public void setSmtpStartTls(boolean smtpStartTls) {
        this.smtpStartTls = smtpStartTls;
    }

    /**
     * @param smtpUser
     *            the smtpUser to set
     */
    public void setSmtpUser(String smtpUser) {
        if (smtpUser != null) {
            smtpUser = smtpUser.trim();
        }
        this.smtpUser = smtpUser;
    }

    /**
     * @param supportAddress
     *            the supportAddress to set
     */
    public void setSupportAddress(String supportAddress) {
        if (supportAddress != null) {
            supportAddress = supportAddress.trim();
        }
        this.supportAddress = supportAddress;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(UserVO user) {
        this.user = user;
    }

    /**
     * Sets the alias.
     *
     * @param alias
     *            the new alias
     */
    public void setUserAlias(String alias) {
        user.setAlias(alias == null ? null : alias.trim());
    }

    /**
     * Sets the email.
     *
     * @param email
     *            the new email
     */
    public void setUserEmail(String email) {
        if (email != null) {
            email = email.trim();
        }
        user.setEmail(email);
    }

    /**
     * Sets the first name.
     *
     * @param value
     *            the new first name
     */
    public void setUserFirstName(String value) {
        user.setFirstName(value == null ? null : value.trim());
    }

    /**
     * Sets the language code.
     *
     * @param code
     *            the new language code
     */
    public void setUserLanguageCode(String code) {
        user.setLanguage(new Locale(code == null ? null : code.trim()));
    }

    /**
     * Sets the last name.
     *
     * @param lastName
     *            the new last name
     */
    public void setUserLastName(String lastName) {
        user.setLastName(lastName == null ? null : lastName.trim());
    }

    /**
     * @param userPassword
     *            the userPassword to set
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * @param userPasswordConfirmation
     *            the userPasswordConfirmation to set
     */
    public void setUserPasswordConfirmation(String userPasswordConfirmation) {
        this.userPasswordConfirmation = userPasswordConfirmation;
    }

}
