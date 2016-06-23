package com.communote.plugins.core.views;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;

import com.communote.common.string.StringEscapeHelper;
import com.communote.plugins.core.views.annotations.Page;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.osgi.OSGiHelper;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.web.commons.MessageHelper;

/**
 * Data and logic all plugins controllers have in common.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ViewControllerData {

    private static final String COMPRESS_CSS = "compressCss";

    private static final String COMPRESS_JAVASCRIPT = "compressJavaScript";

    private static final String PACK_JAVASCRIPT = "packJavaScript";

    private static final String PACK_CSS = "packCss";

    private final Map<String, Object> defaultData = new HashMap<String, Object>();

    private final Page pageInformation;

    private final String symbolicName;

    /**
     * Constructor.
     *
     * @param bundleContext
     *            The bundles context.
     */
    public ViewControllerData(BundleContext bundleContext, PluginViewController viewController) {
        this(bundleContext.getBundle().getSymbolicName(), viewController);
    }

    /**
     * Constructor.
     *
     * @param symbolicName
     *            The symbolic name of the bundle the controller is in.
     * @param viewController
     *
     */
    public ViewControllerData(String symbolicName, PluginViewController viewController) {
        this.symbolicName = symbolicName;
        defaultData.put(ViewControllerParameters.SYMBOLIC_NAME.toString(), symbolicName);
        pageInformation = viewController.getClass().getAnnotation(Page.class);
        if (pageInformation == null) {
            return;
        }
        if (StringUtils.isNotBlank(pageInformation.menuMessageKey())) {
            defaultData.put(ViewControllerParameters.MENU_ENTRY_MESSAGE_KEY.toString(),
                    pageInformation.menuMessageKey());
        }
        defaultData.put(ViewControllerParameters.MENU.toString(), pageInformation.menu());
        defaultData.put(ViewControllerParameters.SUBMENU.toString(), pageInformation.submenu());
        if (pageInformation.cssCategories() != null && pageInformation.cssCategories().length > 0) {
            defaultData.put(ViewControllerParameters.CSS_CATEGORIES.toString(),
                    pageInformation.cssCategories());
        }
        if (pageInformation.jsCategories() != null && pageInformation.jsCategories().length > 0) {
            defaultData.put(ViewControllerParameters.JAVASCRIPT_CATEGORIES.toString(),
                    pageInformation.jsCategories());
        }
        defaultData.put(ViewControllerParameters.JS_MESSAGES_CATEGORY.toString(),
                pageInformation.jsMessagesCategory());
        defaultData.put(
                ViewControllerParameters.CONTENT_TEMPLATE.toString(),
                OSGiHelper.getRelativeBundleResourceLocation(symbolicName,
                        viewController.getContentTemplate()));
        defaultData.put("isHttpsSupported",
                Boolean.parseBoolean(ApplicationProperty.WEB_HTTPS_SUPPORTED.getValue()));
    }

    /**
     * Checks, if scripts should be packed and/or compressed.
     *
     * @param request
     *            The request.
     * @param model
     *            The model.
     */
    public void determineScriptsCompression(HttpServletRequest request, Map<String, Object> model) {
        // TODO do we need this override per request?
        String compress = request.getParameter("scripts_compress");
        if (compress != null) {
            model.put(COMPRESS_JAVASCRIPT, Boolean.parseBoolean(compress));
        }
        String pack = request.getParameter("scripts_pack");
        if (pack != null) {
            model.put(PACK_JAVASCRIPT, Boolean.parseBoolean(pack));
        }
    }

    /**
     * Checks, if styles should be packed and/or compressed.
     *
     * @param request
     *            The request.
     * @param model
     *            The model.
     */
    public void determineStylesCompression(HttpServletRequest request, Map<String, Object> model) {
        // TODO do we need this override per request?
        String compress = request.getParameter("styles_compress");
        if (compress != null) {
            model.put(COMPRESS_CSS, Boolean.parseBoolean(compress));
        }
        String pack = request.getParameter("styles_pack");
        if (pack != null) {
            model.put(PACK_CSS, Boolean.parseBoolean(pack));
        }
    }

    public Map<String, Object> getDataForRequest(HttpServletRequest request) {
        Map<String, Object> data = new HashMap<String, Object>(defaultData);
        if (pageInformation != null && pageInformation.titleKey().length() > 0) {
            String clientName = StringEscapeHelper.escapeXml(ClientHelper.getCurrentClient()
                    .getName());
            data.put(ViewControllerParameters.PAGE_TITLE.toString(), MessageHelper.getText(request,
                    pageInformation.titleKey(), new Object[] { clientName }));
        }
        return data;
    }

    public Map<String, Object> getDefaultData() {
        return defaultData;
    }

    public Page getPageInformation() {
        return pageInformation;
    }

    public String getSymbolicName() {
        return symbolicName;
    }
}
