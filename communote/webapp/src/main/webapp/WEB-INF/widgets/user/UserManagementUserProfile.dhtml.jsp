<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<%--include placeholder for user notification --%>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<c:choose>
    <c:when test="${userStatus == 'PERMANENTLY_DISABLED'}">
        <c:set var="fieldDisabled">true</c:set>
        <c:set var="fieldReadonly">true</c:set>
    </c:when>
    <c:otherwise>
        <c:set var="fieldDisabled">false</c:set>
        <c:set var="fieldReadonly">false</c:set>
    </c:otherwise>
</c:choose>

<form:form>
    <input type="hidden" name="userId" value="${userId}" />
    <input type="hidden" name="alias" value="${alias}" />
    <div class="form-container">
        <fieldset>
            <h5><fmt:message key="widget.user.management.profile.generaluserinformations" /></h5>
            <div class="w100 long">
                <div class="label"><label for="userProfile.salutation"><fmt:message key="widget.user.management.profile.salutation" /></label></div>
                <div class="input"><form:input path="userProfile.salutation" cssClass="text" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="userProfile.salutation" class="error"><form:errors path="userProfile.salutation" /></label></span>
            </div>
            <span class="clear"><!-- ie --></span>
            
            <div class="w50">
                <div class="label"><label for="userProfile.firstName"><fmt:message key="widget.user.management.profile.firstname" /><span class="required">*</span></label></div>
                <div class="input"><form:input path="userProfile.firstName" cssClass="text" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="userProfile.firstName" class="error"><form:errors path="userProfile.firstName" /></label></span>
            </div>
            <div class="w50">
                <div class="label"><label for="userProfile.lastName"><fmt:message key="widget.user.management.profile.lastname" /><span class="required">*</span></label></div>
                <div class="input"><form:input path="userProfile.lastName" cssClass="text" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="userProfile.lastName" class="error"><form:errors path="userProfile.lastName" /></label></span>
            </div>
            <span class="clear"><!-- ie --></span>
       
            <div class="spacer">&nbsp;</div>
                        
            <div class="w50">
                <div class="label"><label for="userProfile.company"><fmt:message key="widget.user.management.profile.company" /></label></div>
                <div class="input"><form:input path="userProfile.company" cssClass="text" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="userProfile.company" class="error"><form:errors path="userProfile.company" /></label></span>
            </div>
            <div class="w50">
                <div class="label"><label for="userProfile.position"><fmt:message key="widget.user.management.profile.position" /></label></div>
                <div class="input"><form:input path="userProfile.position" cssClass="text" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="userProfile.position" class="error"><form:errors path="userProfile.position" /></label></span>
            </div>
            <span class="clear"><!-- ie --></span>
       
            <div class="w100 long">
                <div class="label"><label for="userProfile.street"><fmt:message key="widget.user.management.profile.street" /></label></div>
                <div class="input"><form:input path="userProfile.street" cssClass="text" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="userProfile.street" class="error"><form:errors path="userProfile.street" /></label></span>
            </div>  
            <span class="clear"><!-- ie --></span>
        
            <div class="w50">
                <div class="label"><label for="userProfile.zip"><fmt:message key="widget.user.management.profile.zip" /></label></div>
                <div class="input"><form:input path="userProfile.zip" cssClass="text" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="userProfile.zip" class="error"><form:errors path="userProfile.zip" /></label></span>
            </div>
            <div class="w50">
                <div class="label"><label for="userProfile.city"><fmt:message key="widget.user.management.profile.city" /></label></div>
                <div class="input"><form:input path="userProfile.city" cssClass="text" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="userProfile.city" class="error"><form:errors path="userProfile.city" /></label></span>
            </div>
            <span class="clear"><!-- ie --></span>
        
            <div class="w50">
                <div class="label"><label for="phone"><fmt:message key="widget.user.management.profile.phone" /></label></div>
                <div class="input"><span>+</span><form:input cssClass="text countryCode" path="phone.countryCode" maxlength="341" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /><span>(</span><form:input cssClass="text areaCode" path="phone.areaCode" maxlength="341" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /><span>)</span><form:input cssClass="text phoneNumber" path="phone.phoneNumber" maxlength="341" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="phone" class="error"><form:errors path="phone" /></label></span>
            </div>
            <div class="w50">
                <div class="label"><label for="fax"><fmt:message key="widget.user.management.profile.fax" /></label></div>
                <div class="input"><span>+</span><form:input cssClass="text countryCode" path="fax.countryCode" maxlength="341" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /><span>(</span><form:input cssClass="text areaCode" path="fax.areaCode" maxlength="341" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /><span>)</span><form:input cssClass="text phoneNumber" path="fax.phoneNumber" maxlength="341" htmlEscape="true" disabled="${fieldDisabled}" readonly="${fieldReadonly}" /></div>
                <span class="error"><label for="fax" class="error"><form:errors path="fax" /></label></span>
            </div>
            <span class="clear"><!-- ie --></span>

            <div class="spacer">&nbsp;</div>
                        
            <div class="w50">
                <div class="label"><label for="countryCode"><fmt:message key="widget.user.management.profile.country" /></label></div>
                <div class="input">
                    <jsp:include page="/WEB-INF/jsp/common/country_list.jsp">
                        <jsp:param name="path" value="countryCode" />
                        <jsp:param name="includeEmpty" value="${empty command.countryCode}" />
                        <jsp:param name="disableSelection" value="${fieldDisabled}" />
                    </jsp:include>
                </div>
                <span class="error"><label for="countryCode" class="error"><form:errors path="countryCode" /></label></span>
            </div>
            <div class="w50">
                <div class="label"><label for="languageCode"><fmt:message key="widget.user.management.profile.language" /></label></div>
                <div class="input">
                    <jsp:include page="/WEB-INF/jsp/common/language_list.jsp">
                        <jsp:param name="path" value="languageCode" />
                        <jsp:param name="includeEmpty" value="${empty command.languageCode}" />
                        <jsp:param name="disableSelection" value="${fieldDisabled}" />
                    </jsp:include>
                </div>
                <span class="error"><label for="languageCode" class="error"><form:errors path="languageCode" /></label></span>
            </div>
            <span class="clear"><!-- ie --></span>
            
            <div class="w100">
                <div class="label"><label for="timeZoneId"><fmt:message key="widget.user.management.profile.timezone" /></label></div>
                <div class="input">
                    <jsp:include page="/WEB-INF/jsp/common/timezone_list.jsp">
                        <jsp:param name="path" value="timeZoneId" />
                        <jsp:param name="includeEmpty" value="${empty command.timeZoneId}" />
                        <jsp:param name="disableSelection" value="${fieldDisabled}" />
                    </jsp:include>
                </div>
                <span class="error"><label for="timeZoneId" class="error"><form:errors path="timeZoneId" /></label></span>
            </div>
            <span class="clear"><!-- ie --></span>
            <div class="fieldset-info"><fmt:message key="form.info.required.fields" /></div>
        </fieldset>
        
        <c:if test="${userStatus != 'PERMANENTLY_DISABLED'}">
        <div class="actionbar actionbar-general">
            <div class="button-gray main">
                <input type="submit" name="submit" value="<fmt:message key="widget.user.management.profile.action.save" />" />
            </div>
            <span class="clear"></span>
        </div>
        </c:if>
    </div>
</form:form>