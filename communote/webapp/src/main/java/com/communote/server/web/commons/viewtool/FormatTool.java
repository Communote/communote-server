package com.communote.server.web.commons.viewtool;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.MessageHelper;

/**
 * Tools for message formatting.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@DefaultKey("fmtTool")
@ValidScope(Scope.REQUEST)
public class FormatTool extends RequestAwareTool {

    /**
     * Creates a Javascript declaration of an object containing all the key to localized message
     * mappings for the category. If a namespace is provided the messages are added under the name
     * localizedMessages to the namespace. If no namespace is given the method returns a declaration
     * of the jsLocalizedMessages variable.
     *
     * @param category
     *            the category for which the messages are to be returned. Can be one of the built-in
     *            categories (see constants in
     *            {@link com.communote.server.web.commons.i18n.JsMessagesRegistry}) or one provided
     *            by plugin.
     * @param namespace
     *            A string defining a namespace object (e.g. communote.i18n) to which the
     *            localizedMessages member should be added.
     * @return the Javascript declaration in the form of 'var jsLocalizedMessages = {...};' or as a
     *         namespace member
     */
    public String jsMessagesDeclaration(String category, String namespace) {
        String value = StringUtils.isBlank(category) ? "{}" : WebServiceLocator.instance()
                .getJsMessagesRegistry().getJsMessages(getRequest(), category);
        if (StringUtils.isEmpty(namespace)) {
            return "var jsLocalizedMessages = " + value + ";";
        } else {
            return namespace + ".localizedMessages=" + value + ";";
        }
    }

    /**
     * Returns the localized message for the provided key.
     *
     * @param key
     *            the message key
     * @return the localized message
     */
    public String message(String key) {
        return MessageHelper.getText(getRequest(), key);
    }

    /**
     * Returns the localized message for the provided key.
     *
     * @param key
     *            the message key
     * @param args
     *            additional arguments to insert in place holders
     * @return the localized message
     */
    public String message(String key, List<Object> args) {
        return message(key, args.toArray());
    }

    /**
     * Returns the localized message for the provided key.
     *
     * @param key
     *            the message key
     * @param locale
     *            use the provided locale for localization. If null the current locale provided by
     *            the session handler will be used.
     * @return the localized message
     */
    public String message(String key, Locale locale) {
        if (locale == null) {
            return MessageHelper.getText(getRequest(), key);
        } else {
            return ResourceBundleManager.instance().getText(key, locale);
        }
    }

    /**
     * Returns the localized message for the provided key.
     *
     * @param key
     *            the message key
     * @param args
     *            additional arguments to insert in place holders
     * @return the localized message
     */
    public String message(String key, Object... args) {
        return MessageHelper.getText(getRequest(), key, args);
    }
}
