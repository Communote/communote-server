<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.core.common.session.SessionHandler"%>
<%-- 
    URLs
    You have to use the custom URL Tag here because installationDone is set to true 
    and the normal filter chain will be run on all requests
     
    Attention!
    If you change the resource 'checkInitializationStatus.json', 
    it also must be modified in the file 'authentication.xml' and InitializationFilter
--%>
<script type="text/javascript">
var initUrl = '<ct:url value="installer/completeInstallation.json" />';
var checkUrl = '<ct:url value="/portal/initializationStatus.json" />';

<%
Object lang = SessionHandler.instance().getCurrentLocale(request);
if(lang != null) {
    request.setAttribute("urlParam", "?lang="+lang);
}
%>
checkUrl = checkUrl + '${urlParam}';

window.addEvent('domready', function() {
    startApplicationInitialization(initUrl)
    observeApplicationInitialization(checkUrl);
});

</script>
<div class="form-container">
<form:form>
    <fieldset>
        <legend>${titlePrefix}<fmt:message key="installer.step.finish.legend.title" /></legend>
        <div id="final-step" class="fieldset-description">
            &hellip;&nbsp;<fmt:message key="installer.step.finish.description" />
        </div>
        <div id="reports">
            <div class="loading">
                <img src="<c:url value="/themes/core/images/main/loading.gif" />" alt="loading" />
            </div>
            <div class="warning-report hidden">
                <h3><fmt:message key="installer.step.finish.initialization.error.head" /></h3>
                <div class="message"><fmt:message key="installer.step.finish.initialization.error" /></div>
                <span class="clear"><!-- ie --></span>
            </div>
            <div class="success-report hidden">
                <h3><fmt:message key="installer.step.finish.initialization.success.head" /></h3>
                <div class="message"><fmt:message key="installer.step.finish.initialization.success" /></div>
                <span class="clear"><!-- ie --></span>
            </div>
        </div>
    </fieldset>
    <div class="actionbar actionbar-general">
        <div class="button-gray button-right hidden">
            <a href="<ct:url value="/" />"><fmt:message key="installer.button.login" /></a>
        </div>
        <span class="clear"><!-- ie --></span>
    </div>
</form:form>
</div>