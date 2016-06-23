package com.communote.server.web.api.service.blog;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.core.filter.listitems.blog.UserDetailBlogListItem;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;
import com.communote.server.test.ldap.LdapCommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;
import com.communote.server.test.util.UserAndGroupTestUtils;
import com.communote.server.web.api.service.IllegalRequestParameterException;
import com.communote.server.web.api.service.RequestedResourceNotFoundException;
import com.communote.server.web.api.to.ApiResult;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogApiControllerTest extends LdapCommunoteIntegrationTest {

    /**
     * Test class to have direct access to the method.
     */
    private class TestBlogApiController extends BlogApiController {
        @Override
        public Object doGet(ApiResult apiResult, HttpServletRequest request,
                HttpServletResponse response) throws RequestedResourceNotFoundException,
                IllegalRequestParameterException {
            return super.doGet(apiResult, request, response);
        }
    }

    private String externalSystemId;

    /**
     * @throws Exception
     *             in case the test failed
     */
    @BeforeClass(dependsOnGroups = { "ldap-test-setup", "integration-test-setup" })
    public void setup() throws Exception {
        AuthenticationTestUtils.setManagerContext();
        LdapConfiguration ldapConfiguration = createLdapConfiguration();
        ldapConfiguration.setAllowExternalAuthentication(true);
        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateLdapConfiguration(ldapConfiguration);
        externalSystemId = ldapConfiguration.getSystemId();
    }

    /**
     * Test for KENMEI-4218: user role is not set if user is member of an LDAP group
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForKENMEI4218() throws Exception {
        User localUser = TestUtils.createRandomUser(true);
        User externalUser = TestUtils.createRandomUser(false, externalSystemId);
        Group externalGroup = UserAndGroupTestUtils.createRandomGroup(externalSystemId,
                externalUser);
        Long blogId = TestUtils.createRandomBlog(false, false, localUser).getId();
        AuthenticationTestUtils.setSecurityContext(localUser);
        ServiceLocator.findService(BlogRightsManagement.class).addEntity(blogId,
                externalGroup.getId(), BlogRole.MEMBER);

        TestBlogApiController blogApiController = new TestBlogApiController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("test");
        request.setServletPath("/api/v1.0.1/blogs");
        request.addParameter("blogListType", "READ");
        request.setRequestURI("http://localhost/test/api/v1.0.1/blogs");
        Object result = blogApiController.doGet(new ApiResult(), request,
                new MockHttpServletResponse());
        Assert.assertTrue(result instanceof List<?>);
        request.setContextPath("test");
        request.setServletPath("/api/v1.0.1/blogs/" + blogId + ".json");
        request.setRequestURI("http://localhost/test/api/v1.0.1/blogs/" + blogId + ".json");

        // Local user
        result = blogApiController.doGet(new ApiResult(), request, new MockHttpServletResponse());
        Assert.assertEquals(((UserDetailBlogListItem) result).getUserRole(), BlogRole.MANAGER);

        // External user
        AuthenticationTestUtils.setSecurityContext(externalUser);
        result = blogApiController.doGet(new ApiResult(), request, new MockHttpServletResponse());
        Assert.assertEquals(((UserDetailBlogListItem) result).getUserRole(), BlogRole.MEMBER);
    }
}
