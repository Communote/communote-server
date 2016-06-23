package com.communote.server.web.fe.portal.user.profile.controller;

import org.easymock.EasyMock;
import org.springframework.validation.BindException;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.messaging.NotificationManagement;
import com.communote.server.core.messaging.connector.MessagerConnector;
import com.communote.server.model.messaging.MessagerConnectorType;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;
import com.communote.server.web.commons.FormAction;
import com.communote.server.web.fe.portal.user.profile.forms.UserProfileNotificationsForm;

/**
 * Test for {@link UserProfileNotificationsController}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserProfileNotificationsControllerTest extends CommunoteIntegrationTest {
    /**
     * @return MessagerConnector
     */
    private MessagerConnector getConnector() {
        MessagerConnector messagerConnector = EasyMock.createMock(MessagerConnector.class);
        EasyMock.expect(messagerConnector.getId()).andReturn(MessagerConnectorType.XMPP.toString())
        .anyTimes();
        EasyMock.expect(messagerConnector.getUserMessagerId(EasyMock.anyLong())).andReturn("")
        .anyTimes();
        messagerConnector.enableUser(EasyMock.<String> anyObject());
        EasyMock.expectLastCall().anyTimes();
        messagerConnector.disableUser(EasyMock.<String> anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(messagerConnector);
        return messagerConnector;
    }

    /**
     * Tests submitting the form.
     *
     * @throws Exception
     *             in case the test failed
     */
    // TODO what is the goal of this test?
    @Test
    public void testUpdateUserProfile() throws Exception {
        MessagerConnector messagerConnector = getConnector();
        ServiceLocator.instance().getService(NotificationManagement.class)
        .registerMessagerConnector(messagerConnector);
        User user = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user);
        UserProfileNotificationsForm form = new UserProfileNotificationsForm();
        form.setAction(FormAction.UPDATE_USER_PROFILE);
        UserProfileNotificationsController controller = new UserProfileNotificationsController();
        form.setMail(true);
        controller.handleOnSubmit(null, null, form, new BindException(form, "errors"));
        form.setXmpp(true);
        controller.handleOnSubmit(null, null, form, new BindException(form, "errors"));
        form.setXmppFail(true);
        controller.handleOnSubmit(null, null, form, new BindException(form, "errors"));
        form.setXmppFail(false);
        controller.handleOnSubmit(null, null, form, new BindException(form, "errors"));
        form.setXmpp(false);
        controller.handleOnSubmit(null, null, form, new BindException(form, "errors"));
        form.setMail(false);
        controller.handleOnSubmit(null, null, form, new BindException(form, "errors"));
    }
}
