<%@page import="org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper" %>
<%@page import="java.util.Collection" %>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.model.user.Country" %>
<%@page import="com.communote.server.api.ServiceLocator" %>
<%@page import="com.communote.server.model.user.Language" %>
<%@page import="com.communote.server.core.user.MasterDataManagement"%>

<%-- workaround, because of missing include parameters 
     see http://jira.springframework.org/browse/SEC-363 
     solution found on http://forum.springsource.org/showthread.php?t=29243&page=2 --%>
<%
    HttpServletRequest originalRequest = request;
    if (request instanceof SecurityContextHolderAwareRequestWrapper) {
        SecurityContextHolderAwareRequestWrapper savedRequest = (SecurityContextHolderAwareRequestWrapper) request;
        originalRequest = (HttpServletRequest) savedRequest.getRequest();
    }
%>
<%-- end of workaround --%>

<%
    Collection<Language> languagesList = ServiceLocator.findService(MasterDataManagement.class).getUsedLanguages();
    originalRequest.setAttribute("languagesList", languagesList);
    originalRequest.setAttribute("path", originalRequest.getParameter("path"));
    originalRequest.setAttribute("disableSelection", originalRequest.getParameter("disableSelection"));
%>
<c:set var="fieldDisabled">false</c:set>
<c:if test="${not empty disableSelection && disableSelection == true}">
    <c:set var="fieldDisabled">true</c:set>
</c:if>

<form:select path="${path}" disabled="${fieldDisabled}" cssClass="control-languageCode">
    <form:options items="${languagesList}" itemValue="languageCode" itemLabel="name"/>
</form:select>