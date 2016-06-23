<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<%@page import="com.communote.server.web.fe.widgets.AbstractPagedListWidget"%>
<%@page import="com.communote.common.util.PageableList"%>
<%@page import="com.communote.server.model.user.group.Group"%>
<%@page import="com.communote.server.widgets.WidgetController"%>

<c:if test="${widget.pageInformation.countPages > 1}">
    <div class="paging-box">
            <div class="paging">
        <c:choose>
            <c:when test="${widget.pageInformation.pageNumber > 1}">
                <a class="previous" href="javascript:;" onclick="widgetController.getWidget('${widget.widgetId}').loadPage('${(widget.pageInformation.pageNumber - 2) * widget.pageInformation.elementsPerPage}'); return false;" >
                    <span style="display:none;"><fmt:message key="blog.post.list.paging.backward"/></span>      
                </a>
            </c:when>
            <c:otherwise>
                <span class="previous" ><span style="display:none;">{previous}</span></span>
            </c:otherwise>
        </c:choose>
        <c:if test="${widget.pageInformation.pageNumber-3 > 1}">
            <c:choose>
                <c:when test="${widget.pageInformation.pageNumber-21 < 1}">
                    <a class="previous10 box" href="javascript:;" onclick="widgetController.getWidget('${widget.widgetId}').loadPage('0'); return false;">1</a>
                </c:when>
                <c:otherwise>
                    <a class="previous1 box" href="javascript:;" onclick="widgetController.getWidget('${widget.widgetId}').loadPage('0');return false;">1</a>
                    <a class="previous10 box" href="javascript:;" onclick="widgetController.getWidget('${widget.widgetId}').loadPage('${(widget.pageInformation.pageNumber-21) * widget.pageInformation.elementsPerPage}');return false;">-20</a>
                </c:otherwise>
            </c:choose>
            <span class="seperator">...</span>
        </c:if>

      <%
        AbstractPagedListWidget<PageableList<Object>> pagedListWidget = (AbstractPagedListWidget<PageableList<Object>>) request.getAttribute(WidgetController.OBJECT_WIDGET);
        for (int i = (pagedListWidget.getPageInformation().getPageNumber())-3; i<=(pagedListWidget.getPageInformation().getPageNumber()+3) ;i++){
            if(i < pagedListWidget.getPageInformation().getPageNumber() && i >= 1) {
                out.print("<a class=\"previous" + i + " box\" href=\"javascript:;\" onclick=\"widgetController.getWidget('"+ pagedListWidget.getWidgetId() +"').loadPage('" + (i-1) * pagedListWidget.getPageInformation().getElementsPerPage() + "');return false;\">");
                out.print(i);
                out.print("</a>");
            }
            if(i == pagedListWidget.getPageInformation().getPageNumber()) {
                out.print("<a class=\"current box\" href=\"javascript:;\">");
                out.print(pagedListWidget.getPageInformation().getPageNumber());
                out.print("</a>");
            }
            if(i > pagedListWidget.getPageInformation().getPageNumber() && i <= pagedListWidget.getPageInformation().getCountPages()) {
                out.print("<a class=\"next" + i + " box\" href=\"javascript:;\" onclick=\"widgetController.getWidget('"+ pagedListWidget.getWidgetId() +"').loadPage('" + (i-1) * pagedListWidget.getPageInformation().getElementsPerPage() + "');return false;\">");
                out.print(i);
                out.print("</a>");
            }
        }
        %>
        
        
                <c:if test="${widget.pageInformation.pageNumber+3 < widget.pageInformation.countPages}">
            <span class="seperator">...</span>
            <c:choose>
                <c:when test="${widget.pageInformation.pageNumber+10 > widget.pageInformation.countPages}">
                    <a class="next10 box" href="javascript:;" onclick="widgetController.getWidget('${widget.widgetId}').loadPage('${(widget.pageInformation.countPages - 1) * widget.pageInformation.elementsPerPage}');return false;">${widget.pageInformation.countPages}</a>
                </c:when>
                <c:otherwise>
                    <a class="next10 box" href="javascript:;" onclick="widgetController.getWidget('${widget.widgetId}').loadPage('${(widget.pageInformation.pageNumber + 9) * widget.pageInformation.elementsPerPage}');return false;">+10</a>
                </c:otherwise>
            </c:choose>
        </c:if>
        <c:choose>
            <c:when test="${widget.pageInformation.pageNumber < widget.pageInformation.countPages}">
                <a class="next" href="javascript:;" onclick="widgetController.getWidget('${widget.widgetId}').loadPage('${widget.pageInformation.pageNumber * widget.pageInformation.elementsPerPage}');return false;" >
                    <span style="display:none;"><fmt:message key="blog.post.list.paging.forward"/></span>       
                </a>
            </c:when>
            <c:otherwise>
                <span class="next"><span style="display:none;">{next}</span></span>
            </c:otherwise>
        </c:choose>
    
                <span class="clear"><!-- ie --></span>
            </div></div>
</c:if>