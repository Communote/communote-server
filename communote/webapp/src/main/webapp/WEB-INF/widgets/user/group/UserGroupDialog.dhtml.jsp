<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>
<%@page import="com.communote.server.web.fe.widgets.management.user.group.UserGroupDialogWidget"%>
<div id="group-details">
    <c:if test="${singleResult['groupId'] != null}">
        <div class="TSWidget UserGroupEditWidget" id="UserGroupEdit">&nbsp;
        <!-- 
            groupId=${singleResult['groupId']}
         -->
        </div>        
        <c:if test="${singleResult.isNotExternalGroup}">
            <div class="TSWidget UserGroupAddMemberWidget" id="UserGroupAddMember">&nbsp;
            <!--  
                groupId=${singleResult['groupId']}
            -->
            </div>
        </c:if>
        <div class="TSWidget UserGroupMembersListWidget" id="UserGroupMembersList">&nbsp;
        <!-- 
            groupId=${singleResult['groupId']}
        -->
        </div>
    </c:if>
</div>
<div class="spacer"><!-- ie --></div>