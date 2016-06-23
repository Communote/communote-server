<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@ page import="com.communote.server.web.commons.FeConstants"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<form:form>
    <form:hidden id="invitationProvider" path="invitationProvider" />
    <div class="form-container">
        <fieldset class="no-border">
            <%@ include file="/WEB-INF/jsp/user/client/invite_user_fields.jspf" %>
            <span class="clear"><!-- ie --></span>
            <div class="fieldset-info">
                <fmt:message key="form.info.required.fields" />
            </div>
        </fieldset>
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" name="submit" value="<fmt:message key="client.invite.user.submit"/>" />
            </div>
            <span class="clear"></span>
        </div>
    </div>
</form:form>
