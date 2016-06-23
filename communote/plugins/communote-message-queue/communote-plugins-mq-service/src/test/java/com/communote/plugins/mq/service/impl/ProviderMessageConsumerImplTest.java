package com.communote.plugins.mq.service.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;

import com.communote.plugins.mq.message.base.data.security.UserIdentity;
import com.communote.plugins.mq.message.base.data.security.UserIdentityContext;
import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;
import com.communote.plugins.mq.message.base.handler.ValidationResult;
import com.communote.plugins.mq.message.base.handler.ValidationResult.ValidationStatus;
import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.service.provider.MessagesConverter;
import com.communote.plugins.mq.service.provider.TransferMessage;
import com.communote.server.core.security.UserDetails;
import com.communote.server.model.user.User;
import com.communote.server.service.UserService;

/**
 * The Class TestProviderMessageConsumerImpl.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ProviderMessageConsumerImplTest {

    /**
     * The Class TestMessage.
     */
    private class TestMessage extends BaseMessage {

    }

    /** The handler mock. */
    private CommunoteMessageHandler<TestMessage> handlerMock;

    /** The converter mock. */
    private MessagesConverter converterMock;

    /** The authentication helper mock. */
    private AuthenticationHelperWrapper authenticationHelperMock;

    /** The security helper mock. */
    private SecurityHelperWrapper securityHelperMock;

    /** The user service mock. */
    private UserService userServiceMock;

    /**
     * Inits the mocks.
     */
    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void initMocks() {
        handlerMock = EasyMock.createMock(CommunoteMessageHandler.class);
        converterMock = EasyMock.createMock(MessagesConverter.class);
        authenticationHelperMock = createMock(AuthenticationHelperWrapper.class);
        securityHelperMock = createMock(SecurityHelperWrapper.class);
        userServiceMock = createMock(UserService.class);
    }

    /**
     * Test receive message. Test is disabled because the mocking does not work for client and
     * security.
     * 
     * @throws Exception
     *             exception
     */
    // @Test
    public void testReceiveMessage() throws Exception {
        // message related mocks
        String testCntUserAlias = "test_cnt_alias";
        UserIdentityContext mpc = new UserIdentityContext();
        UserIdentity userIdentity = new UserIdentity();
        userIdentity.setIdentity(testCntUserAlias);
        mpc.setIdentity(userIdentity);

        CommunoteReplyMessage testReplyMessage = new CommunoteReplyMessage();

        TransferMessage tm = new TransferMessage();
        TestMessage message = new TestMessage();
        message.setIdentityContext(mpc);
        EasyMock.expect(
                converterMock.convertToCommunoteMessage(tm, TestMessage.class))
                .andReturn(message);
        EasyMock.expect(
                converterMock.convertToTransferMessage(testReplyMessage))
                .andReturn(tm);
        EasyMock.expect(handlerMock.getHandledMessageClass()).andReturn(
                TestMessage.class);
        ValidationResult validationRes = new ValidationResult();
        validationRes.setValidationStatus(ValidationStatus.VALID);
        // EasyMock.expect(handlerMock.validateMessage(message)).andReturn(
        // validationRes);
        EasyMock.expect(handlerMock.handleMessage(message)).andReturn(
                testReplyMessage);

        EasyMock.replay(converterMock, handlerMock);

        // security mocks

        String currentUserAlias = "current_user_alias";

        User kenmeiUser = User.Factory.newInstance();
        User currentUser = User.Factory.newInstance();
        currentUser.setAlias(currentUserAlias);
        UserDetails currentUserDetails = createMock(UserDetails.class);

        expect(currentUserDetails.getUserAlias()).andReturn(currentUserAlias);
        expect(securityHelperMock.getCurrentUser()).andReturn(
                currentUserDetails);
        expect(userServiceMock.getUser(testCntUserAlias)).andReturn(kenmeiUser);
        expect(userServiceMock.getUser(currentUserAlias))
                .andReturn(currentUser);

        authenticationHelperMock.setSecurityContext(kenmeiUser);
        authenticationHelperMock.setSecurityContext(currentUser);

        EasyMock.replay(authenticationHelperMock, securityHelperMock,
                userServiceMock, currentUserDetails);

        ProviderMessageConsumerImpl<TestMessage> consumerImpl = new ProviderMessageConsumerImpl<TestMessage>();
        // consumerImpl.setAuthenticationHelper(authenticationHelperMock);
        // consumerImpl.setSecurityHelper(securityHelperMock);
        consumerImpl.setUserService(userServiceMock);
        consumerImpl.setMessageHandler(handlerMock);
        consumerImpl.setConverter(converterMock);
        consumerImpl.receiveMessage(tm);

        EasyMock.verify(handlerMock, authenticationHelperMock);
    }

}
