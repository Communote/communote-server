package com.communote.server.web.fe.portal.user.client.controller.security;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AuthenticationController extends BaseFormController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AuthenticationForm authenticationForm = new AuthenticationForm();
        authenticationForm
        .setFailedAttemptsBeforeTemporaryLock(ClientPropertySecurity.FAILED_AUTH_STEPS_TEMPLOCK.getValue(Integer
                        .toString(ClientPropertySecurity.DEFAULT_FAILED_AUTH_STEPS_TEMPLOCK)));
        authenticationForm
        .setFailedAttemptsBeforePermanentLock(ClientPropertySecurity.FAILED_AUTH_LIMIT_PERMLOCK.getValue(Integer
                        .toString(ClientPropertySecurity.DEFAULT_FAILED_AUTH_LIMIT_PERMLOCK)));
        authenticationForm.setRiskLevel(ClientPropertySecurity.FAILED_AUTH_STEPS_RISK_LEVEL
                .getValue(Integer
                        .toString(ClientPropertySecurity.DEFAULT_FAILED_AUTH_STEPS_RISK_LEVEL)));
        authenticationForm.setLockInterval(ClientPropertySecurity.FAILED_AUTH_LOCKED_TIMESPAN
                .getValue(Integer
                        .toString(ClientPropertySecurity.DEFAULT_FAILED_AUTH_LOCKED_TIMESPAN)));
        return authenticationForm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        AuthenticationForm form = (AuthenticationForm) command;
        Map<ClientConfigurationPropertyConstant, String> settings = new HashMap<ClientConfigurationPropertyConstant, String>();
        settings.put(ClientPropertySecurity.FAILED_AUTH_STEPS_TEMPLOCK,
                form.getFailedAttemptsBeforeTemporaryLock());
        settings.put(ClientPropertySecurity.FAILED_AUTH_LIMIT_PERMLOCK,
                form.getFailedAttemptsBeforePermanentLock());
        settings.put(ClientPropertySecurity.FAILED_AUTH_STEPS_RISK_LEVEL, form.getRiskLevel());
        settings.put(ClientPropertySecurity.FAILED_AUTH_LOCKED_TIMESPAN, form.getLockInterval());
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
            .updateClientConfigurationProperties(settings);
        } catch (Exception e) {
            MessageHelper.saveErrorMessageFromKey(request, "client.system.settings.error");
            return new ModelAndView(getSuccessView(), "command", form);
        }
        MessageHelper.saveMessageFromKey(request, "client.system.settings.success");
        return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
    }
}
