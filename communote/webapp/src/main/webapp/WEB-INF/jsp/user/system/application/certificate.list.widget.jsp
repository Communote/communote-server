<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.web.fe.widgets.management.system.application.CertificateListWidget"%>
<%@page import="com.communote.server.widgets.annotations.WidgetAction"%>
<%@page import="java.security.cert.X509Certificate"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<fieldset class="no-border" style="padding: 0px;">
<c:choose>
<c:when test="${!empty list}">
    <div class="table-container ipRangeFilterManagementList">
        <div class="table-header">
            <div class="column w60"><span><fmt:message key="client.system.application.certificate.upload.list.name" /></span></div>
            <div class="column w15"><span><fmt:message key="client.system.application.certificate.upload.list.start" /></span></div>
            <div class="column w15"><span><fmt:message key="client.system.application.certificate.upload.list.end" /></span></div>
            <div class="column w10"><span>&nbsp;</span></div>
            <span class="clear"><!-- Clearing --></span>
        </div>
        <div class="table-content">
<c:forEach var="certificate" items="${list}">
            <div class="row not-clickable"
                         onmouseover="$('${certificate.left}').setStyle('display','block');"
                         onmouseout="$('${certificate.left}').setStyle('display','none');">
<c:choose>
<c:when test="${certificate.right.left.type == 'X.509'}">
                <div class="column w60">
                    <%@ include file="certificate.list.widget.validity.jsp" %>
                    <c:set var="common_name">${fn:substringBefore(fn:substringAfter(certificate.right.left.subjectX500Principal.name,'CN='),',')}</c:set>
<c:if test="${empty common_name}">
                    <c:set var="common_name">
                        ${certificate.right.left.subjectX500Principal.name}
                    </c:set>
</c:if>
                    <span title="${certificate.right.left.subjectX500Principal.name}">
                        ${cf:truncateMiddle(common_name,50,null)}
                    </span>
                </div>                         
                <div class="column w15"><fmt:formatDate dateStyle="short" value="${certificate.right.left.notBefore}"/></div>
                <div class="column w15"><fmt:formatDate dateStyle="short" value="${certificate.right.left.notAfter}"/></div>
</c:when>
<c:otherwise>
                <c:set var="newCertificate">
                    <fmt:message key="client.system.application.certificate.upload.list.unknown">
                        <fmt:param>${certificate.right.left}</fmt:param>
                    </fmt:message>
                </c:set>
                <div class="column w100"><span title="${certificate.right.left}">${cf:truncateMiddle(newCertificate,100,null)}</span></div>
</c:otherwise>
</c:choose>
                <div class="column w10" style="display:none" id="${certificate.left}">
                    <form:form>
                        <input type="hidden" name="certificate" value="${certificate.left}"/>
                        <input type="hidden" name="<%= WidgetAction.ACTION %>" value="<%= CertificateListWidget.ACTION %>"/>
                        <!-- input type="submit" name="submitButton" class="toolbox-icon delete clickable" value="<fmt:message key="button.delete" />" title="<fmt:message key="client.system.application.certificate.remove" />" / -->
                        <input type="submit" name="submitButton" class="button-link" value="<fmt:message key="button.delete" />" title="<fmt:message key="client.system.application.certificate.remove" />" />
                    </form:form>
                </div>
                <span class="clear"><!-- --></span>
                </div>
</c:forEach>
            </div>
        </div>
        <span class="clear"><!-- --></span>
</c:when>
<c:otherwise>
        <div class="notify-inline">
            <div class="notify-info">
                <div class="message">
                    <fmt:message key="client.system.application.certificate.upload.list.empty" />
                </div>
                <span class="clear"><!-- --></span>
            </div>
        </div>
</c:otherwise>
</c:choose>
</fieldset>
