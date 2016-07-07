package com.communote.server.web.fe.portal.user.profile.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.communote.common.util.ParameterHelper;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.InvalidUserStatusTransitionException;
import com.communote.server.core.user.NoClientManagerLeftException;
import com.communote.server.core.user.UserDeletionDisabledException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementException;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.service.UserProfileService;
import com.communote.server.service.UserService;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.commons.FormAction;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.helper.ImageUrlHelper;
import com.communote.server.web.commons.helper.JsonRequestHelper;

/**
 * Controller to delete the current logged in user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileActionController extends MultiActionController {

    /** */
    private static final String JSON_MEDIUM_IMAGE_PATH_KEY = "pathMedium";

    /** */
    private static final String JSON_LARGE_IMAGE_PATH_KEY = "pathLarge";

    /** */
    private static final String PARAM_FORM_ACTION = "action";

    /** */
    private static final String PARAM_DELETE_MODE = "deleteMode";

    /** */
    private static final String DELETE_MODE_DISABLE = "disable";

    /** */
    private static final String DELETE_MODE_ANONYMIZE = "anonymize";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileActionController.class);

    /**
     * Create the result object containing the URL paths to the large and medium image
     *
     * @param userId
     *            the ID of the user
     * @return the JSON result object
     */
    private ObjectNode createSuccessResult(Long userId) {
        ObjectNode result = JsonHelper.getSharedObjectMapper().createObjectNode();
        result.put(JSON_MEDIUM_IMAGE_PATH_KEY,
                ImageUrlHelper.buildUserImageUrl(userId, ImageSizeType.MEDIUM));
        result.put(JSON_LARGE_IMAGE_PATH_KEY,
                ImageUrlHelper.buildUserImageUrl(userId, ImageSizeType.LARGE));
        return result;
    }

    /**
     * Deletion of the current user account.
     *
     * @param request
     *            the servlet request
     * @param response
     *            the servlet response
     *
     * @throws IOException
     *             in case of a IO error
     */
    public void deleteAccount(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();
        String errorMessage = null;
        String mode = getDeleteMode(request);
        if (mode == null) {
            errorMessage = MessageHelper.getText(request, "user.delete.account.failed");
        } else {
            errorMessage = doDeleteAccount(request, mode);
        }
        if (errorMessage != null) {
            jsonResponse.put("message", errorMessage);
            jsonResponse.put("status", ApiResult.ResultStatus.ERROR.name());
        } else {
            // log user out
            AuthenticationHelper.removeAuthentication();
            jsonResponse.put("status", ApiResult.ResultStatus.OK.name());
        }
        JsonHelper.writeJsonTree(response.getWriter(), jsonResponse);
    }

    /**
     * Tries to delete the user account.
     *
     * @param request
     *            the servlet request
     * @param deleteMode
     *            the delete mode
     * @return error message in case of an error, null otherwise
     */
    private String doDeleteAccount(HttpServletRequest request, String deleteMode) {
        UserManagement um = ServiceLocator.findService(UserManagement.class);
        String errorMessage = null;
        Long userId = null;
        try {
            userId = SecurityHelper.getCurrentUserId();

            if (DELETE_MODE_ANONYMIZE.equals(deleteMode)) {
                ServiceLocator.findService(UserService.class).anonymizeUser(userId, new Long[] { },
                        false);
            } else {
                um.permanentlyDisableUser(userId, new Long[] { }, false);
            }
        } catch (AuthorizationException e) {
            errorMessage = MessageHelper.getText(request, "user.delete.account.failed.no.auth",
                    new Object[] { userId });
        } catch (UserDeletionDisabledException e) {
            errorMessage = MessageHelper.getText(request, "user.delete.account.failed.disabled");
        } catch (NoClientManagerLeftException e) {
            errorMessage = MessageHelper.getText(request,
                    "user.delete.account.failed.last.clientmanager");
        } catch (NoBlogManagerLeftException e) {
            String[] blogTitles = new String[] { StringUtils.join(e.getBlogIdsToTitleMapping()
                    .values(), ", ") };
            errorMessage = MessageHelper.getText(request,
                    "user.delete.account.failed.last.blogmanager", blogTitles);
        } catch (InvalidUserStatusTransitionException e) {
            // only log and no error message because user will be logged out
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Account of user with ID " + userId + " has already been deleted.", e);
            }
        } catch (UserManagementException e) {
            LOGGER.error("Deleting user with ID " + userId + " failed.", e);
            errorMessage = MessageHelper.getText(request, "user.delete.account.failed");
        }

        return errorMessage;
    }

    /**
     * Updates the external user image.
     *
     * @param request
     *            HttpServletRequest.
     * @param userId
     *            The current user id.
     * @return the JSON response
     */
    private ObjectNode doRefreshExternalImageAjax(HttpServletRequest request, Long userId) {

        ImageManager imageManagement = ServiceLocator.findService(ImageManager.class);
        Image image;
        try {
            image = imageManagement.getImage(UserImageDescriptor.IMAGE_TYPE_NAME,
                    userId.toString(), ImageSizeType.LARGE);
            if (image.isExternal()) {
                imageManagement.imageChanged(UserImageDescriptor.IMAGE_TYPE_NAME,
                        image.getProviderId(), userId.toString());
            }
            return JsonRequestHelper.createJsonSuccessResponse(MessageHelper.getText(request,
                    "user.profile.image.refresh.external.image.success"),
                    createSuccessResult(userId));
        } catch (AuthorizationException | IOException | ImageNotFoundException e) {
            LOGGER.error("Refreshing external image failed", e);
            return JsonRequestHelper.createJsonErrorResponse(MessageHelper.getText(request,
                    "common.error.unspecified"));
        }
    }

    /**
     * Remove the current image and replace with default logo.
     *
     * @param request
     *            the servlet request
     * @param userId
     *            the ID of the user whose image will be removed
     *
     * @return the JSON response
     */
    private ObjectNode doRemoveImageAjax(HttpServletRequest request, Long userId) {

        try {
            ServiceLocator.findService(UserProfileService.class).removeUserImage(userId);
            return JsonRequestHelper.createJsonSuccessResponse(null, createSuccessResult(userId));
        } catch (UserNotFoundException | AuthorizationException e) {
            LOGGER.error("Unexpected exception while removing image of current user", e);
            String errorMessage = MessageHelper.getText(request, "common.error.unspecified");
            return JsonRequestHelper.createJsonErrorResponse(errorMessage);
        }

    }

    /**
     * Upload an image using AJAX. The response will be returned as JSON object.
     *
     * @param request
     *            the servlet request
     * @param userId
     *            the ID of the user whose image will be updated
     * @return the JSON response object
     */
    private ObjectNode doUploadImageAjax(HttpServletRequest request, Long userId) {

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        CommonsMultipartFile cFile = (CommonsMultipartFile) multipartRequest.getFile("file");
        String errorMessage = null;
        if (cFile != null && cFile.getSize() > 0) {
            if (cFile.getSize() < getMaxUploadSize()) {
                errorMessage = storeImage(request, cFile, userId);
            } else {
                errorMessage = MessageHelper.getText(request, "user.profile.upload.filesize.error",
                        new Object[] { FileUtils.byteCountToDisplaySize(getMaxUploadSize()) });
            }
        } else {
            errorMessage = MessageHelper.getText(request, "user.profile.upload.empty.image");
        }
        if (errorMessage != null) {
            return JsonRequestHelper.createJsonErrorResponse(errorMessage);
        }
        return JsonRequestHelper.createJsonSuccessResponse(null, createSuccessResult(userId));
    }

    /**
     * Returns the requested delete mode.
     *
     * @param request
     *            the servlet request
     * @return the delete mode set or null if unset or unsupported value
     */
    private String getDeleteMode(HttpServletRequest request) {
        String mode = ParameterHelper.getParameterAsString(request.getParameterMap(),
                PARAM_DELETE_MODE);
        if (DELETE_MODE_ANONYMIZE.equals(mode) || DELETE_MODE_DISABLE.equals(mode)) {
            return mode;
        }
        return null;
    }

    /**
     * Helper method which returns the maximal image upload file size.
     *
     * @return the number of bytes representing the upper limit for image uploads
     */
    private long getMaxUploadSize() {
        return Long.parseLong(CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE));
    }

    /**
     * handles the upload process of the user image.
     *
     * @param request
     *            the servlet request
     * @param response
     *            the servlet response
     * @return The model and view.
     * @throws IOException
     *             in case sending the response failed
     */
    public ModelAndView imageUpload(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String action = ParameterHelper.getParameterAsString(request.getParameterMap(),
                PARAM_FORM_ACTION);
        Long userId = SecurityHelper.getCurrentUserId();
        ObjectNode jsonResponse = JsonHelper.getSharedObjectMapper().createObjectNode();

        if (action.equals(FormAction.UPLOAD_IMAGE_FILE_AJAX)) {
            jsonResponse = doUploadImageAjax(request, userId);
        } else if (action.equals(FormAction.RESET_USER_IMAGE_AJAX)) {
            jsonResponse = doRemoveImageAjax(request, userId);
        } else if (action.equals(FormAction.REFRESH_EXTERNAL_USER_IMAGE_AJAX)) {
            jsonResponse = doRefreshExternalImageAjax(request, userId);
        } else {
            jsonResponse = JsonRequestHelper.createJsonErrorResponse("Unsupported operation");
        }

        return ControllerHelper.prepareModelAndViewForJsonResponse(request, response, jsonResponse,
                true);
    }

    /**
     * Stores the image contained in a multipart attachment file in database.
     *
     * @param request
     *            the current request
     * @param cFile
     *            image file found in the multipart attachment
     * @param userId
     *            the user ID
     * @return an error message in case of an error, null otherwise
     */
    private String storeImage(HttpServletRequest request, CommonsMultipartFile cFile, Long userId) {
        String errorKey = null;
        try {
            ServiceLocator.findService(UserProfileService.class).storeOrUpdateUserImage(userId,
                    cFile.getBytes());
        } catch (AuthorizationException | UserNotFoundException e) {
            LOGGER.error("Unexpected exception while updating image of current user", e);
            errorKey = "user.profile.image.upload.error";
        } catch (VirusFoundException e) {
            errorKey = "user.profile.image.upload.error.virus.found";
        } catch (VirusScannerException e) {
            errorKey = "user.profile.image.upload.error.virus.config";
        }
        if (errorKey != null) {
            return MessageHelper.getText(request, errorKey);
        }
        return null;
    }
}
