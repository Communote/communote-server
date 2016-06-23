package com.communote.server.web.commons.helper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.util.JsonHelper;

/**
 * Util to work with JSON requests and responses.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class JsonRequestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonRequestHelper.class);
    /**
     * Create a JSON error response object
     *
     * @param message
     *            a message to add to the response, can be null
     * @return the JSON
     */
    public static ObjectNode createJsonErrorResponse(String message) {
        return JsonRequestHelper.createJsonResponse(false, message);
    }

    /**
     * Create a JSON response object
     *
     * @param success
     *            whether a success or error response should be created
     * @param message
     *            the message to put into the response object, can be null
     * @return the JSON
     */
    private static ObjectNode createJsonResponse(boolean success, String message) {
        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        // using same status values as REST API
        jsonResponse.put("status", success ? "OK" : "ERROR");
        if (message != null) {
            jsonResponse.put("message", message);
        }
        return jsonResponse;
    }

    /**
     * Create a JSON success response object
     *
     * @param message
     *            a message to add to the response, can be null
     * @param result
     *            a result object to add to the response, can be null
     * @return the JSON
     */
    public static ObjectNode createJsonSuccessResponse(String message, ObjectNode result) {
        ObjectNode jsonResponse = JsonRequestHelper.createJsonResponse(true, message);
        if (result != null) {
            jsonResponse.put("result", result);
        }
        return jsonResponse;
    }

    /**
     * Parse the mime string for a relative quality factor as described in rfc2616
     *
     * @param mime
     *            the mime string
     * @return the quality factor
     */
    private static float extractQualityFactor(String mime) {
        int idx = mime.indexOf(';');
        if (idx > 0) {
            idx = mime.indexOf("q=", idx);
            if (idx > 0 && mime.length() > idx + 2) {
                int nextParamIdx = mime.indexOf(';', idx + 2);
                if (nextParamIdx < 0) {
                    mime = mime.substring(idx + 2);
                } else {
                    mime = mime.substring(idx + 2, nextParamIdx);
                }
                mime = mime.trim();
                float qualifier;
                try {
                    qualifier = Float.parseFloat(mime);
                } catch (NumberFormatException e) {
                    qualifier = 0;
                }
                return qualifier;
            }
        }
        // if no qualifier parameter is given, default to 1
        return 1f;
    }

    /**
     * Test whether a JSON response should be returned by inspecting the Accept header.
     *
     * @param request
     *            the request
     * @return true if a JSON response is to be returned
     */
    public static boolean isJsonResponseRequested(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        float jsonQuality = 0f;
        float htmlQuality = 0f;
        float catchAllQuality = 0f;
        if (acceptHeader != null) {
            String[] acceptedMimes = acceptHeader.toLowerCase().split(",");
            for (String mime : acceptedMimes) {
                if (mime.contains("application/json")) {
                    jsonQuality = extractQualityFactor(mime);
                    if (jsonQuality == 1f) {
                        return true;
                    }
                } else if (mime.contains("text/html")) {
                    htmlQuality = extractQualityFactor(mime);
                } else if (mime.contains("text/*")) {
                    float qualifier = extractQualityFactor(mime);
                    if (qualifier > htmlQuality) {
                        htmlQuality = qualifier;
                    }
                } else if (mime.contains("*/*")) {
                    catchAllQuality = extractQualityFactor(mime);
                }
            }
        }
        return jsonQuality > htmlQuality || catchAllQuality > htmlQuality;
    }

    /**
     * Write a JSON object to the response.
     *
     * @param response
     *            the response
     * @param json
     *            the JSON object to write
     * @throws IOException
     *             in case of an exception while serializing the json object
     */
    public static void writeJsonResponse(HttpServletResponse response, ObjectNode json)
            throws IOException {
        response.setContentType("application/json");
        JsonHelper.writeJsonTree(response.getWriter(), json);
    }
    /**
     * Write a JSON error to the response.
     *
     * @param response
     *            the response
     * @param errorMessage
     *            the message to send
     * @throws IOException
     *             in case of an exception while serializing the json object
     */
    public static void writeJsonErrorResponse(HttpServletResponse response, String errorMessage)
            throws IOException {
        ObjectNode node = JsonRequestHelper.createJsonErrorResponse(errorMessage);
        writeJsonResponse(response, node);
    }
    
    /**
     * Write a JSON error to the response without throwing exceptions in case of errors.
     *
     * @param response
     *            the response
     * @param errorMessage
     *            the message to send
     */
    public static void writeJsonErrorResponseQuietly(HttpServletResponse response, String errorMessage) {
        ObjectNode node = JsonRequestHelper.createJsonErrorResponse(errorMessage);
        try {
            writeJsonResponse(response, node);
        } catch (Exception e) {
            LOGGER.error("Error writing JSON error message {}", errorMessage, e);
        }
    }
}
