package com.communote.plugins.webservice.helloworld;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@WebService(name = "Hello_PortType", targetNamespace = "http://www.examples.com/wsdl/HelloService.wsdl")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class HelloWorldImpl implements HelloPortType {

    @WebMethod(action = "sayHello")
    @WebResult(name = "greeting", partName = "greeting")
    @Override
    public String sayHello(String firstName) {
        return "Hello " + firstName + "! How are you today?";
    }

}
