package com.communote.plugins.api.rest.v22.resource.attachment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.communote.common.io.MaxLengthReachedException;
import com.communote.plugins.api.rest.v22.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v22.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v22.request.RequestHelper;
import com.communote.plugins.api.rest.v22.resource.DefaultParameter;
import com.communote.plugins.api.rest.v22.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v22.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v22.resource.validation.ParameterValidationException;
import com.communote.plugins.api.rest.v22.response.ResponseHelper;
import com.communote.plugins.api.rest.v22.service.IllegalRequestParameterException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.retrieval.helper.AttachmentHelper;
import com.communote.server.core.vo.content.AttachmentStreamTO;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentStatus;
import com.communote.server.persistence.crc.ContentRepositoryException;

/**
 * A handler class to provide data for an image resource to the resource class. All the list
 * parameter are collected in a parameter map.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentResourceHandler extends
DefaultResourceHandler<CreateAttachmentParameter, DefaultParameter, DefaultParameter,
GetAttachmentParameter, DefaultParameter> {

    /**
     * Get the file name for saving to attachment. If the fileName is an path than take the last
     * element after backslash with extension as file name.
     *
     * @param fileName
     *            name of file or path, the filename can be the complete path (e.g. in ie)
     * @return the name of the file.
     */
    private static String getFileName(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {

            int lastIndex = fileName.lastIndexOf("\\");

            if (lastIndex == -1) {
                lastIndex = fileName.lastIndexOf("/");
            }

            if (lastIndex != -1) {
                return fileName.substring(lastIndex + 1);
            } else {
                return fileName;
            }

        }

        return fileName;
    }

    /**
     * extracts attachments from the http request.
     *
     * @param httpRequest
     *            request containing attachments DefaultMultipartHttpServletRequest is supported
     * @param request
     *            {@link Request}
     * @return ids of extracted attachments
     * @throws MaxLengthReachedException
     *             attachment size is to large
     * @throws IOException
     *             throws exception if problems during attachments accessing occurred
     * @throws FileUploadException
     *             Exception.
     * @throws AuthorizationException
     *             in case there is no authenticated user
     */
    private List<AttachmentResource> extractAttachments(HttpServletRequest httpRequest,
            Request request) throws MaxLengthReachedException, FileUploadException, IOException,
            AuthorizationException {
        List<AttachmentResource> result = new ArrayList<AttachmentResource>();
        if (httpRequest.getContentType().startsWith(AttachmentResourceHelper.MULTIPART_FORM_DATA)) {
            FileItemFactory factory = new DiskFileItemFactory();
            FileUpload upload = new FileUpload(factory);
            List<FileItem> items = upload.parseRequest(new ServletRequestContext(httpRequest));
            for (FileItem file : items) {
                if (!file.isFormField()) {
                    AttachmentTO attachmentTo = new AttachmentStreamTO(file.getInputStream(),
                            AttachmentStatus.UPLOADED);
                    AttachmentResourceHelper.assertAttachmentSize(file.getContentType(),
                            file.getSize(), false);
                    attachmentTo.setMetadata(new ContentMetadata());
                    attachmentTo.getMetadata().setFilename(getFileName(file.getName()));
                    attachmentTo.getMetadata().setContentSize(file.getSize());
                    result.add(persistAttachment(request, attachmentTo));
                }
            }
        }
        return result;
    }

    @Override
    protected Response handleCreateInternally(CreateAttachmentParameter createParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws ParameterValidationException, ResponseBuildException, MaxLengthReachedException,
                    ExtensionNotSupportedException, FileUploadException, IOException,
                    IllegalRequestParameterException, AuthorizationException {

        HttpServletRequest httpReq = RequestHelper.getHttpServletRequest(
                org.restlet.Request.getCurrent());

        // get parameter directly from request because createParameter is null for this mime
        // type
        AttachmentResourceHelper.checkAttachmentUploadSessionId(httpReq
                .getParameter("attachmentUploadSessionId"));

        if (httpReq.getContentType().startsWith(MediaType.MULTIPART_FORM_DATA)) {
            return ResponseHelper.buildSuccessResponse(extractAttachments(httpReq, request),
                    request);
        } else {
            String fileName = httpReq.getParameter("fileName");
            if (StringUtils.isBlank(fileName)) {
                throw new IllegalRequestParameterException("fileName",
                        fileName, "Name of can not been empty.");
            }

            String mimeType = AttachmentHelper.determineKnownMimeType(httpReq
                    .getParameter("fileType"));

            String isBase64Value = httpReq.getParameter("isBase64");
            boolean isBase64 = isBase64Value == null ? false : Boolean.parseBoolean(isBase64Value);
            AttachmentResourceHelper.assertAttachmentSize(mimeType,
                    Long.valueOf(httpReq.getContentLength()), isBase64);

            InputStream inputStream = org.restlet.Request.getCurrent().getEntity().getStream();

            if (isBase64) {
                byte[] decodedInputStream = Base64.decodeBase64(IOUtils.toByteArray(inputStream));
                inputStream = new ByteArrayInputStream(decodedInputStream);
            }

            AttachmentTO attachmentTo = new AttachmentStreamTO(inputStream,
                    AttachmentStatus.UPLOADED);

            attachmentTo.setMetadata(new ContentMetadata());
            attachmentTo.getMetadata().setFilename(getFileName(fileName));
            attachmentTo.getMetadata().setContentSize(httpReq.getContentLength());

            List<AttachmentResource> result = new ArrayList<AttachmentResource>();
            result.add(persistAttachment(request, attachmentTo));

            return ResponseHelper.buildSuccessResponse(result, request);
        }

    }

    /**
     * Returns an existing attachment of a given note.
     *
     * @param getAttachmentParameter
     *            the parameters of the attachment
     * @param requestedMimeType
     *            MIME type requested by client
     * @param uriInfo
     *            additional information about request
     * @param requestSessionId
     *            the session id
     * @param request
     *            - javax request
     * @return response for client
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws AuthorizationException
     *             user is not authorized
     * @throws ContentRepositoryException
     *             internal exception.
     * @throws IOException
     *             exception while i/o operation.
     * @throws NotFoundException
     *             in case the attachment was not found
     */
    @Override
    public Response handleGetInternally(GetAttachmentParameter getAttachmentParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws ResponseBuildException, ExtensionNotSupportedException,
                    ContentRepositoryException, AuthorizationException, IOException, NotFoundException {
        long attachmentId = getAttachmentParameter.getAttachmentId();
        AttachmentTO attachment;

        Response response = null;

        attachment = AttachmentResourceHelper.getResourceStoringManagement()
                .getAttachment(attachmentId);

        Boolean asBinary = getAttachmentParameter.getGetAttachmentAsBinary();
        if (asBinary != null && asBinary) {
            return AttachmentResourceHelper.getAttachmentAsBinary(attachment);
        } else {
            AttachmentResource attachmentResource = AttachmentResourceHelper
                    .buildAttachmentResource(attachment);
            attachmentResource.setAttachmentId(attachmentId);

            response = ResponseHelper.buildSuccessResponse(attachmentResource, request);
        }

        return response;

    }

    /**
     * performs actual attachment persisting to communote
     *
     * @param request
     *            {@link Request}
     * @param attachmentTo
     *            transfer object for attachment
     * @return id of attachmnet in communote
     * @throws AuthorizationException
     *             in case there is no authenticated user
     */
    private AttachmentResource persistAttachment(Request request, AttachmentTO attachmentTo)
            throws AuthorizationException {
        Attachment attachment = AttachmentResourceHelper.getResourceStoringManagement()
                .storeAttachment(attachmentTo);

        AttachmentResource attachmentResource = new AttachmentResource();
        attachmentResource.setAttachmentId(attachment.getId());
        attachmentResource.setFileName(attachment.getName());
        attachmentResource.setContentLength(attachment.getSize());
        attachmentResource.setFileType(attachment.getContentType());

        ResourceHandlerHelper.getUploadedAttachmentsFromSession(request, null).add(
                attachment.getId());
        return attachmentResource;
    }

}
