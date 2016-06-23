<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div class="layer">
    <form:form>
        <div class="from-description">
            <fmt:message key="client.system.application.cacheinvalidation.description" />
        </div>
        <div>&nbsp;</div>
        <fieldset class="no-border">
            <span class="clear"><!-- ie --></span>
            <div class="check">
                <form:checkbox path="mainCacheEnabled" id="mainCacheEnabled" />
                <label for="mainCacheEnabled"><fmt:message key="client.system.application.cacheinvalidation.mainCacheEnabled" /></label>
                <br/>
                <br/>
                <form:checkboxes path="invalidateCaches" element="div class='cn-checkbox'" items="${invalidateCachesMap}" delimiter="<br/>" />
                <span class="clear"><!-- ie --></span>
            </div>
            <span class="clear"><!-- ie --></span>
        </fieldset>
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" name="button" value="<fmt:message key="client.system.application.cacheinvalidation.button" />" title="<fmt:message key='client.system.application.cacheinvalidation.button' />" />
            </div>
            <span class="clear"><!-- ie --></span>
        </div>
    </form:form>
</div>