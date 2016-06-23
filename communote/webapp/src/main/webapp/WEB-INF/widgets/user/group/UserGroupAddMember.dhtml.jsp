<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<%--include placeholder for user notification --%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<form method="post">
    <input id="entityId" name="entityId" type="hidden" />
    <input id="isGroup" name="isGroup" type="hidden" />
    <input name="groupId" value="${singleResult}" type="hidden" />
    <input name="submit" value="submit" type="hidden" />
</form>
<div class="form-container">
    <fieldset class="group-add">
        <legend><fmt:message key="client.user.group.management.group.addto" /></legend>
        <div class="w100 button">
            <div class="input">
                <input class="text keyword" id="keyword" name="keyword" value="" />
                <span class="clear"><!-- ie --></span>
            </div>
            <div class="button-gray main">
                <input type="submit" value="<fmt:message key="client.user.group.management.group.addto.button" />" name="addMember" onclick="widgetController.getWidget('${widget.widgetId}').doSubmit();"/>
            </div>
            <span class="clear"><!-- ie --></span>
        </div>
        <span class="clear"><!-- ie --></span>
    </fieldset>
</div>

