<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@page import="com.communote.server.web.commons.MessageHelper"%>
<%@page import="com.communote.server.model.user.ImageSizeType"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div class="admin-profile">
    <div class="panel">
        <h4><fmt:message key="administration.title.menu.admin-clientprofil" /> &gt; <span><fmt:message key="client.change.email.title" /></span></h4>
    </div>
    <div class="wrapper last">
        <div class="layer">
            <form:form method="post">
                <fieldset class="email">
                    <div class="fieldset-description">
                        <fmt:message key="client.change.email.description" />
                    </div>
                    <div class="spacer">&nbsp;</div>
                    <div class="line">
                        <div class="w50">
                            <div class="label"><label for="clientEmail"><fmt:message key="client.change.email.address" />:<span class="required">*</span></label></div>
                            <div class="input"><form:input path="clientEmail" cssClass="text" htmlEscape="true" /></div>
                            <form:errors cssClass="error" path="clientEmail" element="span" />
                        </div>
                        <div class="w50">
                            <div class="label"><label for="clientEmailName"><fmt:message key="client.change.email.name" />:<span class="required">*</span></label></div>
                            <div class="input"><form:input path="clientEmailName" cssClass="text" htmlEscape="true" /></div>
                            <form:errors cssClass="error" path="clientEmailName" element="span" />
                        </div>
                        <span class="clear"><!-- --></span>
                    </div>
                    <div class="line">
                        <div class="w50">
                            <div class="label"><label for="clientSupportEmailAddress"><fmt:message key="client.support.email.address" /><span class="required">*</span></label></div>
                            <div class="input"><form:input path="clientSupportEmailAddress" cssClass="text" htmlEscape="true" /></div>
                            <form:errors cssClass="error" path="clientSupportEmailAddress" element="span" />
                        </div>
                        <span class="clear"><!-- --></span>
                    </div>
                    <div class="info"><fmt:message key="form.info.required.fields" /></div>   
                </fieldset>
                <div class="actionbar actionbar-general">
                    <div class="button-gray main">
                        <input type="submit" name="button" value="<fmt:message key="button.save" />" title="<fmt:message key='button.save' />">
                    </div>
                    <span class="clear"></span>
                </div>
            </form:form>
            <span class="clear"></span>
            <h5><fmt:message key="client.change.email.signature" /></h5>
            <div class="fieldset-description">
                <fmt:message key="client.change.email.signature.description" />
            </div>
            <div class="TSWidget LocalizeMessageWidget" id="LocalizeMessage">
                <!-- 
                    messageKey=custom.message.client.email.signature
                    showIsHtml=false
                -->
            </div>
        </div>
    </div>
</div>
