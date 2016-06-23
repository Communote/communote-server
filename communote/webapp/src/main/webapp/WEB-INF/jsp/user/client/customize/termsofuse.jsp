<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp"%>

<div class="panel">
    <h4><fmt:message key="administration.title.menu.user-management" /> &gt; <span><fmt:message key="client.customization.localization.key.termsofuse" /></span></h4>
</div>
<div class="wrapper last">
    <div class="layer">
        <div class="fieldset-description"><fmt:message key="client.customization.localization.termsofuse.description" /></div>
        <div class="spacer">&nbsp;</div>
        <div class="TSWidget LocalizeMessageWidget" id="LocalizeMessage">
            <!-- 
                messageKey=custom.message.termsofuse
                showIsHtml=true
            -->
        </div>
        <span class="clear"><!-- --></span>
        <h5><fmt:message key="client.customization.localization.termsofuse.settings" /></h5>
        <form method="POST" action="updateUsersPolicy.do">
            <fieldset class="no-border">
                <div class="w100">
                    <div class="input label">
                        <input type="checkbox" class="input" name="usersMustAcceptTermsOfUse" id="usersMustAcceptTermsOfUse" <c:if test="${usersMustAcceptTermsOfUse}">checked="checked"</c:if> />
                        <label for="usersMustAcceptTermsOfUse"><fmt:message key="client.customization.localization.termsofuse.users" /></label>
                    </div>          
                </div>
                <span class="clear"><!-- --></span>
            </fieldset>
            <div class="actionbar actionbar-general">
                <div class="button-gray main">
                    <input type="submit" name="button" value="<fmt:message key="button.save" />" />
                </div>
            </div>
        </form>
        <span class="clear"><!-- --></span>
        <h5><fmt:message key="client.customization.localization.termsofuse.reset" /></h5>
        <form method="POST" action="resetUsers.do">
            <fieldset>
                <div class="w100">
                    <div class="label">
                        <fmt:message key="client.customization.localization.termsofuse.users.reset" />
                    </div>
                </div>
                <span class="clear"><!-- --></span>
            </fieldset>
            <div class="actionbar actionbar-general">
                <div class="button-gray">
                    <input type="submit" name="button" value="<fmt:message key="client.customization.localization.termsofuse.users.reset.button" />" />
                </div>
            </div>
            <span class="clear"><!-- --></span>
        </form>
    </div>
</div>
<span class="clear"><!-- --></span>