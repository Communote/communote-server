<%@page import="com.communote.server.api.core.application.CommunoteRuntime"%>
<%@page import="com.communote.server.core.common.session.SessionHandler"%>
<%@page import="com.communote.server.api.core.bootstrap.InitializationStatus"%>
<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<%
InitializationStatus status = CommunoteRuntime.getInstance().getInitializationStatus();
boolean refreshPage = false;
if (InitializationStatus.Type.FAILURE.equals(status.getStatus())) {
    if (status.getMessage() != null) {
    	request.setAttribute("errorMessage", status.getMessage().toString(SessionHandler.instance().getCurrentLocale(request)));
    }
} else if (InitializationStatus.Type.IN_PROGRESS.equals(status.getStatus())) {
    refreshPage = true;   
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Communote</title>
<link rel="stylesheet" type="text/css" href="<ct:url staticResource="true" value="/styles/main/layout-starting.css" />" media="screen,projection" />
<link rel="stylesheet" type="text/css" href="<ct:url staticResource="true" value="/styles/frameworks/roar/roar.css" />"
    media="screen,projection" />
<c:if test="${refreshPage}">
    <meta http-equiv="refresh" content="60; url=<ct:url absolute="true" />" />
</c:if>
    
</head>


<body>
<div id="communote">
<div id="container">
<div id="header">
<div class="logo"><img alt="communote logo" src="<ct:url staticResource="true" value="/themes/core/images/misc/communote_logo.jpg" />" /></div>
<span class="clear"><!-- ie --></span></div>
<div id="main" style="padding: 15px;">
<br />
<br />
<c:choose><c:when test="${errorMessage != null}">
    <h1><fmt:message key="initialization.page.error.title" /></h1>
    <h2>${errorMessage}</h2>
</c:when><c:otherwise>
    <h1><fmt:message key="initialization.page.title" /></h1>
</c:otherwise></c:choose></div>
<div id="footer">&nbsp;</div>
</div>
</div>
</body>
</html>
