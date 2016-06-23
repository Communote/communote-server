<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>
<div class="form-container no-border">
<form:form>
    <div class="from-description">
        <fmt:message key="client.system.communication.xmpp.advanced.description" />
    </div>
<fieldset class="no-border">
    <cform:input    key="client.system.communication.xmpp.advanced.user.suffix" name="userSuffix" required="true" isSpringForm="true" hint="true" /> 
    <cform:input    key="client.system.communication.xmpp.advanced.blog.suffix" name="blogSuffix" required="true" isSpringForm="true" hint="true" />
    <span class="clear"><!-- ie --></span>
    <cform:input    key="client.system.communication.xmpp.advanced.posting.interval" hint="true" name="postingInterval" required="true" isSpringForm="true"  /> 
    <span class="clear"><!-- ie --></span>
    <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
</fieldset>
<div class="actionbar actionbar-general">
    <div class="button-gray main">
        <input type="submit" name="send_form" value="<fmt:message key="button.save" />" />
    </div>
    <span class="clear"><!-- ie --></span>
</div>
</form:form>
</div>