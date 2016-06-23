<%@page import="com.communote.server.persistence.user.client.ClientHelper"%>
<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp"%>
<% pageContext.setAttribute("isGlobal", ClientHelper.isCurrentClientGlobal()); %>

<script type="text/javascript">addClientManagementFilterGroups=true;</script>
<div id="admin-group-management">
    <div class="panel">
        <h4><fmt:message key="administration.title.menu.user-management" /> &gt; <span><fmt:message key="client.user.group.management" /></span></h4>
    </div>
    <!-- div class="wrapper last" -->
        <div id="UserGroupManagementTabPanel" class="mootabs mootabs-admin-area">
            <div class="mootabs-bar">
                <ul class="mootabs_title">
                    <li id="Tab1" title="<fmt:message key="client.overview" />" class="first"><span><fmt:message key="client.overview" /></span></li>
                    <li id="Tab2" 
                       <c:if test="${anErrorOccured}">class="active"</c:if>
                    title="<fmt:message key="client.user.group.create" />"><span><fmt:message key="client.user.group.create" /></span></li>
                    <li id="Tab3" title="" class="mootabs_closeable" style="display:none;">
                        <span>&nbsp;</span>
                    </li>
                </ul>
                <span class="clear"><!-- ie --></span>
            </div>
            <div id="Tab1Panel" class="mootabs_panel">
                <div id="UserGroupSearchBox" class="box">
                    <div class="box-head-discreet">
                        <form onsubmit="if (document.getElementById('SearchBoxInput').value != '<fmt:message key="widget.user.management.searchbox.usersearch.default" />') {widgetController.getWidget('UserGroupList').setGroupNameFilter(document.getElementById('SearchBoxInput').value); }; return false;">
                            <div class="cn-searchbox">
                                <div class="cn-border">
                                    <input type="text"
                                                id="SearchBoxInput"
                                                name="SearchBoxInput" 
                                                class="text searchbox-input"
                                                size="20" 
                                                maxlength="200" 
                                                autocomplete="off" 
                                                placeholder="<fmt:message key="widget.user.management.searchbox.usersearch.default" />" />
                                    <a href="javascript:;"
                                                class="cn-filter-search-clear" 
                                                onclick="document.getElementById('SearchBoxInput').value='';"></a>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="spacer">&nbsp;</div>
                <div class="TSWidget UserGroupListWidget" id="UserGroupList">
                    <!--
                        maxCount=10
                        filterWidgetGroupId=userManagementGroup
                    -->
                </div>
            </div>
            <div id="Tab2Panel" class="mootabs_panel form-background">
                <div class="TSWidget UserGroupCreateWidget" id="UserGroupCreateWidget">
                    &nbsp;
                    <!--
                    -->
                </div>
            </div>
            <div id="Tab3Panel" class="mootabs_panel form-background">
                <div class="TSWidget UserGroupDialogWidget" id="UserGroupDialog">
                    <!-- 
                        groupId=none
                        tabId=Tab3
                        tabGroupId=UserGroupManagementTabPanel
                        filterWidgetGroupId=userManagementGroup
                    -->
                </div>
            </div>
        </div>
    <!-- /div -->
</div>
