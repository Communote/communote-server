package com.communote.server.core.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.stereotype.Service;

import com.communote.common.util.DescendingOrderComparator;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class AuthenticationFilterManagement {

    /**
     * Copied from org.springframework.web.filter.CompositeFilter.VirtualFilterChain as
     * CompositeFilter doesn't allow custom ordering.
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    private static class VirtualFilterChain implements FilterChain {
        private final FilterChain originalChain;
        private final List<CommunoteAuthenticationFilter> additionalFilters;
        private int currentPosition = 0;

        /**
         * Constructor.
         * 
         * @param chain
         *            Original chain.
         * @param additionalFilters
         *            Additional filters.
         */
        private VirtualFilterChain(FilterChain chain,
                List<CommunoteAuthenticationFilter> additionalFilters) {
            this.originalChain = chain;
            this.additionalFilters = additionalFilters;
        }

        /**
         * Executes the filtering.
         * 
         * @param request
         *            The request.
         * @param response
         *            The response.
         * @throws IOException
         *             Exception.
         * @throws ServletException
         *             Exception.
         */
        @Override
        public void doFilter(final ServletRequest request, final ServletResponse response)
                throws IOException,
                ServletException {
            if (currentPosition == additionalFilters.size()) {
                originalChain.doFilter(request, response);
            } else {
                currentPosition++;
                Filter nextFilter = additionalFilters.get(currentPosition - 1);
                nextFilter.doFilter(request, response, this);
            }
        }

    }

    private final List<CommunoteAuthenticationFilter> filters = new ArrayList<CommunoteAuthenticationFilter>();

    private final DescendingOrderComparator descendingOrderComparator = new DescendingOrderComparator();

    /**
     * Adds a filter to the chain.
     * 
     * @param filter
     *            The filter filter to add.
     */
    public void addFilter(CommunoteAuthenticationFilter filter) {
        filters.add(filter);
        Collections.sort(filters, descendingOrderComparator);
    }

    /**
     * Executes the chain with the additional filters.
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param chain
     *            The original chain.
     * @throws IOException
     *             Exception.
     * @throws ServletException
     *             Exception.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        new VirtualFilterChain(chain, filters).doFilter(request, response);
    }

    /**
     * Removes a filter from the chain.
     * 
     * @param filter
     *            The filter to remove.
     */
    public void removeFilter(CommunoteAuthenticationFilter filter) {
        filters.remove(filter);
    }
}
