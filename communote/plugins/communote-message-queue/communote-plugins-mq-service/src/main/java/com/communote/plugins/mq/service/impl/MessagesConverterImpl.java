package com.communote.plugins.mq.service.impl;

import java.io.IOException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.service.exception.MessageParsingException;
import com.communote.plugins.mq.service.provider.MessagesConverter;
import com.communote.plugins.mq.service.provider.TransferMessage;
import com.communote.plugins.mq.service.provider.TransferMessage.TMContentType;

/**
 * Converts transfer message to the CNT specific and vice versa.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "mq.MessagesConverter")
@Provides
public class MessagesConverterImpl implements MessagesConverter {

    /** The LOG. */
    private static Logger LOGGER = LoggerFactory.getLogger(MessagesConverterImpl.class);

    /** The mapper. */
    private ObjectMapper mapper;

    private final TMContentType contentType = TransferMessage.TMContentType.JSON;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends BaseMessage> T convertToCommunoteMessage(
            TransferMessage messageToBeConverted, Class<T> messageClass)
            throws MessageParsingException {
        T res = null;
        if (contentType.equals(messageToBeConverted.getContentType())) {
            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            try {
                /*
                 * use class loader of message class to get Jackson's deserialization working with
                 * class name based typeId resolving (@class property). However, this only works if
                 * the message class and the type to be resolved are part of the OSGI bundle.
                 * therefore we should find a better solution like a registry based TypeIdResolver.
                 */
                // TODO find a better solution which doesn't require to fiddle with the classloader
                Thread.currentThread().setContextClassLoader(
                        messageClass.getClassLoader());
                res = mapper.readValue(messageToBeConverted.getContent(),
                        messageClass);
            } catch (JsonParseException e) {
                LOGGER.debug(
                        "Non-well formed JSON content was encountered during message parsing: {}",
                        e.getMessage());
                throw new MessageParsingException(
                        "Non-well formed JSON content was encountered during message parsing: "
                                + e.getMessage(), e, false);
            } catch (JsonMappingException e) {
                LOGGER.debug("Exception during JSON to message mapping: " + e.getMessage(), e);
                throw new MessageParsingException(
                        "Exception during JSON to message mapping: " + e.getMessage()
                                + e.getMessage(), e, false);
            } catch (IOException e) {
                LOGGER.error("IO Exception during JSON parsing: {}", e.getMessage());
                // if it is an io error we should not send details back but a general error message
                throw new MessageParsingException(
                        "IO Exception during JSON parsing: "
                                + e.getMessage(), e, true);
            } finally {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
        }
        if (res == null) {
            throw new MessageParsingException("Parsing Message resulted in null message!", false);
        }
        return res;
    }

    /**
     * Convert to transfer message.
     * 
     * @param <T>
     *            the generic type
     * @param messageToBeConverted
     *            message to be converted
     * @return transfer message
     */
    @Override
    public <T extends BaseMessage> TransferMessage convertToTransferMessage(
            T messageToBeConverted) {
        TransferMessage result = null;
        try {
            result = new TransferMessage();
            result.setContentType(contentType);
            String jsonContent = mapper
                    .writeValueAsString(messageToBeConverted);
            result.setContent(jsonContent);
        } catch (JsonParseException e) {
            LOGGER.error("Non-well formed JSON content was encountered during message parsing: {}",
                    e.getMessage());
        } catch (JsonMappingException e) {
            LOGGER.error("Exception during JSON to message mapping: {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("IO Exception during JSON parsing: {}", e.getMessage());
        }
        return result;
    }

    /**
     * 
     * @return the content type used for serialization
     */
    @Override
    public TMContentType getContentType() {
        return contentType;
    }

    /**
     * Inits the.
     */
    @Validate
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(
                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
                JsonTypeInfo.As.PROPERTY);
        DeserializerProvider p = mapper.getDeserializerProvider();
        // mapper.setPropertyNamingStrategy(new
        // DotNetTollerantNamingStrategy());
        mapper.setDeserializerProvider(p);
        setMapper(mapper);
    }

    /**
     * Sets the mapper.
     * 
     * @param mapper
     *            the new mapper
     */
    void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

}
