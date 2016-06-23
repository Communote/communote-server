<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/custom-fmt.tld"%>
<fmt:setBundle basename="com.communote.server.core.i18n.messages" />
<%
    pageContext.setAttribute("code", request
            .getAttribute("javax.servlet.error.status_code"));
%>
<c:if test="${not empty code}">
${code} - <fmt:message key="error.http.${code}" />
    <br />
    <br />
</c:if>
<c:if test="${not empty errorMessages}">
    <div class="error"><c:forEach var="msg" items="${errorMessages}">${msg}<br />
    </c:forEach></div>
</c:if>