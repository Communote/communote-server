<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<div class="admin-welcome">
    <div class="panel">
        <h4><fmt:message key="client.administration.communote" /> &gt; <span><fmt:message key="client.administration.overview" /></span></h4>
    </div>

    <div class="wrapper last">
        <div class="layer">
            <fieldset class="statistic-short">
                <!-- legend><fmt:message key="client.welcome.title.administrative.information" /></legend -->
                <h5><fmt:message key="client.welcome.title.administrative.information" /></h5>
                <table border="0">
                    <thead>
                        <tr>
                            <th class="label left">&nbsp;</th>
                            <th class="current"><fmt:message key="client.welcome.current.size" /></th>
                            <th class="limit"><fmt:message key="client.welcome.limit" /></th>
                        </tr>
                    </thead>
                    <tbody>
<c:if test="${crcDataAvailable}">
                        <tr>
                            <td class="label"><fmt:message key="client.welcome.repository" /></td>
                            <td class="current">${crcSize}</td>
                            <td class="limit">${crcLimit}</td>
                        </tr>
    <c:if test="${crcLimitReached || not empty crcSizePercent}">
                        <tr>
                            <td>&nbsp;</td>
                            <td class="notify" colspan="2">
        <c:choose>
            <c:when test="${crcLimitReached}">
                                <c:set var="notifyClass" value="error" />
            </c:when>
            <c:otherwise>
                                <c:set var="notifyClass" value="warning" />
            </c:otherwise>
        </c:choose>
                                <div class="notify-inline">
                                    <div class="notify-${notifyClass}">
                                        <div class="message">
        <c:choose>
            <c:when test="${not empty crcSizePercent}">
                                            ${crcSizePercent} <fmt:message key="client.welcome.error.limit.short" />
            </c:when>
            <c:otherwise>
                                            <fmt:message key="client.welcome.error.limit.reached" />
            </c:otherwise>
        </c:choose>
                                        </div>
                                        <span class="clear"><!-- ie --></span>
                                    </div>
                                </div>
                            </td>
                        </tr>
    </c:if>
</c:if>
                        <tr>
                            <td class="label"><fmt:message key="client.welcome.blog" /></td>
                            <td class="current">${blogSize}</td>
                            <td class="limit">${blogLimit}</td>
                        </tr>
<c:if test="${blogLimitReached || not empty blogPercent}">
                        <tr>
                            <td>&nbsp;</td>
                            <td class="notify" colspan="2">
<c:choose>
<c:when test="${blogLimitReached}">
                                <c:set var="notifyClass" value="error" />
</c:when>
<c:otherwise>
                                <c:set var="notifyClass" value="warning" />
</c:otherwise>
</c:choose>
                                <div class="notify-inline">
                                    <div class="notify-${notifyClass}">
                                        <div class="message">
<c:choose>
<c:when test="${not empty blogPercent}">
                                            ${blogPercent} <fmt:message key="client.welcome.error.limit.short" />
</c:when>
<c:otherwise>
                                            <fmt:message key="client.welcome.error.limit.reached" />
</c:otherwise>
</c:choose>
                                        </div>
                                        <span class="clear"><!-- ie --></span>
                                    </div>
                                </div>
                            </td>
                        </tr>
</c:if>
                        <tr>
                            <td class="label"><fmt:message key="client.welcome.user.account" /></td>
                            <td class="current">${userCount}</td>
                            <td class="limit">${userCountLimit }</td>
                        </tr>
<c:if test="${userLimitReached || not empty userPercent}">
                        <tr>
                            <td>&nbsp;</td>
                            <td class="notify" colspan="2">
<c:choose>
<c:when test="${userLimitReached}">
                                <c:set var="notifyClass" value="error" />
</c:when>
<c:otherwise>
                                <c:set var="notifyClass" value="warning" />
</c:otherwise>
</c:choose>
                                <div class="notify-inline">
                                    <div class="notify-${notifyClass}">
                                        <div class="message"><c:choose>
<c:when test="${not empty userPercent}">
                                            ${userPercent} <fmt:message key="client.welcome.error.limit.short" />
</c:when>
<c:otherwise>
                                            <fmt:message key="client.welcome.error.limit.reached" />
</c:otherwise>
</c:choose></div>
                                            <span class="clear"><!-- ie --></span>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:if>
                        <tr>
                            <td class="label"><fmt:message key="client.welcome.user.tagged.item" /></td>
                            <td class="current">${userTaggedSize}</td>
                            <td class="limit">${userTaggedLimit}</td>
                        </tr>
<c:if test="${userTaggedLimitReached || not empty userTaggedPercent}">
                        <tr>
                            <td>&nbsp;</td>
                            <td class="notify" colspan="2">
<c:choose>
<c:when test="${userTaggedLimitReached}">
<c:set var="notifyClass" value="error" />
</c:when>
<c:otherwise>
                                <c:set var="notifyClass" value="warning" />
</c:otherwise>
</c:choose>
                                <div class="notify-inline">
                                    <div class="notify-${notifyClass}">
                                        <div class="message">
<c:choose>
<c:when test="${not empty userTaggedPercent}">
                                            ${userTaggedPercent} <fmt:message key="client.welcome.error.limit.short" />
</c:when>
<c:otherwise>
                                            <fmt:message key="client.welcome.error.limit.reached" />
</c:otherwise>
</c:choose>
                                        </div>
                                        <span class="clear"><!-- --></span>
                                    </div>
                                </div>
                            </td>
                        </tr>
</c:if>
                    </tbody>
                </table>
            </fieldset>
            <fieldset class="statistic-short sequently">
                <h4><fmt:message key="service.documentation.manual" /></h4>
                <fmt:message key="service.documentation.manual.description" />
                 <a href="<fmt:message key="communote.documentation.usermanual.url" />" target="_blank"><fmt:message key="service.documentation.manual" /></a>
                <br />
                <h4><fmt:message key="service.documentation.administrative.manual" /></h4>
                <fmt:message key="service.documentation.administrative.manual.description" />
                <a href="<fmt:message key="communote.documentation.adminmanual.url" />" target="_blank"><fmt:message key="service.documentation.administrative.manual" /></a>
            </fieldset>
        </div>
    </div>
</div>
