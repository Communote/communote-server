package com.communote.server.persistence.user.client;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.application.CommunoteRuntime;

/**
 * The Class ClientUrlHelper offers methods to add the client and module id to an url.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientUrlHelper {

    /**
     * Name of the microblogging module.
     */
    public static final String MODULE_MICROBLOG = "microblog";

    /**
     * As long we have the tiles def with the module placeholder we need this
     */
    private static final String MODULE_MICROBLOG_PART = "/microblog/";

    /**
     * Appends a urlPath to the provided url if the urlPath is not empty or blank
     *
     * @param url
     *            the URL to extend. The URL is expected to not have a trailing '/'.
     * @param urlPath
     *            the path to append. Can be null or empty and does not have to start with a '/'.
     */
    public static void appendUrlPath(StringBuilder url, String urlPath) {
        if (StringUtils.isNotBlank(urlPath)) {
            if (!urlPath.startsWith("/")) {
                url.append("/");
            }
            url.append(urlPath);
        }
    }

    /**
     * Gets the client id from the URL part. The URL part should look like
     * '/microblog/client-id[/optional-url-path]'
     *
     * @param urlPart
     *            the part of the URL after the context
     * @return the client id or the default id if no id was found
     */
    public static String getClientId(String urlPart) {
        String[] uriFragments = StringUtils.split(urlPart, "/");
        return uriFragments.length > 1 ? uriFragments[1] : ClientHelper.getGlobalClientId();
    }

    /**
     * Prepends the module and client ID of the current client to the provided URL path. The
     * returned string starts with a '/'.
     *
     * @param urlPath
     *            path to a resource to be prepended. The path doesn't have to start with a '/' and
     *            can be null.
     * @return the module and client URL part
     */
    public static String prependModuleClientPart(String urlPath) {
        return prependModuleClientPart(null, urlPath);
    }

    /**
     * Prepends the module and client ID to the provided URL path. The returned string starts with a
     * '/'.
     *
     * @param clientId
     *            the client ID to include. Can be null which will lead to including the ID of the
     *            current client.
     * @param urlPath
     *            path to a resource to be prepended. The path doesn't have to start with a '/' and
     *            can be null.
     * @return the module and client URL part
     */
    public static String prependModuleClientPart(String clientId, String urlPath) {
        StringBuilder result = new StringBuilder();
        result.append(MODULE_MICROBLOG_PART);
        if (clientId == null) {
            result.append(ClientHelper.getCurrentClientId());
        } else {
            result.append(clientId);
        }
        ClientUrlHelper.appendUrlPath(result, urlPath);
        return result.toString();
    }

    /**
     * Removes the module and client id from the given uri.
     *
     * @param uri
     *            the uri
     * @return the uri without ids
     */
    public static String removeIds(String uri) {
        // remove module id
        int pos = uri.indexOf(MODULE_MICROBLOG_PART);
        if (pos > -1) {
            uri = uri.substring(pos + MODULE_MICROBLOG_PART.length());
            // remove client id
            pos = uri.indexOf("/", 1);
            if (pos > -1) {
                uri = uri.substring(pos);
            }
        }
        return uri;
    }

    /**
     * Renders an absolute URL pointing to a resource on the current client. This method should only
     * be used if there is no access to a Request object because the URL might not be working if the
     * configuration is wrong or wasn't adapted to the latest changes of the server settings. In
     * case of access to Request object the appropriate method of the ControllerHelper should be
     * used.
     *
     * @param urlPath
     *            a (relative) URL path without client and module information, can be empty or null
     * @param isSecure
     *            whether the URL should be rendered with the HTTPS or HTTP protocol. If the
     *            configuration states that HTTPS is not supported, the returned URL will use the
     *            HTTP protocol.
     * @return the URL
     */
    public static String renderConfiguredAbsoluteUrl(String urlPath, boolean isSecure) {
        return ClientUrlHelper.renderConfiguredAbsoluteUrl(null, urlPath, isSecure, false);
    }

    /**
     * Renders an absolute URL pointing to a resource on the given client. This method should only
     * be used if there is no access to a Request object because the URL might not be working if the
     * configuration is wrong or wasn't adapted to the latest changes of the server settings. In
     * case of access to Request object the appropriate method of the ControllerHelper should be
     * used.
     *
     * @param clientId
     *            the client ID to include. Can be null which will lead to including the ID of the
     *            current client.
     * @param urlPath
     *            a (relative) URL path without client and module information, can be empty or null
     * @param isSecure
     *            whether the URL should be rendered with the HTTPS or HTTP protocol. If the
     *            configuration states that HTTPS is not supported, the returned URL will use the
     *            HTTP protocol.
     * @param staticResource
     *            true if the url should be rendered as a resource not to be delivered by the
     *            dispatcher servlet
     * @return the URL
     */
    public static String renderConfiguredAbsoluteUrl(String clientId, String urlPath,
            boolean isSecure, boolean staticResource) {
        StringBuilder prefix = new StringBuilder();
        if (isSecure) {
            prefix.append(CommunoteRuntime.getInstance().getConfigurationManager()
                    .getApplicationConfigurationProperties().getUrlPrefixSecured());
        } else {
            prefix.append(CommunoteRuntime.getInstance().getConfigurationManager()
                    .getApplicationConfigurationProperties().getUrlPrefix());
        }
        if (staticResource) {
            ClientUrlHelper.appendUrlPath(prefix, urlPath);
        } else {
            prefix.append(ClientUrlHelper.prependModuleClientPart(clientId, urlPath));
        }
        return prefix.toString();
    }

    /**
     * Instantiates a new url helper.
     */
    private ClientUrlHelper() {
        // Do nothing.
    }
}
