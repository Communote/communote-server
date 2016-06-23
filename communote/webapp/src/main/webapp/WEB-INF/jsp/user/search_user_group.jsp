<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<h2><fmt:message key="user.group.search.heading" /></h2>

<form name="searchUserGroupForm">
	<label for="searchString"><fmt:message key="user.group.search.description" /></label>
	<input id="searchString" name="searchString" class="text" value="" type="text"><br />
	<button type="button" name="action" class="button" onclick="E('onTeamSearchClick',document.forms['searchUserGroupForm'].searchString.value);return false;"><span>Find</span></button>
</form>

<br style="clear:both;" />
