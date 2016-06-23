<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="java.util.List"%>
<%@page import="com.communote.server.core.common.session.SessionHandler"%>
<%@page import="com.communote.server.api.ServiceLocator"%>
<%@page import="com.communote.server.model.user.Country"%>
<%@page import="com.communote.server.core.user.MasterDataManagement"%>

<%
	List<Country> countryList  = ServiceLocator.findService(MasterDataManagement.class).getCountries(SessionHandler.instance().getCurrentLocale(request)); 
request.setAttribute("countryList", countryList);
%>

<h2>User Profile</h2>

<%@ include file="/WEB-INF/jsp/common/success_messages.jspf" %>
<form:form>
<label for="userProfile.firstname">
<fmt:message key="user.profile.firstname" />:</label><form:input path="userProfile.firstname" cssClass="text" /><br/>
	
	<label for="userProfile.lastname"><fmt:message key="user.profile.lastname" />:</label><form:input path="userProfile.lastname" cssClass="text" /><br/>
	
	<label for="userProfile.salutation"><fmt:message key="user.profile.salutation" />:</label><form:input path="userProfile.salutation" cssClass="text" /><br/>
	
	<label for="countryCode"><fmt:message key="user.profile.country" />:</label>
        <form:select path="countryCode">
            <form:option value="-" ><fmt:message key="user.profile.country.empty" /></form:option>
            <form:options items="${countryList}" itemValue="countryCode" itemLabel="name"/>
        </form:select><br/>
	<input type="submit" value="<fmt:message key="user.profile.submit"/>" class="button" />
</form:form>


