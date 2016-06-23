package com.communote.server.web.fe.portal.user.system.application;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.validation.Errors;

import com.communote.server.web.commons.controller.GenericValidator;


/**
 * Validator for the {@link ServerController}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ServerValidator extends GenericValidator<ServerForm> {
    /**
     * {@inheritDoc}
     */
    @Override
    public void doValidate(ServerForm form, Errors errors) {
        int httpPort = NumberUtils.toInt(form.getHttpPort(), 0);
        int httpsPort = NumberUtils.toInt(form.getHttpsPort(), 0);
        if (form.getHttpsEnabled() && httpPort != 0 && httpPort == httpsPort) {
            errors.rejectValue("httpPort", "client.system.application.server.error.ports");
            errors.rejectValue("httpsPort", "client.system.application.server.error.ports");
        } else {
            if (httpPort < 1 || httpPort > 65535) {
                errors.rejectValue("httpPort", "string.validation.numbers.port");
            }
            if (form.getHttpsEnabled() && httpsPort < 1 || httpsPort > 65535) {
                errors.rejectValue("httpsPort", "string.validation.numbers.port");
            }
        }
        if (StringUtils.isBlank(form.getHostname())) {
            errors.rejectValue("hostname", "string.validation.empty");
        }
    }
}
