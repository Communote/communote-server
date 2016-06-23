<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp"%>

<div class="panel">
    <h4><fmt:message key="administration.title.menu.user-management" /> &gt; <span><fmt:message key="client.customization.localization.key.imprint" /></span></h4>
</div>
<div class="wrapper last">
    <div class="layer">
        <div class="fieldset-description"><fmt:message key="client.customization.localization.imprint.description" /></div>
        <div class="spacer">&nbsp;</div>
        <div class="TSWidget LocalizeMessageWidget" id="LocalizeMessage" data-cnt-widget-settings="{&quot;messageKey&quot;:&quot;custom.message.imprint&quot;,&quot;showIsHtml&quot;:true}">
        </div>
        <span class="clear"><!-- --></span>
    </div>
</div>
<span class="clear"><!-- --></span>