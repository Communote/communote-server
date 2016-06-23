package com.communote.server.web.api.to;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.communote.server.api.util.JsonHelper;

/**
 * The resulting object of an api call is encapsualted into this class. A status defines if the call
 * has been succeeded or not. The message gives an hint on the error case. The actual result object
 * is stored in {@link #result}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
// TODO make the type of result generic.
@Deprecated
// using ALWAYS for compatibility with replaced org.json framework which returned null values
@JsonSerialize(include = Inclusion.ALWAYS)
public class ApiResult implements Serializable {

    /**
     * The result status.
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    public enum ResultStatus {
        /** everything has been ok */
        OK,
        /** something went wrong - a warning **/
        WARNING,
        /** an error occured */
        ERROR
    }

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(ApiResult.class);

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String message;
    private String status;
    private Object result;

    /**
     * Get a message, mainly used in case of errors
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the result object
     */
    public Object getResult() {
        return result;
    }

    /**
     * @return the state
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param message
     *            an message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @param result
     *            the result object
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * Sets the status (must be of a value defined in {@link ResultStatus}
     * 
     * @param status
     *            status to be set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            JsonHelper.getSharedObjectMapper().writeValue(writer, this);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return writer.toString();
    }
}
