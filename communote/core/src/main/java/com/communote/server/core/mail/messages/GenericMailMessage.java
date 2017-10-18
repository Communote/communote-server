package com.communote.server.core.mail.messages;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.communote.server.model.user.User;


/**
 * General purpose mail message.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class GenericMailMessage extends MailMessage {

    private final Map<String, Object> model = new HashMap<String, Object>();

    /**
     * Constructor.
     * 
     * @param messageKey
     *            The message key of the template.
     * @param recipients
     *            users to send the message to
     * @param locale
     *            The locale.
     */
    public GenericMailMessage(String messageKey, Locale locale, User... recipients) {
        this(messageKey, locale, new HashMap<String, Object>(), recipients);
    }

    /**
     * Constructor.
     * 
     * @param messageKey
     *            The message key of the template.
     * @param recipients
     *            users to send the message to
     * @param locale
     *            The locale for sending the message
     * @param model
     *            Model with key value pairs which should be available when rendering the template.
     */
    public GenericMailMessage(String messageKey, Locale locale,
            Map<String, Object> model, User... recipients) {
        super(messageKey, locale, recipients);
        this.model.putAll(model);
    }

    /**
     * Adds the element to the mail model.
     * 
     * @param key
     *            The key.
     * @param value
     *            The value.
     */
    public void addToModel(String key, Object value) {
        model.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareModel(Map<String, Object> model) {
        model.putAll(this.model);
    }
}
