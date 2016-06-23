<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>

<div class="form-container">
<form:form>
    <fieldset>
        <legend>${titlePrefix}<fmt:message key="installer.step.welcome.legend.title" /></legend>
        <h2><fmt:message key="installer.step.welcome.description.head" /></h2>
        <%-- introduction --%>
        <p><fmt:message key="installer.step.welcome.description.1" /></p>
        <%-- before you start --%>
        <p>
        <fmt:message key="installer.step.welcome.description.2">
            <fmt:param value="http://communote.github.io/doc" />
            <fmt:param value="/microblog/global/installer/contact.do" />
        </fmt:message>
        </p>
        <%-- support and contact --%>
        <p><fmt:message key="installer.step.welcome.description.3" /></p>
        <br />
        <h2><fmt:message key="installer.step.welcome.product.head" /></h2>
        <p><fmt:message key="installer.step.welcome.product.content" /></p>
    </fieldset>
    
    <div class="actionbar actionbar-general">
        <div class="button-gray button-right">
            <input type="submit" id="" name="_target1" value="<fmt:message key="installer.button.next" />" />
        </div>
        <span class="clear"><!-- ie --></span>
    </div>
</form:form>
</div>