package com.communote.server.web.fe.portal.user.client.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.ConfigurationManagementException;
import com.communote.server.core.image.type.ClientImageDescriptor;
import com.communote.server.core.image.type.ClientImageProvider;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.fe.portal.user.client.forms.ClientProfileEmailForm;
import com.communote.server.web.fe.portal.user.client.forms.ClientProfileLogoForm;

/**
 * The Class ClientProfileController handles the update client profile use case.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientProfileController extends BaseFormController {

    /**
     *
     * The action type the controller supports.
     */
    public enum ActionType {
        /**
         * edit action for client email settings
         */
        EMAIL,
        /**
         * action for changing the client logo
         */
        LOGO
    }

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientProfileController.class);

    /** The Constant LOG. */
    private boolean initBinderRequired = false;

    private ActionType action;

    /**
     * Returns a message key for an error message describing the exception that was thrown while
     * uploading the client logo.
     *
     * @param e
     *            the exception
     * @return the message key
     */
    private String getImageUploadExceptionErrorMessageKey(Exception e) {
        String errorKey = "client.change.logo.image.upload.error";
        if (e instanceof ConfigurationManagementException) {
            Throwable cause = e.getCause();
            if (cause instanceof ConfigurationManagementException) {
                cause = cause.getCause();
            }
            if (cause instanceof InitializeException) {
                errorKey = "client.change.logo.image.upload.error.virus.config";
            } else if (cause instanceof VirusFoundException) {
                errorKey = "client.change.logo.image.upload.error.virus.found";
            }
        }
        return errorKey;
    }

    /**
     * Execute the action
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param errors
     *            the errors object
     * @param command
     *            the form backing object
     * @return the model and view to show
     * @throws IOException
     *             in case of an error
     */
    private ModelAndView handelAction(HttpServletRequest request, HttpServletResponse response,
            BindException errors, Object command) throws IOException {
        ModelAndView mav = null;
        switch (action) {
        case LOGO:
            mav = handleUpdateClientLogo(request, errors, (ClientProfileLogoForm) command);
            break;
        case EMAIL:
            mav = handleUpdateEmailSettings(request, (ClientProfileEmailForm) command);
            break;

        default:
            MessageHelper.saveErrorMessageFromKey(request, "client.profile.action.error");
            LOGGER.error("Action property not defined");
            break;
        }
        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {

        ModelAndView mav = handelAction(request, response, errors, command);

        if (mav == null || errors.getErrorCount() > 0) {
            mav = showForm(request, errors, getFormView());
        }

        return mav;
    }

    /**
     * Handle remove client logo.
     *
     * @param request
     *            the request
     * @param form
     *            the form
     * @return the model and view
     */
    private ModelAndView handleRemoveClientLogo(HttpServletRequest request,
            ClientProfileLogoForm form) {
        ServiceLocator.findService(ConfigurationManagement.class).removeClientLogo();
        ServiceLocator.findService(ImageManager.class).imageChanged(
                ClientImageDescriptor.IMAGE_TYPE_NAME, ClientImageProvider.PROVIDER_IDENTIFIER,
                ClientHelper.getCurrentClientId());
        form.setCustomClientLogo(false);
        MessageHelper.saveMessage(request,
                MessageHelper.getText(request, "client.change.logo.image.remove.success"));
        return new ModelAndView(getSuccessView(), getCommandName(), form);
    }

    /**
     * Handles the client logo update action.
     *
     * @param request
     *            the request
     * @param errors
     *            object for binding errors
     * @param form
     *            the form backing object
     * @return the model and view
     */
    private ModelAndView handleUpdateClientLogo(HttpServletRequest request, BindException errors,
            ClientProfileLogoForm form) {
        ModelAndView mav = null;
        if (!form.isResetToDefault()) {
            mav = handleUploadClientLogo(request, errors, form);
        } else {
            mav = handleRemoveClientLogo(request, form);
        }
        return mav;
    }

    /**
     * This method updates the email settings (reply-to address, replay-to name and signature) of
     * the client.
     *
     * @param request
     *            {@link HttpServletRequest}.
     * @param form
     *            {@link ClientProfileEmailForm}.
     * @return The new form.
     */
    private ModelAndView handleUpdateEmailSettings(HttpServletRequest request,
            ClientProfileEmailForm form) {
        Map<ClientConfigurationPropertyConstant, String> map;
        map = new HashMap<ClientConfigurationPropertyConstant, String>();
        String emailName = StringUtils.isNotBlank(form.getClientEmailName()) ? form
                .getClientEmailName() : StringUtils.EMPTY;

        map.put(ClientProperty.REPLY_TO_ADDRESS, form.getClientEmail());
        map.put(ClientProperty.REPLY_TO_ADDRESS_NAME, emailName);
        map.put(ClientProperty.SUPPORT_EMAIL_ADDRESS, form.getClientSupportEmailAddress());
        MessageHelper.saveMessage(
                request,
                ResourceBundleManager.instance().getText("client.profile.mail.success",
                        getLocale(request)));
        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateClientConfigurationProperties(map);
        return new ModelAndView(getSuccessView(), getCommandName(), form);
    }

    /**
     * Handle upload client logo.
     *
     * @param request
     *            the request
     * @param errors
     *            the errors
     * @param form
     *            the form backing object
     * @return the model and view
     */
    private ModelAndView handleUploadClientLogo(HttpServletRequest request, BindException errors,
            ClientProfileLogoForm form) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        CommonsMultipartFile cFile = (CommonsMultipartFile) multipartRequest.getFile("file");
        if (cFile == null || cFile.getSize() <= 0) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.change.logo.image.upload.empty.image");
            return null;
        }
        ModelAndView mav = null;
        long maxUploadSize = Long.parseLong(CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties()
                .getProperty(ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE));
        if (cFile.getSize() < maxUploadSize) {
            try {
                byte[] dataLarge = cFile.getBytes();
                ServiceLocator.instance().getService(ConfigurationManagement.class)
                        .updateClientLogo(dataLarge);
                MessageHelper
                        .saveMessageFromKey(request, "client.change.logo.image.upload.success");
                ServiceLocator.findService(ImageManager.class).imageChanged(
                        ClientImageDescriptor.IMAGE_TYPE_NAME,
                        ClientImageProvider.PROVIDER_IDENTIFIER, ClientHelper.getCurrentClientId());
                form.setCustomClientLogo(true);
                mav = new ModelAndView(getSuccessView(), getCommandName(), form);
            } catch (Exception e) {
                LOGGER.error("image upload failed", e);
                String errorMsgKey = getImageUploadExceptionErrorMessageKey(e);
                MessageHelper
                        .saveErrorMessage(request, MessageHelper.getText(request, errorMsgKey));
            }
        } else {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.change.logo.image.upload.filesize.error");
        }
        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
            throws ServletException {
        if (initBinderRequired) {
            // to actually be able to convert Multipart instance to byte[]
            // we have to register a custom editor
            binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
            // now Spring knows how to handle multipart object and convert them
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors)
            throws Exception {
        if (ActionType.EMAIL.equals(action)) {
            ClientProfileEmailForm form = (ClientProfileEmailForm) command;
            ClientConfigurationProperties props = CommunoteRuntime.getInstance()
                    .getConfigurationManager().getClientConfigurationProperties();
            form.setClientEmailName(props.getProperty(ClientProperty.REPLY_TO_ADDRESS_NAME));
            form.setClientEmail(props.getProperty(ClientProperty.REPLY_TO_ADDRESS));
            form.setClientSupportEmailAddress(props
                    .getProperty(ClientProperty.SUPPORT_EMAIL_ADDRESS));
        } else if (ActionType.LOGO.equals(action)) {
            ClientProfileLogoForm form = (ClientProfileLogoForm) command;
            form.setCustomClientLogo(ServiceLocator.findService(ConfigurationManagement.class)
                    .getClientLogo() != null);
        }
        return super.referenceData(request, command, errors);
    }

    /**
     * Sets the action for the controller instance.
     *
     * @param actionType
     *            the action to set
     */
    public void setAction(ActionType actionType) {
        this.action = actionType;
    }

    /**
     * Set whether the binder initialization should be run.
     *
     * @param initBinderRequired
     *            true if the binder initialization should be run, false otherwise
     */
    public void setInitBinderRequired(boolean initBinderRequired) {
        this.initBinderRequired = initBinderRequired;
    }
}
