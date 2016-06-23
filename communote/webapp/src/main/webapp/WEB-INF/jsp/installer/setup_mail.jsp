<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<div class="form-container">
<form:form>
    <fieldset>
        <legend>${titlePrefix}<fmt:message key="installer.step.mail.legend.title" /></legend>
        <div class="fieldset-description">
            <fmt:message key="installer.step.mail.description" />
        </div>
        <spring:hasBindErrors name="command">  
            <c:forEach items="${errors.globalErrors}" var="errorMessage">  
                <div class="form-error">  
                    <c:out value="${errorMessage.code}" />  
                </div>  
            </c:forEach>  
        </spring:hasBindErrors>  
        <div class="form-input">
            <div class="label">
                <form:label path="smtpHost"><fmt:message key="installer.step.mail.label.host" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="smtpHost" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="client.system.communication.mail.out.server.hint" />
            </div>
            <form:errors path="smtpHost" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="smtpPort"><fmt:message key="installer.step.mail.label.port" /></form:label>
            </div>
            <div class="input">
                <form:input path="smtpPort" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="client.system.communication.mail.out.port.hint" />
            </div>
            <form:errors path="smtpPort" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="smtpStartTls" for="startTls"><fmt:message key="installer.step.mail.label.starttls" /></form:label>
            </div>
            <div class="checkbox">
                <form:checkbox path="smtpStartTls" id="startTls" cssClass="checkbox" cssErrorClass="checkbox error" />
            </div>
            <div class="advice">
                <ct:tip key="client.system.communication.mail.out.starttls.hint" />
            </div>
            <form:errors path="smtpStartTls" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="smtpUser"><fmt:message key="installer.step.mail.label.user" /></form:label>
            </div>
            <div class="input">
                <form:input path="smtpUser" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="client.system.communication.mail.out.login.hint" />
            </div>
            <form:errors path="smtpUser" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="smtpPassword"><fmt:message key="installer.step.mail.label.password" /></form:label>
            </div>
            <div class="input">
                <form:password path="smtpPassword" cssClass="text password" autocomplete="off" cssErrorClass="text password error" htmlEscape="true" showPassword="true" />
            </div>
            <div class="advice">
                <ct:tip key="client.system.communication.mail.out.password.hint" />
            </div>
            <form:errors path="smtpPassword" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="spacer">&nbsp;</div>
        <div class="form-input">
            <div class="label">
                <form:label path="senderName"><fmt:message key="installer.step.mail.label.sender.name" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="senderName" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="client.system.communication.mail.out.sender.name.hint" />
            </div>
            <form:errors path="senderName" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="senderAddress"><fmt:message key="installer.step.mail.label.sender.address" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="senderAddress" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="client.system.communication.mail.out.sender.address.hint" />
            </div>
            <form:errors path="senderAddress" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="spacer">&nbsp;</div>
        <div class="form-input">
            <div class="label">
                <form:label path="supportAddress"><fmt:message key="installer.step.mail.label.support.address" /></form:label>
            </div>
            <div class="input">
                <form:input path="supportAddress" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.mail.tooltip.support.address" />
            </div>
            <form:errors path="supportAddress" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
        <div id="reports" class="hidden">
            <div class="loading">
                <img src="<c:url value="/themes/core/images/main/loading.gif" />" alt="loading" />
            </div>
            <div class="warning-report hidden">
                <h3><fmt:message key="installer.step.mail.test.error.head" /></h3>
                <div class="message">&nbsp;</div>
                <span class="clear"><!-- ie --></span>
            </div>
            <div class="success-report hidden">
                <h3><fmt:message key="installer.step.mail.test.success.head" /></h3>
                <div class="message">&nbsp;</div>
                <span class="clear"><!-- ie --></span>
            </div>
        </div>
    </fieldset>
    <div class="actionbar actionbar-general">
        <div class="button-gray button-right">
            <input type="submit" id="" name="_target5" value="<fmt:message key="installer.button.next" />" />
        </div>
        <div class="button-gray button-left">
            <input type="submit" id="" name="_target3" value="<fmt:message key="installer.button.previous" />" />
        </div>
        <div class="button-gray button-right">
            <a href="javascript:;" onclick="sendTestMessage();"><fmt:message key="installer.button.send.mail" /></a>
        </div>
        <span class="clear"><!-- ie --></span>
    </div>
</form:form>
</div>