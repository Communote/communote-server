<%@page import="com.communote.server.api.core.application.CommunoteRuntime"%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<% 
	pageContext.setAttribute("applicationInfo", CommunoteRuntime.getInstance().getApplicationInformation());
%>

<div id="footer">
	<p class="footer-message">
	<authz:authorize ifNotGranted="ROLE_KENMEI_USER">
		<a id="topbar-about" href="<ct:url value="/user/about.do" />"><fmt:message key="portal.menu.about.title" /></a> | <fmt:message key="portal.footer.service.text" />
	</authz:authorize>
	<authz:authorize ifAnyGranted="ROLE_KENMEI_USER">
        <a id="topbar-about" href="<ct:url value="/portal/service/legal" />"><fmt:message key="portal.menu.about.title" /></a> | <fmt:message key="portal.footer.service.text" />
    </authz:authorize>
	</p>
	<dl id="version-info">
		<dt class="build">Build:</dt><dd class="build">${applicationInfo.buildNumberWithType}<br /></dd>
		<dt class="buildtime">Build-Time:</dt><dd class="buildtime">${applicationInfo.buildTime}<br /></dd>
	</dl>
	<br class="clear" />
</div>