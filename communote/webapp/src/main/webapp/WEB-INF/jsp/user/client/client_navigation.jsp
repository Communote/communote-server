<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<div class="navigation-area">
    <div class="wrapper last">
        <c:set var="currentMenu" value="${administrationTool.accountMenu}" />
        <div id="admin-account">
            <c:forEach var="category" items="${currentMenu.categories}">
                <c:set var="isCurrentCategory" value="${menuEntry.category.id == category.id}"/>
                <c:if test="${category.hasChildren()}">
                    <div id="${category.id}">
                        <h4>${category.getLocalizedLabel(currentLocale, null)}</h4>
                        <c:forEach var="categoryEntry" items="${category.children}">
                            <c:choose>
                                <c:when test="${isCurrentCategory && categoryEntry.id == menuEntry.entry.id }"><c:set var="categoryEntryCss" value="link active"/></c:when>
                                <c:otherwise><c:set var="categoryEntryCss" value="link"/></c:otherwise>
                            </c:choose>
                            <a class="${categoryEntryCss}" href="${categoryEntry.renderPageUrl(pageContext.request)}">${categoryEntry.getLocalizedLabel(currentLocale, null)}</a>
                        </c:forEach>
                    </div>
                </c:if>
            </c:forEach>
        </div>
        <c:set var="currentMenu" value="${administrationTool.systemMenu}" />
        <div id="admin-system">
            <c:forEach var="category" items="${currentMenu.categories}">
                <c:set var="isCurrentCategory" value="${menuEntry.category.id == category.id}"/>
                <c:if test="${category.hasChildren()}">
                    <div id="${category.id}">
                        <h4>${category.getLocalizedLabel(currentLocale, null)}</h4>
                        <c:forEach var="categoryEntry" items="${category.children}">
                            <c:choose>
                                <c:when test="${isCurrentCategory && categoryEntry.id == menuEntry.entry.id }"><c:set var="categoryEntryCss" value="link active"/></c:when>
                                <c:otherwise><c:set var="categoryEntryCss" value="link"/></c:otherwise>
                            </c:choose>
                            <a class="${categoryEntryCss}" href="${categoryEntry.renderPageUrl(pageContext.request)}">${categoryEntry.getLocalizedLabel(currentLocale, null)}</a>
                        </c:forEach>
                    </div>
                </c:if>
            </c:forEach>
        </div>
        <c:set var="extensionCategory" value="${administrationTool.extensions}" />
        <c:if test="${extensionCategory.hasChildren()}">
            <c:set var="isCurrentCategory" value="${menuEntry.category.id == extensionCategory.id}"/>
            <div id="admin-menu-extensions">
                <h4>${extensionCategory.getLocalizedLabel(currentLocale, null)}</h4>
                <c:forEach var="categoryEntry" items="${extensionCategory.children}">
                    <c:choose>
                        <c:when test="${isCurrentCategory && categoryEntry.id == menuEntry.entry.id }"><c:set var="categoryEntryCss" value="link active"/></c:when>
                        <c:otherwise><c:set var="categoryEntryCss" value="link"/></c:otherwise>
                    </c:choose>
                    <a class="${categoryEntryCss}" href="${categoryEntry.renderPageUrl(pageContext.request)}">${categoryEntry.getLocalizedLabel(currentLocale, null)}</a>
                </c:forEach>
            </div>
        </c:if>
    </div>
</div>