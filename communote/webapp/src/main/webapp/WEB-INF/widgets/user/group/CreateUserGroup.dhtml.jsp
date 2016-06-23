<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@page import="com.communote.server.web.commons.MessageHelper"%>

<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<form:form>
    <div id="create-usergroup" class="form-container">
    <fieldset class="no-border">
        <div id="create-user-group-input">
            <div class="w50">
                <div class="label"><label for="name"><fmt:message key="client.user.group.create.name" /><span class="required">*</span></label></div>
                <div class="input"><form:input autocomplete="off" path="name" cssClass="text" /></div>
                <span class="error"><label for="name" class="error"><form:errors cssClass="error" path="name" /></label></span>
            </div>
            <div class="w50">
                <div class="label"><label for="alias"><fmt:message key="client.user.group.create.alias" /><span class="required">*</span><ct:tip key="common.alias.explanation" /></label></div>
                <div class="input"><form:input autocomplete="off" path="alias" cssClass="text" /></div>
                <span class="error"><label for="alias" class="error"><form:errors cssClass="error" path="alias" /></label></span>
            </div>
            <div class="w100 long">
                <div class="label"><label for="description"><fmt:message key="client.user.group.create.description" /></label></div>
                <div class="input"><form:textarea id="description" rows="5" cols="70" path="description"/></div>
                <span class="error"><label for="description" class="error"><form:errors cssClass="error" path="description" /></label></span>
            </div>
        </div>
        <span class="clear"><!-- Empty --></span>
        <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
    </fieldset>
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" name="submit" value="<fmt:message key="client.user.group.create.button" />" />
            </div>
            <span class="clear"></span>
        </div>
    </div>
</form:form>
