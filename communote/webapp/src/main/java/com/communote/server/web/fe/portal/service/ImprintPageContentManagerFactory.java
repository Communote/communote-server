package com.communote.server.web.fe.portal.service;

import org.springframework.beans.factory.FactoryBean;

import com.communote.server.web.commons.controller.StaticPageContentManager;
import com.communote.server.web.commons.controller.StaticPageSection;

/**
 * Factory to configure the {@link StaticPageContentManager} of the imprint page.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ImprintPageContentManagerFactory implements FactoryBean<StaticPageContentManager> {

    /**
     * ID of the contact section
     */
    public static final String SECTION_ID_CONTACT = "contact";
    /**
     * order value of the contact section
     */
    public static final int SECTION_ORDER_CONTACT = 200;
    /**
     * ID of the imprint section
     */
    public static final String SECTION_ID_IMPRINT = "imprint";
    /**
     * order value of the imprint section
     */
    public static final int SECTION_ORDER_IMPRINT = 150;
    /**
     * ID of the terms of use section
     */
    public static final String SECTION_ID_TERMS = "terms-of-use";
    /**
     * order value of the terms of use section
     */
    public static final int SECTION_ORDER_TERMS = 100;
    /**
     * ID of the about section
     */
    public static final String SECTION_ID_ABOUT = "about";
    /**
     * order value of the about section
     */
    public static final int SECTION_ORDER_ABOUT = 50;
    private StaticPageContentManager manager;

    private StaticPageContentManager createManager() {
        StaticPageContentManager manager = new StaticPageContentManager();
        StaticPageSection section = new StaticPageSection(SECTION_ID_CONTACT);
        section.setOrder(SECTION_ORDER_CONTACT);
        section.setTitleMessageKey("service.about.contact");
        section.setContent(new ContactLocalizedMessage(), true);
        manager.addSection(section);
        section = new StaticPageSection(SECTION_ID_IMPRINT);
        section.setOrder(SECTION_ORDER_IMPRINT);
        section.setTitleMessageKey("service.legalnotice.imprint");
        section.setContent(new MessageToolDelegatingLocalizedMessage(true), true);
        manager.addSection(section);
        section = new StaticPageSection(SECTION_ID_TERMS);
        section.setOrder(SECTION_ORDER_TERMS);
        section.setTitleMessageKey("service.legalnotice.terms");
        section.setContent(new MessageToolDelegatingLocalizedMessage(false), true);
        manager.addSection(section);
        section = new StaticPageSection(SECTION_ID_ABOUT);
        section.setOrder(SECTION_ORDER_ABOUT);
        section.setTitleMessageKey("service.legalnotice.about.title");
        section.setContentTemplateName("core.pages.service.legal.section.about");
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
