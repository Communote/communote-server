package com.communote.server.web.commons.filter;

import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.web.commons.filter.BypassSessionTimeoutFilter;

/**
 * Test for {@link BypassSessionTimeoutFilter}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class BypassSessionTimeoutFilterTest {

    /**
     * Test for BypassSessionTimeoutFilter#doFilter(ServletRequest,ServletResponse,FilterChain)
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testDoFilter() throws Exception {
        MockFilterConfig filterConfig = new MockFilterConfig();
        String requestParameterName = UUID.randomUUID().toString();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();
        Filter filter = new BypassSessionTimeoutFilter();
        try {
            filter.init(filterConfig);
            Assert.fail("This should fail, if there is no requestParameterName");
        } catch (ServletException e) {
            // Do nothing.
        }
        filterConfig.addInitParameter("requestParameterName", requestParameterName);
        filter.init(filterConfig);

        // Filter without session
        filter.doFilter(request, response, new MockFilterChain());
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        Assert.assertNull(session.getAttribute("lastInSessionRequestTime"));

        filter.doFilter(request, response, new MockFilterChain());
        Assert.assertNotNull(session.getAttribute("lastInSessionRequestTime"));

        session.setMaxInactiveInterval(Integer.MAX_VALUE);
        request.addParameter(requestParameterName, Boolean.TRUE.toString());
        filter.doFilter(request, response, new MockFilterChain());
        Assert.assertFalse(session.isInvalid());

        session.setMaxInactiveInterval(0);
        session.setAttribute("lastInSessionRequestTime", 0L);
        request.addParameter(requestParameterName, Boolean.TRUE.toString());
        filter.doFilter(request, response, new MockFilterChain());
        Assert.assertTrue(session.isInvalid());

    }
}
