<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.web.fe.portal.user.system.communication.XmppController"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>
<div class="form-container no-border">
<form:form>
    <div class="actionbar actionbar-service">
        <form:hidden path="running" />
        <c:choose>
            <c:when test="${command.running}">
                <div class="service-status">
                    <img alt="active" src="<ct:url staticResource="true" value="/themes/core/images/misc/service-on.png" />" />&nbsp;<fmt:message key="client.system.communication.xmpp.service.status.on" />
                </div>
                <div class="button-gray">
                    <input type="submit" onclick="$('action').set('value','<%= XmppController.ACTION_STOP_SERVICE %>');" name="submit" value="<fmt:message key="button.service.off" />" />
                </div>
            </c:when>
            <c:otherwise>
                <div class="service-status">
                    <img alt="turned off" src="<ct:url staticResource="true" value="/themes/core/images/misc/service-off.png" />" />&nbsp;<fmt:message key="client.system.communication.xmpp.service.status.off" />
                </div>
                <div class="button-gray">
                    <input type="submit" onclick="$('action').set('value','<%= XmppController.ACTION_START_SERVICE %>');" name="submit" value="<fmt:message key="button.service.on" />" />
                </div>
            </c:otherwise>    
        </c:choose>
        <span class="clear"><!-- ie --></span>
    </div>
    <hr />  
    <div class="from-description">
        <fmt:message key="client.system.communication.xmpp.description" />
    </div>
    <fieldset class="no-border">
        <cform:input    key="client.system.communication.xmpp.client.server"  hint="true" name="server" required="true" isSpringForm="true" /> 
        <cform:input    key="client.system.communication.xmpp.client.port"    hint="true" name="port" required="true" isSpringForm="true" maxlength="5" />
        <span class="clear"><!-- ie --></span>
        <cform:input    key="client.system.communication.xmpp.client.login"  hint="true" name="login" required="true" isSpringForm="true"  /> 
        <cform:input    key="client.system.communication.xmpp.client.password" name="password" type="password" isSpringForm="true" required="true"  /> 
        <span class="clear"><!-- ie --></span>
        <cform:hidden name="action" />
        <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
    </fieldset>
    <div class="actionbar actionbar-general">
       <div class="button-gray main">
            <input type="submit" onclick="$('action').set('value','<%= XmppController.ACTION_SAVE %>');"  name="submit" value="<fmt:message key="button.save" />" />
        </div>
        <div class="button-gray">
            <input type="submit" onclick="$('action').set('value','<%= XmppController.ACTION_TEST %>');" name="submit" value="<fmt:message key="client.system.communication.xmpp.client.test" />" />
        </div>
        <span class="clear"><!-- ie --></span>
    </div>
</form:form>
</div>