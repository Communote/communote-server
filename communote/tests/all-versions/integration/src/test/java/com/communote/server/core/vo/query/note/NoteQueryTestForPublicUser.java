package com.communote.server.core.vo.query.note;

import java.util.HashMap;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.NoteQueryParametersConfigurator;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for NoteQuery.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class NoteQueryTestForPublicUser extends CommunoteIntegrationTest {

    /**
     * Test that only notes of topics with enabled public access are returned when requested by the
     * public user.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testNoteRetrievalWithPublicUser() throws Exception {
        boolean previousPublicAccess = false;
        Long previousUserId = SecurityHelper.getCurrentUserId();
        try {
            User manager = TestUtils.createRandomUser(true);
            AuthenticationTestUtils.setSecurityContext(manager);
            previousPublicAccess = ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS
                    .getValue(ClientConfigurationHelper.DEFAULT_ALLOW_PUBLIC_ACCESS);
            CommunoteRuntime
                    .getInstance()
                    .getConfigurationManager()
                    .updateClientConfigurationProperty(
                            ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS, Boolean.toString(true));
            Blog topic1 = TestUtils.createRandomBlog(true, true, manager);
            Blog topic2 = TestUtils.createRandomBlog(true, true, manager);
            Long noteId1 = TestUtils.createAndStoreCommonNote(topic1, manager.getId(), "test1");
            TestUtils.createAndStoreCommonNote(topic2, manager.getId(), "test2");
            ServiceLocator.instance().getService(BlogRightsManagement.class)
            .changePublicAccess(topic1.getId(), true);
            // set public user
            AuthenticationTestUtils.setAuthentication(null);
            AuthenticationHelper.setPublicUserToSecurityContext(new MockHttpServletRequest());
            Assert.assertTrue(SecurityHelper.isPublicUser());
            // do a query for notes
            // TODO maybe restrict to notes of user created above to avoid conflicts with other
            // tests?
            NoteQuery noteQuery = QueryDefinitionRepository.instance().getQueryDefinition(
                    NoteQuery.class);
            NoteQueryParameters params = noteQuery.createInstance();
            NoteQueryParametersConfigurator configurer = new NoteQueryParametersConfigurator(
                    FilterWidgetParameterNameProvider.INSTANCE);
            configurer.configure(new HashMap<String, Object>(), params);
            PageableList<SimpleNoteListItem> result = ServiceLocator.instance()
                    .getService(QueryManagement.class).query(noteQuery, params);
            // check that only note of publicly available topic is contained
            Assert.assertEquals(result.size(), 1);
            Assert.assertEquals(result.get(0).getId(), noteId1);
        } finally {
            CommunoteRuntime
                    .getInstance()
                    .getConfigurationManager()
                    .updateClientConfigurationProperty(
                            ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS,
                            Boolean.toString(previousPublicAccess));

            if (previousUserId != null) {
                User previousUser = ServiceLocator.instance().getService(UserManagement.class)
                        .findUserByUserId(previousUserId);
                AuthenticationTestUtils.setSecurityContext(previousUser);
            } else {
                AuthenticationTestUtils.setAuthentication(null);
            }
        }
    }
}
