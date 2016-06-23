package com.communote.server.web.fe.portal.user.system.content;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class FileStorageController extends BaseFormController {
    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new FileStorageForm(
                ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        FileStorageForm form = (FileStorageForm) command;
        Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT, form.getPath());
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
            .updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.content.file.storage.error");
            return new ModelAndView(getSuccessView(), "command", command);
        }
        MessageHelper.saveMessageFromKey(request, "client.system.content.file.storage.success");
        return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
    }

}
