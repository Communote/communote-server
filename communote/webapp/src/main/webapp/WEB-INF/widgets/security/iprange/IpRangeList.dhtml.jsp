<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<c:choose>
<c:when test="${!empty list}">
        <table class="table-container ipRangeFilterManagementList" width="100%" style="border-collapse: collapse;">
            <tr class="table-header">
                <th class="w8"><span><fmt:message key="client.iprange.enabled.table.head" /></span></th>
                <th><span><fmt:message key="client.iprange.name.table.head" /></span></th>
                <th><span><fmt:message key="client.iprange.includes.table.head" /></span></th>
                <th><span><fmt:message key="client.iprange.excludes.table.head" /></span></th>
                <th><span><fmt:message key="client.iprange.enabled_for.table.head" /></span></th>
            </tr>
<c:forEach var="filter" items="${list}">
                <tr class="table-content row clickable"
                     onmouseover="mOverAddHoverClass(this);"
                     onmouseout="mOutRemoveHoverClass(this);"
                     onclick="E('updateIpRange', '${filter.id}');widgetController.getWidget('${widget.widgetId}').showTab('${filter.name}');">
                   <td class="">
                       <input type="checkbox" class="checkbox" disabled="disabled" <c:if test="${filter.enabled }">checked="checked"</c:if> />
                   </td>
                   <td>${cf:truncateMiddle(filter.name,12,null)}</td>                         
                   <td class="includes">${fn:replace(filter.includes, ",", "<br />")}&nbsp;</td>
                   <td class="excludes">${fn:replace(filter.excludes, ",", "<br />")}&nbsp;</td>
                   <td class="enabledFor">
                       <c:set var="separator" value="" />
                       <c:forEach var="filterChannel" items="${filter.channels}">${separator}${filterChannel}<c:set var="separator" value=", " /></c:forEach>
                   </td>
                </tr>
</c:forEach>
        </table>
        <jsp:include page="/WEB-INF/jsp/common/paging.jsp" />
        <span class="clear"><!-- --></span>
</c:when>
<c:otherwise>
        <span><fmt:message key="client.iprange.no.filter.defined" /></span>
</c:otherwise>
</c:choose>
       