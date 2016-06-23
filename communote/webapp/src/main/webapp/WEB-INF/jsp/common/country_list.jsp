<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.core.user.MasterDataManagement"%>
<%@page import="com.communote.server.core.common.session.SessionHandler" %>
<%@page import="com.communote.server.api.ServiceLocator" %>
<%@page import="org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper" %>
<%@page import="java.util.List" %>
<%@page import="com.communote.server.model.user.Country" %>


<%-- workaround, because of missing include parameters 
     see http://jira.springframework.org/browse/SEC-363 
     solution found on http://forum.springsource.org/showthread.php?t=29243&page=2 --%>
<% HttpServletRequest originalRequest = request;
    if (request instanceof SecurityContextHolderAwareRequestWrapper) {
        SecurityContextHolderAwareRequestWrapper savedRequest = (SecurityContextHolderAwareRequestWrapper) request;
        originalRequest = (HttpServletRequest) savedRequest.getRequest();
    }
%>
<%-- end of workaround --%>

<%
	List<Country> countryList = ServiceLocator.findService(MasterDataManagement.class).getCountries(SessionHandler.instance().getCurrentLocale(request));
    originalRequest.setAttribute("countryList", countryList);
    originalRequest.setAttribute("path", originalRequest.getParameter("path"));
    originalRequest.setAttribute("includeEmpty", originalRequest.getParameter("includeEmpty"));
    originalRequest.setAttribute("disableSelection", originalRequest.getParameter("disableSelection"));
%>
<c:set var="fieldDisabled">false</c:set>
<c:if test="${not empty disableSelection && disableSelection == true}">
    <c:set var="fieldDisabled">true</c:set>
</c:if>
<form:select path="${path}" disabled="${fieldDisabled}">
    <c:if test="${includeEmpty}">
        <option value=""><fmt:message key="user.profile.country.empty"/></option>
    </c:if>
    <form:options items="${countryList}" itemValue="countryCode" itemLabel="name"/>
</form:select>