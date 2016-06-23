package com.communote.server.web.fe.portal.user.system.content;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.commons.resolver.CommunoteMultipartResolver;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class FileUploadController extends BaseFormController {
    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        String attachmentSize = ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE.getValue();
        String imageSize = ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE.getValue();
        Long attachmentSizeAsLong = NumberUtils.toLong(attachmentSize, 10485760) / 1024;
        Long imageSizeAsLong = NumberUtils.toLong(imageSize, 1048576) / 1024;
        return new FileUploadForm(attachmentSizeAsLong.toString(), imageSizeAsLong.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        FileUploadForm form = (FileUploadForm) command;
        Long attachmentSize = NumberUtils.toLong(form.getAttachmentSize(), 0);
        Long imageSize = NumberUtils.toLong(form.getImageSize(), 0);
        Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationProperty.IMAGE_MAX_UPLOAD_SIZE, Long.toString(imageSize * 1024));
        settings.put(ApplicationProperty.ATTACHMENT_MAX_UPLOAD_SIZE,
                Long.toString(attachmentSize * 1024));
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
            .updateApplicationConfigurationProperties(settings);
            // set upper limit of moth as limit handled by MaxUploadSizeExceededExceptionResolver
            ((CommunoteMultipartResolver) WebServiceLocator.instance().getWebApplicationContext()
                    .getBean("multipartResolver")).setMaxUploadSize(Math.max(attachmentSize,
                    imageSize) * 1024);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.content.file.upload.error");
            return new ModelAndView(getSuccessView(), "command", command);
        }
        MessageHelper.saveMessageFromKey(request, "client.system.content.file.upload.success");
        return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
    }
}
