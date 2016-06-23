<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.web.fe.portal.user.system.communication.XmppController"%>
<%@page import="com.communote.server.api.core.config.type.ApplicationPropertyXmpp"%>
<%@page import="com.communote.server.api.ServiceLocator"%>
<%@page import="org.apache.velocity.app.VelocityEngine"%>
<%@page import="org.apache.velocity.VelocityContext"%>
<%@page import="org.springframework.ui.velocity.VelocityEngineUtils"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.springframework.ui.velocity.VelocityEngineFactory"%>

<% 
String xmppLogin = ApplicationPropertyXmpp.LOGIN.getValue();
if(xmppLogin == null){
    xmppLogin = StringUtils.EMPTY;
}
pageContext.setAttribute("xmppLogin", xmppLogin);

String xmppHost = ApplicationPropertyXmpp.HOST.getValue();
if(xmppHost == null){
    xmppHost = StringUtils.EMPTY;
}
pageContext.setAttribute("xmppHost", xmppHost);

String blogSuffix = ApplicationPropertyXmpp.BLOG_SUFFIX.getValue();
if(blogSuffix == null){
    blogSuffix = StringUtils.EMPTY;
}
pageContext.setAttribute("blogSuffix", blogSuffix);
%>

<div class="form-container no-border">
    <div class="from-description">
        <fmt:message key="client.system.communication.xmpp.openfire.description" />
    </div>
    <fieldset>
        <legend><fmt:message key="client.system.communication.xmpp.openfire.alias" /></legend>
        <fmt:message key="client.system.communication.xmpp.openfire.alias.description" />
        <br /><br />
        <fmt:message key="client.system.communication.xmpp.openfire.alias.data" />
        <br /><br />
        <table style="width: auto;">
            <tr>
                <td style="font-weight: bold;"><fmt:message key="client.system.communication.xmpp.openfire.alias.data.realjid" /></td>
                <td style="border-right: 0px;">
                    <c:if test="${!empty xmppLogin || !empty xmppHost}">
                        ${xmppLogin}@${xmppHost}
                    </c:if>
                </td>
            </tr>
            <tr>
                <td style="font-weight: bold;"><fmt:message key="client.system.communication.xmpp.openfire.alias.data.alias" /></td>
                <td style="border-right: 0px;">*${fn:substringBefore(blogSuffix,"@")}</td>
            </tr>
            <tr>
                <td style="font-weight: bold;"><fmt:message key="client.system.communication.xmpp.openfire.alias.data.subdomain" /></td>
                <td style="border-right: 0px;">
                    <%= blogSuffix.substring(blogSuffix.indexOf("@")+1).replace("." + xmppHost, "") %>
                </td>
            </tr>
        </table>
        <br /><br />
        <img class="screenshot" src="<ct:url staticResource="true" value="/images/xmpp/openfire_alias_example.png"/>" />
        <!-- img src="<ct:url staticResource="true" value="/images/xmpp/openfire_alias_example.png"/>" style="border: 1px solid #CCCCCC;" / -->
    </fieldset>

    <fieldset>
        <legend><fmt:message key="client.system.communication.xmpp.openfire.http-auth" /></legend>
        <fmt:message key="client.system.communication.xmpp.openfire.http-auth.description" />
        <br /><br />
        <textarea style="width: 100%; height: auto;" rows="50" readonly="readonly">
            <%= XmppController.getOpenfireHttpAuthProperties(request)%>
        </textarea>
    </fieldset>
</div>