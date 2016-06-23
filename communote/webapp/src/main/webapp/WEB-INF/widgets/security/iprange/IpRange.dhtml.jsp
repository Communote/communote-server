<%@ include file="/WEB-INF/jsp/common/include.jsp"%>

<%@page import="com.communote.server.web.fe.portal.user.client.controller.AddUpdateIPRangeController"%>
<%@page import="com.communote.server.web.fe.widgets.management.security.iprange.IpRangeWidget"%>

<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<c:if test="${openFilterId != null && openFilterName != null}">
    <input type="hidden" name="openFilterId" value="${openFilterId }"/>
    <input type="hidden" name="openFilterName" value="${openFilterName}"/>
</c:if>

<div class="form-container">
    <form:form>
        <c:if test="${alwaysOpen}">
            <input type="hidden" name="alwaysOpen" value="${alwaysOpen}"/>
        </c:if>
        <input type="hidden" name="<%=IpRangeWidget.FILTER_ID%>" value="${singleResult.id }" />
        <input type="hidden" name="action" id="${widget.widgetId}_form_action" value="<%=IpRangeWidget.ACTION_UPDATE%>" />
        <fieldset class="bottomspace">
            <h5><fmt:message key="client.iprange.filter.config.headline" /></h5>
            <div class="w50">
                <div class="label"><label for="name"><fmt:message key="client.iprange.name.table.head" /><span class="required">*</span></label></div>
                <div class="input"><input type="text" name="<%=IpRangeWidget.FILTER_NAME%>" class="text" value="${singleResult.name}"/></div>
                <div class="hint"><fmt:message key="client.iprange.name.table.head.description" /></div>
            </div>
            <div class="w50">
                <div class="checkbox">
                    <input type="checkbox" id="enabled-checkbox" name="<%=IpRangeWidget.FILTER_ENABLED%>" <c:if test="${singleResult.enabled}">checked="checked"</c:if>/>
                    <label for="enabled-checkbox"><fmt:message key="client.iprange.enabled.table.head" /></label>
                </div>
                <div class="hint"><fmt:message key="client.iprange.enabled.table.head.description" /></div>
            </div>
            <span class="clear"><!-- --></span>
            <div class="w50">
                <div class="label">
                    <fmt:message key="client.iprange.enabled_for.table.head" />:
                </div>
                <div class="input">
<c:forEach var="channelType" items="${singleResult.channelTypes}" varStatus="varStatus">
                    <div class="w25">
                        <input type="checkbox" id="channelTypes-${varStatus.index}" name="<%=IpRangeWidget.FILTER_CHANNEL_TYPE_PREFIX%>${channelType.channelType}" <c:if test="${channelType.enabled}">checked="checked"</c:if>/>
                        <label for="channelTypes-${varStatus.index}">${channelType.channelType}</label>
                    </div>
</c:forEach>
                    <span class="clear"><!-- --></span>
                </div>
            </div>
            <span class="clear"><!-- --></span>
         </fieldset>
         <fieldset>
            <h5><fmt:message key="client.iprange.definitions.h4" /></h5>
            <div class="fieldset-description">
                <fmt:message key="client.iprange.definitions.description" />
            </div>
            <div class="w50">
                <div class="label"><label for="includes"><fmt:message key="client.iprange.includes.table.head" /></label></div>
                <div class="input"><textarea rows="10" name="<%=IpRangeWidget.FILTER_INCLUDES%>">${singleResult.includes}</textarea></div>
            </div>
            <div class="w50">
                <div class="label"><label for="excludes"><fmt:message key="client.iprange.excludes.table.head" /></label></div>
                <div class="input"><textarea rows="10" name="<%=IpRangeWidget.FILTER_EXCLUDES%>">${singleResult.excludes}</textarea></div>
            </div>
        </fieldset>
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" name="button" value="<fmt:message key="client.iprange.save.button.text" />" />
            </div>
<c:if test="${singleResult.id != null}">
            <div class="button-gray">
                <input type="submit" name="button" value="<fmt:message key="client.iprange.delete.button.text" />" onclick="document.getElementById('${widget.widgetId}_form_action').value='<%=IpRangeWidget.ACTION_DELETE%>'" />
            </div>
</c:if>
            <span class="clear"></span>
        </div>
    </form:form>
 </div>