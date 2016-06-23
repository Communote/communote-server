package com.communote.server.core.blog.notes.processors.exceptions;

import java.util.Collection;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;

/**
 * Thrown to indicate that a direct message could not be created because there were no users to be
 * notified.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class DirectMessageMissingRecipientException extends NoteStoringPreProcessorException {

    private static final long serialVersionUID = -3199049004682888743L;
    private final String[] unresolvableUsers;

    private final String[] uninformableUsers;

    private LocalizedMessage localizedMessage;
    private boolean wrongAtAtNotation;

    /**
     * Constructor.
     *
     * @param wrongAtAtNotation
     *            Set to true, if this was thrown because of an invalid usage of @@ within a direct
     *            message.
     */
    public DirectMessageMissingRecipientException(boolean wrongAtAtNotation) {
        this(null, null);
        this.wrongAtAtNotation = wrongAtAtNotation;
    }

    /**
     * Constructor.
     *
     * @param unresolvableUsers
     *            possibly empty collection of aliases that could not be resolved to existing users
     * @param uninformableUsers
     *            possibly empty collection of aliases of users that do not have read access to the
     *            blog of the direct message
     */
    public DirectMessageMissingRecipientException(Collection<String> unresolvableUsers,
            Collection<String> uninformableUsers) {
        super("The direct message has no recipients");
        if (unresolvableUsers == null || unresolvableUsers.isEmpty()) {
            this.unresolvableUsers = null;
        } else {
            this.unresolvableUsers = unresolvableUsers
                    .toArray(new String[unresolvableUsers.size()]);
        }
        if (uninformableUsers == null || uninformableUsers.isEmpty()) {
            this.uninformableUsers = null;
        } else {
            this.uninformableUsers = uninformableUsers
                    .toArray(new String[uninformableUsers.size()]);
        }
    }

    /**
     * Returns a detailed localized error message.
     *
     * @param locale
     *            the locale for creating the localized message
     * @return the error message
     */
    public String getLocalizedMessage(Locale locale) {
        return getPreparedLocalizedMessage().toString(locale);
    }

    /**
     * @return the message object to get a localized message from
     */
    public LocalizedMessage getPreparedLocalizedMessage() {
        if (localizedMessage == null) {
            if (wrongAtAtNotation) {
                localizedMessage = new MessageKeyLocalizedMessage(
                        "error.blogpost.blog.content.processing.failed.direct.wrong.atat-notation",
                        NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS);
            } else if (this.unresolvableUsers == null && this.uninformableUsers == null) {
                localizedMessage = new MessageKeyLocalizedMessage(
                        "error.blogpost.blog.content.processing.failed.direct.users.missing");
            } else if (this.unresolvableUsers == null) {
                localizedMessage = new MessageKeyLocalizedMessage(
                        "error.blogpost.blog.content.processing.failed.direct.users.uninformable",
                        StringUtils.join(this.uninformableUsers, ", "));
            } else if (this.uninformableUsers == null) {
                localizedMessage = new MessageKeyLocalizedMessage(
                        "error.blogpost.blog.content.processing.failed.direct.users.unresolvable",
                        StringUtils.join(this.unresolvableUsers, ", "));
            } else {
                localizedMessage = new MessageKeyLocalizedMessage(
                        "error.blogpost.blog.content.processing.failed.direct.users.missing.mixed",
                        StringUtils.join(this.uninformableUsers, ", "), StringUtils.join(
                                this.unresolvableUsers, ", "));
            }
        }
        return localizedMessage;
    }

    /**
     * @return the aliases of the uninformable users that caused this exception or null if there
     *         were no uniformable users
     */
    public String[] getUninformableUsers() {
        return this.uninformableUsers;

    }

    /**
     * @return the aliases of the unresolvable users that caused this exception or null if there
     *         were no unresolvable users
     */
    public String[] getUnresolvableUsers() {
        return this.unresolvableUsers;
    }

}
