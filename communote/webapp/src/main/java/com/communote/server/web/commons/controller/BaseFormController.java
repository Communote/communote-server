package com.communote.server.web.commons.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Basic class for Communote controllers with forms. One task is to replace the MODULE in a tiles
 * defs name with the current active module.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class BaseFormController extends SimpleFormController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFormController.class);
    private boolean refuseOnExternalAuthentication = false;

    /**
     * @param request
     *            the request
     * @return the locale of the current user. will never be null
     */
    protected Locale getLocale(HttpServletRequest request) {
        return SessionHandler.instance().getCurrentLocale(request);
    }

    /**
     * Is called by
     * {@link BaseFormController#onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)}
     * <br>
     * Submit callback with all parameters. Called in case of submit without errors reported by the
     * registered validator, or on every submit if no validator.
     * <p>
     * The default implementation delegates to {@link #onSubmit(Object, BindException)}. For simply
     * performing a submit action and rendering the specified success view, consider implementing
     * {@link #doSubmitAction} rather than an <code>onSubmit</code> variant.
     * <p>
     * Subclasses can override this to provide custom submission handling like storing the object to
     * the database. Implementations can also perform custom validation and call showForm to return
     * to the form. Do <i>not</i> implement multiple onSubmit methods: In that case, just this
     * method will be called by the controller.
     * <p>
     * Call <code>errors.getModel()</code> to populate the ModelAndView model with the command and
     * the Errors instance, under the specified command name, as expected by the "spring:bind" tag.
     *
     * @param request
     *            current servlet request
     * @param response
     *            current servlet response
     * @param command
     *            form object with request parameters bound onto it
     * @param errors
     *            Errors instance without errors (subclass can add errors if it wants to)
     * @return the prepared model and view, or <code>null</code>
     * @throws Exception
     *             in case of errors
     * @see #onSubmit(Object, BindException)
     * @see #doSubmitAction
     * @see #showForm
     * @see org.springframework.validation.Errors
     * @see org.springframework.validation.BindException#getModel
     */
    protected abstract ModelAndView handleOnSubmit(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors) throws Exception;

    /**
     * Override to disable autogrowth.
     *
     * {@inheritDoc}
     */
    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
            throws Exception {
        binder.setAutoGrowNestedPaths(false);
        super.initBinder(request, binder);
    }

    /**
     * Returns true if the access to this form is not allowed on activated external authentiaction
     * e.g. LDAP
     *
     * @return the refuseOnExternalAuthentication
     */
    public boolean isRefuseOnExternalAuthentication() {
        return refuseOnExternalAuthentication;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        ModelAndView mav = handleOnSubmit(request, response, command, errors);
        ControllerHelper.replaceModuleInMAV(mav);
        return mav;
    }

    /**
     * {@inheritDoc} <br>
     * Is final to avoid inheriting subclasse. They should use
     * {@link #handleOnSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)}
     * instead.
     */
    @Override
    protected final ModelAndView onSubmit(Object command) throws Exception {
        return super.onSubmit(command);
    }

    /**
     * {@inheritDoc} <br>
     * Is final to avoid inheriting subclasse. They should use
     * {@link #handleOnSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)}
     * instead.
     */
    @Override
    protected final ModelAndView onSubmit(Object command, BindException errors) throws Exception {
        return super.onSubmit(command, errors);
    }

    /**
     * If the form is refused on activated external authentication, the user will be redirected to
     * the portal home page.
     *
     * @param redirectOnExternalAuthentication
     *            the redirectOnExternalAuthentication to set
     */
    public void setRefuseOnExternalAuthentication(boolean redirectOnExternalAuthentication) {
        this.refuseOnExternalAuthentication = redirectOnExternalAuthentication;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.web.servlet.mvc.SimpleFormController#showForm(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response,
            BindException errors) throws Exception {
        ModelAndView result = null;

        if (errors.hasErrors()) {
            MessageHelper.saveErrorMessageFromKey(request, "form.error.hasFieldErrors");
        }

        // redirect to portal home page, if the current operation is not allowed with activated
        // external authentication
        if (isRefuseOnExternalAuthentication()
                && CommunoteRuntime.getInstance().getConfigurationManager()
                        .getClientConfigurationProperties().isExternalAuthenticationActivated()) {
            LOGGER.error("current request not allowed with activated ldap authentication: '"
                    + request.getRequestURI() + "'");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            // ControllerHelper.sendRedirect(request, response, ClientUrlHelper
            // .renderUrl("/portal/home.do"));
        } else {
            result = super.showForm(request, response, errors);
            ControllerHelper.replaceModuleInMAV(result);
        }
        return result;
    }
}
