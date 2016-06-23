<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div class="layer">
    <form:form>
        <div class="from-description">
            <fmt:message key="client.system.content.file.upload.description" />
        </div>
        <fieldset class="no-border">
            <div class="w100 long">
                <div class="label">
                    <label for="attachmentSize"><fmt:message key="client.system.content.file.upload.attachments" /><span class="required">*</span></label>
                    <ct:tip key="client.system.content.file.upload.attachments.hint" />
                </div>
                <div class="input"><form:input path="attachmentSize" cssClass="text" htmlEscape="true" /></div>
                <form:errors cssClass="error" path="attachmentSize" element="span"/>
            </div>
            <div class="w100 long">
                <div class="label">
                    <label for="imageSize"><fmt:message key="client.system.content.file.upload.images" /><span class="required">*</span></label>
                    <ct:tip key="client.system.content.file.upload.images.hint" />
                </div>
                <div class="input"><form:input path="imageSize" cssClass="text" htmlEscape="true" /></div>
                <form:errors cssClass="error" path="imageSize" element="span"/>
            </div>
            <span class="clear"><!-- ie --></span>
            <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
        </fieldset>
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" name="submit" value="<fmt:message key="button.save" />" />
            </div>
            <span class="clear"></span>
        </div>
    </form:form>
</div>
