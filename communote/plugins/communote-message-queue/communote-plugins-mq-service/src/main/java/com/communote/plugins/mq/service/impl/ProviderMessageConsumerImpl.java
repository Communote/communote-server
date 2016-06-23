package com.communote.plugins.mq.service.impl;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.communote.plugins.mq.message.base.data.security.TrustAuthentication;
import com.communote.plugins.mq.message.base.data.security.UserIdentity;
import com.communote.plugins.mq.message.base.data.security.UserIdentityContext;
import com.communote.plugins.mq.message.base.data.security.UsernamePasswordAuthentication;
import com.communote.plugins.mq.message.base.data.status.Error;
import com.communote.plugins.mq.message.base.data.status.Status;
import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;
import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.message.base.message.ReplyType;
import com.communote.plugins.mq.service.exception.MessageValidationException;
import com.communote.plugins.mq.service.provider.MessagesConverter;
import com.communote.plugins.mq.service.provider.ProviderMessageConsumer;
import com.communote.plugins.mq.service.provider.TransferMessage;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapperManagement;
import com.communote.server.core.exception.Reason;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.AuthenticationManagement;
import com.communote.server.core.security.FieldUserIdentification;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.security.SwitchUserHelper;
import com.communote.server.core.security.SwitchUserNotAllowedException;
import com.communote.server.core.security.UserIdentification;
import com.communote.server.core.user.client.ClientManagement;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.service.UserService;
import com.communote.server.service.UserService.UserServiceRetrievalFlag;
import com.communote.server.web.WebServiceLocator;

/**
 * Implements ProviderMessageConsumer service.
 *
 * @param <T>
 *            type of the messages that are handled by this message consumer
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component(name = "message_consumer")
@Provides
public class ProviderMessageConsumerImpl<T extends BaseMessage> implements ProviderMessageConsumer {

    /** The LOG. */
    private static Logger LOGGER = LoggerFactory.getLogger(ProviderMessageConsumerImpl.class);

    private ExceptionMapperManagement exceptionMapper;

    /** The user service. */
    private UserService userService;

    /** The message handler. */
    @Property
    private CommunoteMessageHandler<T> messageHandler;

    /** The converter. */
    @Requires
    private MessagesConverter converter;

    /**
     * TODO should we throw an exception if it cannot be applied? Maybe we have message that run
     * anonymous ?
     *
     * @param communoteMessage
     *            the message to take the authentication from
     * @throws UserNotFoundException
     *             in case user has not been found
     */
    private void applyAuthentication(T communoteMessage) throws UserNotFoundException {

        if (communoteMessage.getAuthentication() instanceof TrustAuthentication) {
            TrustAuthentication trustAuthentication = (TrustAuthentication) communoteMessage
                    .getAuthentication();

            FieldUserIdentification userIdentification = new FieldUserIdentification();
            if (UserIdentity.IDENTITY_TYPE_ALIAS.equals(trustAuthentication.getIdentity()
                    .getIdentityType())) {
                userIdentification.setUserAlias(trustAuthentication.getIdentity().getIdentity());
            } else if (UserIdentity.IDENTITY_TYPE_USER_ID.equals(trustAuthentication.getIdentity()
                    .getIdentityType())) {
                userIdentification.setUserId(new Long(trustAuthentication.getIdentity()
                        .getIdentity()));
            } else if (UserIdentity.IDENTITY_TYPE_USER_ID.equals(trustAuthentication.getIdentity()
                    .getIdentityType())) {
                userIdentification.setExternalUserId(trustAuthentication.getIdentity()
                        .getIdentityType());
                userIdentification.setExternalSystemId(communoteMessage.getExternalSystemId());
            }

            User user = getUserService().getUser(userIdentification,
                    UserServiceRetrievalFlag.CREATE);
            AuthenticationHelper.setAsAuthenticatedUser(user);
        } else if (communoteMessage.getAuthentication() instanceof UsernamePasswordAuthentication) {

            UsernamePasswordAuthentication auth = (UsernamePasswordAuthentication) communoteMessage
                    .getAuthentication();

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    auth.getIdentity().getIdentity(), new String(auth.getPassword()));

            AuthenticationManager manager = WebServiceLocator.instance().getProviderManager();
            org.springframework.security.core.Authentication authResult = manager
                    .authenticate(token);

            ServiceLocator.findService(AuthenticationManagement.class).onSuccessfulAuthentication(
                    authResult);
        }
    }

    /**
     * Take the identity context of the message and apply it, that is the security context will be
     * switched. Of course this will fail if the current user is not allowed to switch useds.
     *
     * @param message
     *            the message to take the identity context from
     * @throws UserNotFoundException
     *             in case the user of the identity context could not be found
     * @throws SwitchUserNotAllowedException
     *             in case the switching is not allowed
     * @throws MessageValidationException
     *             in case the identity context is inconsistent
     */
    private void applyIdentityContext(T message) throws UserNotFoundException,
            SwitchUserNotAllowedException, MessageValidationException {
        // no context? nothing to do ...
        if (!(message.getIdentityContext() instanceof UserIdentityContext)) {
            return;
        }
        // check it here to avoid to create a user if it cannot be switched anyway
        if (!SwitchUserHelper.canSwitchUser()) {
            throw new SwitchUserNotAllowedException(SecurityHelper.getCurrentUserId(), null,
                    "Current user is not allowed to apply identity context.");
        }

        UserIdentityContext identityContext = (UserIdentityContext) message.getIdentityContext();

        FieldUserIdentification userIdentification;

        String identityType = identityContext.getIdentity().getIdentityType();

        if (identityType == null || identityType.trim().length() == 0) {
            // the default
            identityType = UserIdentity.IDENTITY_TYPE_ALIAS;
        }

        if (UserIdentity.IDENTITY_TYPE_EXTERNAL_ID.equals(identityType)) {

            userIdentification = ProviderMessageConsumerHelper.applyIdentificationExternalId(
                    identityContext, message);
        } else if (UserIdentity.IDENTITY_TYPE_USER_ID.equals(identityType)) {
            userIdentification = ProviderMessageConsumerHelper
                    .applyIdentificationUserId(identityContext);
        } else if (UserIdentity.IDENTITY_TYPE_ALIAS.equals(identityType)) {
            userIdentification = ProviderMessageConsumerHelper
                    .applyIdentificationUserAlias(identityContext);
        } else {
            throw new UserNotFoundException("Invalid identity type identityType="
                    + identityContext.getIdentity().getIdentityType() + " identity="
                    + identityContext.getIdentity().getIdentity());
        }

        // will throw exception if user cannot be found
        User user = getUserService().getUser(userIdentification);

        SwitchUserHelper.switchUser(user);

    }

    /**
     * Clear the switched user and the authentication
     */
    private void clearAuthentication() {

        try {
            // actually we dont need to do this as long as we always remove authentication
            if (SwitchUserHelper.isUserSwitched()) {
                SwitchUserHelper.removeSwitchedUser();
            }
        } catch (Exception e) {
            LOGGER.error("Error removing switched user. Will ignore exception.", e);
        } finally {
            AuthenticationHelper.removeAuthentication();
        }
    }

    /**
     * Convert the Communote core status object into a MQ status object
     *
     * @param cntStatus
     *            the status to convert
     * @param locale
     *            the locale to use for translating error messages
     * @return the converted status
     */
    private Status convertStatus(com.communote.server.core.exception.Status cntStatus, Locale locale) {
        Status mqStatus = new Status();
        mqStatus.setStatusCode(cntStatus.getErrorCode());
        if (cntStatus.getMessage() != null) {
            mqStatus.setMessage(cntStatus.getMessage().toString(locale));
        }
        Reason[] errors = cntStatus.getErrors();
        if (errors != null) {
            ArrayList<Error> convertedErrors = new ArrayList<Error>();
            for (Reason reason : errors) {
                Error convertedError = new Error();
                convertedError.setCauseErrorCode(reason.getErrorCause());
                convertedError.setErrorField(reason.getErrorField());
                if (reason.getErrorMessage() != null) {
                    convertedError.setErrorMessage(reason.getErrorMessage().toString(locale));
                }
                convertedErrors.add(convertedError);
            }
            mqStatus.setErrors(convertedErrors.toArray(new Error[0]));
        }
        return mqStatus;
    }

    /**
     * Create a reply message based on the given exception
     *
     * @param communoteMessage
     *            the communote message
     * @param e
     *            the exception
     * @return the tranfer message to send back
     */
    private TransferMessage createExceptionReplyMessage(T communoteMessage, Throwable e) {
        TransferMessage replyTransfer = null;

        CommunoteReplyMessage reply = new CommunoteReplyMessage();
        com.communote.server.core.exception.Status cntStatus = getExceptionMapper().mapException(e);
        reply.setStatus(convertStatus(cntStatus, getCurrentUserLocale()));
        replyTransfer = converter.convertToTransferMessage(reply);
        return replyTransfer;
    }

    /**
     * Create a reply message if necessary
     *
     * @param reply
     *            the reply returned by the message handler
     * @param replyType
     *            whether the sender of the message expects a reply
     * @return the reply message or null if none should be sent
     */
    private TransferMessage createReplyMessage(CommunoteReplyMessage reply, ReplyType replyType) {
        TransferMessage replyMessage = null;
        if (replyType == ReplyType.FULL || replyType == ReplyType.STATUS_ALWAYS) {
            /*
             * according to the contract, message handler should return full reply in any case. At
             * this place we're checking whether sender has requested the full reply, and if it has
             * not, the reply is replaced with the status one
             */
            if (reply == null || replyType != ReplyType.FULL) {
                reply = new CommunoteReplyMessage();
            }
            if (reply.getStatus() == null || reply.getStatus().getStatusCode() == null) {
                reply.setStatus(getSuccessfulStatus());
            }
            replyMessage = converter.convertToTransferMessage(reply);
        } else if (replyType == ReplyType.STATUS_ERRORS_ONLY) {
            Status status = reply == null ? null : reply.getStatus();
            if (isErrorStatus(status)) {
                replyMessage = converter.convertToTransferMessage(reply);
            }
        }
        return replyMessage;
    }

    /**
     * Check if there is a valid pre authenticated user available.
     *
     * @param message
     *            the message to check
     * @param preAuthenticatedClientId
     *            the client id of the preauthentication
     * @param clientId
     *            the client id of the communote message
     * @return the user identififcation that can be used without another authentication
     */
    private UserIdentification determineValidPreAuthentication(TransferMessage message,
            String preAuthenticatedClientId, String clientId) {
        UserIdentification userIdentification = null;
        if (preAuthenticatedClientId != null) {
            if (!preAuthenticatedClientId.equals(clientId)) {
                LOGGER.warn("The preauthenticated client id does not match the client id of the communote message. "
                        + "Pre Authentication will be ignored. preAuthenticatedClientId="
                        + preAuthenticatedClientId + " communoteMessage.clientId=" + clientId);
            } else {
                userIdentification = (UserIdentification) message
                        .getHeader(TransferMessage.HEADER_PRE_AUTHENTICATED_USER_IDENTIFICATION);
            }
        }
        return userIdentification;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.service.provider.ProviderMessageConsumer#
     * getConsumedMessageType()
     */
    @Override
    public String getConsumedMessageType() {
        return messageHandler.getHandledMessageType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.service.provider.ProviderMessageConsumer#
     * getConsumedMessageVersion()
     */
    @Override
    public String getConsumedMessageVersion() {
        return this.messageHandler.getVersion();
    }

    /**
     * Determine the locale to use depending on if there is a current user or not. It has to be
     * generalized, e.g. if the authentication fails an exception is thrown an no current user is
     * available on the other hand the exception is thrown during processing the message and hence a
     * user is available.
     *
     * @return the user local of the current user (or the default one)
     */
    private Locale getCurrentUserLocale() {
        UserDetails user = SecurityHelper.getCurrentUser();
        return user == null ? CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getDefaultLanguage() : user.getUserLocale();
    }

    /**
     * @return the lazily initialized exception mapper
     */
    private ExceptionMapperManagement getExceptionMapper() {
        if (exceptionMapper == null) {
            exceptionMapper = ServiceLocator.instance().getService(ExceptionMapperManagement.class);
        }
        return exceptionMapper;
    }

    /**
     * Gets the message handler.
     *
     * @return the messageHandler
     */
    CommunoteMessageHandler<T> getMessageHandler() {
        return messageHandler;
    }

    /**
     * @return successful status object
     */
    private Status getSuccessfulStatus() {
        Status status = new Status();
        status.setStatusCode(ErrorCodes.OKAY);
        return status;
    }

    /**
     * @return user service
     */
    private UserService getUserService() {
        if (userService == null) {
            userService = ServiceLocator.instance().getService(UserService.class);
        }
        return userService;
    }

    /**
     * Test whether the status is an error status. Currently a missing statusCode and the codes OKAY
     * and WARNING are not treated as errors.
     *
     * @param status
     *            the status to check
     * @return true if it is an error, false otherwise
     */
    private boolean isErrorStatus(Status status) {
        if (status != null && status.getStatusCode() != null) {
            String statusCode = status.getStatusCode();
            if (!ErrorCodes.OKAY.equals(statusCode) && !ErrorCodes.WARNING.equals(statusCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param communoteMessage
     *            communote message to be processed
     * @return transfer message
     */
    private TransferMessage processCommunoteMessage(T communoteMessage) {
        TransferMessage replyTransfer = null;
        try {
            // providing message to the actual handler
            CommunoteReplyMessage reply = messageHandler.handleMessage(communoteMessage);
            replyTransfer = createReplyMessage(reply, communoteMessage.getReplyType());
        } catch (Throwable e) {
            if (sendExceptionReply(communoteMessage)) {
                replyTransfer = createExceptionReplyMessage(communoteMessage, e);
            } else {
                LOGGER.error("MessageHandler {} failed with an exception", messageHandler
                        .getClass().getSimpleName(), e);
            }
        }
        return replyTransfer;
    }

    /**
     * Helper method to prepare the processing by applying the authentication, the identity context
     * and the let it all run in the client
     *
     * @param client
     *            the client to use
     * @param communoteMessage
     *            the message to process
     * @param preAuthenticatedUserIdentification
     *            the pre authenticated user identification that can be taken without further
     *            checking (well the user has to exist
     * @return the reply transfer message
     * @throws Exception
     *             in case of an error
     */
    private TransferMessage processMessage(ClientTO client, final T communoteMessage,
            final UserIdentification preAuthenticatedUserIdentification) throws Exception {
        TransferMessage replyMessage;
        ClientDelegateCallback<Object> clientCallback = new ClientDelegateCallback<Object>() {

            @Override
            public Object doOnClient(ClientTO client) throws Exception {

                TransferMessage innerReplyMessage = null;
                try {
                    ClientAndChannelContextHolder.setChannel(ChannelType.API);

                    // if it exists take the pre authenticated user information
                    if (preAuthenticatedUserIdentification != null) {
                        User user = getUserService()
                                .getUser(preAuthenticatedUserIdentification,
                                        UserServiceRetrievalFlag.CREATE);
                        AuthenticationHelper.setAsAuthenticatedUser(user);
                    } else {
                        // if not use the authentication of the message
                        applyAuthentication(communoteMessage);
                    }
                    // apply the identity context, that is switch the user
                    applyIdentityContext(communoteMessage);

                    // finally process the message
                    innerReplyMessage = processCommunoteMessage(communoteMessage);

                } finally {
                    ClientAndChannelContextHolder.setChannel(null);
                    // clear the security context
                    clearAuthentication();
                }

                return innerReplyMessage;
            }

        };
        replyMessage = (TransferMessage) new ClientDelegate(client).execute(clientCallback);
        return replyMessage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.service.provider.ProviderMessageConsumer#
     * receiveMessage(com.communote.plugins.mq.service.provider.TransferMessage)
     */
    @Override
    public TransferMessage receiveMessage(TransferMessage message) {
        LOGGER.debug("Message is received by ProviderMessageConsumer. MessageHandler == {}",
                messageHandler.getClass().getSimpleName());
        T communoteMessage = null;
        TransferMessage replyMessage = null;
        try {
            communoteMessage = converter.convertToCommunoteMessage(message,
                    messageHandler.getHandledMessageClass());

            String preAuthenticatedClientId = (String) message
                    .getHeader(TransferMessage.HEADER_PRE_AUTHENTICATED_CLIENT_ID);
            String clientId = communoteMessage.getClientAlias();

            if (clientId == null || clientId.trim().length() == 0) {
                clientId = ClientHelper.getGlobalClientId();
            }

            final UserIdentification preAuthenticatedUserIdentification = determineValidPreAuthentication(
                    message, preAuthenticatedClientId, clientId);

            boolean trustUser = Boolean.parseBoolean(String.valueOf(message
                    .getHeader(TransferMessage.HEADER_TRUST_USER)));
            if (trustUser
                    && communoteMessage.getAuthentication() instanceof UsernamePasswordAuthentication) {
                communoteMessage.setAuthentication(new TrustAuthentication(
                        (UsernamePasswordAuthentication) communoteMessage.getAuthentication()));
            }

            // this can fail if client does not exist
            ClientTO client = ServiceLocator.findService(ClientManagement.class).findClient(
                    clientId);

            replyMessage = processMessage(client, communoteMessage,
                    preAuthenticatedUserIdentification);

        } catch (Throwable e) {
            // TODO should we log all exceptions in here?
            LOGGER.error("An exception has occurred during message handling. MessageHandler == "
                    + messageHandler.getClass().getSimpleName(), e);
            if (sendExceptionReply(communoteMessage)) {
                replyMessage = createExceptionReplyMessage(communoteMessage, e);
            }
        }

        return replyMessage;
    }

    /**
     * is checked whether any answer should be sent in the case of exception during message
     * processing
     *
     * it will return true if the cnt message null. In this case it could not be determined the
     * reply type. But the underlying transport will decide finally if we want to send a reply.
     *
     * @param cntMessage
     *            the cnt message
     * @return true if a reply should be send
     */
    private boolean sendExceptionReply(T cntMessage) {
        if (cntMessage == null || cntMessage.getReplyType() != null
                && cntMessage.getReplyType() != ReplyType.NONE) {
            return true;
        }
        return false;
    }

    /**
     * only for test purposes.
     *
     * @param converter
     *            the new converter
     */
    void setConverter(MessagesConverter converter) {
        this.converter = converter;
    }

    /**
     * only for test purposes.
     *
     * @param messageHandler
     *            the new message handler
     */
    void setMessageHandler(CommunoteMessageHandler<T> messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * only for test purposes.
     *
     * @param userService
     *            the userService to set
     */
    void setUserService(UserService userService) {
        this.userService = userService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Provider message consumer for " + messageHandler.getHandledMessageType();
    }

}
