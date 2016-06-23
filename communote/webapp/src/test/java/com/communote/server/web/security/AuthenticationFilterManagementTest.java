package com.communote.server.web.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.core.security.AbstractCommunoteAuthenticationFilter;
import com.communote.server.core.security.AuthenticationFilterManagement;
import com.communote.server.core.security.CommunoteAuthenticationFilter;
import com.communote.server.web.filter.PluginAuthenticationFilter;

/**
 * Test for correct handling of PluginAuthenticationFilter.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthenticationFilterManagementTest {

    /**
     * Filter for testing.
     */
    private class TestCommunoteFilter extends AbstractCommunoteAuthenticationFilter {

        private final int priority;
        private final String stringToBeAdded;
        private final StringBuilder stringToAdd;

        /**
         * Constructor
         * 
         * @param priority
         *            Priority of this filter.
         * @param stringToBeAdded
         *            The string to be added, when the filtering is done.
         * @param stringToAdd
         *            The string to add.
         */
        public TestCommunoteFilter(int priority, String stringToBeAdded, StringBuilder stringToAdd) {
            this.priority = priority;
            this.stringToBeAdded = stringToBeAdded;
            this.stringToAdd = stringToAdd;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            stringToAdd.append(stringToBeAdded);
            chain.doFilter(request, response);
        }

        /**
         * @return The priority.
         */
        @Override
        public int getOrder() {
            return priority;
        }

    }

    /**
     * Filter chain for testing.
     */
    private class TestFilterChain implements FilterChain {

        private int counter = -1;
        private final Filter[] filters;

        /**
         * Constructor.
         * 
         * @param filters
         *            Filters of this chain.
         */
        public TestFilterChain(Filter... filters) {
            this.filters = filters;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException,
                ServletException {
            counter++;
            if (counter < filters.length) {
                filters[counter].doFilter(request, response, this);
            }
        }

        /**
         * @param request
         *            The request.
         * @param response
         *            The response.
         * @param resetCounter
         *            Counter will be reset, when true.
         * @throws Exception
         *             Exception.
         */
        public void doFilter(ServletRequest request, ServletResponse response, boolean resetCounter)
                throws Exception {
            counter = resetCounter ? -1 : counter;
            doFilter(request, response);
        }

    }

    /**
     * Tests, that all filters are executed within the correct order.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testDoFilter() throws Exception {
        AuthenticationFilterManagement filterManagement = new AuthenticationFilterManagement();
        StringBuilder stringToBuild = new StringBuilder();
        // Standard filters.
        Filter firstFilter = new TestCommunoteFilter(0, "A", stringToBuild);
        PluginAuthenticationFilter pluginFilter = new PluginAuthenticationFilter();
        pluginFilter.setFilterManagement(filterManagement);
        Filter lastFilter = new TestCommunoteFilter(0, "E", stringToBuild);

        // Plugin filters.
        CommunoteAuthenticationFilter lowPriorityFilter = new TestCommunoteFilter(0, "B",
                stringToBuild);
        CommunoteAuthenticationFilter midPriorityFilter = new TestCommunoteFilter(500, "C",
                stringToBuild);
        CommunoteAuthenticationFilter highPriorityFilter = new TestCommunoteFilter(1000, "D",
                stringToBuild);

        TestFilterChain chain = new TestFilterChain(firstFilter, pluginFilter, lastFilter);

        // Test without filters
        chain.doFilter(null, null, true);
        Assert.assertEquals(stringToBuild.toString(), "AE");

        // Test with 1 additional filter
        stringToBuild.delete(0, 10);
        filterManagement.addFilter(highPriorityFilter);
        chain.doFilter(null, null, true);
        Assert.assertEquals(stringToBuild.toString(), "ADE");

        // Test with 2 additional filter
        stringToBuild.delete(0, 10);
        filterManagement.addFilter(lowPriorityFilter);
        chain.doFilter(null, null, true);
        Assert.assertEquals(stringToBuild.toString(), "ADBE");

        // Test with 3 additional filter
        stringToBuild.delete(0, 10);
        filterManagement.addFilter(midPriorityFilter);
        chain.doFilter(null, null, true);
        Assert.assertEquals(stringToBuild.toString(), "ADCBE");

        // Test with 2 additional filter
        stringToBuild.delete(0, 10);
        filterManagement.removeFilter(highPriorityFilter);
        chain.doFilter(null, null, true);
        Assert.assertEquals(stringToBuild.toString(), "ACBE");

        // Test with 1 additional filter
        stringToBuild.delete(0, 10);
        filterManagement.removeFilter(lowPriorityFilter);
        chain.doFilter(null, null, true);
        Assert.assertEquals(stringToBuild.toString(), "ACE");
    }
}
