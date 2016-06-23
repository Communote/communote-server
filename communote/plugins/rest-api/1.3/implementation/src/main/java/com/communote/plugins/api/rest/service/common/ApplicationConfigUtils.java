package com.communote.plugins.api.rest.service.common;

import java.util.Set;

import com.communote.plugins.api.rest.service.exception.AuthenticationExceptionMapper;
import com.communote.plugins.api.rest.service.exception.AuthorizationExceptionMapper;
import com.communote.plugins.api.rest.service.exception.BlogAccessExceptionMapper;
import com.communote.plugins.api.rest.service.exception.BlogNotFoundExceptionMapper;
import com.communote.plugins.api.rest.service.exception.BlogValidationExceptionMapper;
import com.communote.plugins.api.rest.service.exception.CommunoteEntityNotFoundExceptionMapper;
import com.communote.plugins.api.rest.service.exception.ExceptionMapper;
import com.communote.plugins.api.rest.service.exception.ExtensionNotSupportedExceptionMapper;
import com.communote.plugins.api.rest.service.exception.IllegalArgumentExceptionMapper;
import com.communote.plugins.api.rest.service.exception.IllegalRequestParameterExceptionMapper;
import com.communote.plugins.api.rest.service.exception.JsonMappingExceptionMapper;
import com.communote.plugins.api.rest.service.exception.JsonParseExceptionMapper;
import com.communote.plugins.api.rest.service.exception.MaxLengthReachedExceptionMapper;
import com.communote.plugins.api.rest.service.exception.NoBlogManagerLeftExceptionMapper;
import com.communote.plugins.api.rest.service.exception.NotFoundExceptionMapper;
import com.communote.plugins.api.rest.service.exception.NoteManagementAuthorizationExceptionMapper;
import com.communote.plugins.api.rest.service.exception.NoteManagementExceptionMapper;
import com.communote.plugins.api.rest.service.exception.NoteNotFoundExceptionMapper;
import com.communote.plugins.api.rest.service.exception.NotePreProcessorExceptionMapper;
import com.communote.plugins.api.rest.service.exception.ParameterValidationExceptionMapper;
import com.communote.plugins.api.rest.service.exception.ResponseBuildExceptionMapper;
import com.communote.plugins.api.rest.service.exception.RestApiIllegalRequestParameterExceptionMapper;

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
        singletons.add(new AuthenticationExceptionMapper());
        singletons.add(new AuthorizationExceptionMapper());
        singletons.add(new BlogAccessExceptionMapper());
        singletons.add(new BlogNotFoundExceptionMapper());
        singletons.add(new BlogValidationExceptionMapper());
        singletons.add(new ExceptionMapper());
        singletons.add(new ExtensionNotSupportedExceptionMapper());
        singletons.add(new IllegalArgumentExceptionMapper());
        singletons.add(new IllegalRequestParameterExceptionMapper());
        singletons.add(new JsonMappingExceptionMapper());
        singletons.add(new JsonParseExceptionMapper());
        singletons.add(new CommunoteEntityNotFoundExceptionMapper());
        singletons.add(new MaxLengthReachedExceptionMapper());
        singletons.add(new NoBlogManagerLeftExceptionMapper());
        singletons.add(new NoteManagementAuthorizationExceptionMapper());
        singletons.add(new NoteManagementExceptionMapper());
        singletons.add(new NoteNotFoundExceptionMapper());
        singletons.add(new NotePreProcessorExceptionMapper());
        singletons.add(new NotFoundExceptionMapper());
        singletons.add(new ParameterValidationExceptionMapper());
        singletons.add(new ResponseBuildExceptionMapper());
        singletons.add(new RestApiIllegalRequestParameterExceptionMapper());
        return singletons;
    }

    /**
     * Default constuctor
     */
    private ApplicationConfigUtils() {
    }
}
