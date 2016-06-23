<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@page import="com.communote.server.web.commons.FormAction"%>

<%
	// TODO Change to a widget and complete.
%>

<div style="padding: 5px;display:block">
	<p>	
		<fmt:message key="widget.user.management.profile.action.delete.main" >
			<fmt:param><%= request.getParameter("alias") %></fmt:param>
		</fmt:message>
	</p>
</div>