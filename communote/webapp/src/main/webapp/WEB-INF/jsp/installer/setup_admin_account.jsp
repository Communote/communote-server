<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<script type="text/javascript">
window.addEvent('domready', function() {
    var passwordField = $('userPassword');
    if(passwordField != null && passwordField.get('value') != null){
        updateQualityMeter(passwordField);
    }

    setLocationSettings();
});
</script>

<div class="form-container">
<form:form>
    <fieldset>
        <legend>${titlePrefix}<fmt:message key="installer.step.admin.legend.title" /></legend>
        <div class="fieldset-description">
            <fmt:message key="installer.step.admin.description" />
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
                <form:label path="userEmail"><fmt:message key="installer.step.admin.label.mail" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="userEmail" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.admin.tooltip.mail" />
            </div>
            <form:errors path="userEmail" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="userFirstName"><fmt:message key="installer.step.admin.label.firstname" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="userFirstName" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.admin.tooltip.firstname" />
            </div>
            <form:errors path="userFirstName" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="userLastName"><fmt:message key="installer.step.admin.label.lastname" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="userLastName" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.admin.tooltip.lastname" />
            </div>
            <form:errors path="userLastName" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="userLanguageCode"><fmt:message key="installer.step.admin.label.language" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <jsp:include page="/WEB-INF/jsp/common/language_list.jsp">
                    <jsp:param name="path" value="userLanguageCode" />
                    <jsp:param name="includeEmpty" value="${empty command.userLanguageCode}" />
                </jsp:include>
            </div>
            <div class="advice">
                <ct:tip key="installer.step.admin.tooltip.language" />
            </div>
            <form:errors path="userLanguageCode" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="spacer">&nbsp;</div>
        <div class="form-input">
            <div class="label">
                <form:label path="userAlias"><fmt:message key="installer.step.admin.label.alias" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="userAlias" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.admin.tooltip.alias" />
            </div>
            <form:errors path="userAlias" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="userPassword"><fmt:message key="installer.step.admin.label.password" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:password path="userPassword" cssClass="text password" cssErrorClass="text password error" autocomplete="off" htmlEscape="true" showPassword="true" onkeyup="updateQualityMeter(this);" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.admin.tooltip.password" />
            </div>
            <div class="pwd-strength-indicator">
                <span class="pwd-strength-label"><fmt:message key="user.register.password_strength.low" /></span>
                <div id="progressbar" class="pwd-strength-bar"><div id="progressbarArrow" class="pwd-strength-arrow"></div></div>
                <span class="pwd-strength-label"><fmt:message key="user.register.password_strength.strong" /></span>
                <span class="clear"><!-- ie --></span>
            </div>
            <form:errors path="userPassword" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="userPasswordConfirmation"><fmt:message key="installer.step.admin.label.password.confirm" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:password path="userPasswordConfirmation" cssClass="text password" autocomplete="off" cssErrorClass="text password error" htmlEscape="true" showPassword="true" />
            </div>
            <form:errors path="userPasswordConfirmation" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
    </fieldset>
    
    <div class="actionbar actionbar-general">
        <div class="button-gray button-right">
            <input type="submit" id="" name="_finish" value="<fmt:message key="installer.button.finish" />" />
        </div>
        <div class="button-gray button-left">
            <input type="submit" id="" name="_target4" value="<fmt:message key="installer.button.previous" />" />
        </div>
        <span class="clear"><!-- ie --></span>
    </div>
</form:form>         
</div>