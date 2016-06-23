<%@ tag body-content="scriptless"
    description="This tag could be used to display an inline notification. The body of the tag is the message, which should be displayed."%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/custom-fmt.tld"%>
<%@ taglib prefix="ct" uri="/WEB-INF/tld/custom-taglib.tld"%>

<%@ attribute name="type" description="Type of the tag. Must be one of help, info, success, warning, error or failure."
    required="true" %>

<c:if test="${empty pageScope.type}">
    <c:set var="type" scope="page">info</c:set>
</c:if>

<div class="notify-inline">
<div class="notify-${pageScope.type}">
<div class="message"><jsp:doBody /></div>
<span class="clear"><!-- ie --></span></div>
</div>