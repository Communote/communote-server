<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<%--/* Add flag for adding client management filter groups */--%>
<script type="text/javascript">
    addClientManagementFilterGroups = true;
</script>
<div id="usermanagement" class="admin-usermanagement">
    <div class="panel">
        <h4><fmt:message key="administration.title.menu.user-management"/> &gt; <span><fmt:message
                key="administration.title.menu.user-management"/></span></h4>
    </div>
    <%
        if ("true".equals(request.getParameter("inviteUser"))) {
            request.setAttribute("showInviteUser", true);
        }
    %>
    <!-- div class="wrapper last" -->
    <div id="UserManagementTabPanel" class="mootabs mootabs-admin-area">
        <div class="mootabs-bar">
            <ul class="mootabs_title">
                <li id="Tab1" title="<fmt:message key="client.overview" />" class="first"><span><fmt:message
                        key="client.overview"/></span></li>
                <li id="Tab2"
                    <c:if test="${anErrorOccured || showInviteUser}">class="active"</c:if>
                    title="<fmt:message key="client.invite.user.link.text" />">
                    <span><fmt:message key="client.invite.user.link.text"/></span>
                </li>
                <li id="Tab3" title="" class="mootabs_closeable" style="display: none;">
                    <span>&nbsp;</span>
                </li>
            </ul>
            <span class="clear"><!-- --></span>
        </div>
        <div id="Tab1Panel" class="mootabs_panel">
            <div class="TSWidget UserManagementSearchBoxWidget" id="UserManagementSearchBox">
                <!--
                    filterWidgetGroupId=userManagementGroup
                 -->
            </div>
            <div class="TSWidget UserManagementListWidget" id="UserManagementList">
                <!--
                    filterWidgetGroupId=userManagementGroup
                    maxCount=10
                -->
            </div>
        </div>
        <div id="Tab2Panel" class="mootabs_panel form-background">
            <form>
                <fieldset class="no-border">
                    <div class="fieldset-description">
                        <fmt:message key="blog.member.invite.description"/>
                    </div>
                </fieldset>
                <c:if test="${fn:length(invitationProviders) gt 1}">
                    <c:forEach var="invitationProvider" items="${invitationProviders}" varStatus="status">
                        <div class="w100">
                            <input type="radio" id="selectionBox${invitationProvider}" name="selectionBox"
                                   onchange="$$('.invitationContainer').addClass('hidden');$('invitationContainer${invitationProvider}').removeClass('hidden');"
                                   <c:if test="${status.first}">checked="checked"</c:if>/>
                            <label for="selectionBox${invitationProvider}"><fmt:message
                                    key="blog.member.invite.provider.${invitationProvider}"/></label>
                            <span class="clear"><!-- --></span>
                        </div>
                    </c:forEach>
                    <span class="clear"><!-- --></span>
                </c:if>
            </form>
            <c:forEach var="invitationProvider" items="${invitationProviders}" varStatus="status">
                <div id="invitationContainer${invitationProvider}"
                     class="invitationContainer<c:if test="${not status.first}"> hidden</c:if>">
                    <div class="TSWidget InviteUserWidget" id="InviteUserWidget${invitationProvider}">&nbsp;<!--
                            invitationProvider=${invitationProvider}
                        --></div>
                </div>
            </c:forEach>
        </div>
        <div id="Tab3Panel" class="mootabs_panel form-background">
            <div class="TSWidget UserManagementUserDialogWidget" id="UserManagementUserDialog">
                <!--
                    filterWidgetGroupId=userManagementGroup
                    groupId=none
                    tabId=Tab3
                    tabGroupId=UserManagementTabPanel
                -->
            </div>
        </div>
    </div>
    <!-- /div -->
</div>
