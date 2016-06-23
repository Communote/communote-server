package com.communote.server.core.vo.client.converters;

import com.communote.common.converter.CollectionConverter;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.model.client.Client;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientToClientTOConverter extends CollectionConverter<Client, ClientTO> {

    /**
     * method to convert a client to a client TO. Be aware, that this can throw an
     * LazyInitializationException, when not used within a hibernate session.
     *
     * @param source
     *            The client.
     * @return The corresponding ClientTO.
     */
    @Override
    public ClientTO convert(Client source) {
        if (source == null) {
            return null;
        }
        ClientTO target = new ClientTO();
        target.setId(source.getId());
        target.setClientId(source.getClientId());
        target.setClientStatus(source.getClientStatus());
        target.setCreationDate(source.getCreationTime());
        target.setCreationRevision(source.getCreationRevision());
        target.setCreationVersion(source.getCreationVersion());
        target.setName(source.getName());
        return target;
    }
}
