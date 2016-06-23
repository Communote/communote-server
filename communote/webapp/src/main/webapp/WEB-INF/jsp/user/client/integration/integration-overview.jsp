<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp"%>

<script type="text/javascript">

	function getAllSystemIds() {
	    return [	        
	        <% String prefix = ""; %>
		    <c:forEach items="${command.numberOfAdminsForExternalSystems}" var="entry">
		        <%= prefix %>
		        '${entry.key}'
		    	<% prefix = ","; %>
		    </c:forEach>
		];
	}

    function integrationOverview_typeSelected(type) {
        var i, type = $(type);
        var allSystemIds = getAllSystemIds();
        if (type.getProperty('checked')) {
            $('allowDBAuth').removeProperty("disabled");
            for (i = 0; i < allSystemIds.length; i++) {
                if (type.id != allSystemIds[i]) {
                    $(allSystemIds[i]).setProperty('checked', false);
                }
            }
        } else {
            $('allowDBAuth').setProperty("disabled", "disabled");
        }
    }
</script>
<div class="panel">
    <h4>
        <fmt:message key="client.authentication" />
        &gt; <span><fmt:message key="client.integration.overview" /> </span>
    </h4>
</div>
<div class="admin-integration-overview">
    <div class="wrapper last">
        <div class="layer">
            <h5><fmt:message key="client.integration.title" /></h5>
            <fmt:message key="client.integration.description" />
            <form:form>
                <table>
                    <tr>
                        <th><fmt:message key="client.integration.table.header.primary" />
                        </th>
                        <th><fmt:message key="client.integration.table.header.name" />
                        </th>
                        <th><fmt:message key="client.integration.table.header.administrators" />
                        </th>
                        <th><fmt:message key="client.integration.table.header.configuration" />
                        </th>

                     <c:forEach items="${command.numberOfAdminsForExternalSystems}" var="entry">
                      <tr>
                        <td><form:checkbox path="selectedAuthenticationType" value="${entry.key}" id="${entry.key}"
                                onclick="integrationOverview_typeSelected(this)" cssClass="checkbox-${entry.key}" />
                        </td>
                        <td><label for="${entry.key}">${command.configurationNamesForExternalSystems[entry.key]}</label>
                        </td>
                        <td>${entry.value}</td>
                        <td><a href="<ct:url value="${command.configurationUrlsForExternalSystems[entry.key]}" />">
                        	<fmt:message key="client.integration.table.action.edit" /> </a>
                        </td>
					</tr>
                    </c:forEach>
                </table>
                <div id="allowDBAuthContainer">
                    <c:if
                        test="${command.selectedAuthenticationType == null || command.selectedAuthenticationType == 'default' }">
                        <c:set var="disableTheNextField" value="true" />
                    </c:if>
                    <form:checkbox path="allowAuthOverDbOnExternal" id="allowDBAuth" disabled="${disableTheNextField}" />
                    <label for="allowDBAuth" id="allowDBAuthContainerLabel"><fmt:message
                            key="client.integration.allow.db.title" />
                            <ct:tip key="client.integration.allow.db.description" /></label>
                </div>
                <br />
                <h5><fmt:message key="client.integration.user.repository.mode"/></h5>
                <div class="check">
                        <c:choose>
                            <c:when test="${command.userServiceRepositoryMode == 'FLEXIBLE' }">
                                <c:set var="modeFlexible" value="true" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="modeStrict" value="true" />
                            </c:otherwise>
                        </c:choose>
                        <input type="radio" value="STRICT" name="userServiceRepositoryMode" id="modeStrict" <c:if test="${modeStrict}">checked</c:if>/>
                        <label class="label" for="modeStrict">
                            <fmt:message key="client.integration.user.repository.mode.strict"/> 
                            <ct:tip key="client.integration.user.repository.mode.strict.tip" />
                        </label>
                    </div>
                    <div class="check">
                        <input type="radio" value="FLEXIBLE" name="userServiceRepositoryMode" id="modeFlexible" <c:if test="${modeFlexible}">checked</c:if>/>
                        <label class="label" for="modeFlexible"> 
                            <fmt:message key="client.integration.user.repository.mode.flexible"/>
                            <ct:tip key="client.integration.user.repository.mode.flexible.tip" />
                        </label>
                    </div>
                <div class="actionbar actionbar-general">
                    <div class="button-gray main">
                        <input type="submit" class="button" name="send"
                            value="<fmt:message key="client.authentication.update.submit" />" />
                    </div>
                    <span class="clear"> <!-- --> </span>
                </div>

            </form:form>
        </div>
    </div>
</div>