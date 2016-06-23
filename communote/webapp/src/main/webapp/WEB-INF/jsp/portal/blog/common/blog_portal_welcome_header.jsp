<%@include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.api.core.application.CommunoteRuntime"%>
<%@page import="com.communote.server.core.image.CoreImageType"%>
<%@page import="com.communote.server.api.core.config.type.ClientProperty"%>
<%@page import="com.communote.server.model.user.ImageSizeType"%>
<%@page import="com.communote.server.model.client.ClientStatus"%>
<%
	String supportEmailAddress = CommunoteRuntime.getInstance().getConfigurationManager().getClientConfigurationProperties().getProperty(ClientProperty.SUPPORT_EMAIL_ADDRESS, "");
pageContext.setAttribute("supportEmailAddress", supportEmailAddress);
%>

<div id="header">
    <div id="topbar"> 	
		<%-- home link --%>
		<a id="home" href="<ct:url />"><fmt:message key="portal.menu.home.title" /></a>
        <c:if test="${not empty supportEmailAddress}">
		<a id="support" href="mailto:${supportEmailAddress}">Support</a>
        </c:if>
		<a id="download" href="<ct:url value="/user/download.do" />"><fmt:message key="portal.menu.download.title" /></a>
 	</div>
 	<div id="clientlogo">
 		<a href="<ct:url />" class="imagelink"><img src="<ct:img type="<%=CoreImageType.clientlogo.name()%>" size="<%=ImageSizeType.LARGE %>"/>" alt="client-logo" id="client-logo" /></a>
 	</div>
</div>