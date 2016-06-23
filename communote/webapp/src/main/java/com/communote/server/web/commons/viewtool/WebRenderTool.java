package com.communote.server.web.commons.viewtool;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.tools.config.DefaultKey;

import com.communote.server.core.common.velocity.tools.RenderTool;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.view.TemplateManager;

/**
 * Tool providing rendering utilities.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@DefaultKey(value = "renderTool")
public class WebRenderTool extends RenderTool {
    private TemplateManager templateManager;

    private TemplateManager getTemplateManager() {
        if (this.templateManager == null) {
            this.templateManager = WebServiceLocator.findService(TemplateManager.class);
        }
        return this.templateManager;
    }

    /**
     * Get the path to a velocity template by the name it was registered with at the
     * {@link TemplateManager}.
     *
     * @param tempalteName
     *            the name of the template
     * @return the path to the template or an empty string
     */
    public String getVelocityTemplateLocation(String tempalteName) {
        TemplateManager templateManager = getTemplateManager();
        if (templateManager == null) {
            return StringUtils.EMPTY;
        }
        String templateLocation = templateManager.getTemplate(tempalteName);
        if (templateLocation == null) {
            return StringUtils.EMPTY;
        }
        return templateLocation;
    }
}
