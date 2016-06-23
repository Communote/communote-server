<%@include file="/WEB-INF/jsp/common/include.jsp" %>

<div class="widgetParameterDescriptions">
<c:choose>
	<c:when test="${not empty widget.parameterDescriptions}">
		<fmt:message key="widget.parameter.descriptions.show.resources.intro"/><br/>
	<c:if test="${not empty widget.parameterDescriptions.userId}">
		<fmt:message key="widget.parameter.descriptions.person"/>&nbsp;${widget.parameterDescriptions.userId}<br/>
	</c:if>
	<c:if test="${not empty widget.parameterDescriptions.groupId}">
		<fmt:message key="widget.parameter.descriptions.team"/>&nbsp;${widget.parameterDescriptions.groupId}<br/>
	</c:if>
	<c:if test="${not empty widget.parameterDescriptions.filter}">
		<fmt:message key="widget.parameter.descriptions.tags"/>&nbsp;${widget.parameterDescriptions.filter}<br/>
	</c:if>
</c:when>
<c:otherwise>
<fmt:message key="widget.parameter.descriptions.show.resources.all"/>
</c:otherwise>
</c:choose>
</div>