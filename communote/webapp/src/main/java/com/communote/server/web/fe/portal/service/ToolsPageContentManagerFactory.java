package com.communote.server.web.fe.portal.service;

import org.springframework.beans.factory.FactoryBean;

import com.communote.server.web.commons.controller.StaticPageContentManager;
import com.communote.server.web.fe.portal.controller.EnhancedPageSection;

/**
 * Factory to configure the {@link StaticPageContentManager} of the tools page.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ToolsPageContentManagerFactory implements FactoryBean<StaticPageContentManager> {
    /**
     * ID of the desktop client section
     */
    public static final String SECTION_ID_DESKTOP_CLIENT = "desktopclient";
    /**
     * order value of the desktop client section
     */
    public static final int SECTION_ORDER_DESKTOP_CLIENT = 300;
    /**
     * ID of the desktop client section
     */
    public static final String SECTION_ID_MOBILE_APPS = "mobile-apps";
    /**
     * order value of the desktop client section
     */
    public static final int SECTION_ORDER_MOBILE_APPS = 200;

    private StaticPageContentManager manager;

    private StaticPageContentManager createManager() {
        StaticPageContentManager manager = new StaticPageContentManager();
        EnhancedPageSection section = new EnhancedPageSection(SECTION_ID_DESKTOP_CLIENT);
        section.setOrder(SECTION_ORDER_DESKTOP_CLIENT);
        section.setShortTitleMessageKey("service.apps.desktop.title.short");
        section.setTitleMessageKey("service.apps.desktop.title");
        section.setImageUrl("/themes/core/images/service/desktopclient.png");
        section.setContentMessageKey("service.apps.desktop.description");
        section.addActionLink("service.download", "http://desktop.communote.com");
        manager.addSection(section);

        section = new EnhancedPageSection(SECTION_ID_MOBILE_APPS);
        section.setOrder(SECTION_ORDER_MOBILE_APPS);
        section.setShortTitleMessageKey("service.apps.mobile.title.short");
        section.setTitleMessageKey("service.apps.mobile.title");
        section.setImageUrl("/themes/core/images/service/iphone-app.png");
        section.setContent(new MobileAppsDescriptionLocalizedMessage(), true);
        section.addActionLink("service.apps.mobile.button.iphone", "http://iosapp.communote.com");
        section.addActionLink("service.apps.mobile.button.android",
                "http://androidapp.communote.com");
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
