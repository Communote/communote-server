/**
 *
 */
package com.communote.server.web.fe.portal.blog.controller;

import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.BaseCommandController;

import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.crc.ContentRepositoryManagementHelper;
import com.communote.server.core.crc.ResourceSizeLimitReachedException;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.storing.LocalizedResourceStoringManagementException;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.storing.ResourceStoringManagementException;
import com.communote.server.core.vo.content.AttachmentStreamTO;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentStatus;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.helper.JsonRequestHelper;
import com.communote.server.web.fe.portal.blog.helper.CreateBlogPostFeHelper;

/**
 * The controller for file uploads during blog posting.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AttachmentUploadController extends BaseCommandController {

    private final SessionHandler sessionHandler = SessionHandler.instance();

    /**
     * @return the max upload size.
     */
    public long getMaxUploadSize() {
        return NumberUtils.toLong(
                CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE), 0);
    }

    /**
     * Returns an error message describing the exception that was thrown while uploading a file.
     *
     * @param e
     *            the exception
     * @param request
     *            the request
     * @param locale
     *            The locale.
     * @return the message
     */
    private String getUploadExceptionErrorMessage(HttpServletRequest request,
            ResourceStoringManagementException e, Locale locale) {
        String errorMessage;
        Throwable cause = e.getCause();
        if (cause instanceof LocalizedResourceStoringManagementException) {
            return ((LocalizedResourceStoringManagementException) cause)
                    .getLocalizedMessage(locale);
        }
        if (cause instanceof ResourceStoringManagementException) {
            cause = cause.getCause();
        }
        if (cause instanceof InitializeException) {
            errorMessage = MessageHelper
                    .getText(request, "error.blogpost.file.upload.virus.config");
        } else if (cause instanceof VirusFoundException) {
            errorMessage = MessageHelper.getText(request, "error.blogpost.file.upload.virus.found");
        } else if (cause instanceof ResourceSizeLimitReachedException) {
            errorMessage = MessageHelper.getText(request, "error.blogpost.upload.limit.reached",
                    new Object[] { FileUtils
                    .byteCountToDisplaySize(ContentRepositoryManagementHelper
                            .getSizeLimit()) });
        } else {
            errorMessage = MessageHelper.getText(request, "error.blogpost.upload.failed");
        }

        return errorMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Locale locale = sessionHandler.getCurrentLocale(request);
        ObjectNode uploadResult = null;
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        CommonsMultipartFile cFile = (CommonsMultipartFile) multipartRequest.getFile("file");
        String errorMessage = null;
        if (cFile != null && cFile.getSize() > 0) {
            if (cFile.getSize() < getMaxUploadSize()) {
                try {
                    // create a binary content TO
                    AttachmentTO binContent = new AttachmentStreamTO(cFile.getInputStream(),
                            AttachmentStatus.UPLOADED);
                    binContent.setMetadata(new ContentMetadata());
                    binContent.getMetadata().setFilename(cFile.getOriginalFilename());
                    binContent.setContentLength(cFile.getSize());

                    ResourceStoringManagement rsm = ServiceLocator
                            .findService(ResourceStoringManagement.class);
                    Attachment attachment = rsm.storeAttachment(binContent);

                    // save attachment IDs in session to allow removing attachments that are removed
                    // from the note before publishing
                    // we do not do this via separate requests because the ownership is not checked
                    // when deleting the attachment
                    Set<Long> uploadedFiles = CreateBlogPostFeHelper
                            .getUploadedAttachmentsFromSession(request);
                    uploadedFiles.add(attachment.getId());
                    uploadResult = CreateBlogPostFeHelper.createAttachmentJSONObject(attachment);
                } catch (ResourceStoringManagementException e) {
                    errorMessage = getUploadExceptionErrorMessage(request, e, locale);
                }
            } else {
                errorMessage = ResourceBundleManager.instance().getText(
                        "error.blogpost.upload.filesize.limit", locale,
                        FileUtils.byteCountToDisplaySize(getMaxUploadSize()));
            }
        } else {
            errorMessage = ResourceBundleManager.instance().getText(
                    "error.blogpost.upload.empty.file", locale);
        }
        ObjectNode jsonResponse;
        if (errorMessage != null) {
            jsonResponse = JsonRequestHelper.createJsonErrorResponse(errorMessage);
        } else {
            jsonResponse = JsonRequestHelper.createJsonSuccessResponse(null, uploadResult);
        }
        return ControllerHelper.prepareModelAndViewForJsonResponse(multipartRequest, response,
                jsonResponse, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
            throws ServletException {
        // to actually be able to convert Multipart instance to byte[]
        // we have to register a custom editor
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        // now Spring knows how to handle multipart object and convert them
    }

}
