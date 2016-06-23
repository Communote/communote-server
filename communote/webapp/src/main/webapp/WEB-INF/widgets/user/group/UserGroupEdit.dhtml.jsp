<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<c:choose>
    <c:when test="${empty externalSystemId}">
        <form:form>
            <input type="hidden" name="groupId" value="${param.groupId}"/>
            <%-- add a hidden field for alias because browser does not submit disabled fields --%>
            <form:hidden path="alias"/>
            <input type="hidden" name="displayName" id="groupEditDisplayName" value="<c:out value="${cf:truncateMiddle(command.name,25,null)}" escapeXml="true" />" />
            <%--include placeholder for user notification --%>
            <div id="view-group" class="form-container">
                <fieldset>
                    <h5><fmt:message key="client.user.group.management.group.details" /></h5>
                    <div class="w50">
                        <div class="label"><label for="name"><fmt:message key="client.user.group.management.group.details.name" /><span class="required">*</span></label></div>
                        <div class="input"><form:input path="name" cssClass="text" htmlEscape="true"/></div>
                        <span class="error"><label for="name" class="error"><form:errors cssClass="error" path="name" /></label></span>
                    </div>
                    <div class="w50">
                        <div class="label"><label for="disabledAlias"><fmt:message key="client.user.group.management.group.details.alias" /></label></div>
                        <div class="input"><input name="disabledAlias" id="disabledAlias" disabled="disabled" class="text" value="${command.alias}"/></div>
                    </div>
                    <div class="w100 long">
                        <div class="label"><label for="description"><fmt:message key="client.user.group.management.group.details.description" /></label></div>
                        <div class="input"><form:textarea id="description" rows="5" cols="70" path="description"  htmlEscape="true"/></div>
                        <span class="error"><label for="description" class="error"><form:errors cssClass="error" path="description" /></label></span>
                    </div>
                    <span class="clear"><!--  ie  --></span>
                    <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
                </fieldset>
                <div class="actionbar actionbar-general">
                    <div class="button-gray main">
                        <input type="submit" value="<fmt:message key="button.save" />" name="submit"/>
                    </div>
                    <div class="button-gray">
                        <input type="button" name="deleteBtn" onclick="E('deleteGroupRequested', {groupId: '${param.groupId}'}); return false;" value="<fmt:message key="button.delete" />" />
                    </div>
                    <span class="clear"><!--  ie  --></span>
                </div>
            </div>
        </form:form>
        </c:when>
        <c:otherwise>
            <div id="view-group" class="form-container">
                <fieldset>
                    <h5><fmt:message key="client.user.group.management.group.details" /></h5>
                    <dl>
                        <dt><label for="id1"><fmt:message key="client.user.group.management.group.details.name" />:</label></dt>
                        <dd><c:out value="${command.name}" escapeXml="true" />&nbsp;</dd>
                        <dt><label for="id3"><fmt:message key="client.user.group.management.group.details.alias" />:</label></dt>
                        <dd>${command.alias}&nbsp;</dd>
                        <dt><label for="id2"><fmt:message key="client.user.group.management.group.details.source" />:</label></dt>
                        <fmt:message key="blog.member.management.sublist.label.external" var="externalSysIdName">
                            <fmt:param value="${externalSystemId}" />
                        </fmt:message>
	                    <dd>
                            <span class="displayName"><c:out value="${externalSysIdName}" escapeXml="true"/></span>
                        </dd>
                        <dt><label for="id5"><fmt:message key="client.user.group.management.group.details.external.id" />:</label></dt>
                        <dd>${externalId}&nbsp;</dd>
                        <dt><label for="id4"><fmt:message key="client.user.group.management.group.details.description" />:</label></dt>
                        <dd><c:out value="${command.description}" escapeXml="true" />&nbsp;</dd>
                    </dl>
                </fieldset>
            </div>
        </c:otherwise>
</c:choose>