<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<%--include placeholder for user notification --%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp"%>

<div class="admin-profile">
    <form:form commandName="sslConfigurationCommand">
        <div class="panel">
            <h4>
                <fmt:message key="client.security" />
                &gt; <span><fmt:message key="client.security.ssl.link.text" /> </span>
            </h4>
        </div>
        <div class="wrapper last">
            <div class="layer">
                <fieldset>
                    <h5 class="subheadline">
                        <fmt:message key="client.security.ssl.title" />
                    </h5>
                    <div class="fieldset-description">
                        <fmt:message key="client.security.ssl.introduction" />
                    </div>
                    <cform:notification type="warning">
                        <fmt:message key="client.security.ssl.warning.tomcat" />
                    </cform:notification>
                    <div class="spacer">&nbsp;</div>
                    <h5 class="subheadline">
                        <fmt:message key="client.security.ssl.head" />
                    </h5>
                    <div class="description">
                        <fmt:message key="client.security.ssl.head.description" />:
                    </div>
                    <div class="check">
                        <form:checkbox path="web" id="web" />
                        <label for="web"><fmt:message key="client.security.ssl.channel.web" /> </label> <span
                            class="clear"> <!-- ie --> </span>
                    </div>
                    <div class="check">
                        <form:checkbox path="api" id="api" />
                        <label for="api"><fmt:message key="client.security.ssl.channel.api" /> </label> <span
                            class="clear"> <!-- ie --> </span>
                    </div>
                    <div class="check">
                        <form:checkbox path="rss" id="rss" />
                        <label for="rss"><fmt:message key="client.security.ssl.channel.rss" /> </label> <span
                            class="clear"> <!-- ie --> </span>
                    </div>
                </fieldset>
                <div class="actionbar actionbar-general">
                    <div class="button-gray main">
                        <input type="submit" name="button" value="<fmt:message key="client.security.ssl.submit" />" />
                    </div>
                    <span class="clear"> <!-- ie --> </span>
                </div>
            </div>
        </div>
    </form:form>
</div>