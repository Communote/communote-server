<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div class="layer">
    <form:form>
        <fieldset class="no-border">
            <div class="fieldset-description">
                <fmt:message key="client.security.authentication.description" />
            </div>
            <div class="line">
                <cform:input key="client.security.authentication.lock.temporary" name="failedAttemptsBeforeTemporaryLock" required="true" isSpringForm="true" /> 
                <cform:input key="client.security.authentication.lock.permanent" name="failedAttemptsBeforePermanentLock" required="true" isSpringForm="true" /> 
                <span class="clear"><!-- ie --></span>
            </div>
            <div class="line">
                <cform:input key="client.security.authentication.lock.interval" name="lockInterval" required="true" isSpringForm="true" hint="true" /> 
                <div class="w50">
                    <div class="label">
                        <label for="riskLevel">
                            <fmt:message key="client.security.authentication.risk.level" />
                            <span class="required">*</span>
                        </label>
                        <ct:tip key="client.security.authentication.risk.level.hint" />
                    </div>
                    <div class="input">
                        <form:select path="riskLevel" >
                            <form:option value="1" ><fmt:message key="client.security.authentication.risk.level.list" /> 1</form:option>
                            <form:option value="2" ><fmt:message key="client.security.authentication.risk.level.list" /> 2</form:option>
                            <form:option value="3" ><fmt:message key="client.security.authentication.risk.level.list" /> 3</form:option>
                        </form:select>
                    </div>
                    <form:errors cssClass="error" path="riskLevel" element="span" />
                </div>
                <span class="clear"><!-- ie --></span>
            </div>
            <div class="info"><fmt:message key="form.info.required.fields" /></div>
        </fieldset>
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" name="submit" value="<fmt:message key="button.save" />" />
            </div>
            <span class="clear"></span>
        </div> 
    </form:form>
</div>