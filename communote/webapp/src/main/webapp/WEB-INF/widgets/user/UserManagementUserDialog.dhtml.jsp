<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<c:if test="${not empty singleResult}">
    <span style="display:none" class="control-widget-response-metadata" data-widget-metadata="<c:out value="${widget.responseMetadata}" escapeXml="true" />"></span>
    <div id="userprofile">
        <div class="TSWidget UserManagementUserOverviewWidget" id="UserManagementUserOverview">
            &nbsp;
            <!-- 
                 userId=<%=request.getAttribute("userId")%>
                 filterWidgetGroupId=userManagementGroup
            -->
        </div>
        <span class="clear">
            <!-- ie -->
        </span>
        <c:choose>
            <c:when test="${not isSystemUser}">
                <div class="TSWidget UserManagementUserProfileWidget" id="UserManagementUserProfile">
                    &nbsp;
                    <!-- 
                    userId=<%=request.getAttribute("userId")%>
                    filterWidgetGroupId=userManagementGroup
             -->
                </div>
            </c:when>
            <c:otherwise>
                <div class="TSWidget UserManagementSystemUserProfileWidget" id="UserManagementSystemUserProfile">
                    &nbsp;
                    <!-- 
                    userId=<%=request.getAttribute("userId")%>
                    filterWidgetGroupId=userManagementGroup
             -->
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="spacer">
        <!-- ie -->
    </div>
</c:if>