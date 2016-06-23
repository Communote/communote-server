<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<script type="text/javascript">
<!--
window.addEvent('domready', function() {
    setDefaultDatabasePortnumber(false);
});
//-->
</script>
<div class="form-container">
<form:form>
    <fieldset>
        <legend>${titlePrefix}<fmt:message key="installer.step.database.selection.legend.title" /></legend>
        <div class="fieldset-description">
            <fmt:message key="installer.step.database.selection.description" />
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
                <form:label path="databaseType"><fmt:message key="installer.step.database.selection.label.type" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:select path="databaseTypeIdentifier" cssErrorClass="error" onchange="setDefaultDatabasePortnumber(true);">
                    <c:forEach var="type" items="${command.supportedDatabaseTypes}">
                        <form:option value="${type.identifier}" data-cnt-database-default-port="${type.defaultPort}"><fmt:message key="installer.database.${type.identifier}" /></form:option>
                    </c:forEach>
                </form:select>
            </div>
            <div class="advice">
                <ct:tip key="installer.step.database.selection.tooltip.type" />
            </div>
            <form:errors path="databaseType" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="databaseHost"><fmt:message key="installer.step.database.selection.label.host" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="databaseHost" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.database.selection.tooltip.host" />
            </div>
            <form:errors path="databaseHost" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="databasePort"><fmt:message key="installer.step.database.selection.label.port" /></form:label>
            </div>
            <div class="input">
                <form:input path="databasePort" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.database.selection.tooltip.port" />
            </div>
            <form:errors path="databasePort" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="spacer">&nbsp;</div>
        <div class="form-input">
            <div class="label">
                <form:label path="databaseName"><fmt:message key="installer.step.database.selection.label.name" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="databaseName" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.database.selection.tooltip.name" />
            </div>
            <form:errors path="databaseName" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="spacer">&nbsp;</div>
        <div class="form-input">
            <div class="label">
                <form:label path="databaseUser"><fmt:message key="installer.step.database.selection.label.user" /><span class="required">*</span></form:label>
            </div>
            <div class="input">
                <form:input path="databaseUser" cssClass="text" cssErrorClass="text error" htmlEscape="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.database.selection.tooltip.user" />
            </div>
            <form:errors path="databaseUser" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="form-input">
            <div class="label">
                <form:label path="databasePassword"><fmt:message key="installer.step.database.selection.label.password" /></form:label>
            </div>
            <div class="input">
                <form:password path="databasePassword" cssClass="text password" autocomplete="off" cssErrorClass="text password error" htmlEscape="true" showPassword="true" />
            </div>
            <div class="advice">
                <ct:tip key="installer.step.database.selection.tooltip.password" />
            </div>
            <form:errors path="databasePassword" cssClass="error" element="div" />
            <span class="clear"><!-- ie --></span>
        </div>
        <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
    </fieldset>
    
    <div class="actionbar actionbar-general">
        <div class="button-gray button-right">
            <input type="submit" id="" name="_target2" value="<fmt:message key="installer.button.next" />" />
        </div>
        <div class="button-gray button-left">
            <input type="submit" id="" name="_target0" value="<fmt:message key="installer.button.previous" />" />
        </div>
        <span class="clear"><!-- ie --></span>
    </div>
</form:form>
</div>