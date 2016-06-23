<%@ tag body-content="empty"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/custom-fmt.tld"%>
<%@ taglib prefix="ct"      uri="/WEB-INF/tld/custom-taglib.tld" %>

<fmt:setBundle basename="com.communote.server.core.i18n.messages" />

<%@ attribute name="name" description="If set this will be a Spring form with an error field, else a normal form."
    required="false"%>
<%@ attribute name="isSpringForm" description="If set this will be a spring form, an the name will be used as path too."
    required="false"%>
<%@ attribute name="key" description="Message key." required="true"%>
<%@ attribute name="enabled" description="Only used when not a spring form." required="false" type="java.lang.Boolean" %>
<%@ attribute name="secondLine" description="Enable this, if this element is in a row with another input field." required="false" type="java.lang.Boolean" %>
<%@ attribute name="onchange" description="Html onchange event handler." required="false" %>
<%@ attribute name="width" description="Width in percent. Default is 50%." required="false"%>
<%@ attribute name="hint" description="If set 'key'.hint will be used as hint" required="false"  type="java.lang.Boolean"%>

<c:if test="${empty pageScope.width}">
    <c:set var="width" scope="page">50</c:set>
</c:if>

<div class="w${pageScope.width} check">
    <c:if test="${secondLine}"><br /></c:if>
    <c:choose>
        <c:when test="${not empty pageScope.isSpringForm}">
            <form:checkbox path="${pageScope.name}" id="${pageScope.name}" onchange="${pageScope.onchange}" />
        </c:when>
        <c:otherwise>
            <input  type="checkbox" name="${pageScope.name}" id="${pageScope.name}" 
                <c:if test="${pageScope.enabled}">checked="checked"</c:if>
                <c:if test="${not empty pageScope.onchange}">onchange="${pageScope.onchange}"</c:if>
            />
        </c:otherwise>
    </c:choose>
    <label for="${pageScope.name}"><fmt:message key="${pageScope.key}" />
    <c:if test="${pageScope.hint}">
        <ct:tip key="${pageScope.key}.hint"/>
    </c:if>
    </label>
    <span class="clear"><!-- ie --></span>
</div>