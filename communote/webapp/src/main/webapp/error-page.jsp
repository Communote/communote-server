<%@page pageEncoding="UTF-8"%>
<%@page session="false" isErrorPage="true"%>
<%@page import="java.util.Locale"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>

<%@page import="com.communote.server.api.core.application.CommunoteRuntime"%>
<%@page import="com.communote.server.api.core.common.ClientAndChannelContextHolder"%>
<%@page import="com.communote.server.api.core.client.ClientTO"%>
<%@page import="com.communote.server.core.common.session.SessionHandler"%>
<%@page import="com.communote.server.persistence.user.client.ClientHelper"%>
<%@page import="com.communote.server.web.commons.filter.ExposeLocaleToErrorPageFilter"%>
<%@page import="com.communote.server.web.commons.helper.JsonRequestHelper"%>
<%@page import="com.communote.server.web.commons.MessageHelper"%>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"%>
<%@ taglib prefix="ct" uri="/WEB-INF/tld/custom-taglib.tld"%>
<%
	// prefer the locale that is exported by the filter as it respects the current user which isn't
	// accessible anymore when the error-page is called 
	Locale locale = (Locale) request.getAttribute(ExposeLocaleToErrorPageFilter.CURRENT_LOCALE);
    boolean localeOverridden = SessionHandler.instance().overrideCurrentUserLocaleIfNotOverridden(request,
    		locale);
    boolean jsonRequested = JsonRequestHelper.isJsonResponseRequested(request);
    // same goes for client ID, which is set and removed by a filter. The error page is called after
    // the filter chain has been left. The filter therefore exports the clientId to a request parameter
    // and we restore the thread-local here to make sure URL rendering and stuff works correctly.
    String clientId = (String)request.getAttribute(MessageHelper.CLIENT_ID_REQUEST_ATTRIBUTE);
    boolean clearThreadlocal =false;
    ClientTO currentClient = null;
    if (clientId != null) {
    	currentClient = ClientHelper.getClient(clientId);
    }
    try {
        if (currentClient != null && currentClient.isActive()) {
            clearThreadlocal = true;
            ClientAndChannelContextHolder.setClient(currentClient);
        }
    	String requestUri = (String) request
    			.getAttribute("javax.servlet.forward.request_uri");
    	// can return null, but not really sure when and why
    	if (requestUri == null) {
    		requestUri = "";
    	}
    	if (request.getAttribute("javax.servlet.forward.query_string") != null) {
    		requestUri += '?' + (String) request
    				.getAttribute("javax.servlet.forward.query_string");
    	}
    	int port = request.getServerPort();
    	if ((!request.isSecure() && port == 80)
    			|| (request.isSecure() && port == 443)) {
    		port = -1;
    	}
    	String requestUrl = new java.net.URL(request.getScheme(),
    			request.getServerName(), port, requestUri).toString();
    	int errorCode = pageContext.getErrorData().getStatusCode();
    
    	String message = (String)request.getAttribute("com.communote.errorPage.message");
    
    	Object[] messageArguments = new Object[] { requestUrl,
    			request.getParameter("code") };
    	Throwable throwable = (Throwable) request
    			.getAttribute("javax.servlet.error.exception");
    	if (throwable == null) {
    		throwable = (Throwable) request.getAttribute("throwable");
    	}
        if (message == null) {
        	if (throwable != null
        			&& MessageHelper.hasText(request, "error.http." + errorCode
        					+ "." + throwable.getClass().getName(),
        					messageArguments)) {
        		message = MessageHelper.getText(request, "error.http."
        				+ errorCode + "." + throwable.getClass().getName(),
        				messageArguments);
        	} else if (MessageHelper.hasText(request,
        			"error.http." + errorCode, messageArguments)) {
                if (jsonRequested && MessageHelper.hasText(request,
                        "error.http." + errorCode + ".json", messageArguments)) {
                	  message = MessageHelper.getText(request, "error.http." + errorCode + ".json", messageArguments);
                } else {
                	  message = MessageHelper.getText(request, "error.http." + errorCode, messageArguments);
                }
        	} else {
        		message = MessageHelper.getText(request, "error.http.unknown",
        				messageArguments);
        	}
        }
    	Logger logger = LoggerFactory.getLogger("de.communardo.kenmei.fe.uncaught_exception");
    	if (throwable != null) {
    		logger.error("Requesting " + requestUrl + " resulted in "
    				+ throwable.toString(), throwable);
    	} else {
    		logger.debug("Requesting {} resulted in HTTP status code {}", requestUrl, errorCode);
    	}
        if (jsonRequested) {
            JsonRequestHelper.writeJsonErrorResponseQuietly(response, message);
        } else {
            boolean showForwardButton = true;
            if (request.getAttribute("showForwardButton") != null) {
            	showForwardButton = (Boolean)request.getAttribute("showForwardButton");
            }
    
        	if (CommunoteRuntime.getInstance().isInitialized()) {
        		String errorPageUrl = "/microblog/"
        				+ ClientHelper.getCurrentClientId() + "/error-page";
        		Object next = request.getAttribute("com.communote.errorPage.next");
                if (next == null) {
                    next = "/portal/home";   
                }
%>
<jsp:include page="<%=errorPageUrl%>">
    <jsp:param name="requestUrl" value="<%=requestUrl%>" />
    <jsp:param name="errorCode" value="<%=errorCode%>" />
    <jsp:param name="message" value="<%=message%>" />
    <jsp:param name="next" value="<%=next%>" />
    <jsp:param name="showForwardButton" value="<%=showForwardButton%>" />
</jsp:include>
<%
	        } else {
%>
<b>Error Code:</b>
<%=errorCode%><br />
<b>Request Url:</b>
<%=requestUrl%><br />
<b>Message:</b>
<%=message%>
<%
            }
        }
    } finally {
        if (localeOverridden) {
            SessionHandler.instance().resetOverriddenCurrentUserLocale(request);   
        }
        if (clearThreadlocal) {
            ClientAndChannelContextHolder.clear();   
        }
    }
%>