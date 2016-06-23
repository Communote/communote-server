<%@include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.api.core.application.CommunoteRuntime"%>
<%@page import="com.communote.server.core.image.CoreImageType"%>
<%@page import="com.communote.server.api.core.config.type.ClientProperty"%>
<%@page import="com.communote.server.core.user.helper.UserNameHelper"%>
<%@page import="com.communote.server.core.security.SecurityHelper"%>
<%@page import="com.communote.server.web.fe.portal.user.forms.ForgottenPWForm"%>
<%@page import="org.springframework.web.util.WebUtils" %>
<%@page import="com.communote.server.model.user.ImageSizeType"%>

<%
	String supportEmailAddress = CommunoteRuntime.getInstance().getConfigurationManager().getClientConfigurationProperties().getProperty(ClientProperty.SUPPORT_EMAIL_ADDRESS, "");
pageContext.setAttribute("supportEmailAddress", supportEmailAddress);
%>

<script type="text/javascript">
function beforeProfilePopup(pId) {
    widgetController.findWidgets($(pId));
}
<%-- remove the widgets defined in complete user profile popup--%>
function beforeCloseProfilePopup() {
    widgetController.removeWidgetById('UserProfileEdit');
    widgetController.removeWidgetById('ImageUpload');
    widgetController.removeWidgetById('ChangeEmail');
    widgetController.removeWidgetById('ChangePassword');
    widgetController.removeWidgetById('DeleteAccount');
}
</script>
<tiles:importAttribute name="portalSection" ignore="true" />

<div id="cn-header">
    <div id="cn-header-content">
        <div id="cn-header-logo">
            <a class="cn-client-image" href="<ct:url />"><img id="client-logo" alt="client-logo" src="<ct:img type="<%=CoreImageType.clientlogo.name()%>" size="<%=ImageSizeType.LARGE%>"/>" /></a>
        </div>
        <div id="cn-header-navigation">
            <authz:authorize ifAllGranted="ROLE_KENMEI_USER">
            <div id="cn-service-navigation">
                <div class="cn-toplink-administration">
                    <a id="cn-toplink-overview" class="cn-link" href="<ct:url value="/portal/home" />"><fmt:message key="portal.menu.home.title" /></a>
                    <authz:authorize ifAllGranted="ROLE_KENMEI_CLIENT_MANAGER">
                        <%-- client administration --%>
                        <a id="cn-toplink-administration" class="cn-link cn-active" href="<ct:url value="/admin/client/welcome" />">
                            <fmt:message key="client.menu.myclient.title" />
                        </a>
                    </authz:authorize>
                </div>
            </div>
            <div id="cn-profile-navigation">
                    
                    <c:set var="userLink">/portal/users/<%=SecurityHelper.getCurrentUserAlias()%></c:set>
            
                    <div class="cn-bar">
                        <ul class="cn-menu">
                            <li class="cn-more-actions"><a class="cn-link" href="<ct:url value="${userLink}" />"><span><%=UserNameHelper.getSimpleDefaultUserSignature(SecurityHelper.assertCurrentKenmeiUser())%></span></a> <span class="cn-icon cn-arrow"><!-- &#9660; --></span><span class="cn-clear"><!--  --></span></li>
                            <li>
                                <ul class="cn-menu-list">
                                    <li id="cn-editprofile"><a class="cn-link" href="<ct:url value="/portal/user-edit" />"><fmt:message key="portal.menu.myprofile.edit"/></a></li>
                                    <li id="cn-logout"><a class="cn-link" href="<ct:url value="/logout" />"><fmt:message key="login.logout"/></a></li>
                                </ul>
                            </li>
                        </ul>
                        <a href="<ct:url value="${userLink}" />"><img class="profile-image" src="<ct:img userId="<%=SecurityHelper.getCurrentUserId()%>" type="<%=CoreImageType.userlogo.name()%>" size="<%=ImageSizeType.SMALL %>" />" title="<%= UserNameHelper.getDefaultUserSignature(SecurityHelper.assertCurrentKenmeiUser()) %>" alt="<%= UserNameHelper.getDefaultUserSignature(SecurityHelper.assertCurrentKenmeiUser()) %>" width="20" height="20" /></a>
                    </div>
            </div>
            <span class="cn-clear"><!-- --></span>
            </authz:authorize>
        </div>
    </div>
</div>
