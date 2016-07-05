package com.communote.service.exception;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.exception.ExceptionMapperManagement;
import com.communote.server.core.exception.Status;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExceptionMapperManagementTest {
    /**
     * Test exception.
     */
    private class MyIllegalArgumentException extends IllegalArgumentException {
        private static final long serialVersionUID = 4867610766275225930L;
    }

    /**
     * Test for adding and removing mappers.
     */
    @Test
    public void testExceptionMapperManagement() {
        String errorCode = UUID.randomUUID().toString();
        ExceptionMapperManagement management = new ExceptionMapperManagement();
        TestExceptionMapper<IllegalArgumentException> exceptionMapper = new TestExceptionMapper<IllegalArgumentException>(
                IllegalArgumentException.class, "1", errorCode);
        management.addExceptionMapper(exceptionMapper);
        // Direct mapping
        Status status = management.mapException(new IllegalArgumentException());
        Assert.assertEquals(status.getErrorCode(), errorCode);
        // Inherited mapping
        status = management.mapException(new MyIllegalArgumentException());
        Assert.assertEquals(status.getErrorCode(), errorCode);
        // No mapping (fallback)
        status = management.mapException(new NullPointerException());
        Assert.assertEquals(status.getErrorCode(), ErrorCodes.INTERNAL_ERROR);

        // Remove mapper -> Unknown
        management.removeExceptionMapper(exceptionMapper);
        status = management.mapException(new MyIllegalArgumentException());
        Assert.assertEquals(status.getErrorCode(), ErrorCodes.INTERNAL_ERROR);
        status = management.mapException(new IllegalArgumentException());
        Assert.assertEquals(status.getErrorCode(), ErrorCodes.INTERNAL_ERROR);
    }
}
