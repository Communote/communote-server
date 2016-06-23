<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<div class="form-container">
<form:form>
    <fieldset>
        <legend>${titlePrefix}<fmt:message key="installer.step.application.legend.title" /></legend>
        <div class="fieldset-description">
            <fmt:message key="installer.step.application.description" />
        </div>
        <spring:hasBindErrors name="command">  
            <c:forEach items="${errors.globalErrors}" var="errorMessage">  
                <div class="form-error">  
                    <fmt:message key="${errorMessage.code}" >
                    <fmt:param value="${errorMessage.arguments[0]}" />
                    </fmt:message>  
                </div>  
            </c:forEach>  
        </spring:hasBindErrors>  
        <div class="form-input">
            <div class="label">
                <form:label path="accountName"><fmt:message key="installer.step.application.label.name" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="accountName" cssClass="text" cssErrorClass="text error" maxlength="150" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.application.tooltip.name" />
            </div>
            <form:errors path="accountName" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="accountTimeZoneId"><fmt:message key="installer.step.application.label.timezone" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <jsp:include page="/WEB-INF/jsp/common/timezone_list.jsp">
                    <jsp:param name="path" value="accountTimeZoneId" />
                    <jsp:param name="includeEmpty" value="${empty command.accountTimeZoneId}" />
                </jsp:include>
            </div>
            <div class="advice">
                <ct:tip key="installer.step.application.tooltip.timezone" />
            </div>
            <form:errors path="accountTimeZoneId" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
    </fieldset>
        
    <div class="actionbar actionbar-general">
        <div class="button-gray button-right">
            <input type="submit" id="" name="_target4" value="<fmt:message key="installer.button.next" />" />
        </div>
        <div class="button-gray button-left">
            <input type="submit" id="" name="_target2" value="<fmt:message key="installer.button.previous" />" />
        </div>
        <span class="clear"><!-- ie --></span>
    </div>
</form:form>
</div>