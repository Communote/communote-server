package com.communote.server.web.commons.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.view.TemplateManager;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class VelocityViewController extends AbstractController {

    private String view = "core.layout.main.wrapper";
    private String contentTemplate;
    private String selectedMenu;
    private TemplateManager templateManager;

    private String[] javaScriptCategories = new String[] { };

    private String[] cssCategories = new String[] { };
    private String page;

    /**
     * Returns the template to be used for rendering the view
     *
     * @param model
     *            the current model of the model and view
     * @return the contentTemplate
     *
     * @see #setContentTemplate(String)
     */
    protected String getContentTemplate(Map<String, Object> model) {
        return this.contentTemplate;
    }

    /**
     * @return the page
     */
    protected String getPage() {
        return page;
    }

    /**
     * Returns the identifier to be exported as selected menu.
     *
     * @param model
     *            the current model of the model and view
     * @return the selected menu
     *
     * @see #setSelectedMenu(String)
     */
    protected String getSelectedMenu(Map<String, Object> model) {
        return this.selectedMenu;
    }

    /**
     * @return the view
     */
    public String getView() {
        return this.view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        if (prepareModel(request, response, model)) {
            return new ModelAndView(ControllerHelper.replaceModuleInViewName(getView()), model);
        }
        // return null as the response already has been committed
        return null;
    }

    /**
     * Prepares the model by adding the key value pairs.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param model
     *            the model to prepare
     * @return true if the preparation succeeded and the model can be passed to the view to write to
     *         the response. False should be returned if an error occurred and the error handler
     *         already wrote to the response which is now considered to be committed.
     * @throws Exception
     *             Exception.
     *
     */
    protected boolean prepareModel(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws Exception {
        String template = getContentTemplate(model);
        String templateURI = templateManager.getTemplate(template);
        if (templateURI != null) {
            model.put("contentTemplate", templateURI);
        } else {
            model.put("contentTemplate", template);
        }
        model.put("selectedMenu", getSelectedMenu(model));

        ApplicationConfigurationProperties applicationConfigurationProperties = CommunoteRuntime
                .getInstance().getConfigurationManager().getApplicationConfigurationProperties();
        model.put("javaScriptCategories", javaScriptCategories);
        model.put("cssCategories", cssCategories);
        model.put("packJavaScript", applicationConfigurationProperties.getProperty(
                ApplicationProperty.SCRIPTS_PACK, true));
        model.put("compressJavaScript", applicationConfigurationProperties.getProperty(
                ApplicationProperty.SCRIPTS_COMPRESS, true));
        model.put("packCss", applicationConfigurationProperties.getProperty(
                ApplicationProperty.STYLES_PACK, true));
        model.put("compressCss", applicationConfigurationProperties.getProperty(
                ApplicationProperty.STYLES_COMPRESS, true));
        String currentPage = getPage();
        if (currentPage != null) {
            model.put("page", currentPage);
        }
        return true;
    }

    /**
     * Set the template to be rendered as view.
     *
     * @param contentTemplate
     *            the content template to set
     */
    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    /**
     * Set the names of the categories whose CSS resources should be included when rendering the
     * page
     *
     * @param categories
     *            the names of the categories in the order the resources of the category should be
     *            rendered
     */
    public void setCssCategories(String[] categories) {
        if (categories == null) {
            this.cssCategories = new String[] { };
        } else {
            this.cssCategories = categories;
        }
    }

    /**
     * Set the names of the categories whose JavaScript resources should be included when rendering
     * the page
     *
     * @param categories
     *            the names of the categories in the order the resources of the category should be
     *            rendered
     */
    public void setJavaScriptCategories(String[] categories) {
        if (categories == null) {
            this.javaScriptCategories = new String[] { };
        } else {
            this.javaScriptCategories = categories;
        }
    }

    /**
     * @param page
     *            the page to set
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Set the identifier to be exported as selected menu. This could be used in the view to
     * highlight the current page in a top navigation
     *
     * @param menuIdentifier
     *            the menu identifier to set
     */
    public void setSelectedMenu(String menuIdentifier) {
        this.selectedMenu = menuIdentifier;
    }

    /**
     * Set the template manager to use for resolving template names into templates
     *
     * @param templateManager
     *            the manager
     */
    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    /**
     * @param view
     *            the view to set
     */
    public void setView(String view) {
        this.view = view;
    }

}
