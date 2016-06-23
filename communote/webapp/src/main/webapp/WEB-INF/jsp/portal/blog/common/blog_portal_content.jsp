<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<%-- import column variables --%>
<tiles:importAttribute name="col1" />
<tiles:importAttribute name="col2" />

<div id="content">
	<tiles:insert attribute="col1" />
</div>

<div id="filter">
	<tiles:insert attribute="col2" />
	<%--fhi div id="ie_clearing">&#160;</div --%>
</div>
<span class="clear"><!-- --></span>