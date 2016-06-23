<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div class="layer">
    <form:form>
        <div class="from-description">
            <fmt:message key="client.system.application.server.description" />
        </div>
        <fieldset class="no-border">
            <cform:input key="client.system.application.server.host" name="hostname" isSpringForm="true" required="true"/>
            <cform:input key="client.system.application.server.http.port" name="httpPort" isSpringForm="true" required="true"/>
            <span class="clear"><!-- ie --></span>
            <div class="check">
                <form:checkbox path="httpsEnabled" id="httpsEnabled" onclick="if($(httpsEnabled).checked == false){ $(httpsPort).setProperty('disabled','disabled') ;} else { $(httpsPort).removeProperty('disabled'); }" />
                <label for="httpsEnabled"><fmt:message key="client.system.application.server.https.enabled" /></label>
                <span class="clear"><!-- ie --></span>
            </div>

            <span class="clear"><!-- ie --></span>
            <div class="w50">
                <div class="label">
                    <label for="httpsPort"><fmt:message key="client.system.application.server.https.port" /></label>
                </div>
                <div class="input"><form:input id="httpsPort" path="httpsPort" cssClass="text" disabled="${!command.httpsEnabled}"/></div>
                <form:errors cssClass="error" path="httpsPort" element="span"/>
            </div>
            <cform:input key="client.system.application.server.context" hint="true" name="context" isSpringForm="true" required="false"/>
            <span class="clear"><!-- ie --></span>
        </fieldset>
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" onclick="$(httpsPort).removeProperty('disabled');" name="submit" value="<fmt:message key="button.save" />" />
            </div>
            <span class="clear"><!-- ie --></span>
        </div>
    </form:form>
</div>
