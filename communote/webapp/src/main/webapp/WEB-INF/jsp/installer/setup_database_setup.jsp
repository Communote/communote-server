<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<script type="text/javascript">
<!--
window.addEvent('domready', function() {
    startDatabaseSetup();
    observeDatabaseSetup();
});
//-->
</script>
<div class="form-container">
<form:form>
    <fieldset>
        <legend>${titlePrefix}<fmt:message key="installer.step.database.setup.legend.title" /></legend>
        <div class="fieldset-description">
            <fmt:message key="installer.step.database.setup.description">
                <fmt:param><span class="type-italic">${command.databaseUrl}</span></fmt:param>
            </fmt:message>
        </div>
        <div id="database-setup-progress">
            <div id="connection" class="row">
                <div class="col280 col active">&hellip;&nbsp;<fmt:message key="installer.step.database.setup.step.1" /></div>
                <div class="col60 col"><img class="loading" src="<c:url value="/themes/core/images/misc/loading-small.gif" />" alt="loading" /></div>
                <span class="clear"><!-- ie --></span>
            </div>
            <div id="preparing" class="row">
                <div class="col280 col">&hellip;&nbsp;<fmt:message key="installer.step.database.setup.step.2" /></div>
                <div class="col60 col"></div>
                <span class="clear"><!-- ie --></span>
            </div>
            <div id="schema" class="row">
                <div class="col280 col">&hellip;&nbsp;<fmt:message key="installer.step.database.setup.step.3" /></div>
                <div class="col60 col"></div>
                <span class="clear"><!-- ie --></span>
            </div>
            <div id="data" class="row">
                <div class="col280 col">&hellip;&nbsp;<fmt:message key="installer.step.database.setup.step.4" /></div>
                <div class="col60 col"></div>
                <span class="clear"><!-- ie --></span>
            </div>
        </div>
        <div id="reports" class="hidden">
            <div class="loading">&nbsp;</div>
            <div class="warning-report hidden">
                <h3><fmt:message key="installer.step.database.setup.report.error.head" /></h3>
                <div class="message">&nbsp;</div>
                <span class="clear"><!-- ie --></span>
            </div>
            <div class="success-report hidden">
                <h3><fmt:message key="installer.step.database.setup.report.success.head" /></h3>
                <div class="message">&nbsp;</div>
                <span class="clear"><!-- ie --></span>
            </div>
        </div>
    </fieldset>
    
    <div class="actionbar actionbar-general">
        <div class="button-gray button-right hidden">
            <input type="submit" id="next" name="_target3" value="<fmt:message key="installer.button.next" />" />
        </div>
        <div class="button-gray button-left">
            <input type="submit" id="previous" name="_target1" value="<fmt:message key="installer.button.previous" />" />
        </div>
        <span class="clear"><!-- ie --></span>
    </div>
</form:form>
</div>