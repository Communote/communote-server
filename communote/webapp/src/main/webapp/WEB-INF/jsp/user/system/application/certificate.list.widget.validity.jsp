<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<c:set var="validity" scope="page">${certificate.right.right}</c:set>

<c:choose>
    <c:when test="${validity == 'VALID'}">
        <c:set var="validity_source" scope="page">
            <ct:url staticResource="true" value="/themes/core/images/misc/icon16_accept.png" />
        </c:set>
    </c:when>
    <c:when test="${validity == 'NOT_VALID'}">
        <c:set var="validity_source" scope="page">
            <ct:url staticResource="true" value="/themes/core/images/misc/icon16_remove.png" />
        </c:set>
    </c:when>
    <c:when test="${validity == 'NOT_YET_VALID'}">
        <c:set var="validity_source" scope="page">
            <ct:url staticResource="true" value="/themes/core/images/misc/icon16_info.png" />
        </c:set>
    </c:when>
    <c:otherwise>
        <c:set var="validity_source" scope="page">
            <ct:url staticResource="true" value="/themes/core/images/misc/icon16_warning.png" />
        </c:set>
    </c:otherwise>
</c:choose>
<c:set var="validity_alternate" scope="page">
    <fmt:message key="client.system.application.certificate.upload.list.${validity}" />
</c:set>
<span class="validity-icon" title="${validity_alternate}"> <img src="${validity_source}"
    alt="${validity_alternate}" /> </span>
