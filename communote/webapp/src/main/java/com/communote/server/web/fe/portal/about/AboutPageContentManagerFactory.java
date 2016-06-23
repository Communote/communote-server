package com.communote.server.web.fe.portal.about;

import org.springframework.beans.factory.FactoryBean;

import com.communote.server.web.commons.controller.StaticPageContentManager;
import com.communote.server.web.commons.controller.StaticPageSection;
import com.communote.server.web.fe.portal.service.MessageToolDelegatingLocalizedMessage;

/**
 * Factory for creating the {@link StaticPageContentManager} of the about page.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AboutPageContentManagerFactory implements FactoryBean<StaticPageContentManager> {
    public static final String SECTION_ID_IMPRINT = "imprint";
    public static final int SECTION_ORDER_IMPRINT = 150;
    private StaticPageContentManager manager;

    private StaticPageContentManager createManager() {
        StaticPageContentManager manager = new StaticPageContentManager();
        StaticPageSection section = new StaticPageSection(SECTION_ID_IMPRINT);
        section.setOrder(SECTION_ORDER_IMPRINT);
        section.setTitleMessageKey("service.legalnotice.imprint");
        section.setContent(new MessageToolDelegatingLocalizedMessage(true), true);
        manager.addSection(section);
        return manager;
    }

    @Override
    public StaticPageContentManager getObject() throws Exception {
        if (this.manager == null) {
            this.manager = createManager();
        }
        return manager;
    }

    @Override
    public Class<?> getObjectType() {
        return StaticPageContentManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
