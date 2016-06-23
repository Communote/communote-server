package com.communote.server.core.messaging.connectors.xmpp;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.client.ClientManagement;
import com.communote.server.model.user.User;

/**
 * Class for extracting information from jids.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPPatternUtils {

    /**
     * This method extracts the blog id from the alias.
     *
     * @param aliasJid
     *            Jid to extract user id from.
     * @return User id as {@link Long},
     */
    public static Long extractBlogId(String aliasJid) {
        if (!aliasJid.contains(getBlogSuffix())) {
            return null;
        }
        String preAt = aliasJid.split("@")[0];
        int lastDot = preAt.lastIndexOf(".");
        String blog = preAt.substring(0, lastDot);
        return ServiceLocator.instance().getService(BlogManagement.class)
                .findBlogByIdentifierWithoutAuthorizationCheck(blog).getId();
    }

    /**
     * This method extracts a client from an alias jid.
     *
     * @param aliasJid
     *            Jid to extract client id from.
     * @return The extracted {@link ClientTO}.
     * @throws ClientNotFoundException
     *             If no client was found.
     */
    public static ClientTO extractClient(String aliasJid) throws ClientNotFoundException {
        if (!aliasJid.contains(getBlogSuffix())) {
            return null;
        }
        String preAt = aliasJid.split("@")[0];
        int lastDot = preAt.lastIndexOf(".");
        String clientId = preAt.substring(lastDot + 1);
        try {
            return ServiceLocator.findService(ClientManagement.class).findClient(clientId);
        } catch (ClientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    /**
     * This method extracts the client from a user jid.
     *
     * @param jid
     *            Jid to extract the client id from.
     * @return The extracted client id.
     */
    public static String extractClientIdFromUser(String jid) {
        String normalisedUsername = jid.split("/")[0].replace(getUserSuffix(), "");
        int lastDot = normalisedUsername.lastIndexOf(".");
        String client = normalisedUsername.substring(lastDot + 1);
        return client;
    }

    /**
     * This method returns a client extracted from the alias. It uses a {@link ClientDelegate} to
     * switch the client.
     *
     * @param jid
     *            Jid to extract the user from.
     * @return {@link User} for the jid.
     */
    public static User extractKenmeiUser(String jid) {
        String normalisedUsername = jid.split("/")[0].replace(getUserSuffix(), "");
        int lastDot = normalisedUsername.lastIndexOf(".");
        final String user = normalisedUsername.substring(0, lastDot);
        final String clientId = normalisedUsername.substring(lastDot + 1);
        try {
            ClientTO client = ServiceLocator.findService(ClientManagement.class).findClient(
                    clientId);
            return new ClientDelegate(client).execute(new ClientDelegateCallback<User>() {
                @Override
                public User doOnClient(ClientTO client) throws Exception {
                    return ServiceLocator.instance().getService(UserManagement.class)
                            .findUserByAlias(user);
                }
            });
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This works on the current assigned client.
     *
     * @param jid
     *            Jid to extract the user from.
     * @return {@link User} for the jid.
     */
    public static User extractKenmeiUserWithoutDatabaseDelegate(String jid) {
        String normalisedUsername = jid.split("/")[0].replace(getUserSuffix(), "");
        int lastDot = normalisedUsername.lastIndexOf(".");
        String user = normalisedUsername.substring(0, lastDot);
        return ServiceLocator.instance().getService(UserManagement.class)
                .findUserByAlias(user);
    }

    /**
     * @return Suffix to separate blogs
     */
    public static String getBlogSuffix() {
        return ApplicationPropertyXmpp.BLOG_SUFFIX.getValue("");
    }

    /**
     * Suffix to separate the user from specific host settings.
     *
     * @return suffix to separate the user from specific host settings.
     */
    public static String getUserSuffix() {
        return ApplicationPropertyXmpp.USER_SUFFIX.getValue("");
    }

    /**
     * Hidden constructor to avoid instances of this utility class.
     */
    private XMPPPatternUtils() {
        // Do nothing.
    }
}
