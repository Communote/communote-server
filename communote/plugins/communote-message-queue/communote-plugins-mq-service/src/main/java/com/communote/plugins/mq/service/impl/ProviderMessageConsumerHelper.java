package com.communote.plugins.mq.service.impl;

import com.communote.plugins.mq.message.base.data.security.UserIdentityContext;
import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.service.exception.MessageValidationException;
import com.communote.server.core.exception.Reason;
import com.communote.server.core.security.FieldUserIdentification;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;

/**
 * Helper forr the ProviderMessageConsumer service.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
final class ProviderMessageConsumerHelper {
    /**
     * Take the identification as external user id and create a UserIdentification
     * 
     * @param identityContext
     *            the context to use
     * @param message
     *            the message to take the external system id from
     * @return the identification
     * @throws MessageValidationException
     *             in case the external system id is missing
     * @param <T>
     *            type of the message
     */
    static <T extends BaseMessage> FieldUserIdentification applyIdentificationExternalId(
            UserIdentityContext identityContext, T message) throws MessageValidationException {

        if (message.getExternalSystemId() == null
                || message.getExternalSystemId().length() > 0) {

            Reason reason = new Reason(
                    "communote.plugins.mq.serivce.error.message.validation.identity.context.missing.external.system.id",
                    "identityContext.identity.identity", "MISSING_FIELD", identityContext
                            .getIdentity()
                            .getIdentity());

            throw new MessageValidationException(
                    "A external user id was given but not external system id was provided. externalId="
                            + identityContext.getIdentity().getIdentity(),
                    new MessageKeyLocalizedMessage(
                            "communote.plugins.mq.serivce.error.message.validation.general"),
                    reason);
        }

        FieldUserIdentification userIdentification = new FieldUserIdentification();
        userIdentification.setExternalSystemId(message
                .getExternalSystemId());
        userIdentification.setExternalUserId(identityContext
                .getIdentity().getIdentity());

        return userIdentification;
    }

    /**
     * Take the identification as internal user alias and create a UserIdentification
     * 
     * @param identityContext
     *            the context to use
     * @return the identification
     */

    static FieldUserIdentification applyIdentificationUserAlias(UserIdentityContext identityContext) {
        FieldUserIdentification userIdentification = new FieldUserIdentification();
        userIdentification.setUserAlias(identityContext.getIdentity()
                .getIdentity());
        return userIdentification;
    }

    /**
     * Take the identification as internal user id and create a UserIdentification
     * 
     * @param identityContext
     *            the context to use
     * @return the identification
     * @throws MessageValidationException
     *             in case the user id could not be parsed
     */
    static FieldUserIdentification applyIdentificationUserId(UserIdentityContext identityContext)
            throws MessageValidationException {
        FieldUserIdentification userIdentification = new FieldUserIdentification();
        try {
            userIdentification.setUserId(Long.parseLong(identityContext.getIdentity()
                    .getIdentity()));
        } catch (NumberFormatException e) {

            Reason reason = new Reason(
                    "communote.plugins.mq.serivce.error.message.validation.identity.context.invalid.user.identifier",
                    "identityContext.identity.identity", "NUMBER_FORMAT", identityContext
                            .getIdentity()
                            .getIdentity());

            throw new MessageValidationException(
                    "The provided user id could not be parsed into a long. userId="
                            + identityContext.getIdentity().getIdentity(),
                    new MessageKeyLocalizedMessage(
                            "communote.plugins.mq.serivce.error.message.validation.general"), e,
                    reason);

        }
        return userIdentification;
    }

    /**
     * Do not use me this way
     */
    private ProviderMessageConsumerHelper() {

    }
}
