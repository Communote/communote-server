package com.communote.server.web.fe.portal.user.client.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.web.commons.controller.BaseFormController;
import com.communote.server.web.fe.portal.user.client.forms.ClientPermissionsForm;


/**
 * The Class ClientProfileController handles the update client profile use case.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientPermissionsController extends BaseFormController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ClientPermissionsForm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        return new ModelAndView(getSuccessView(), getCommandName(), new ClientPermissionsForm());
    }
}
