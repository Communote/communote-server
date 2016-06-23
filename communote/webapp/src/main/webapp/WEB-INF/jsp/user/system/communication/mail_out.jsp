<%@page import="com.communote.server.web.fe.portal.user.system.communication.MailOutController"%>

<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div class="layer">
    <form:form>
        <div class="from-description">
            <fmt:message key="client.system.communication.mail.out.description" />
        </div>
        <fieldset class="no-border">
            <cform:input key="client.system.communication.mail.out.server" required="true" hint="true" isSpringForm="true" name="server" />
            <cform:input key="client.system.communication.mail.out.port" hint="true" isSpringForm="true" name="port" />
            <span class="clear"><!-- --></span>
            <cform:checkbox key="client.system.communication.mail.out.starttls" name="startTls" isSpringForm="true" secondLine="false" width="100" hint="true" />
            <span class="clear"><!-- --></span>
            <cform:input key="client.system.communication.mail.out.login" hint="true" isSpringForm="true" name="login"/>
            <cform:input key="client.system.communication.mail.out.password" hint="true" isSpringForm="true" name="password" type="password" />
            <span class="clear"><!-- --></span>
            <div class="spacer">&nbsp;</div>
            <cform:input key="client.system.communication.mail.out.sender.name" required="true" hint="true" isSpringForm="true" name="senderName" />
            <cform:input key="client.system.communication.mail.out.sender.address" required="true" hint="true" isSpringForm="true" name="senderAddress" />
            <span class="clear"><!-- --></span>
            <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
        </fieldset>
        <div class="actionbar actionbar-general">
            <form:hidden path="action" id="action" />
            <div class="button-gray main">
                <input type="submit" onclick="$('action').set('value','<%= MailOutController.ACTION_SAVE %>');"  name="submit" value="<fmt:message key="button.save" />" />
            </div>
            <div class="button-gray">
                <input type="submit" onclick="$('action').set('value','<%= MailOutController.ACTION_TESTMAIL %>');" name="submit" value="<fmt:message key="client.system.communication.mail.out.test.mail.button" />" />
            </div>
            <span class="clear"><!-- --></span>
        </div>
    </form:form>
</div>
