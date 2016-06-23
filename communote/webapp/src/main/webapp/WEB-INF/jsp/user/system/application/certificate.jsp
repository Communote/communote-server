<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div id="TabPanel" class="mootabs mootabs-admin-area">
    <ul class="mootabs_title">
        <li id="Tab1" class="first<c:if test="${not uploadIsFirst}"> active</c:if>"><span><fmt:message key="client.overview" /></span></li>
        <li id="Tab2" <c:if test="${uploadIsFirst}">class="active"</c:if>><span><fmt:message key="client.system.application.certificate.upload" /></span></li>
    </ul>
    <div class="mootabs_panel" id="Tab1Panel">
        <div class="panel-description">
            <fmt:message key="client.system.application.certificate.description" />
        </div>
        <div class="TSWidget CertificateListWidget" id="CertificateList">
            &nbsp;<!-- -->
        </div>
    </div>
     <div class="mootabs_panel form-background" id="Tab2Panel">
        <div class="form-container">
            <form:form enctype="multipart/form-data">
                <fieldset class="no-border">
                    <div class="fieldset-description">
                        <fmt:message key="client.system.application.certificate.upload.description" />
                        <br /><br />
                        <cform:notification type="info">
                            <fmt:message key="client.system.application.certificate.info" />
                        </cform:notification>
                    </div>
                    <div class="w100 long">
                        <div class="input">
                            <input type="file" name="certificate" class="file" id="certificate" />
                        </div>
                    </div>
                    <span class="clear"><!-- ie --></span>
                </fieldset>
                <div class="actionbar actionbar-general">
                   <div class="button-gray main">
                        <input type="submit" name="submitButton" value="<fmt:message key="button.upload" />" />
                    </div>
                    <span class="clear"><!-- ie --></span>
                </div>
           </form:form>
        </div>
    </div>
</div>
