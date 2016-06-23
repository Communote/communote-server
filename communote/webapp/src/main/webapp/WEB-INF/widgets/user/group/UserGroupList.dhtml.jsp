<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<div class="table-container">
    <div class="table-header">
        <div class="row">
            <div class="column w35">
                <span><fmt:message key="client.user.group.management.group.details.name"/></span>
            </div>
            <div class="column w15">
                <span><fmt:message key="client.user.group.management.group.details.members.count"/></span>
            </div>
            <div class="column w49">
                <span><fmt:message key="client.user.group.management.group.details.description"/></span>
            </div>
            <span class="clear"><!-- ie --></span>
        </div>
    </div>
    <div class="table-content">
        <c:choose>
            <c:when test="${not empty list}">
                <c:forEach var="item" items="${list}" varStatus="counter">
                    <div class="row clickable" id="client_user_group_${item.group.id}"
                         onclick="E2G('onGroupClick', null, '${item.group.id}', {type: 'group', key: '${item.group.id}', name: '${cf:escapeJavascriptInline(cf:truncateMiddle(item.group.name,25,null))}'});"
                         onmouseover="mOverAddHoverClass(this);"
                         onmouseout="mOutRemoveHoverClass(this);"
                         style="z-index:${fn:length(list) - counter.index}">
                        <div class="column w35">
                            <div class="group-icon" title="<c:out value="${item.group.name}" escapeXml="true" />">
                                 <c:out value="${cf:truncateMiddle(item.group.name,22,null)}" escapeXml="true" />
                            </div>
                        </div>
                        <div class="column w15">${fn:length(item.group.groupMembers)}</div>
                        <div class="column w45">
                            <span title="${item.group.description}">
                            <c:choose>
                                <c:when test="${fn:length(item.group.description) > 45}">
                                    <c:out value="${ fn:substring(item.group.description,0,35)}[..]" escapeXml="true" />
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${item.group.description}" escapeXml="true" />
                                </c:otherwise>
                            </c:choose>
                            </span>
                        </div>
                        <span class="clear"><!-- ie --></span>
                        <c:choose>
                            <c:when test="${not item.isExternal}">
                            <div class="row-tools"
                                 onmouseover="mOverShowToolbox(this, '.toolbox');"
                                 onmouseout="mOutHideToolbox(this, '.toolbox');">
                                <div class="toolbox">
                                    <a class="delete" href="javascript:;" onclick="E('deleteGroupRequested', {event: event, groupId: '${item.group.id}'}); return false;" title="<fmt:message key="client.user.group.management.action.delete" />" >
                                        <span class="delete"><fmt:message key="button.delete" /></span>
                                    </a>
                                </div> 
                            </div>
                            </c:when>
                            <c:otherwise>
                                <div class="row-tools inactive">
                                    &nbsp;
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise><fmt:message key="client.user.group.management.group.content.void"/></c:otherwise>
        </c:choose>
    </div>
</div>
<jsp:include page="/WEB-INF/jsp/common/paging.jsp" />
<span class="clear"><!-- ie --></span>
