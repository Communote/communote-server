package com.communote.plugins.core.registries;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.exception.ExceptionMapper;
import com.communote.server.core.exception.ExceptionMapperManagement;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate
public class ExceptionMapperRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapperRegistry.class);
    private final ExceptionMapperManagement exceptionMapperManagement = ServiceLocator.instance()
            .getService(ExceptionMapperManagement.class);

    /**
     * Adds an {@link ExceptionMapper} to the list of mappers. Existing mappers for the given
     * exception will be replaced.
     * 
     * @param exceptionMapper
     *            The exception mapper.
     * @param <T>
     *            Type of the exception.
     */
    @Bind(id = "exceptionMapperRegistrationHook", optional = true, aggregate = true)
    public <T extends Throwable> void addExceptionMapper(ExceptionMapper<T> exceptionMapper) {
        exceptionMapperManagement.addExceptionMapper(exceptionMapper);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Added exception mapper " + exceptionMapper.getClass().getName());
        }
    }

    /**
     * Removes the given mapper.
     * 
     * @param exceptionMapper
     *            The mapper to remove.
     * @param <T>
     *            Type of the exception.
     */
    @Unbind(id = "exceptionMapperRegistrationHook", optional = true, aggregate = true)
    public <T extends Throwable> void removeExceptionMapper(ExceptionMapper<T> exceptionMapper) {
        exceptionMapperManagement.removeExceptionMapper(exceptionMapper);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removed exception mapper " + exceptionMapper.getClass().getName());
        }
    }
}
