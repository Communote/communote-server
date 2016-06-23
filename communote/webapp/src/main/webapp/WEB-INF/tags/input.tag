<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/custom-fmt.tld"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="ct"      uri="/WEB-INF/tld/custom-taglib.tld" %>

<%@ attribute name="key" description="Message key." required="true"%>
<%@ attribute name="isLong" description="If set this is a long field amd width will be 100%." required="false" %>
<%@ attribute name="width" description="Width in percent. Default is 50%." required="false"%>
<%@ attribute name="name" description="Name of the element." required="false" rtexprvalue="true" %>
<%@ attribute name="id" description="Id of the element." required="false" rtexprvalue="true" %>
<%@ attribute name="isSpringForm" description="If set this will be a spring form, an the name will be used as path too." required="false" %>
<%@ attribute name="hint" description="If set 'key'.hint will be used as hint" required="false" type="java.lang.Boolean" rtexprvalue="true" %>
<%@ attribute name="required" description="If set this will be used as a key for a hint." required="false" type="java.lang.Boolean" rtexprvalue="true" %>
<%@ attribute name="type" description="Type of the field, either text or password. Hidden is not supported. Text is default." required="false" %>
<%@ attribute name="value" description="Value of the field. Only used when not a spring form." required="false" rtexprvalue="true"%>
<%@ attribute name="disabled" description="If set this field is disabled." required="false" %>
<%@ attribute name="onkeyup" description="Html onkeyup event handler." required="false" %>
<%@ attribute name="maxlength" description="The maximum number of enterable characters." required="false" %>

<fmt:setBundle basename="com.communote.server.core.i18n.messages" />

<c:if test="${empty pageScope.type}">
    <c:set var="type" scope="page">text</c:set>
</c:if>

<c:if test="${empty pageScope.width}">
    <c:set var="width" scope="page">50</c:set>
</c:if>

<c:if test="${not empty pageScope.isLong}">
    <c:set var="isLong" scope="page">long</c:set>
    <c:set var="width" scope="page">100</c:set>
</c:if>
<c:choose>
    <c:when test="${not empty pageScope.id}">
        <c:set var="labelFor" scope="page">${pageScope.id}</c:set>
    </c:when>
    <c:otherwise>
        <c:set var="labelFor" scope="page">${pageScope.name}</c:set>
    </c:otherwise>
</c:choose>
<div class="w${pageScope.width} ${pageScope.isLong}">
<div class="label">
    <label for="${labelFor}">
        <fmt:message key="${pageScope.key}" /><c:if test="${pageScope.required}"><span class="required">*</span></c:if><%--
    --%></label><c:if test="${pageScope.hint}"><ct:tip key="${pageScope.key}.hint" /></c:if>
</div>
<c:choose>
    <c:when test="${not empty pageScope.isSpringForm}">
        <div class="input">
            <c:choose>
             <c:when test="${pageScope.type=='password'}">
                <form:password path="${pageScope.name}" cssClass="text" htmlEscape="true" showPassword="false" autocomplete="off"
                    disabled="true" id="${pageScope.name}"
                    onkeyup="${pageScope.onkeyup}" />
                <form:hidden path="passwordChanged" />
                <a href="javascript:;" onclick="enableField('${pageScope.name}', 'passwordChanged', this); return false;">
                <fmt:message key="client.form.change.password" /></a>
              </c:when>
             <c:otherwise>
                <form:input path="${pageScope.name}" cssClass="text" htmlEscape="true"
                        id="${pageScope.id}" onkeyup="${pageScope.onkeyup}"
                        disabled="${pageScope.disabled}" maxlength="${pageScope.maxlength}" />
             </c:otherwise>
            </c:choose>
         </div>
         <form:errors path="${pageScope.name}" cssClass="error" element="div" />
    </c:when>
    <c:otherwise>
        <div class="input">
            <input type="${pageScope.type}" value="${pageScope.value}" class="text" name="${pageScope.name}" 
              <c:if test="${not empty pageScope.id}">id="${pageScope.id}"</c:if>
              <c:if test="${not empty pageScope.disabled}">disabled="disabled"</c:if>
              <c:if test="${not empty pageScope.onkeyup}">onkeyup="${pageScope.onkeyup}"</c:if>
            />
        </div>
    </c:otherwise>
</c:choose></div>