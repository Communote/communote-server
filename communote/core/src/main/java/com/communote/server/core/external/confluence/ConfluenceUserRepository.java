package com.communote.server.core.external.confluence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.external.AbstractExternalUserRepository;
import com.communote.server.core.external.ExternalRepositoryException;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.UserProfileFields;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;
import com.communote.server.persistence.user.ExternalUserVO;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
// Uncomment me once I am a plugin
// @Component(immediate = true)
// @Instantiate
// @Provides
public class ConfluenceUserRepository extends AbstractExternalUserRepository {

    /**
     * Define constant here, once this is a plugin.
     */
    public final static String EXTERNAL_SYSTEM_ID_DEFAULT_CONFLUENCE = ConfigurationManagement.DEFAULT_CONFLUENCE_SYSTEM_ID;

    /**
     * Default constructor setting the default system id
     */
    public ConfluenceUserRepository() {
        super(EXTERNAL_SYSTEM_ID_DEFAULT_CONFLUENCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfluenceConfiguration createConfiguration() {
        return ConfluenceConfiguration.Factory.newInstance();
    }

    /**
     * Get the confluence configuration of the client
     *
     * @return {@link ConfluenceConfiguration}
     */
    @Override
    public ConfluenceConfiguration getConfiguration() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getConfluenceConfiguration();
    }

    @Override
    public Collection<StringPropertyTO> getExternalLoginProperties(Long userId,
            ExternalUserAuthentication externalUserAuthentication) {
        return Collections.emptySet();
    }

    @Override
    public LocalizedMessage getName() {
        return new MessageKeyLocalizedMessage("client.integration.types."
                + ConfluenceUserRepository.EXTERNAL_SYSTEM_ID_DEFAULT_CONFLUENCE);
    }

    @Override
    public Set<UserProfileFields> getProvidedProfileFieldNames() {
        HashSet<UserProfileFields> fields = new HashSet<>();
        fields.add(UserProfileFields.FIRSTNAME);
        fields.add(UserProfileFields.LASTNAME);
        return fields;
    }

    /**
     * Get the {@link ExternalUserVO} of the external user identifier
     *
     * @param externalUserId
     *            external user identifier
     * @return {@link ExternalUserVO}
     * @throws ExternalRepositoryException
     *             repository exception
     */
    @Override
    public ExternalUserVO getUser(String externalUserId) throws ExternalRepositoryException {
        if (getConfiguration() == null) {
            throw new ExternalRepositoryException("confluence configuration is null");
        }
        return null;
    }

}
