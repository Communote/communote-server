<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div class="layer">
    <form:form>
        <fieldset class="no-border">
            <div class="notify-inline">
                <div class="notify-warning">
                    <div class="message">
                        <fmt:message key="client.system.content.file.storage.warning" />
                    </div>
                    <span class="clear"><!-- --></span>
                </div>
            </div>
            <div class="spacer">&nbsp;</div>
            <div class="w100 long">
                <div class="label"><label for="path"><fmt:message key="client.system.content.file.storage.repository" /><span class="required">*</span></label></div>
                <div class="input"><form:input path="path" cssClass="text" htmlEscape="true" /></div>
                <span class="error"><form:errors cssClass="error" path="path" /></span>
             </div>
             <span class="clear"><!-- --></span>
             <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
        </fieldset>
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" name="submit" value="<fmt:message key="button.save" />" />
            </div>
            <span class="clear"><!-- --></span>
        </div>
    </form:form>
</div>
