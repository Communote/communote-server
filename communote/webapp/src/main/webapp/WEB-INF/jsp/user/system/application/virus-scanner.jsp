<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>
<%@ page import="com.communote.server.web.fe.portal.user.system.application.SupportedVirusScannerTypes" %>
<%@ page import="com.communote.server.web.fe.portal.user.system.application.VirusScanningController" %>
<script type="text/javascript">

function toggleViewArea(obj){
    var selectedValue = obj.options[obj.selectedIndex].value;
    var area = $(selectedValue + '-form-area');
    var areas = $$('div[id*=-form-area]');

    if(area == null || areas == null){
        return;
    }
    areas.addClass('hidden');
    area.removeClass('hidden');
}
</script>

<div class="layer">
    <form:form>
        <cform:hidden name="action" isSpringForm="true" />
        <div class="actionbar actionbar-service">
            <form:hidden path="enabled" />
<c:choose>
<c:when test="${command.enabled}">
            <div class="service-status">
                <img alt="active" src="<ct:url staticResource="true" value="/themes/core/images/misc/service-on.png" />" />&nbsp;<fmt:message key="client.system.application.virusscanning.service.status.on" />
            </div>
            <div class="button-gray">
                <input type="submit" onclick="$('action').set('value','<%= VirusScanningController.ACTION_STOP_SERVICE %>');" name="submit" value="<fmt:message key="button.service.off" />" />
            </div>
</c:when>
<c:otherwise>
            <div class="service-status">
                 <img alt="turned off" src="<ct:url staticResource="true" value="/themes/core/images/misc/service-off.png" />" />&nbsp;<fmt:message key="client.system.application.virusscanning.service.status.off" />
            </div>
            <div class="button-gray">
                <input type="submit" onclick="$('action').set('value','<%= VirusScanningController.ACTION_START_SERVICE %>');" name="submit" value="<fmt:message key="button.service.on" />" />
            </div>
</c:otherwise>    
</c:choose>
            <span class="clear"><!-- ie --></span>
        </div>
        <hr />
        <div class="from-description">
            <fmt:message key="client.system.application.virusscanning.description" />
        </div>
        <fieldset class="no-border">
            <div class="w100">
                <div class="label">
                    <label for="scannerType"><fmt:message key="client.system.application.virusscanning.label.type" /><span class="required">*</span></label><ct:tip key="client.system.application.virusscanning.label.type.hint" />
                </div>
                <div class="input">
                    <form:select path="scannerType" cssErrorClass="error" onchange="toggleViewArea(this);">
<c:forEach var="type" items="<%= SupportedVirusScannerTypes.values() %>">
                        <c:set var="text"><fmt:message key="client.system.application.virusscanning.type.${type}" /></c:set>
<c:if test="${text ne 'no_scanner'}">
                        <form:option value="${type}"><fmt:message key="client.system.application.virusscanning.type.${type}" /></form:option>
</c:if>
</c:forEach>
                    </form:select>
                </div>
                <form:errors path="scannerType" cssClass="error" element="div" />
            </div>
            <span class="clear"><!-- --></span>
            <div id="<%= SupportedVirusScannerTypes.CLAMAV %>-form-area" <c:if test="${not command.clamAVScanner}">class="hidden"</c:if>>
                <cform:input key="client.system.application.virusscanning.label.clamhost" hint="true" name="clamHost" isSpringForm="true" required="true" />
                <cform:input key="client.system.application.virusscanning.label.clamport" hint="true" name="clamPort" isSpringForm="true" required="true" maxlength="5" />
                <span class="clear"><!-- --></span>
                <div class="spacer">&nbsp;</div>
                <cform:input key="client.system.application.virusscanning.label.clamdir" isLong="true" hint="true" name="clamTempDir" isSpringForm="true" required="true" />
                <span class="clear"><!-- --></span>
                <cform:input key="client.system.application.virusscanning.label.clamtimeout" hint="true" name="clamConnectionTimeout" isSpringForm="true" required="true" />
                <span class="clear"><!-- --></span>
            </div>
            <div id="<%= SupportedVirusScannerTypes.CMDLINE %>-form-area" <c:if test="${not command.cmdLineScanner}">class="hidden"</c:if>>
                <cform:input key="client.system.application.virusscanning.label.cmdcommand" isLong="true" hint="true" name="cmdCommand" isSpringForm="true" required="true" />
                <span class="clear"><!-- --></span>      
                <cform:input key="client.system.application.virusscanning.label.cmdexitcode" hint="true" name="cmdExitCode" isSpringForm="true" required="true" />
                <cform:input key="client.system.application.virusscanning.label.cmdtimeout" hint="true" name="cmdProcessTimeout" isSpringForm="true" required="true" />
                <span class="clear"><!-- --></span>
                <div class="spacer">&nbsp;</div>
                <cform:input key="client.system.application.virusscanning.label.cmddir" isLong="true" hint="true" name="cmdTempDir" isSpringForm="true" required="true" />
                <span class="clear"><!-- --></span>      
                <cform:input key="client.system.application.virusscanning.label.cmdfileprefix" hint="true" name="cmdTempFilePrefix" isSpringForm="true" required="true" />
                <cform:input key="client.system.application.virusscanning.label.cmdfilesuffix" hint="true" name="cmdTempFileSuffix" isSpringForm="true" required="true" />
                <span class="clear"><!-- --></span>
            </div>
        </fieldset>
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" onclick="$('action').set('value','<%= VirusScanningController.ACTION_SAVE %>');" name="submit" value="<fmt:message key="button.save" />" />
            </div>
            <span class="clear"><!-- --></span>
        </div>
    </form:form>
</div>