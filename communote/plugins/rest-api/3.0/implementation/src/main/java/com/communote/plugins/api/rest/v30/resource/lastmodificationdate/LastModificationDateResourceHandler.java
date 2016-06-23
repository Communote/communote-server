package com.communote.plugins.api.rest.v30.resource.lastmodificationdate;

import java.util.List;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.resource.user.UserResource;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.plugins.api.rest.v30.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.lastmodifieddate.LastModificationDateManagement;

/**
 * Handler for {@link UserResource}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LastModificationDateResourceHandler
extends
DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, DefaultParameter,
GetCollectionLastModificationDateParameter> {

    private final LastModificationDateResourceFactory lastModificationDateResourceFactory = new LastModificationDateResourceFactory();

    private final LastModificationDateManagement lastModificationDateManagement = ServiceLocator
            .findService(LastModificationDateManagement.class);

    private List<LastModificationDateResource> getAttachmentLastModificationDates()
            throws AuthorizationException {

        return lastModificationDateManagement
                .getAttachmentCrawlLastModificationDates(lastModificationDateResourceFactory);
    }

    private List<LastModificationDateResource> getNoteLastModificationDates()
            throws AuthorizationException {
        return lastModificationDateManagement
                .getNoteCrawlLastModificationDates(lastModificationDateResourceFactory);
    }

    private List<LastModificationDateResource> getTopicLastModificationDates()
            throws AuthorizationException {
        return lastModificationDateManagement
                .getTopicCrawlLastModificationDates(lastModificationDateResourceFactory);
    }

    @Override
    protected Response handleListInternally(
            GetCollectionLastModificationDateParameter listParameter, String requestedMimeType,
            UriInfo uriInfo, String requestSessionId, Request request) throws Exception {

        List<LastModificationDateResource> dates;
        if (listParameter.getType() == null) {
            throw new IllegalRequestParameterException("type", "null", "Invalid value.");
        }
        switch (listParameter.getType()) {
        case ATTACHMENT:
            dates = getAttachmentLastModificationDates();
            break;
        case NOTE:
            dates = getNoteLastModificationDates();
            break;
        case TOPIC:
            dates = getTopicLastModificationDates();
            break;
        default:
            throw new IllegalRequestParameterException("type", String.valueOf(listParameter
                    .getType()), "Unknown type.");
        }

        return ResponseHelper.buildSuccessResponse(dates, request);
    }

}
