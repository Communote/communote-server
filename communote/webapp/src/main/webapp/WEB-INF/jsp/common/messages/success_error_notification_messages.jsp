<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<c:if test="${not empty successMessages}">
    <c:forEach var="msg" items="${successMessages}">
        <div class="roar-notification-container success" style="display: none;">
            ${msg}
        </div>
    </c:forEach>
</c:if>
<c:if test="${not empty warningMessages}">
    <c:forEach var="msg" items="${warningMessages}">
        <div class="roar-notification-container warning" style="display: none;">
        ${msg}
        </div>
    </c:forEach>
</c:if>
<c:if test="${not empty errorMessages}">
    <c:forEach var="msg" items="${errorMessages}">
        <div class="roar-notification-container error" style="display: none;">
        ${msg}
        </div>
    </c:forEach>
</c:if>
<%--
    prints errors stored with .reject("message.key")
    requirement: must be wrapped within
        <spring:hasBindErrors name="commandBeanName">
            ...
        </spring:hasBindErrors>
 --%>
<c:if test="${not empty errors.globalErrors }">
    <div class="roar-notification-container error" style="display: none;">
        <c:forEach var="err" items="${errors.globalErrors}">
<fmt:message key="${err.code}" />
        </c:forEach>
    </div>
</c:if>
