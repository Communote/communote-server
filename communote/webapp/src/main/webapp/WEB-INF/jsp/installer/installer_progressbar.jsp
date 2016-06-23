<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<div class="installer-progressbar">
    <%-- STEP 1 :: target0 --%>
    <c:choose>
        <c:when test="${command.currentPage eq 0 and not command.finishRequest}">
            <span class="active"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>1</fmt:param></fmt:message></span><fmt:message key="installer.step.welcome.progress.title" /></span>
        </c:when>
        <c:when test="${command.currentProgress ge 0 and not command.finishRequest}">
            <a href="javascript:;" class="done" onclick="goTo(0);"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>1</fmt:param></fmt:message></span><fmt:message key="installer.step.welcome.progress.title" /></a>
        </c:when>
        <c:otherwise>
            <span class="disabled"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>1</fmt:param></fmt:message></span><fmt:message key="installer.step.welcome.progress.title" /></span>
        </c:otherwise>
    </c:choose>
    <%-- STEP 2 :: target1 --%>
    <c:choose>
        <c:when test="${command.currentPage eq 1 and not command.finishRequest}">
            <span class="active"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>2</fmt:param></fmt:message></span><fmt:message key="installer.step.database.selection.progress.title" /></span>
        </c:when>
        <c:when test="${command.currentProgress ge 1 and not command.finishRequest}">
            <a href="javascript:;" class="done" onclick="goTo(1);"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>2</fmt:param></fmt:message></span><fmt:message key="installer.step.database.selection.progress.title" /></a>
        </c:when>
        <c:otherwise>
            <span class="disabled"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>2</fmt:param></fmt:message></span><fmt:message key="installer.step.database.selection.progress.title" /></span>
        </c:otherwise>
    </c:choose>
    <%-- STEP 3 :: target2 --%>
    <c:choose>
        <c:when test="${command.currentPage eq 2 and not command.finishRequest}">
            <span class="active"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>3</fmt:param></fmt:message></span><fmt:message key="installer.step.database.setup.progress.title" /></span>
        </c:when>
        <c:when test="${command.currentProgress ge 2 and not command.finishRequest}">
            <a href="javascript:;" class="done" onclick="goTo(2);"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>3</fmt:param></fmt:message></span><fmt:message key="installer.step.database.setup.progress.title" /></a>
        </c:when>
        <c:otherwise>
            <span class="disabled"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>3</fmt:param></fmt:message></span><fmt:message key="installer.step.database.setup.progress.title" /></span>
        </c:otherwise>
    </c:choose>
    
    <%-- STEP 4 :: target3 --%>
    <c:choose>
        <c:when test="${command.currentPage eq 3 and not command.finishRequest}">
            <span class="active"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>5</fmt:param></fmt:message></span><fmt:message key="installer.step.application.progress.title" /></span>
        </c:when>
        <c:when test="${command.currentProgress ge 3 and not command.finishRequest}">
            <a href="javascript:;" class="done" onclick="goTo(3);"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>4</fmt:param></fmt:message></span><fmt:message key="installer.step.application.progress.title" /></a>
        </c:when>
        <c:otherwise>
            <span class="disabled"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>4</fmt:param></fmt:message></span><fmt:message key="installer.step.application.progress.title" /></span>
        </c:otherwise>
    </c:choose>
    
    <%-- STEP 5 :: target4 --%>
    <c:choose>
        <c:when test="${command.currentPage eq 4 and not command.finishRequest}">
            <span class="active"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>6</fmt:param></fmt:message></span><fmt:message key="installer.step.mail.progress.title" /></span>
        </c:when>
        <c:when test="${command.currentProgress ge 4 and not command.finishRequest}">
            <a href="javascript:;" class="done" onclick="goTo(4);"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>5</fmt:param></fmt:message></span><fmt:message key="installer.step.mail.progress.title" /></a>
        </c:when>
        <c:otherwise>
            <span class="disabled"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>5</fmt:param></fmt:message></span><fmt:message key="installer.step.mail.progress.title" /></span>
        </c:otherwise>
    </c:choose>
    
    <%-- STEP 6 :: target5 --%>
    <c:choose>
        <c:when test="${command.currentPage eq 5 and not command.finishRequest}">
            <span class="active"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>7</fmt:param></fmt:message></span><fmt:message key="installer.step.admin.progress.title" /></span>
        </c:when>
        <c:when test="${command.currentProgress ge 5 and not command.finishRequest}">
            <a href="javascript:;" class="done" onclick="goTo(5);"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>6</fmt:param></fmt:message></span><fmt:message key="installer.step.admin.progress.title" /></a>
        </c:when>
        <c:otherwise>
            <span class="disabled"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>6</fmt:param></fmt:message></span><fmt:message key="installer.step.admin.progress.title" /></span>
        </c:otherwise>
    </c:choose>
    
    <%-- STEP 7 :: target6 --%>
    <c:choose>
        <c:when test="${command.currentPage eq 6 and command.currentProgress eq 5}">
            <span class="active"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>7</fmt:param></fmt:message></span><fmt:message key="installer.step.finish.progress.title" /></span>
        </c:when>
        <c:otherwise>
            <span class="disabled"><span class="progress-bullet"><fmt:message key="installer.step"><fmt:param>7</fmt:param></fmt:message></span><fmt:message key="installer.step.finish.progress.title" /></span>
        </c:otherwise>
    </c:choose>
</div>