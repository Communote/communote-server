package com.communote.server.api.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for working with JSON.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class JsonHelper {
    // shared mapper
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static Logger LOG = LoggerFactory.getLogger(JsonHelper.class);

    /**
     * Returns a pre-configured ObjectMapper which (and especially its JSON and node factories) can
     * be reused. Because the mapper is shared, it must not be configured by the caller.
     *
     * @return the shared, pre-configured ObjectMapper.
     */
    public static ObjectMapper getSharedObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Deserialize a JSON string into a JSON tree. If the string cannot be converted null will be
     * returned.
     * 
     * @param jsonString
     *            the string to parse
     * @return the JSON tree or null
     */
    public static JsonNode readJsonTree(String jsonString) {
        if (StringUtils.isNotBlank(jsonString)) {
            try {
                return OBJECT_MAPPER.readTree(jsonString);
            } catch (IOException e) {
                LOG.error("Deserializing JSON from string {} failed", jsonString, e);
            }
        }
        return null;
    }

    /**
     * Serialize a JSON tree.
     *
     * @param writer
     *            the writer to write to
     * @param rootNode
     *            the root node of the JSON object structure
     * @throws IOException
     *             in case of an exception while serializing the json object
     */
    public static void writeJsonTree(Writer writer, JsonNode rootNode) throws IOException {
        // reuse factory for efficiency
        JsonGenerator generator = OBJECT_MAPPER.getJsonFactory().createJsonGenerator(writer);
        OBJECT_MAPPER.writeTree(generator, rootNode);
    }

    /**
     * Converts a JSON tree into a string and ignores exceptions. In case of an exception the result
     * will be the empty string.
     *
     * @param rootNode
     *            the root node of the tree, can be null to produce a null node
     * @return the serialized JSON object
     */
    public static String writeJsonTreeAsString(JsonNode rootNode) {
        if (rootNode == null) {
            rootNode = JsonHelper.getSharedObjectMapper().getNodeFactory().nullNode();
        }
        StringWriter writer = new StringWriter();
        try {
            JsonHelper.writeJsonTree(writer, rootNode);
            return writer.toString();
        } catch (IOException e) {
            LOG.error("Serializing JSON object to string failed", e);
        }
        return "";
    }

    /**
     * Private constructor for utility class
     */
    private JsonHelper() {
        // nothing
    }
}
