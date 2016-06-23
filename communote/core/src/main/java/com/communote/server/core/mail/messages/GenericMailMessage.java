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
     *            The message key.
     * @param receivers
     *            Receivers.
     * @param locale
     *            The locale.
     */
    public GenericMailMessage(String messageKey, Locale locale, User... receivers) {
        this(messageKey, locale, new HashMap<String, Object>(), receivers);
    }

    /**
     * Constructor.
     * 
     * @param messageKey
     *            The template.
     * @param receivers
     *            Receivers.
     * @param locale
     *            The locale.
     * @param model
     *            Model.
     */
    public GenericMailMessage(String messageKey, Locale locale,
            Map<String, Object> model, User... receivers) {
        super(messageKey, locale, receivers);
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
    protected void prepareModel(Map<String, Object> model) {
        model.putAll(this.model);
    }
}
