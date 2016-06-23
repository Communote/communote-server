package com.communote.plugins.mq.provider.activemq.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.jaas.UserPrincipal;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.plugins.mq.provider.activemq.security.authentication.Authenticator;
import com.communote.plugins.mq.provider.activemq.security.authentication.MessageQueueUserAuthenticator;

/**
 * Login module.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteJaasLoginModule implements LoginModule {
    /** The LOG. */
    private static Logger LOGGER = LoggerFactory.getLogger(CommunoteJaasLoginModule.class);

    /**
     * Constant for the name of the role users
     */
    public final static String ROLE_USERS = "users";

    /**
     * checks whether client address is local
     * 
     * @param remoteAddress
     *            remote address
     * @return true if local, false otherwise
     */
    public static boolean isAddressLocal(String remoteAddress) {
        if (remoteAddress.startsWith("vm")) {
            return true;
        }
        String dotRemoteAddress = null;
        // strip protocol
        int protocolIdx = remoteAddress.indexOf("://");
        if (protocolIdx > 0) {
            remoteAddress = remoteAddress.substring(protocolIdx + 3);
        }
        Matcher matcher = PATTERN.matcher(remoteAddress);
        if (matcher.matches()) {
            dotRemoteAddress = matcher.group(1);
        }
        if ("127.0.0.1".equals(dotRemoteAddress) || remoteAddress.startsWith("localhost")) {
            return true;
        }
        try {
            String dotLocalAddress = InetAddress.getLocalHost()
                    .getHostAddress();
            return dotLocalAddress.equals(dotRemoteAddress);
        } catch (UnknownHostException e) {
            LOGGER.error("No ip address found for the given host.", e);
        }
        return false;
    }

    private CallbackHandler callbackHandler;
    private Subject subject;
    private String username;

    private final Set<Principal> principals = new HashSet<Principal>();

    /**
     * Name of the user if its local / internal
     */
    public final static String LOCAL_COMMUNOTE_USER = "local_communote";

    private final static Pattern PATTERN = Pattern.compile("^([0-9\\.]*):(.*)");
    private final List<Authenticator> authenticators = new ArrayList<Authenticator>();

    {
        authenticators.add(new MessageQueueUserAuthenticator(getPluginPropertyService()));
    }

    private Set<String> roles;

    private PluginPropertyService pluginPropertyService;

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    @Override
    public boolean abort() throws LoginException {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    @Override
    public boolean commit() throws LoginException {
        principals.add(new UserPrincipal(username));
        for (String role : roles) {
            principals.add(new GroupPrincipal(role));
        }
        this.subject.getPrincipals().addAll(principals);
        return true;
    }

    /**
     * @return the pluginPropertyManagement
     */
    public PluginPropertyService getPluginPropertyService() {
        if (pluginPropertyService == null) {
            BundleContext context = FrameworkUtil.getBundle(getClass())
                    .getBundleContext();
            ServiceReference serviceRef = context
                    .getServiceReference(PluginPropertyService.class);
            pluginPropertyService = (PluginPropertyService) context.getService(serviceRef);
        }
        return pluginPropertyService;
    }

    // replace with User dao

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject ,
     * javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#login()
     */
    @Override
    public boolean login() throws LoginException {

        LOGGER.debug("Login");

        NameCallback nameCallback = new NameCallback("Username: ");
        PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        RemoteAddressCallBack remoteAddressCallBack = new RemoteAddressCallBack();
        ConnectorURICallBack connectorURICallBack = new ConnectorURICallBack();

        try {
            callbackHandler.handle(new Callback[] { nameCallback, passwordCallback,
                    remoteAddressCallBack, connectorURICallBack });
        } catch (IOException e) {
            throw new LoginException(e.getMessage());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.getMessage());
        }

        String remoteUsername = nameCallback.getName();
        if (remoteUsername == null) {
            throw new LoginException("No username provided");
            // if (isAddressLocal(remoteAddressCallBack.getRemoteAddress())) {
            // LOGGER.debug("local login succeed");
            // this.username = LOCAL_COMMUNOTE_USER;
            // roles = new HashSet<String>();
            // roles.add(ROLE_USERS);
            // return true;
            // } else {
            // throw new LoginException("No username provided");
            // }
        }
        char[] remotePassword = passwordCallback.getPassword();
        LoginException lastLoginException = new LoginException(
                "There was something wrong with the login of user " + remoteUsername);
        for (Authenticator authenticator : authenticators) {
            try {
                roles = authenticator.authenticate(remoteUsername, new String(remotePassword));
                username = remoteUsername;
                return true;
            } catch (LoginException e) {
                lastLoginException = e;
            }
        }
        throw lastLoginException;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    @Override
    public boolean logout() throws LoginException {
        this.subject.getPrincipals().removeAll(principals);
        principals.clear();
        username = null;
        roles = null;
        return true;
    }
}
