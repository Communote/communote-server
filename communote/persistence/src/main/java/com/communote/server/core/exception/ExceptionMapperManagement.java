package com.communote.server.core.exception;

import java.util.HashMap;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.core.exception.mapper.BlogAccessExceptionMapper;
import com.communote.server.core.exception.mapper.BlogIdentifierValidationExceptionMapper;
import com.communote.server.core.exception.mapper.BlogNotFoundExceptionMapper;
import com.communote.server.core.exception.mapper.BlogValidationExceptionMapper;
import com.communote.server.core.exception.mapper.CommunoteEntityNotFoundExceptionMapper;
import com.communote.server.core.exception.mapper.ExceptionExceptionMapper;
import com.communote.server.core.exception.mapper.ExternalObjectAlreadyAssignedExceptionMapper;
import com.communote.server.core.exception.mapper.ExternalObjectNotAssignedExceptionMapper;
import com.communote.server.core.exception.mapper.GroupNotFoundExceptionMapper;
import com.communote.server.core.exception.mapper.IllegalArgumentExceptionMapper;
import com.communote.server.core.exception.mapper.JsonMappingExceptionMapper;
import com.communote.server.core.exception.mapper.JsonParseExceptionMapper;
import com.communote.server.core.exception.mapper.MaxLengthReachedExceptionMapper;
import com.communote.server.core.exception.mapper.NoBlogManagerLeftExceptionMapper;
import com.communote.server.core.exception.mapper.NonUniqueBlogIdentifierExceptionMapper;
import com.communote.server.core.exception.mapper.NotFoundExceptionMapper;
import com.communote.server.core.exception.mapper.NoteManagementAuthorizationExceptionMapper;
import com.communote.server.core.exception.mapper.NoteManagementExceptionMapper;
import com.communote.server.core.exception.mapper.NoteNotFoundExceptionMapper;
import com.communote.server.core.exception.mapper.NotePreProcessorExceptionMapper;
import com.communote.server.core.exception.mapper.SpringAccessDeniedExceptionMapper;
import com.communote.server.core.exception.mapper.SwitchUserNotAllowedExceptionMapper;
import com.communote.server.core.exception.mapper.UserActivationValidationExceptionMapper;

/**
 * Service that provides means to map exceptions into a status object which describes the occurred
 * exception with a localizable message and other details. The translation into status objects is
 * accomplished by {@link ExceptionMapper}s which can be registered to this service.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class ExceptionMapperManagement {

    private final HashMap<Class<? extends Throwable>, ExceptionMapper<?>> mappers = new HashMap<Class<? extends Throwable>, ExceptionMapper<?>>();

    private final ExceptionMapper<Throwable> defaultExceptionMapper = new AbstractExceptionMapper<Throwable>(
            Throwable.class) {
        @Override
        public Status mapException(Throwable exception) {
            return new Status("exception.unknown", null, ErrorCodes.UNKNOWN_ERROR);
        }
    };

    {
        // TODO Annotate the mapper with @Component and remove from here.
        addExceptionMapper(new BlogAccessExceptionMapper());
        addExceptionMapper(new BlogIdentifierValidationExceptionMapper());
        addExceptionMapper(new BlogNotFoundExceptionMapper());
        addExceptionMapper(new BlogValidationExceptionMapper());
        addExceptionMapper(new ExceptionExceptionMapper());
        addExceptionMapper(new ExternalObjectAlreadyAssignedExceptionMapper());
        addExceptionMapper(new ExternalObjectNotAssignedExceptionMapper());
        addExceptionMapper(new IllegalArgumentExceptionMapper());
        addExceptionMapper(new JsonMappingExceptionMapper());
        addExceptionMapper(new JsonParseExceptionMapper());
        addExceptionMapper(new GroupNotFoundExceptionMapper());
        addExceptionMapper(new CommunoteEntityNotFoundExceptionMapper());
        addExceptionMapper(new MaxLengthReachedExceptionMapper());
        addExceptionMapper(new NoBlogManagerLeftExceptionMapper());
        addExceptionMapper(new NonUniqueBlogIdentifierExceptionMapper());
        addExceptionMapper(new NoteManagementAuthorizationExceptionMapper());
        addExceptionMapper(new NoteManagementExceptionMapper());
        addExceptionMapper(new NoteNotFoundExceptionMapper());
        addExceptionMapper(new NotePreProcessorExceptionMapper());
        addExceptionMapper(new NotFoundExceptionMapper());
        addExceptionMapper(new SpringAccessDeniedExceptionMapper());
        addExceptionMapper(new SwitchUserNotAllowedExceptionMapper());
        addExceptionMapper(new UserActivationValidationExceptionMapper());

    }

    /**
     * Adds an {@link ExceptionMapper} to the list of mappers. Existing mappers for the given
     * exception will be replaced.
     *
     * @param exceptionMapper
     *            The exception mapper.
     * @param <T>
     *            Type of the exception.
     */
    public <T extends Throwable> void addExceptionMapper(ExceptionMapper<T> exceptionMapper) {
        mappers.put(exceptionMapper.getExceptionClass(), exceptionMapper);
    }

    /**
     * Adds a list of {@link ExceptionMapper} to the list of mappers. Existing mappers for the given
     * exception will be replaced.
     *
     * @param exceptionMappers
     *            Set of exception mappers to add.
     * @param <T>
     *            Type of the exception.
     */
    @Autowired
    public <T extends Throwable> void addExceptionMappers(Set<ExceptionMapper<T>> exceptionMappers) {
        for (ExceptionMapper<T> exceptionMapper : exceptionMappers) {
            addExceptionMapper(exceptionMapper);
        }
    }

    /**
     * Maps the given exception to a status.
     *
     * @param exception
     *            Exception to map.
     * @param <T>
     *            Type of the exception.
     * @return Status describing the exception.
     *
     */
    public <T extends Throwable> Status mapException(T exception) {
        return mapException(exception, exception.getClass());
    }

    /**
     * @param exception
     *            Exception to map.
     * @param clazz
     *            The class of the exception.
     * @param <T>
     *            Type of the exception.
     * @return The status.
     */
    private <T extends Throwable> Status mapException(T exception, Class<?> clazz) {
        ExceptionMapper<T> exceptionMapper = (ExceptionMapper<T>) mappers.get(clazz);
        if (exceptionMapper != null) {
            return exceptionMapper.mapException(exception);
        }
        Class<?> superclass = clazz.getSuperclass();
        if (Throwable.class.isAssignableFrom(superclass)) {
            return mapException(exception, superclass);
        }
        return defaultExceptionMapper.mapException(exception);
    }

    /**
     * Removes the given mapper.
     *
     * @param exceptionMapper
     *            The mapper to remove.
     * @param <T>
     *            Type of the exception.
     */
    public <T extends Throwable> void removeExceptionMapper(ExceptionMapper<T> exceptionMapper) {
        ExceptionMapper<?> mapper = mappers.get(exceptionMapper.getExceptionClass());
        if (mapper == exceptionMapper) {
            mappers.remove(mapper.getExceptionClass());
        }
    }
}
