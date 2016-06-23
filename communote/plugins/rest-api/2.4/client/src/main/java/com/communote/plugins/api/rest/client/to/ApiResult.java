package com.communote.plugins.api.rest.client.to;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * The resulting object of an api call is encapsulated into this class. A status defines if the call
 * has been succeeded or not. The message gives an hint on the error case. The actual result object
 * is stored in {@link #result}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <T>
 *            the capsuled resource
 */
@JsonSerialize(include = Inclusion.NON_NULL)
public class ApiResult<T> implements Serializable {

    /** View class for development purposes. */
    public static class DevelopmentView extends PublicView {
    };

    /** View class for development purposes. */
    public static class PublicView {
    };

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

    private static ObjectWriter OBJECT_WRITER = new ObjectMapper().writerWithView(PublicView.class);

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String message;
    private String status;
    private T result;
    private Map<String, Object> metaData;
    private List<ApiResultError> errors = new ArrayList<ApiResultError>();

    /**
     * @return the errors
     */
    public List<ApiResultError> getErrors() {
        return errors;
    }

    /**
     * Get a message, mainly used in case of errors
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get a map of meta data
     * 
     * @return map of meta data
     */
    public Map<String, Object> getMetaData() {
        return metaData;
    }

    /**
     * @return the result object
     */
    public T getResult() {
        return result;
    }

    /**
     * @return the state
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param errors
     *            the errors to set
     */
    public void setErrors(List<ApiResultError> errors) {
        this.errors = errors;
    }

    /**
     * @param message
     *            an message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Set the meta data for the response
     * 
     * @param metaData
     *            map with meta data to extend the result
     */
    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    /**
     * @param result
     *            the result object
     */
    public void setResult(T result) {
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
        try {
            return OBJECT_WRITER.writeValueAsString(this);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    /**
     * Writes the object in json format to the given stream.
     * 
     * @param outputStream
     *            The stream to write the object to.
     * @throws IOException
     *             Exception.
     */
    public void writeToOutputStream(OutputStream outputStream) throws IOException {
        OBJECT_WRITER.writeValue(outputStream, this);
    }

}
