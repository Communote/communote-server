<!DOCTYPE html>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.api.core.config.type.ApplicationProperty"%>
<%@page import="com.communote.server.api.core.config.ApplicationConfigurationProperties"%>
<%@page import="com.communote.server.api.core.application.CommunoteRuntime"%>
<%@page import="com.communote.server.web.commons.viewtool.AdministrationTool"%>
<%@page import="com.communote.server.core.common.velocity.CommunoteTool"%>
<%@page import="com.communote.server.web.commons.viewtool.UrlTool"%>
<%@page import="com.communote.server.core.security.SecurityHelper"%>
<%@page import="com.communote.server.core.user.UserManagementHelper"%>
<%@page import="com.communote.server.web.WebServiceLocator"%>
<html lang="en">
    <head>
        <tiles:importAttribute name="title" />
        <title><fmt:message key="${title}" /> &ndash; <c:out value="<%= ClientHelper.getCurrentClient().getName() %>"/></title>
        <meta http-equiv="X-UA-Compatible" content="IE=9" />
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta name="language" content="en" />
        <link rel="shortcut icon" type="image/x-icon" href="<ct:url staticResource="true" value="/favicon.ico" />" />
        <%
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance().getConfigurationManager().getApplicationConfigurationProperties();
        boolean packCss = props.getProperty(ApplicationProperty.STYLES_PACK, true);
        boolean compressCss = props.getProperty(ApplicationProperty.STYLES_COMPRESS, true);
        boolean packJavaScript = props.getProperty(ApplicationProperty.SCRIPTS_PACK, true);
        boolean compressJavaScript = props.getProperty(ApplicationProperty.SCRIPTS_COMPRESS, true);
        // create a request tool to reuse the code there, currently only need the request in the URL tool
        UrlTool urlTool = new UrlTool();
        urlTool.setRequest(request);
        AdministrationTool adminTool = new AdministrationTool();
        request.setAttribute("administrationTool",  adminTool);
        request.setAttribute("menuEntry", adminTool.getCurrentMenuEntry(request));
        request.setAttribute("currentLocale", new CommunoteTool().getCurrentUserLocale(request));
        %>
        <%-- just hard-coding the categories here, should be refactored when switching to VM --%>
        <c:choose>
            <c:when test="<%= packCss%>"><link rel="stylesheet" type="text/css" href="<%= urlTool.renderConcatenatedCssUrl("admin", compressCss) %>" media="screen,projection" />
            </c:when>
            <c:otherwise>
                <c:forEach items="<%= urlTool.renderCssUrls(\"admin\", compressCss) %>" var="cssResource">
                <link rel="stylesheet" type="text/css" href="${cssResource}" media="screen,projection" />
                </c:forEach>
            </c:otherwise>
        </c:choose>
        <c:set var="jsMessagesCategory" value="admin" scope="page" />
        <script type="text/javascript">
        <%-- namespace setup --%>
        communote = {
                classes: {},
                configuration: {
                    openLinksInNewWindow: (window != window.parent)
                },
                environment: {
                    page: 'administration'
                },
                i18n: {},
                server: {}
        };
        communote.i18n.localizedMessages = <%=WebServiceLocator.instance().getJsMessagesRegistry().getJsMessages(request, (String)pageContext.getAttribute("jsMessagesCategory"))%>;
        <%-- save userId and alias of currently logged in user in global javascript vars --%>
        communote.currentUser = {
            id: <%=SecurityHelper.getCurrentUserId()%>,
            alias: '<%=SecurityHelper.getCurrentUserAlias()%>',
            isManager: <%= SecurityHelper.isClientManager()%>,
            timeZoneOffset: <%=UserManagementHelper.getCurrentOffsetOfEffectiveUserTimeZone()%>,
            language: '<%=SecurityHelper.getCurrentUser().getUserLocale()%>'
        };
        </script>
        <%@ include file="/WEB-INF/jsp/portal/blog/common/blog_basic_header.jspf"%>
        <c:choose>
            <c:when test="<%= packJavaScript%>">
                <script type="text/javascript" src="<%= urlTool.renderConcatenatedJsUrl("communote-core", compressJavaScript)%>"></script>
                <script type="text/javascript" src="<%= urlTool.renderConcatenatedJsUrl("admin", compressJavaScript)%>"></script>
            </c:when>
            <c:otherwise>
                <c:forEach items="<%= urlTool.renderJsUrls(\"communote-core\", compressJavaScript) %>" var="javaScriptResource">
                <script type="text/javascript" src="${javaScriptResource}"></script>
                </c:forEach>
                <c:forEach items="<%= urlTool.renderJsUrls(\"admin\", compressJavaScript) %>" var="javaScriptResource">
                <script type="text/javascript" src="${javaScriptResource}"></script>
                </c:forEach>
            </c:otherwise>
        </c:choose>        
    </head>
    <body id="cn-admin">
        <div id="communote">
            <div id="container">
                <%-- the header --%>
                <tiles:importAttribute name="portalSection" />          
                <tiles:insert attribute="header">
                    <tiles:put name="activeMenuEntry" value="" />
                    <tiles:put name="portalSection" value="${portalSection}" />
                </tiles:insert>

                <div id="admin-main">
                    <noscript>
                        <div class="error noscript"><fmt:message key="portal.main.no.javascript.enabled" /></div>
                    </noscript>

                    <%-- import column variables --%>
                    <tiles:importAttribute name="navigation" />
                    <tiles:importAttribute name="content" />
                    <div id="admin-main-left">
                        <tiles:insert attribute="navigation" />
                    </div>
                    <div id="admin-main-right">
                        <tiles:importAttribute name="innerContent"  ignore="true" />
                        <tiles:insert attribute="content">
                             <tiles:put name="innerContent"  value="${innerContent}" />
                             <tiles:put name="innerTitle"    value="${menuEntry.category.getLocalizedLabel(currentLocale, null)}" />
                             <tiles:put name="innerSubtitle" value="${menuEntry.entry.getLocalizedLabel(currentLocale, null)}" />
                        </tiles:insert>
                    </div>
                    <span class="clear"><!-- --></span>
                </div>
                <tiles:insert attribute="footer"></tiles:insert>
            </div>
        </div>
    </body>
</html>