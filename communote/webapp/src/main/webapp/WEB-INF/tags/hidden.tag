<%@ tag body-content="empty"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<%@ attribute name="name" description="If set this will be a Spring form with an error field, else a normal form."
    required="false"%>
<%@ attribute name="isSpringForm" description="If set this will be a spring form, an the name will be used as path too."
    required="false"%>
<%@ attribute name="value" description="Value of the field. Only used when not a spring form." required="false" rtexprvalue="true" %>

<c:choose>
    <c:when test="${not empty pageScope.isSpringForm}">
        <form:hidden path="${pageScope.name}" htmlEscape="true" />
    </c:when>
    <c:otherwise>
        <input type="hidden" value="${pageScope.value}" class="text" name="${pageScope.name}" id="${pageScope.name}"/>
    </c:otherwise>
</c:choose>