<%@page import="com.communote.server.persistence.user.client.ClientHelper"%>
<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<c:set var="message" value="<ul>" />
<c:if test="${not empty errorMessages}">
    <c:forEach var="msg" items="${errorMessages}">
        <c:set var="message" value="${message}<li>${msg}</li>" />
    </c:forEach>
</c:if>
<c:set var="message" value="${message}</ul>" />
<%
    String errorPageUrl = request.getContextPath() + "/microblog/"
            + ClientHelper.getCurrentClientId() + "/error-page";
%>
<jsp:include page="<%=errorPageUrl%>">
    <jsp:param name="requestUrl" value="" />
    <jsp:param name="errorCode" value="401" />
    <jsp:param name="message" value="${message}" />
</jsp:include>
