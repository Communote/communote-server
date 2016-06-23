<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.web.fe.portal.user.client.controller.ClientConfluenceAuthConfigController"%>
<%@page import="com.communote.server.core.osgi.OSGiManagement"%>
<%@page import="com.communote.server.api.ServiceLocator"%>
<%@page import="com.communote.server.web.commons.FeConstants"%>

<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div class="admin-authentication-confluence">
    <form:form commandName="clientConfluenceAuthenticationCommand">
        <div class="panel">
            <h4><fmt:message key="client.authentication" /> &gt; <span><fmt:message key="client.authentication.confluence.server.header" /></span></h4>
        </div>
        <input name="action" type="hidden" value="<%=ClientConfluenceAuthConfigController.UPDATE_CONFLUENCE_AUTH_CONFIG_ACTION %>"/>
        <div class="wrapper last">
            <div class="layer">
                <fieldset class="confluence">
                    <div class="line">
                        <div class="w100">
                            <div class="label">
                                <label for="config.basePath">
                                    <fmt:message key="client.authentication.confluence.url" />
                                    <ct:tip key="client.authentication.confluence.link.help" />
                                </label>
                            </div>
                            <div class="input authenticationApiUrl"><form:input path="config.basePath" cssClass="text"/></div>
                            <form:errors cssClass="error" path="config.basePath" element="span" />
                        </div>
                        <span class="clear"><!-- --></span>
                    </div>
                    <h6 class="subheadline"><fmt:message key="client.authentication.confluence.sync.account" /></h6>
                    <div class="line">
                        <div class="w50">
                            <div class="label"><label for="config.adminLogin"><fmt:message key="client.authentication.confluence.login" /></label></div>
                            <div class="input login"><form:input path="config.adminLogin" cssClass="text"/></div>
                            <form:errors cssClass="error" path="config.adminLogin" element="span" />
                        </div>
                        <div class="w50">
                            <div class="label"><label for="config.adminPassword"><fmt:message key="client.authentication.confluence.password" /></label></div>
                            <div class="input password">
                                <form:password path="config.adminPassword" cssClass="text" showPassword="false" disabled="true" autocomplete="off" />
                                <form:hidden path="passwordChanged" />
                                <a href="javascript:;" onclick="enableField('config.adminPassword', 'passwordChanged', this); return false;">
                                 <fmt:message key="client.form.change.password" /></a>
                            </div>
                            <form:errors cssClass="error" path="config.adminPassword" element="span" />
                        </div>
                        <span class="clear"><!-- --></span>
                    </div>
                    <% if(ServiceLocator.instance().getService(OSGiManagement.class).isBundleStarted("com.communote.plugins.communote-confluence-plugin")) { %>
                        <div class="line">
                            <div class="w100">
                                <div class="checkbox">
                                    <form:checkbox path="useConfluenceImages" id="useConfluenceImages" cssClass="checkbox"/>
                                    <label class="clickable label-checkbox" for="useConfluenceImages"><fmt:message key="client.authentication.confluence.use.images" /></label>
                                </div>
                                <form:errors cssClass="error" path="useConfluenceImages" element="span" />
                            </div>
                            <span class="clear"><!-- --></span>
                        </div>
                    <% } %>
                </fieldset>  
<c:if test="${syncIsAvailable}">
                <fieldset class="confluence-synchronisation">
                    <h5><fmt:message key="client.authentication.confluence.sync.title" /></h5>
                    <div class="line">
                        <div class="w100">
                            <div class="checkbox">
                                <form:checkbox path="config.synchronizeUserGroups" id="cb-synchronize" cssClass="checkbox" />
                                <label class="clickable label-checkbox" for="cb-synchronize"><fmt:message key="client.authentication.synchronize" /></label>
                            </div>
                            <form:errors cssClass="error" path="config.synchronizeUserGroups" element="span" />
                        </div>
                        <span class="clear"><!-- --></span>
                    </div>
                </fieldset>
                <div class="actionbar actionbar-general">
                    <div class="button-gray main">
                        <input type="submit" class="button" name="send2" value="<fmt:message key="client.authentication.update.submit" />" />
                    </div>
                    <span class="clear"><!-- --></span>
                </div>
</c:if>
                <fieldset class="confluence-synchronisation">
                    <h5><fmt:message key="client.authentication.allowExternalAuthentication.header" /></h5>
                    <div class="line">
                        <div class="w100">
                            <div class="checkbox">
                                <form:checkbox path="config.allowExternalAuthentication" id="cb-allowExternalAuthentication" cssClass="checkbox" />
                                <label class="clickable label-checkbox" for="cb-allowExternalAuthentication"><fmt:message key="client.authentication.allowExternalAuthentication" /></label>
                            </div>
                            <form:errors cssClass="error" path="config.allowExternalAuthentication" element="span" />
                        </div>
                        <span class="clear"><!-- --></span>
                    </div>
                    <span class="clear"><!-- --></span>
                </fieldset>
                <div class="actionbar actionbar-general">
                    <div class="button-gray main">
                        <input type="submit" class="button" name="send3" value="<fmt:message key="client.authentication.update.submit" />" />
                    </div>
                    <span class="clear"><!-- --></span>
                </div>
            </div>
        </div>
    </form:form>
</div>
