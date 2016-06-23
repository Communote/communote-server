package com.communote.server.web.commons.view.velocity;

import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

import com.communote.server.web.commons.view.TemplateManager;

/**
 * A velocity view resolver which uses the {@link TemplateManager} for resolving the view names.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO this class should be an eventlistener which observes TemplatesChangeEvents, but since this
// would require a registration at the EventDispatcher on creation this would lead to an exception
// when the app is not yet installed and the installer must be run. The installer should be
// refactored to have his own private spring context so we can get rid of the dependencies.
public class DynamicVelocityViewResolver extends VelocityViewResolver {

    private TemplateManager templateManager;

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        String view = templateManager.getTemplate(viewName);
        return super.buildView(view);
    }

    @Override
    protected void initApplicationContext() {
        super.initApplicationContext();
        // TODO see comment on class
        // ServiceLocator.findService(EventDispatcher.class).register(this);
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
     * Update the view names in the base class to indicate whether or not this
     * {@link org.springframework.web.servlet.ViewResolver} can handle the supplied view name.
     *
     * @see org.springframework.web.servlet.view.UrlBasedViewResolver#createView(String,
     *      java.util.Locale)
     * @see org.springframework.web.servlet.view.UrlBasedViewResolver#canHandle(String,
     *      java.util.Locale)
     */
    private void setViewNames() {
        setViewNames(templateManager.getTemplateNames());
    }

    /**
     * Should be called when the templates have changed.
     */
    // TODO ugly workaround for the Event problem described above
    public void templatesChanged() {
        setViewNames();
        clearCache();
    }
}
