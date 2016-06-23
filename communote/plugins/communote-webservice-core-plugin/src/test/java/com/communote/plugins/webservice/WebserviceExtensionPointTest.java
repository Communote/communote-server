package com.communote.plugins.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.communote.plugins.webservice.helloworld.HelloWorldImpl;
import com.communote.plugins.webservice.impl.CommunoteWebServiceController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Test
public class WebserviceExtensionPointTest {

    private CommunoteWebServiceController communoteWebServiceController;

    private static final String PLUGIN_NAME = "com.communote.plugins.webservice.test";

    private static final String REL_URL_PATTERN = "hello";

    private static final String SAY_HELLO_SOAP_ACTION = "\"sayHello\"";

    private CommunoteWebServiceDefinition communoteWebServiceDefinition;

    private String getSoapMessage(String filename) throws IOException {

        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(filename);
            List<String> lines = IOUtils.readLines(in, "UTF8");
            List<String> editedLines = new ArrayList<>();
            for (String line : lines) {
                editedLines.add(line.trim());
            }

            return StringUtils.join(lines, "\n");
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @BeforeSuite
    public void setup() {
        communoteWebServiceController = new CommunoteWebServiceController();
        communoteWebServiceController.setServletContext(new MockServletContext());

        communoteWebServiceDefinition = new CommunoteWebServiceDefinition();

        communoteWebServiceDefinition.setEndpointName("HelloWorldImpl");
        communoteWebServiceDefinition.setLocalPartName("HelloWorldImpl");
        communoteWebServiceDefinition.setNameSpaceUri("http://www.examples.com/wsdl/HelloService");
        communoteWebServiceDefinition.setPluginName(PLUGIN_NAME);
        communoteWebServiceDefinition.setRelativeUrlPattern(REL_URL_PATTERN);
        communoteWebServiceDefinition.setServiceClass(HelloWorldImpl.class);

        communoteWebServiceController.registerService(communoteWebServiceDefinition);
    }

    @Test(dependsOnMethods = { "testWebServiceGet" })
    public void testUnregisterWebService() throws ServletException, IOException {
        communoteWebServiceController.unregisterService(PLUGIN_NAME, REL_URL_PATTERN);
    }

    @Test
    public void testWebServiceGet() throws ServletException, IOException {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        mockRequest.setMethod("POST");
        mockRequest.setServletPath("/");
        mockRequest.setContentType("text/xml;charset=UTF-8");
        mockRequest.setCharacterEncoding("UTF-8");
        mockRequest.setRequestURI("/microblog/someclient/api/ws/" + PLUGIN_NAME + "/"
                + REL_URL_PATTERN);

        mockRequest.addHeader("SOAPAction:", SAY_HELLO_SOAP_ACTION);
        mockRequest.addHeader("Host:", "127.0.0.1");

        final String myName = "Machine " + UUID.randomUUID().toString();

        String soapMessage = getSoapMessage("sayHelloRequestSoapMessage.xml");
        soapMessage = soapMessage.replace("HELLO_NAME", myName);

        mockRequest.setContent(soapMessage.getBytes("UTF-8"));
        mockRequest.addHeader("Content-Length:", soapMessage.length());

        communoteWebServiceController.handleRequest(mockRequest, mockResponse);

        Assert.assertEquals(200, mockResponse.getStatus());

        String resultContent = mockResponse.getContentAsString();
        Assert.assertNotNull(resultContent);
        Assert.assertTrue(
                resultContent,
                resultContent.contains("<greeting>Hello " + myName
                        + "! How are you today?</greeting>"));

    }
}
