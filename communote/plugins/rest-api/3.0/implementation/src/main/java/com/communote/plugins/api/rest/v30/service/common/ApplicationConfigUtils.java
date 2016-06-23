package com.communote.plugins.api.rest.v30.service.common;

import java.util.Set;

import com.communote.plugins.api.rest.v30.service.exception.mapper.CommunoteExceptionMapper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class ApplicationConfigUtils {

    /**
     * Get extented singleton set with exception mappers
     * 
     * @param singletons
     *            set of root resource and provider instances
     * @return set of objects
     */
    public static Set<Object> extentSingletonsWithExceptionMappers(Set<Object> singletons) {
        singletons.add(new CommunoteExceptionMapper());
        return singletons;
    }

    /**
     * Default constuctor
     */
    private ApplicationConfigUtils() {
    }
}
