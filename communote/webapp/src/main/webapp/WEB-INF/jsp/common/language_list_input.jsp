<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="java.util.List"%>
<%@page import="com.communote.server.model.user.Country"%>
<%@page import="com.communote.server.api.ServiceLocator"%>
<%@page import="com.communote.server.model.user.Language"%>
<%@page import="com.communote.server.core.user.MasterDataManagement"%>

<% 
List<Language> languagesList = ServiceLocator.findService(MasterDataManagement.class).getLanguages();
request.setAttribute("languagesList", languagesList);
%>

<select id="<%= request.getParameter("path") %>" name="<%= request.getParameter("path") %>">
<c:forEach var="language" items="${languagesList}">
	<option value="${language.languageCode}">${language.name}</option>
</c:forEach>
</select>