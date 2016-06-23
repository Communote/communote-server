package com.communote.server.persistence.common.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.core.i18n.LocalizationChangeObservable;

/**
 * Message that aggregates multiple {@link LocalizedMessage} and joins them on
 * {@link #toString(Locale, Object...)} using the provided {@link #seperator} (default is a space
 * " ").
 *
 * Important: The arguments provided to {@link #toString(Locale, Object...)} are ignored! The
 * arguments must be provided independently for each {@link MessageKeyLocalizedMessage} added.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MultipleMessageKeyLocalizedMessage implements LocalizedMessage,
        LocalizationChangeObservable {

    private final List<LocalizedMessage> messages = new ArrayList<>();

    final String seperator;

    public MultipleMessageKeyLocalizedMessage(LocalizedMessage... messages) {
        this(" ", messages);
    }

    public MultipleMessageKeyLocalizedMessage(String seperator, LocalizedMessage... messages) {
        if (messages == null || messages.length == 0 || messages[0] == null) {
            throw new IllegalArgumentException("Must have at least one message. messages="
                    + messages);
        }
        if (seperator == null) {
            throw new IllegalArgumentException("seperator cannot be null.");
        }
        this.messages.addAll(Arrays.asList(messages));
        this.seperator = seperator;
    }

    public void addMessage(LocalizedMessage message) {
        this.messages.add(message);
    }

    @Override
    public Class<ResourceBundleChangedEvent> getChangeNotificationEvent() {
        return ResourceBundleChangedEvent.class;
    }

    @Override
    public String toString(Locale locale, Object... arguments) {
        StringBuilder message = new StringBuilder();
        String prefix = "";
        for (LocalizedMessage m : this.messages) {
            message.append(prefix);
            message.append(m.toString(locale, (Object[]) null));
            prefix = seperator;
        }
        return message.toString();
    }

}