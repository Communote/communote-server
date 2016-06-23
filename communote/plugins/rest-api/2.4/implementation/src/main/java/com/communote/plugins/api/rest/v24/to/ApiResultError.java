package com.communote.plugins.api.rest.v24.to;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.annotate.JsonView;

import com.communote.plugins.api.rest.v24.to.ApiResult.DevelopmentView;


/**
 * This class represents errors, which can occur, while speaking to the rest api.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@JsonSerialize(include = Inclusion.NON_NULL)
public class ApiResultError {

    private String cause;
    private String message;

    @JsonView(DevelopmentView.class)
    private String exceptionMessage;

    @JsonIgnore
    private Throwable exception;

    /**
     * @return Name of the field, which caused the error. If this is null, it is an global error not
     *         bound to any field.
     */
    public String getCause() {
        return cause;
    }

    /**
     * @return The exception, which was the cause for this error.
     */
    @JsonIgnore
    public Throwable getException() {
        return exception;
    }

    /**
     * @return the exceptionMessage
     */
    @JsonView(DevelopmentView.class)
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param cause
     *            the cause to set
     */
    public void setCause(String cause) {
        this.cause = cause;
    }

    /**
     * @param exception
     *            the exception to set
     */
    @JsonIgnore
    public void setException(Throwable exception) {
        this.exception = exception;
    }

    /**
     * @param exceptionMessage
     *            the exceptionMessage to set
     */
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
