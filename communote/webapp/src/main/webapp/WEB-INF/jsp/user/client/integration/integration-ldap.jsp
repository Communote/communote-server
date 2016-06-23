<%@include file="/WEB-INF/jsp/common/include.jsp"%>
<%@page import="com.communote.server.web.fe.portal.user.client.forms.LdapConfigurationForm"%>
<%@page import="com.communote.server.web.fe.portal.user.client.controller.ClientLdapAuthenticationController"%>
<%@page import="com.communote.server.core.common.ldap.LdapGroupAttribute"%>
<%@page import="com.communote.server.core.common.ldap.LdapUserAttribute"%>
<%@ include
	file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp"%>
<script type="text/javascript">
(function() {
    function toggleRequiredFlagOfConnectionTestFields() {
        var cb = document.id('cb-allowExternalAuthentication');
        var style = cb.get('checked') ? '' : 'none';
        document.getElement('.ldap-account-login').getElements('.required').setStyle('display',style);
    }
    communote.initializer.addApplicationReadyCallback(function(){
        toggleRequiredFlagOfConnectionTestFields();
        document.id('cb-allowExternalAuthentication').addEvent('change', toggleRequiredFlagOfConnectionTestFields);
    });
})();
</script>
<div class="admin-authentication-ldap">
	<form:form>
		<div class="panel">
			<h4>
				<fmt:message key="client.authentication" />
				&gt; <span><fmt:message
						key="client.authentication.ldap.link.text" /></span>
			</h4>
		</div>
		<input name="submitAction" type="hidden"
			value="<%=ClientLdapAuthenticationController.UPDATE_LDAP_ACTION%>" />
		<div class="wrapper last">
			<div class="layer">
				<fieldset class="ldap-general">
					<h5>
						<fmt:message
							key="client.authentication.ldap.section.header.general" />
					</h5>
					<div class="check">
						<form:radiobutton path="serverDetectionMode"
							value="<%=LdapConfigurationForm.ServerDetectionMode.STATIC%>"
							id="mode_static"
							onclick="if($('mode_static').get('checked')){ $('staticModeSettings').setStyle('display','block');$('dynamicModeSettings').setStyle('display','none')};" />
						<label class="label" for="mode_static"> <fmt:message
								key="client.authentication.ldap.mode.static" />
						</label>
					</div>
					<div class="check">
						<form:radiobutton path="serverDetectionMode"
							value="<%=LdapConfigurationForm.ServerDetectionMode.DYNAMIC%>"
							id="mode_dynamic"
							onclick="if($('mode_dynamic').get('checked')){ $('staticModeSettings').setStyle('display','none');$('dynamicModeSettings').setStyle('display','block')};" />
						<label class="label" for="mode_dynamic"> <fmt:message
								key="client.authentication.ldap.mode.dynamic" />
						</label>
					</div>
					<div class="line" id="staticModeSettings"
						<c:if test="${command.serverDetectionMode != 'STATIC'}">style="display: none;"</c:if>>
						<cform:input key="client.authentication.ldap.url" width="100" isSpringForm="true" name="url" hint="true" required="true" />
						<span class="clear"> <!-- -->
						</span>
					</div>
					<div id="dynamicModeSettings"
						<c:if test="${command.serverDetectionMode != 'DYNAMIC'}">style="display: none;"</c:if>>
						<div class="line">
							<div class="w50">
								<cform:input 
									key="client.authentication.ldap.mode.dynamic.query-prefix"
									width="100" isSpringForm="true" name="queryPrefix"
									hint="true" required="true" />
							</div>
							<div class="w50">
								<cform:input
									key="client.authentication.ldap.mode.dynamic.domain"
									width="100" isSpringForm="true" name="domain" hint="false"
									required="true" />
							</div>
							<span class="clear"> <!-- Empty -->
							</span>
						</div>
					</div>
					<div class="line">
						<cform:input key="client.authentication.ldap.managerdn"
							isSpringForm="true" name="bindUser" hint="true" />
                        <div class="w50">
                            <div class="label"><label for="bindUserPassword"><fmt:message key="client.authentication.ldap.managerpassword" /></label></div>
                            <div class="input password">
                                    <form:hidden path="passwordChanged" />
                                <c:choose>
                                    <c:when test="${command.configurationExists}">
                                    <form:password path="bindUserPassword" cssClass="text" showPassword="false" disabled="true" autocomplete="off" />
                                    <a href="javascript:;" onclick="enableField('bindUserPassword', 'passwordChanged', this); return false;">
                                     <fmt:message key="client.form.change.password" /></a>
                                    </c:when>
                                    <c:otherwise>
                                    <form:password path="bindUserPassword" cssClass="text" showPassword="false" autocomplete="off" />
                                    </c:otherwise>
                                </c:choose>
                            </div> 
                        </div>
						<span class="clear"> <!-- -->
						</span>
					</div>
					<div class="line">
						<div class="w100">
							<div class="label">
								<label><fmt:message
										key="client.authentication.ldap.authmode" /></label>
								<ct:tip key="client.authentication.ldap.authmode.hint" />
							</div>
							<div class="input">
								<form:select id="authenticationMode" path="authenticationMode">
									<c:forEach var="authMode"
										items="${command.supportedAuthenticationModes}">
										<form:option value="${authMode}">
											<fmt:message
												key="client.authentication.ldap.authmode.${authMode}" />
										</form:option>
									</c:forEach>
								</form:select>
							</div>
						</div>
						<span class="clear"> <!-- -->
						</span>
					</div>
				</fieldset>
				<c:if test="${isAdPluginActive}">
					<fieldset class="ldap-advanced-config">
						<h5><fmt:message key="client.authentication.ldap.advanced"/></h5>
						<div class="line">
							<div id="ldap-advanced-allowPaging"
								class="<c:choose><c:when test="${command.allowPaging}">w50</c:when><c:otherwise>w100</c:otherwise></c:choose>">
								<div class="label">&nbsp;</div>
								<div class="checkbox">
	                                <form:checkbox path="allowPaging"
	                                    id="allowPaging" cssClass="checkbox"
	                                    onclick="if($('allowPaging').get('checked')){ $('ldap-advanced-pagingSize').setStyle('display','block');$('ldap-advanced-allowPaging').set('class', 'w50');}else{$('ldap-advanced-pagingSize').setStyle('display','none');$('ldap-advanced-allowPaging').set('class', 'w100');};" />
									<label for="allowPaging"><fmt:message key="client.authentication.ldap.advanced.paging"/></label>
								</div>
							</div>
							<div id="ldap-advanced-pagingSize"
								style="<c:choose><c:when test="${command.allowPaging}">display: block;</c:when><c:otherwise>display: none;</c:otherwise></c:choose>">
								<cform:input key="client.authentication.ldap.advanced.paging.size" id="pagingSize" isSpringForm="true" name="pagingSize" required="true" />
							</div>
						</div>
                    </fieldset>
				</c:if>
				<fieldset class="ldap-user-sync sequently">
					<div class="line">
						<h5>
							<fmt:message
								key="client.authentication.ldap.section.header.user.sync" />
						</h5>
                        <c:if test="${isAdPluginActive}">
                            <div id="ldap-incrementalSync" class="w50">
                                <div class="checkbox">
                                    <form:checkbox path="incrementalUserSync" cssClass="checkbox" id="incrementalUserSync" />
                                        <label for="incrementalUserSync"><fmt:message key="client.authentication.ldap.incremental.sync"/>
                                        <ct:tip key="client.authentication.ldap.incremental.sync.hint" /></label>
                                </div>
                            </div>
                            <span class="clear"><!-- Empty --></span>
                        </c:if>
						<cform:input key="client.authentication.ldap.user.searchfilter"	width="100" isSpringForm="true" name="userSearchfilter" hint="true" />
						<span class="clear"> <!-- -->
						</span>
					</div>
					<c:forEach var="userSearchBase" items="${command.userSearchBases}"
						varStatus="counter">
						<div id="user-searchbase-${counter.index}" class="user-searchbase">
							<div class="line">
								<cform:input key="client.authentication.ldap.searchbase"
									isSpringForm="true"
									name="userSearchBases[${counter.index}].searchBase"
									hint="${counter.first}" id="userSearchBase-${counter.index}"
									required="${counter.first}" />
								<div class="w50">
									<div class="label">
										<label for="userSearchSubtree-${counter.index}"><fmt:message
												key="client.authentication.ldap.searchsubtree" /> <c:if
												test="${counter.first}">
												<span class="required">*</span>
												<ct:tip key="client.authentication.ldap.searchsubtree.hint" />
											</c:if></label>
									</div>
									<div
										class="input<c:if test="${not counter.first}"> small float-left</c:if>">
										<form:select id="userSearchSubtree-${counter.index}"
											path="userSearchBases[${counter.index}].searchSubtree">
											<form:option value="true">
												<fmt:message
													key="client.authentication.ldap.searchsubtree.true" />
											</form:option>
											<form:option value="false">
												<fmt:message
													key="client.authentication.ldap.searchsubtree.false" />
											</form:option>
										</form:select>
									</div>
									<c:if test="${not counter.first}">
										<div class="field-actions float-left">
											<img
												src="<ct:url staticResource="true" value="/themes/core/images/misc/icon20_delete-hover.gif" />"
												class="clickable"
												onclick="removeContainer('user-searchbase-${counter.index}');"
												alt="<fmt:message key="button.delete" />"
												title="<fmt:message key="client.authentication.ldap.action.remove.searchbase" />" />
										</div>
										<span class="clear"> <!-- -->
										</span>
									</c:if>
									<form:errors cssClass="error"
										path="userSearchBases[${counter.index}].searchSubtree"
										element="span" />
								</div>
								<span class="clear"> <!-- -->
								</span>
							</div>
						</div>
					</c:forEach>
					<div id="user-searchbase-INDEX"
						class="dummy-user-searchbase hidden">
						<div class="line">
							<cform:input key="client.authentication.ldap.searchbase"
								name="DUMMY-userSearchBases[INDEX].searchBase"
								id="userSearchBase-INDEX" />
							<div class="w50">
								<div class="label">
									<label for="userSearchSubtree-INDEX"><fmt:message
											key="client.authentication.ldap.searchsubtree" /></label>
								</div>
								<div class="input small float-left">
									<select id="userSearchSubtree-INDEX"
										name="DUMMY-userSearchBases[INDEX].searchSubtree">
										<option value="true">
											<fmt:message
												key="client.authentication.ldap.searchsubtree.true" />
										</option>
										<option value="false" selected="selected">
											<fmt:message
												key="client.authentication.ldap.searchsubtree.false" />
										</option>
									</select>
								</div>
								<div class="field-actions float-left">
									<img
										src="<ct:url staticResource="true" value="/themes/core/images/misc/icon20_delete-hover.gif" />"
										class="clickable"
										onclick="removeContainer('user-searchbase-INDEX');"
										alt="<fmt:message key="button.delete" />"
										title="<fmt:message key="client.authentication.ldap.action.remove.searchbase" />" />
								</div>
								<span class="clear"> <!-- -->
								</span>
							</div>
							<span class="clear"> <!-- -->
							</span>
						</div>
					</div>
					<div class="actionbar actionbar-general">
						<div class="button-gray">
							<a class="button" href="javascript:;"
								onclick="addSearchBaseFields('user');"><fmt:message
									key="client.authentication.ldap.action.add.searchbase" /></a>
						</div>
						<span class="clear"> <!-- -->
						</span>
					</div>
					<h6 class="subheadline">
						<fmt:message
							key="client.authentication.ldap.user.properties.header" />
					</h6>
					<div class="fieldset-description">
						<fmt:message key="client.authentication.ldap.user.properties.help" />
					</div>
					<div class="line">
						<cform:input
							key="client.authentication.ldap.user.properties.identifier"
							isSpringForm="true" name="userIdentifier" hint="true"
							required="true" />
						<div class="w50">
							<div class="label">&nbsp;</div>
							<div class="checkbox">
								<form:checkbox path="userIdentifierIsBinary" cssClass="checkbox"
									id="userIdentifierIsBinary" />
								<label for="userIdentifierIsBinary"><fmt:message
										key="client.authentication.ldap.user.properties.identifier.isbinary" /></label>
							</div>
						</div>
						<span class="clear"> <!-- -->
						</span>
					</div>
					<cform:input key="client.authentication.ldap.user.properties.login"
						isSpringForm="true" name="userAlias" hint="true"
						required="<%=LdapUserAttribute.ALIAS.isRequired()%>" />
					<cform:input key="client.authentication.ldap.user.properties.mail"
						isSpringForm="true" name="userEmail" hint="true"
						required="<%=LdapUserAttribute.EMAIL.isRequired()%>" />
					<span class="clear"> <!-- -->
					</span>
					<cform:input
						key="client.authentication.ldap.user.properties.firstname"
						isSpringForm="true" name="userFirstName" hint="true"
						required="<%=LdapUserAttribute.FIRSTNAME.isRequired()%>" />
					<cform:input
						key="client.authentication.ldap.user.properties.lastname"
						isSpringForm="true" name="userLastName" hint="true"
						required="<%=LdapUserAttribute.LASTNAME.isRequired()%>" />
					<span class="clear"> <!-- -->
					</span>
					<h5 class="subheadline sequently topspace">
						<fmt:message
							key="client.authentication.allowExternalAuthentication.header" />
					</h5>
					<div class="fieldset-description">
						<fmt:message key="client.authentication.ldap.auth.external.help" />
					</div>
					<div class="line">
						<div class="w100">
							<div class="checkbox">
								<form:checkbox path="config.allowExternalAuthentication"
									id="cb-allowExternalAuthentication"/>
								<label for="cb-allowExternalAuthentication"><fmt:message
										key="client.authentication.allowExternalAuthentication" /></label>
							</div>
							<form:errors cssClass="error"
								path="config.allowExternalAuthentication" element="span" />
						</div>
					</div>
				</fieldset>
				<fieldset class="ldap-group-sync sequently">
					<div class="line">
						<h5>
							<fmt:message
								key="client.authentication.ldap.section.header.group.sync" />
						</h5>
						<div id="ldap-activateGroupSync"
							class="<c:choose><c:when test="${command.synchronizeUserGroups || not isAdPluginActive}">w50</c:when><c:otherwise>w100</c:otherwise></c:choose>">
							<div class="checkbox">
								<form:checkbox path="synchronizeUserGroups"
									id="activateGroupSync"
									onclick="toggleVisibility('ldap-group-settings', this.checked);if($('activateGroupSync').get('checked')){ $('ldap-incrementalGroupSync').setStyle('display','block');$('ldap-activateGroupSync').set('class', 'w50');}else{$('ldap-incrementalGroupSync').setStyle('display','none');$('ldap-activateGroupSync').set('class', 'w100');};" />
								<label for="activateGroupSync"><fmt:message
										key="client.authentication.ldap.group.activate" /></label>
							</div>
							<form:errors cssClass="error" path="synchronizeUserGroups"
								element="span" />
						</div>
						<c:if test="${isAdPluginActive}">
							<div id="ldap-incrementalGroupSync" class="w50"
									style="<c:choose><c:when test="${command.synchronizeUserGroups}">display: block;</c:when><c:otherwise>display: none;</c:otherwise></c:choose>">
								<div class="checkbox">
								    <form:checkbox path="incrementalGroupSync" cssClass="checkbox" id="incrementalGroupSync" />
										<label for="incrementalGroupSync"><fmt:message key="client.authentication.ldap.incremental.sync"/>
										<ct:tip key="client.authentication.ldap.incremental.sync.hint" /></label>
								</div>
							</div>
						</c:if>
						<span class="clear"> <!-- --></span>
					</div>
					<c:if test="${not command.synchronizeUserGroups}">
						<c:set var="showGroupSettings" value="hidden" />
					</c:if>
					<div id="ldap-group-settings" class="${showGroupSettings}">
						<cform:input key="client.authentication.ldap.group.searchfilter"
							width="100" isSpringForm="true" name="groupSearchfilter"
							hint="true" />
						<span class="clear"> <!-- -->
						</span>
						<div class="line">
							<div class="w50">
								<div class="label">
									<label for="memberMode"><fmt:message
											key="client.authentication.ldap.group.mode" /> <ct:tip
											key="client.authentication.ldap.group.mode.hint" /></label>
								</div>
								<div class="input">
									<form:select id="memberMode" path="memberMode">
										<form:option value="true">
											<fmt:message
												key="client.authentication.ldap.group.mode.member" />
										</form:option>
										<form:option value="false">
											<fmt:message
												key="client.authentication.ldap.group.mode.memberof" />
										</form:option>
									</form:select>
								</div>
								<form:errors cssClass="error" path="memberMode" element="span" />
							</div>
							<cform:input key="client.authentication.ldap.group.mode.property"
								isSpringForm="true" name="groupMembership" hint="true"
								required="<%=LdapGroupAttribute.MEMBERSHIP.isRequired()%>" />
							<span class="clear"> <!-- -->
							</span>
						</div>
						<c:forEach var="groupSearchBase"
							items="${command.groupSearchBases}" varStatus="counter">
							<div id="group-searchbase-${counter.index}"
								class="group-searchbase">
								<div class="line">
									<cform:input key="client.authentication.ldap.searchbase"
										isSpringForm="true"
										name="groupSearchBases[${counter.index}].searchBase"
										hint="${counter.first}" id="groupSearchBase-${counter.index}"
										required="${counter.first}" />
									<div class="w50">
										<div class="label">
											<label for="groupSearchSubtree-${counter.index}"><fmt:message
													key="client.authentication.ldap.searchsubtree" /> <c:if
													test="${counter.first}">
													<span class="required">*</span>
													<ct:tip key="client.authentication.ldap.searchsubtree.hint" />
												</c:if></label>
										</div>
										<div
											class="input<c:if test="${not counter.first}"> small float-left</c:if>">
											<form:select id="groupSearchSubtree-${counter.index}"
												path="groupSearchBases[${counter.index}].searchSubtree">
												<form:option value="true">
													<fmt:message
														key="client.authentication.ldap.searchsubtree.true" />
												</form:option>
												<form:option value="false">
													<fmt:message
														key="client.authentication.ldap.searchsubtree.false" />
												</form:option>
											</form:select>
										</div>
										<c:if test="${not counter.first}">
											<div class="field-actions float-left">
												<img
													src="<ct:url staticResource="true" value="/themes/core/images/misc/icon20_delete-hover.gif" />"
													class="clickable"
													onclick="removeContainer('group-searchbase-${counter.index}');"
													alt="<fmt:message key="button.delete" />"
													title="<fmt:message key="client.authentication.ldap.action.remove.searchbase" />" />
											</div>
											<span class="clear"> <!-- -->
											</span>
										</c:if>
										<form:errors cssClass="error"
											path="groupSearchBases[${counter.index}].searchSubtree"
											element="span" />
									</div>
									<span class="clear"> <!-- -->
									</span>
								</div>
							</div>
						</c:forEach>
						<div id="group-searchbase-INDEX"
							class="dummy-group-searchbase hidden">
							<div class="line">
								<cform:input key="client.authentication.ldap.searchbase"
									name="DUMMY-groupSearchBases[INDEX].searchBase"
									id="groupSearchBase-INDEX" />
								<div class="w50">
									<div class="label">
										<label for="groupSearchSubtree-INDEX"><fmt:message
												key="client.authentication.ldap.searchsubtree" /></label>
									</div>
									<div class="input small float-left">
										<select id="groupSearchSubtree-INDEX"
											name="DUMMY-groupSearchBases[INDEX].searchSubtree">
											<option value="true">
												<fmt:message
													key="client.authentication.ldap.searchsubtree.true" />
											</option>
											<option value="false" selected="selected">
												<fmt:message
													key="client.authentication.ldap.searchsubtree.false" />
											</option>
										</select>
									</div>
									<div class="field-actions float-left">
										<img
											src="<ct:url staticResource="true" value="/themes/core/images/misc/icon20_delete-hover.gif" />"
											class="clickable"
											onclick="removeContainer('group-searchbase-INDEX');"
											alt="<fmt:message key="button.delete" />"
											title="<fmt:message key="client.authentication.ldap.action.remove.searchbase" />" />
									</div>
									<span class="clear"> <!-- -->
									</span>
								</div>
							</div>
							<span class="clear"> <!-- -->
							</span>
						</div>
						<div class="actionbar actionbar-general">
							<div class="button-gray">
								<a class="button" href="javascript:;"
									onclick="addSearchBaseFields('group');"><fmt:message
										key="client.authentication.ldap.action.add.searchbase" /></a>
							</div>
							<span class="clear"> <!-- -->
							</span>
						</div>
						<h6 class="subheadline">
							<fmt:message
								key="client.authentication.ldap.group.properties.header" />
						</h6>
						<div class="fieldset-description">
							<fmt:message
								key="client.authentication.ldap.group.properties.help" />
						</div>
						<div class="line">
							<cform:input
								key="client.authentication.ldap.group.properties.identifier"
								isSpringForm="true" name="groupIdentifier" hint="true"
								required="true" />
							<div class="w50">
								<div class="label">&nbsp;</div>
								<div class="checkbox">
									<form:checkbox path="groupIdentifierIsBinary"
										cssClass="checkbox" id="groupIdentifierIsBinary" />
									<label for="groupIdentifierIsBinary"><fmt:message
											key="client.authentication.ldap.group.properties.identifier.isbinary" /></label>
								</div>
							</div>
							<span class="clear"> <!-- -->
							</span>
						</div>
						<cform:input
							key="client.authentication.ldap.group.properties.name"
							isSpringForm="true" name="groupName" hint="true"
							required="<%=LdapGroupAttribute.NAME.isRequired()%>" />
						<cform:input
							key="client.authentication.ldap.group.properties.alias"
							isSpringForm="true" name="groupAlias" hint="true"
							required="<%=LdapGroupAttribute.ALIAS.isRequired()%>" />
						<span class="clear"> <!-- -->
						</span>
						<cform:input
							key="client.authentication.ldap.group.properties.description"
							isSpringForm="true" name="groupDescription" hint="true"
							required="<%=LdapGroupAttribute.DESCRIPTION.isRequired()%>" />
						<span class="clear"> <!-- -->
						</span>
					</div>
				</fieldset>
				<fieldset class="ldap-account-login sequently">
					<h5>
						<fmt:message key="client.authentication.ldap.account.header" />
					</h5>
					<div class="fieldset-description">
						<fmt:message key="client.authentication.ldap.ldaplogin.help" />
					</div>
					<cform:input key="client.authentication.ldap.ldaplogin"
						isSpringForm="true" name="ldapLogin" required="true" />
					<div class="w50">
                            <div class="label"><label for="ldapPassword"><fmt:message key="client.authentication.ldap.ldappassword" /><span class="required">*</span></label></div>
                            <div class="input password">
                                    <form:password path="ldapPassword" cssClass="text" showPassword="false" autocomplete="off" />
                            </div> 
                        </div>
					<span class="clear"> <!-- -->
					</span>
				</fieldset>
				<div class="info">
					<fmt:message key="form.info.required.fields" />
				</div>
				<div class="actionbar actionbar-general">
					<div class="button-gray main">
						<input type="submit" class="button" name="send1"
							value="<fmt:message key="client.authentication.update.submit" />" />
					</div>
					<span class="clear"> <!-- -->
					</span>
				</div>
			</div>
		</div>
	</form:form>
</div>
