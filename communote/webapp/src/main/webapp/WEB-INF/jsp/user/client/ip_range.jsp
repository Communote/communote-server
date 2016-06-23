<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<%@ include
    file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp"%>

<div class="panel">
    <h4><fmt:message key="client.security" /> &gt; <span><fmt:message key="client.iprange.link.text"/></span></h4>
</div>
<div id="IpRangeTabs" class="mootabs mootabs-admin-area">
    <div class="mootabs-bar">
        <ul class="mootabs_title">
            <li id="Tab1" title="<fmt:message key="client.overview" />" class="first"><span><fmt:message key="client.overview" /></span></li>
            <li id="Tab2" title="<fmt:message key="client.iprange.link.text.create" />"><span><fmt:message key="client.iprange.link.text.create" /></span></li>
            <li id="Tab3" title="" class="mootabs_closeable" style="display:none">
                <span>&nbsp;</span>
            </li>
        </ul>
        <span class="clear"><!-- --></span>
    </div>
    <div id="Tab1Panel" class="mootabs_panel"> 
        <fieldset class="IpRangeListTable">
            <fmt:message key="client.iprange.description" />
            <br /><br />
            <div class="TSWidget IpRangeListWidget" id="IpRangeList">
                <!-- 
                    empty=widget
                    updateTabId=Tab3
                    tabGroupId=IpRangeTabs
                -->
            </div>
        </fieldset>
    </div>
    <div id="Tab2Panel" class="mootabs_panel form-background">
        <div class="TSWidget IpRangeWidget" id="IpRangeCreate">
            <!-- 
                filterId=none
                alwaysOpen=true
                creationMode=true
            -->
        </div>
    </div>
     <div id="Tab3Panel" class="mootabs_panel form-background">
         <div class="TSWidget IpRangeWidget" id="IpRange">
            <!-- 
                filterId=none
                alwaysOpen=true
                updateTabId=Tab3
                tabGroupId=IpRangeTabs
            -->
        </div>
    </div>
</div>
