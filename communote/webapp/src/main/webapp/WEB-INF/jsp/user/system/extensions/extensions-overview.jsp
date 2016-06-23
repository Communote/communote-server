<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@page import="com.communote.server.core.osgi.OSGiManagement" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.TreeMap" %>
<%@page import="com.communote.server.api.ServiceLocator" %>
<%@page import="org.osgi.framework.Bundle" %>
<%@ include file="/WEB-INF/jsp/common/messages/success_error_notification_messages.jsp" %>

<div class="layer">
    <table>
        <thead>
        <tr>
            <th>Name</th>
            <th>Version</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <%
            Bundle[] bundles = ServiceLocator.findService(OSGiManagement.class).getFramework().getBundleContext().getBundles();
            Map<String, Bundle> sortedBundles = new TreeMap<String, Bundle>();
            for (Bundle bundle : bundles) {
                sortedBundles.put(bundle.getHeaders().get("Bundle-Name"), bundle);
            }
            for (Bundle bundle : sortedBundles.values()) {
                out.write("<tr>");
                out.write("<td style=\"font-size: 12px;\">" + bundle.getHeaders().get("Bundle-Name") + "</td>");
                out.write("<td style=\"font-size: 12px;\">" + bundle.getVersion().toString().replace(".SNAPSHOT", ""));
                if( bundle.getHeaders().get("Build-Revision") != null){
                    out.write("." + bundle.getHeaders().get("Build-Revision"));
                }
                out.write("</td><td style=\"font-size: 12px;\">");
                request.setAttribute("bundleState", bundle.getState());
        %>
        <fmt:message key="client.system.extensions.bundle.state.${bundleState}"/>
        <%
                out.write("</td></tr>");
            }
        %>
        </tbody>
    </table>
</div>
