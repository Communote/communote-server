<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<tiles:importAttribute name="installStep" scope="request" />
<tiles:importAttribute name="title" />

<c:if test="${not empty installStep}">
    <c:set var="titlePrefix" scope="request"><fmt:message key="installer.step"><fmt:param>${installStep}</fmt:param></fmt:message>&nbsp;-&nbsp;</c:set>
</c:if>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
        <title>${titlePrefix}<fmt:message key="${title}" /> | <fmt:message key="installer.page.title" /></title>
        
        <link rel="stylesheet" type="text/css" href="<ct:url staticResource="true" value="/styles/installer/layout-common.css" />" media="screen,projection" />
        <link rel="stylesheet" type="text/css" href="<ct:url staticResource="true" value="/styles/installer/layout-border.css" />" media="screen,projection" />
        <link rel="stylesheet" type="text/css" href="<ct:url staticResource="true" value="/styles/installer/layout-header.css" />" media="screen,projection" />
        <link rel="stylesheet" type="text/css" href="<ct:url staticResource="true" value="/styles/installer/layout-main.css" />" media="screen,projection" />
        <link rel="stylesheet" type="text/css" href="<ct:url staticResource="true" value="/styles/installer/layout-form.css" />" media="screen,projection" />
        <link rel="stylesheet" type="text/css" href="<ct:url staticResource="true" value="/styles/installer/layout-clearing.css" />" media="screen,projection" />
        <link rel="stylesheet" type="text/css" href="<ct:url staticResource="true" value="/styles/installer/layout-installer.css" />" media="screen,projection" />

        <!-- basic scripts -->
        <script type="text/javascript">
        var baseUrl = '<ct:url staticResource="true" value="/microblog/global/" />';
        var contextUrl = '<ct:url staticResource="true" value="/" />';
        </script>
        <script type="text/javascript" src="<ct:url staticResource="true" value="/javascript/frameworks/mootools/mootools-core-1.5.0.js" />"></script>
        <script type="text/javascript" src="<ct:url staticResource="true" value="/javascript/frameworks/mootools/mootools-more-1.5.0.js" />"></script>
        <script type="text/javascript" src="<ct:url staticResource="true" value="/javascript/utils/AdvancedTips.js" />"></script>
        <script type="text/javascript" src="<ct:url staticResource="true" value="/javascript/utils/FormUtils.js" />"></script>
        <script type="text/javascript" src="<ct:url staticResource="true" value="/javascript/installer/InstallerUtils.js" />"></script>
    </head>
    <body>
        <div id="communote">        
            <div id="container">
                <%-- the header --%>
                <tiles:insert attribute="header" />
                
                <%-- the main section --%>
                <div id="main">
                    <noscript>
                        <div class="error noscript"><fmt:message key="portal.main.no.javascript.enabled" /></div>
                    </noscript>
                    <div id="installer-sidebar">
                        <tiles:insert attribute="col1" />
                    </div>
                    <div id="installer-content">
                        <div class="panel-head">
                            <h1><fmt:message key="installer.panel.title" /></h1>
                        </div>
                        <div class="panel-body">
                            <tiles:insert attribute="col2" />
                        </div>
                    </div>
                    <span class="clear"><!-- ie --></span>
                </div>
                <%-- the footer --%>
                <tiles:insert attribute="footer" />
            </div>
            <div id="communote-south">
                <div id="communote-south-west">
                    <div id="communote-south-east"></div>
                </div>
            </div>
        </div>
    </body>
</html>
