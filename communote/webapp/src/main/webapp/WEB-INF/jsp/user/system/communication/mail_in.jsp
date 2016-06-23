<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.core.mail.fetching.MailInProtocolType"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>
<%@page import="com.communote.server.web.fe.portal.user.system.communication.MailInController"%>

<script type="text/javascript">
    function updateMultiModePreview(){
        var domain = $$('input[name=multiModeDomain]').get('value');
        var suffix = $$('input[name=multiModeSuffix]').get('value');
        var useAccount = $('multiModeUseAccount').get('checked');
        var preview = '&lt;blogAlias&gt;';
        if(useAccount){
            preview = preview + '.&lt;yourAccount&gt;';
        };
        if(suffix.toString().length > 0){
            preview = preview + '.' + suffix;
        };
        preview = preview  + '@' + domain;
        $('multi_preview').set('html',preview);
    }
</script>
<div class="layer">
<form:form>
    <div class="actionbar actionbar-service">
        <form:hidden path="running" />
<c:choose>
<c:when test="${command.running}">
            <div class="service-status">
                <img alt="active" src="<ct:url staticResource="true" value="/themes/core/images/misc/service-on.png" />" />&nbsp;<fmt:message key="client.system.communication.mail.in.service.status.on" />
            </div>
            <div class="button-gray">
                <input type="submit" onclick="$('action').set('value','<%= MailInController.ACTION_STOP_SERVICE %>');" name="submit" value="<fmt:message key="button.service.off" />" />
            </div>
</c:when>
<c:otherwise>
            <div class="service-status">
                <img alt="turned off" src="<ct:url staticResource="true" value="/themes/core/images/misc/service-off.png" />" />&nbsp;<fmt:message key="client.system.communication.mail.in.service.status.off" />
            </div>
            <div class="button-gray">
                <input type="submit" onclick="$('action').set('value','<%= MailInController.ACTION_START_SERVICE %>');" name="submit" value="<fmt:message key="button.service.on" />" />
            </div>
</c:otherwise>    
</c:choose>
            <span class="clear"><!-- ie --></span>
        </div>
        <hr />
        <div class="from-description bottomspace">
            <fmt:message key="client.system.communication.mail.in.description" />
<c:if test="${!onlySingleMode && !onlyMultiMode}">
            <br />
            <fmt:message key="client.system.communication.mail.in.description.modes" />
</c:if>
        </div>
        <fieldset class="bottomspace">
            <h5><fmt:message key="client.system.communication.mail.in.server.settings" /></h5>
            <div class="notify-inline">
                <div class="notify-info">
                    <div class="message">
                        <fmt:message key="client.system.communication.mail.in.server.protocol.info" />
                    </div>
                    <span class="clear"><!-- ie --></span>
                </div>
            </div>
            <div class="spacer">&nbsp;</div>
            <div class="w50">
                <div class="label">
                    <label for="protocol"><fmt:message key="client.system.communication.mail.in.server.protocol" /><span class="required">*</span></label><ct:tip key="client.system.communication.mail.in.server.protocol.hint" />
                </div>
                <div class="input">
                    <form:select path="protocol">
<c:forEach var="type" items="<%= MailInProtocolType.values() %>">
                        <form:option value="${type}"><fmt:message key="client.system.communication.mail.in.server.protocol.name.${type}" /></form:option>
</c:forEach>
                    </form:select>
                </div>
                <form:errors path="protocol" cssClass="error" element="div" />
            </div>
            <cform:checkbox key="client.system.communication.mail.in.server.starttls" name="startTls" isSpringForm="true" secondLine="true" hint="true" />
            <span class="clear"><!-- --></span>
            <cform:input key="client.system.communication.mail.in.server" name="server" isSpringForm="true" required="true" hint="true" />
            <cform:input key="client.system.communication.mail.in.server.port" name="port" isSpringForm="true" hint="true" maxlength="5" />
            <span class="clear"><!-- --></span>
            <cform:input isLong="true" key="client.system.communication.mail.in.server.mailbox" name="mailbox" isSpringForm="true" required="true" hint="true" />
            <span class="clear"><!-- --></span>
            <cform:input key="client.system.communication.mail.in.server.login" name="login" isSpringForm="true" required="true" />
            <cform:input key="client.system.communication.mail.in.server.password" type="password" name="password" isSpringForm="true" required="true" />
            <span class="clear"><!-- --></span>
            <div class="spacer">&nbsp;</div>
            <cform:input key="client.system.communication.mail.in.server.timeout.fetch" name="fetchingTimeout" isSpringForm="true" required="false" hint="true" />
            <cform:input key="client.system.communication.mail.in.server.timeout.reconnect" name="reconnectionTimeout" isSpringForm="true" required="true" hint="true" />
            <span class="clear"><!-- --></span>
            <div class="spacer">&nbsp;</div>
<c:if test="${!onlySingleMode && !onlyMultiMode}">
            <div class="check">
                <form:radiobutton path="mode" value="<%= MailInController.MODE_SINGLE %>" id="mode_single"
                    onclick="if($('mode_single').get('checked')){ $('singleModeSettings').setStyle('display','block');$('multiModeSettings').setStyle('display','none')};" />
                <label class="label" for="mode_single">
                    <fmt:message key="client.system.communication.mail.in.mode.single" />
                    <span class="advice">
                        <ct:tip key="client.system.communication.mail.in.mode.single.hint" />
                    </span>
                </label>
            </div>
            <div class="check">
                <form:radiobutton path="mode" value="<%= MailInController.MODE_MULTI %>" id="mode_multi"
                    onclick="if($('mode_multi').get('checked')){ $('singleModeSettings').setStyle('display','none');$('multiModeSettings').setStyle('display','block')};"/>
                <label class="label" for="mode_multi">
                    <fmt:message key="client.system.communication.mail.in.mode.multi" />
                    <span class="advice">
                        <ct:tip key="client.system.communication.mail.in.mode.multi.hint" />
                    </span>
                </label>
            </div>
</c:if>
            <span class="clear"><!-- --></span>
        </fieldset>
<c:if test="${!onlyMultiMode}">
        <fieldset id="singleModeSettings" <c:if test="${command.mode != 'single'}">style="display: none;"</c:if> >
            <h5><fmt:message key="client.system.communication.mail.in.mode.single.title" /></h5>
            <cform:input width="100" key="client.system.communication.mail.in.mode.single.address" hint="true" name="singleModeAddress" isSpringForm="true" required="true"/>
            <span class="clear"><!-- ie --></span>
        </fieldset>
</c:if>
<c:if test="${!onlySingleMode}">
        <fieldset id="multiModeSettings" <c:if test="${command.mode != 'multi'}">style="display: none;"</c:if> >
            <h5><fmt:message key="client.system.communication.mail.in.mode.multi.title" /></h5>
            <cform:input key="client.system.communication.mail.in.mode.multi.domain" hint="true" name="multiModeDomain" onkeyup="updateMultiModePreview();" isSpringForm="true" required="true"/>
            <cform:input key="client.system.communication.mail.in.mode.multi.suffix" hint="true" name="multiModeSuffix" onkeyup="updateMultiModePreview();" isSpringForm="true" />
            <span class="clear"><!-- --></span>
            <cform:checkbox width="100" key="client.system.communication.mail.in.mode.multi.business" name="multiModeUseAccount" onchange="updateMultiModePreview();" isSpringForm="true"/>
            <span class="clear"><!-- --></span>
            <div class="w100 long">
                <div class="label">
                    <label><b><fmt:message key="client.system.communication.mail.in.mode.multi.preview" /></b></label>
                </div>
                <div class="input"><span id="multi_preview" style="font-style: italic;">
                    &lt;blogAlias&gt;<c:if test="${command.multiModeUseAccount}">.&lt;yourAccount&gt;</c:if>.${command.multiModeSuffix}@${command.multiModeDomain}</span>
                </div>
            </div>
        </fieldset>
</c:if>
        <fieldset class="no-border">
            <span class="clear"><!-- --></span>
            <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
        </fieldset>
        <div class="actionbar actionbar-general">
            <cform:hidden name="action" isSpringForm="true"/>
            <div class="button-gray main">
                <input type="submit" onclick="$('action').set('value','<%= MailInController.ACTION_SAVE %>');" name="submit" value="<fmt:message key="button.save" />" />
            </div>
            <div class="button-gray">
                <input type="submit" onclick="$('action').set('value','<%= MailInController.ACTION_TEST %>');" name="submit" value="<fmt:message key="client.system.communication.mail.in.server.test" />" />
            </div>
            <span class="clear"><!-- --></span>
        </div>
    </form:form>
</div>