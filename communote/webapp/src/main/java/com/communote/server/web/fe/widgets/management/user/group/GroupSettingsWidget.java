package com.communote.server.web.fe.widgets.management.user.group;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget to configure common group settings.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GroupSettingsWidget extends AbstractWidget {

    /**
     * @param outputType
     *            Not used.
     * @return "core.widget.management.groups.settings"
     */
    @Override
    @Deprecated
    public String getTile(String outputType) {
        return "core.widget.management.groups.settings";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        ConfigurationManager propertiesManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        if (getRequest().getParameter("updateConfiguration") != null) {
            Map<ClientConfigurationPropertyConstant, String> settings = new HashMap<ClientConfigurationPropertyConstant, String>();
            String isCreateExternalGroupAutomatically = getRequest().getParameter(
                    "createExternalGroupAutomatically");
            settings.put(ClientProperty.CREATE_EXTERNAL_GROUP_AUTOMATICALLY,
                    Boolean.toString(isCreateExternalGroupAutomatically != null));
            int synchronizationInterval = NumberUtils.toInt(getRequest().getParameter(
                    "syncInterval"));
            if (synchronizationInterval < 1) {
                synchronizationInterval = ClientProperty.DEFAULT_GROUP_SYNCHRONIZATION_INTERVAL_IN_MINUTES;
            }
            settings.put(ClientProperty.GROUP_SYNCHRONIZATION_INTERVAL_IN_MINUTES,
                    Integer.toString(synchronizationInterval));
            propertiesManager.updateClientConfigurationProperties(settings);
            MessageHelper.saveMessageFromKey(getRequest(), "client.user.group.save.success");
        }
        getRequest().setAttribute(
                "createExternalGroupAutomatically",
                ClientProperty.CREATE_EXTERNAL_GROUP_AUTOMATICALLY
                .getValue(ClientProperty.DEFAULT_CREATE_EXTERNAL_GROUP_AUTOMATICALLY));
        getRequest()
        .setAttribute(
                "syncInterval",
                ClientProperty.GROUP_SYNCHRONIZATION_INTERVAL_IN_MINUTES
                .getValue(ClientProperty.DEFAULT_GROUP_SYNCHRONIZATION_INTERVAL_IN_MINUTES));
        return null;
    }

    @Override
    protected void initParameters() {
        // TODO Auto-generated method stub

    }
}
