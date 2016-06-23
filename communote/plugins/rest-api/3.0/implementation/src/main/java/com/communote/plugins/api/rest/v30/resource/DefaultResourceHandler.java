package com.communote.plugins.api.rest.v30.resource;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.resource.validation.DefaultValidator;
import com.communote.plugins.api.rest.v30.resource.validation.Validator;

/**
 * Default implementation of AbstractResourceHandler. If a concrete handler inherits from this
 * class, only the relevant methods should be overridden, and default validator is supplied.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <C>
 *            create resource parameter
 * @param <E>
 *            edit resource parameter
 * @param <D>
 *            delete resource parameter
 * @param <G>
 *            get resource parameter
 * @param <L>
 *            list resource parameter
 */
public abstract class DefaultResourceHandler<C, E, D, G, L> extends
        AbstractResourceHandler<C, E, D, G, L> {

    /**
     * Default constructor, setting a {@link DefaultValidator}
     */
    public DefaultResourceHandler() {
        this(new DefaultValidator<C, E, D, G, L>());
    }

    /**
     * Constructor for using a specific validator
     * 
     * @param validator
     *            the validator to use
     */
    public DefaultResourceHandler(Validator<C, E, D, G, L> validator) {
        setValidator(validator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response handleCreateInternally(C createParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request) throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response handleDeleteInternally(D deleteParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request) throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response handleEditInternally(E editParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request) throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response handleGetInternally(G getParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request) throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response handleListInternally(L listParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request) throws Exception {
        return null;
    }

}
