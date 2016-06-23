package com.communote.plugins.api.rest.v30.resource;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.resource.validation.Validator;

/**
 * 
 * A handler of a a REST API resource. The handler performs invocation to the supplied validator on
 * each handling method. In the case no exceptions occurred the internal handling method (which
 * should be implemented by subclasses) is invoked. A validator MUST be set (through setValidator
 * method) during the concrete resource handler construction
 * 
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
public abstract class AbstractResourceHandler<C, E, D, G, L> {

    private Validator<C, E, D, G, L> validator;

    /**
     * handles creation process
     * 
     * @param createParameter
     *            create parameter
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    public final Response handleCreate(C createParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request) throws Exception {
        validator.validateCreate(createParameter);
        return handleCreateInternally(createParameter, requestedMimeType, uriInfo,
                requestSessionId, request);
    }

    /**
     * hook method, which should be implemented by subclasses, in order to perform actual resource
     * creation
     * 
     * @param createParameter
     *            create parameter
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    abstract protected Response handleCreateInternally(C createParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request) throws Exception;

    /**
     * handles deleting process
     * 
     * @param deleteParameter
     *            delete parameter
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    public final Response handleDelete(D deleteParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request) throws Exception {
        validator.validateDelete(deleteParameter);
        return handleDeleteInternally(deleteParameter, requestedMimeType, uriInfo,
                requestSessionId, request);
    }

    /**
     * 
     * hook method, which should be implemented by subclasses, in order to perform actual resource
     * removal
     * 
     * @param deleteParameter
     *            delete parameter
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    abstract protected Response handleDeleteInternally(D deleteParameter, String requestedMimeType,
            javax.ws.rs.core.UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception;;

    /**
     * @param editParameter
     *            edit parameter
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    public final Response handleEdit(E editParameter, String requestedMimeType, UriInfo uriInfo,
            String requestSessionId, Request request) throws Exception {
        validator.validateEdit(editParameter);
        return handleEditInternally(editParameter, requestedMimeType, uriInfo, requestSessionId,
                request);
    }

    /**
     * 
     * 
     * @param editParameter
     *            edit parameter
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    abstract protected Response handleEditInternally(E editParameter, String requestedMimeType,
            javax.ws.rs.core.UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception;;

    /**
     * handles resource querying
     * 
     * @param getParameter
     *            get parameter
     * 
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    public final Response handleGet(G getParameter, String requestedMimeType, UriInfo uriInfo,
            String requestSessionId, Request request) throws Exception {
        validator.validateGetSingle(getParameter);
        return handleGetInternally(getParameter, requestedMimeType, uriInfo, requestSessionId,
                request);
    }

    /**
     * hook method, which should be implemented by subclasses, in order to perform actual resource
     * querying
     * 
     * @param getParameter
     *            get parameter
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    abstract protected Response handleGetInternally(G getParameter, String requestedMimeType,
            javax.ws.rs.core.UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception;;

    /**
     * handles querying of the resource list
     * 
     * @param listParameter
     *            get list parameter
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    public final Response handleList(L listParameter, String requestedMimeType, UriInfo uriInfo,
            String requestSessionId, Request request) throws Exception {
        validator.validateList(listParameter);
        return handleListInternally(listParameter, requestedMimeType, uriInfo, requestSessionId,
                request);
    }

    /**
     * hook method, which should be implemented by subclasses, in order to perform actual resource
     * querying
     * 
     * @param listParameter
     *            get list parameter
     * @param requestedMimeType
     *            mimeType
     * @param uriInfo
     *            uri
     * @param requestSessionId
     *            session id
     * @param request
     *            request
     * @return result of creation
     * @throws Exception
     *             if anything goes wrong
     */
    abstract protected Response handleListInternally(L listParameter, String requestedMimeType,
            javax.ws.rs.core.UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception;;

    /**
     * 
     * @param validator
     *            validator to be set
     */
    public void setValidator(Validator<C, E, D, G, L> validator) {
        this.validator = validator;
    }
}
