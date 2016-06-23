<%@ include file="/WEB-INF/jsp/common/include.jsp"%>
<c:set var="searchBoxDefaultText"><fmt:message key="widget.user.management.searchbox.usersearch.default" /></c:set>
<div class="userManagementSearchBox">
    <form action="#"
        onSubmit="widgetController.getWidget('${widget.widgetId}').submitSearchBox('${searchBoxDefaultText}'); return false;">
        <div class="box">
            <div id="usermanagmentstatusfilter" class="box-head-discreet block">
                <div class="cn-searchbox">
                    <div class="cn-border">
                        <input type="text" id="SearchBoxInput" name="SearchBoxInput" class="text searchbox-input"
                            size="20" maxlength="200" autocomplete="off"
                            placeholder="<fmt:message key="widget.user.management.searchbox.usersearch.default" />" />
                        <a href="javascript:;" class="cn-filter-search-clear"
                            onclick="widgetController.getWidget('${widget.widgetId}').resetSearchBox('')"></a>
                    </div>
                </div>
            </div>
            <div id="userSearchFilter" class="box-content-discreet block">
                <br />
                <h6 class="subheadline">
                    <fmt:message key="widget.user.management.searchbox.statusfilter" />
                </h6>
                <div class="input-group">
                    <div style="float: left; margin-right: 14px;">
                        <c:forEach items="${widget.userStatusLiterals}" var="item" varStatus="status">
                            <c:if test="${item != 'DELETED'}">
                                <c:if test="${status.count mod 4 == 0}">
                    </div>
                    <div style="float: left; margin-right: 14px;">
                        </c:if>
                        <input id="cb_status_${status.index}" type="checkbox" name="userStatusFilter"
                            onclick="E2G('userStatusFilterClick', widgetController.getWidget('${widget.widgetId}').filterWidgetGroupId, this.value);"
                            value="${item}" style="vertical-align: middle;" /> <label for="cb_status_${status.index}"><fmt:message
                                key="client.user.status.${fn:toLowerCase(item)}" /><c:if test="${item == 'TERMS_NOT_ACCEPTED'}"> <ct:tip key="client.user.status.terms_not_accpeted.tip" /></c:if></label><br />
                        </c:if>
                        </c:forEach>
                    </div>
                    <span class="clear">
                        <!-- -->
                    </span>
                </div>
                <span class="clear">
                    <!-- -->
                </span>
            </div>

            <div class="block">
                <br />
                <h6 class="subheadline">
                    <fmt:message key="widget.user.management.searchbox.rolesfilter" />
                </h6>
                <div class="input-group">
                    <input id="cb_role_admin" type="checkbox" name="userRoleFilter"
                        onclick="E2G('userRoleFilterClick', widgetController.getWidget('${widget.widgetId}').filterWidgetGroupId, this);"
                        value="ROLE_KENMEI_CLIENT_MANAGER" /> <label for="cb_role_admin"><fmt:message
                            key="widget.user.management.searchbox.roles.admin" /></label>
                    <input id="cb_role_system_user" type="checkbox" name="userRoleFilter"
                        onclick="E2G('userRoleFilterClick', widgetController.getWidget('${widget.widgetId}').filterWidgetGroupId, this);"
                        value="ROLE_SYSTEM_USER" /> <label for="cb_role_system_user"><fmt:message
                            key="widget.user.management.searchbox.roles.system.user" /></label>
                    <input id="cb_role_crawl_user" type="checkbox" name="userRoleFilter"
                        onclick="E2G('userRoleFilterClick', widgetController.getWidget('${widget.widgetId}').filterWidgetGroupId, this);"
                        value="ROLE_CRAWL_USER" /> <label for="cb_role_crawl_user"><fmt:message
                            key="widget.user.management.searchbox.roles.crawl.user" /></label>
                </div>
                <span class="clear">
                    <!-- -->
                </span>
            </div>

            <div class="block">
                <br />
                <h6 class="subheadline">
                    <fmt:message key="widget.user.management.searchbox.display" />
                </h6>
                <select id="ShowAnzElementsSelect" size="1"
                    onchange="var selected = document.id(this).getSelected()[0]; if(selected){E('onUserListNumberElementsChange', selected.get('value'));}">
                    <option value="10">
                        10&nbsp;
                        <fmt:message key="widget.user.management.searchbox.members" />
                    </option>
                    <option value="25">
                        25&nbsp;
                        <fmt:message key="widget.user.management.searchbox.members" />
                    </option>
                    <option value="50">
                        50&nbsp;
                        <fmt:message key="widget.user.management.searchbox.members" />
                    </option>
                    <option value="100">
                        100&nbsp;
                        <fmt:message key="widget.user.management.searchbox.members" />
                    </option>
                </select> <span class="clear">
                    <!-- -->
                </span>
            </div>
        </div>
    </form>
</div>