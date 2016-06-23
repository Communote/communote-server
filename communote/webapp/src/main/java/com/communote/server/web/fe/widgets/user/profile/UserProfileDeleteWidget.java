package com.communote.server.web.fe.widgets.user.profile;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.widgets.EmptyWidget;

/**
 * Widget for displaying the form to delete the user
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileDeleteWidget extends EmptyWidget {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.user.profile.delete";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        // Deletion of the user account
        boolean deletionAllowed = ClientConfigurationHelper.isUserDeletionAllowed();
        boolean anonymizationAllowed = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.DELETE_USER_BY_ANONYMIZE_ENABLED, false);
        boolean disableAllowed = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.DELETE_USER_BY_DISABLE_ENABLED, false);

        setParameter("deletionAllowed", String.valueOf(deletionAllowed));
        setParameter("anonymizationAllowed", String.valueOf(anonymizationAllowed));
        setParameter("disableAllowed", String.valueOf(disableAllowed));

    }

}
