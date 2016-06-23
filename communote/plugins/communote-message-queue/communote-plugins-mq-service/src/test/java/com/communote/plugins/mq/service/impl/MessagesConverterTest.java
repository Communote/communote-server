package com.communote.plugins.mq.service.impl;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.service.exception.MessageParsingException;
import com.communote.plugins.mq.service.provider.TransferMessage;
import com.communote.plugins.mq.service.provider.TransferMessage.TMContentType;

/**
 * The Class TestMessagesConverter.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessagesConverterTest {

    /**
     * The Class TestMessage.
     */
    private class TestMessage extends BaseMessage {

    }

    /** The mapper mock. */
    private ObjectMapper mockObjectMapper;

    /** The converter. */
    private MessagesConverterImpl converter;

    /**
     * Inits the mocks.
     */
    @BeforeMethod
    public void init() {
        converter = new MessagesConverterImpl();
        mockObjectMapper = EasyMock.createMock(ObjectMapper.class);
    }

    /**
     * Test convert to cnt message.
     * 
     * @throws MessageParsingException
     *             in case parsing went wrong
     * @throws JsonParseException
     *             the json parse exception
     * @throws JsonMappingException
     *             the json mapping exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testConvertToCNTMessage() throws MessageParsingException, JsonParseException,
            JsonMappingException, IOException {
        TransferMessage tm = new TransferMessage();
        String testContent = "Test Content";
        tm.setContentType(TMContentType.JSON);
        tm.setContent(testContent);
        TestMessage testMessage = new TestMessage();
        EasyMock.expect(mockObjectMapper.readValue(testContent, TestMessage.class))
                .andReturn(testMessage);
        mockObjectMapper.registerSubtypes(new Class[0]);
        EasyMock.replay(mockObjectMapper);
        converter.setMapper(mockObjectMapper);
        TestMessage res = converter.convertToCommunoteMessage(tm,
                TestMessage.class);
        Assert.assertEquals(res, testMessage);
    }

    /**
     * Test convert to transfer message.
     * 
     * @throws JsonGenerationException
     *             the json generation exception
     * @throws JsonMappingException
     *             the json mapping exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testConvertToTransferMessage() throws JsonGenerationException,
            JsonMappingException, IOException {
        TestMessage testMessage = new TestMessage();
        String testContent = "Test Content";
        EasyMock.expect(mockObjectMapper.writeValueAsString(testMessage)).andReturn(
                testContent);
        EasyMock.replay(mockObjectMapper);
        converter.setMapper(mockObjectMapper);
        TransferMessage res = converter.convertToTransferMessage(testMessage);
        Assert.assertEquals(res.getContent(), testContent);
    }

}
