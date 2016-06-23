package com.communote.server.widgets.springmvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.widgets.FormWidget;
import com.communote.server.widgets.WidgetController;

/**
 * FormWidget that supports data binding and validation as done by spring's form controllers.
 * 
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class SpringFormWidget<T> extends FormWidget {

    private Validator validator;

    /**
     * Bind request parameters to the form backing object.
     * 
     * @param request
     *            the current request
     * @param formBackingObject
     *            the backing object to fill
     * @return the binder
     */
    private ServletRequestDataBinder bind(HttpServletRequest request, T formBackingObject) {
        ServletRequestDataBinder binder = createBinder(request, formBackingObject);
        binder.bind(request);
        prepareBackingObjectOnSubmit(request, formBackingObject);
        return binder;
    }

    /**
     * Create a binder for the given request and form backing object. The name of the target object
     * will be {@value WidgetController#OBJECT_SINGLE}
     * 
     * @param request
     *            the current request
     * @param formBackingObject
     *            the backing object
     * @return the prepared binder
     */
    protected ServletRequestDataBinder createBinder(HttpServletRequest request, T formBackingObject) {
        ServletRequestDataBinder binder = new ServletRequestDataBinder(formBackingObject,
                WidgetController.OBJECT_SINGLE);
        initBinder(request, binder);
        return binder;
    }

    /**
     * Create the form backing object. The returned object can be a pre-initialized or empty object.
     * In case an empty one is returned it can be filled later within
     * {@link #prepareBackingObjectOnRefresh(HttpServletRequest, Object)} or
     * {@link #prepareBackingObjectOnSubmit(HttpServletRequest, Object)}.
     * 
     * @param request
     *            the current request
     * @return the backing object
     */
    protected abstract T formBackingObject(HttpServletRequest request);

    @Override
    public ModelAndView handleRefresh() {
        HttpServletRequest request = getRequest();
        T formBackingObject = formBackingObject(request);
        prepareBackingObjectOnRefresh(request, formBackingObject);
        // create binder but do not bind
        ServletRequestDataBinder binder = createBinder(request, formBackingObject);
        return new ModelAndView(getViewIdentifier(), binder.getBindingResult().getModel());
    }

    @Override
    public ModelAndView handleSubmit() {
        HttpServletRequest request = getRequest();
        T formBackingObject = formBackingObject(request);
        ServletRequestDataBinder binder = bind(request, formBackingObject);
        BindingResult bindingResult = binder.getBindingResult();
        if (!suppressValidation(request, formBackingObject)) {
            this.validateForm(formBackingObject, bindingResult);
        }
        processFormSubmission(request, formBackingObject, bindingResult);
        if (bindingResult.hasErrors()) {
            setSuccess(false);
        }
        return new ModelAndView(getViewIdentifier(), bindingResult.getModel());
    }

    /**
     * Called by {@link #createBinder(HttpServletRequest, T)} to init the binder. This allows
     * registering custom editors for certain fields of the backing object class.
     * 
     * @param request
     *            the current request
     * @param binder
     *            the binder
     * @see org.springframework.validation.DataBinder#registerCustomEditor
     */
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        // nothing
    }

    /**
     * Do the actual submit operation, like updating database values. This method is only called if
     * the validation did not find any errors.
     * 
     * @param request
     *            the current request
     * @param formBackingObject
     *            the form backing object
     * @param errors
     *            for adding errors that occur during the submit operation
     */
    protected abstract void onSubmit(HttpServletRequest request, T formBackingObject,
            BindingResult errors);

    /**
     * Called when the form widget is refreshing to allow initialization of the backing object if
     * not already done in {@link #formBackingObject(HttpServletRequest)}. The default
     * implementation does nothing.
     * 
     * @param request
     *            the current request
     * @param formBackingObject
     *            the backing object
     */
    protected void prepareBackingObjectOnRefresh(HttpServletRequest request, T formBackingObject) {
    }

    /**
     * Called during submit of a form widget after the binder applied the request parameters to the
     * backing object, but before calling the validator. The default implementation does nothing.
     * 
     * @param request
     *            the current request
     * @param formBackingObject
     *            the backing object
     */
    protected void prepareBackingObjectOnSubmit(HttpServletRequest request, T formBackingObject) {
    }

    protected void processFormSubmission(HttpServletRequest request, T formBackingObject,
            BindingResult errors) {
        if (!errors.hasErrors()) {
            onSubmit(request, formBackingObject, errors);
        }
    }

    /**
     * Set the validator to validate the submitted form backing object.
     * 
     * @param validator
     *            the validator to use. If null no validation will be done.
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * Whether to suppress validation for the current submit
     * 
     * @param request
     *            the current request
     * @param formBackingObject
     *            the backing object that will be validated
     * @return false
     */
    protected boolean suppressValidation(HttpServletRequest request, T formBackingObject) {
        return false;
    }

    protected void validateForm(T formBackingObject, BindingResult errors) {
        if (this.validator != null) {
            ValidationUtils.invokeValidator(this.validator, formBackingObject, errors);
        }
    }
}
