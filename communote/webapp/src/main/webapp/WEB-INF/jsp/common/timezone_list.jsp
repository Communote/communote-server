<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="java.util.List" %>
<%@page import="org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper" %>
<%@page import="com.communote.server.api.ServiceLocator" %>
<%@page import="com.communote.server.core.common.time.SimplifiedTimeZone"%>
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

    List<SimplifiedTimeZone> timeZoneList = ServiceLocator.findService(MasterDataManagement.class).getTimeZones();
    originalRequest.setAttribute("timeZoneList", timeZoneList);
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
        <option value=""><fmt:message key="user.profile.timezone.empty"/></option>
    </c:if>
    <c:forEach var="timeZoneItem" items="${timeZoneList}">
        <form:option value="${timeZoneItem.timeZoneId}"><fmt:message key="${timeZoneItem.messageKey}"/></form:option>
    </c:forEach>
</form:select>