<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<div id="TabPanel" class="mootabs mootabs-admin-area">
<ul class="mootabs_title">
    <li id="Tab1" class="first"><span><fmt:message key="client.system.communication.xmpp.client"/></span></li>
    <li id="Tab2"><span><fmt:message key="client.system.communication.xmpp.advanced"/></span></li>
    <li id="Tab3"><span><fmt:message key="client.system.communication.xmpp.openfire"/></span></li>
</ul>
<div class="mootabs_panel form-background" id="Tab1Panel">
    <%@ include file="xmpp.settings-main.jsp" %>
</div>
<div class="mootabs_panel form-background" id="Tab2Panel">
    <div class="TSWidget XmppAdvancedSettingsWidget" id="XmppAdvancedSettings">&nbsp;<!-- 
    -->
    </div>
</div>
<div class="mootabs_panel form-background" id="Tab3Panel">
    <%@ include file="xmpp.openfire.jsp" %>
</div>
</div>