/**
 * @class Special widget that provides means to submit HTML forms that are embedded inside the HTML
 *        content of the widget. These submits are treated as a special kind of widget refreh.
 * @extends C_Widget
 */
var C_FormWidget = new Class( /** @lends C_FormWidget.prototype */
{

    Extends: C_Widget,

    serializedForm: '',

    /** flag for differentiating refreshs by cause */
    refreshCausedBySubmit: false,

    getWidgetForm: function() {
        return this.domNode.getElement('form');
    },

    getWidgetForms: function() {
        var forms = this.domNode.getElements('form');
        var foundForms = [];
        var counter = 0;
        for ( var i = 0; i < forms.length; i++) {
            var f = forms[i];
            /** only transform and return those which should be exposed to the widget */
            if (!f.getProperty("not_exposed_to_spring_ts_widget")
                    && !f.getProperty("not_exposed_to_widget")) {
                if (counter == 0) {
                    f.setAttribute('id', this.getWidgetId() + '_form');
                } else {
                    f.setAttribute('id', this.getWidgetId() + '_form' + counter);
                }
                foundForms.push(f);
                counter++;
            }
        }
        return foundForms;
    },
    /** connect onFormSubmit handler to submit action of form */
    prepareForms: function() {
        var forms = this.getWidgetForms();
        for ( var i = 0; i < forms.length; i++) {
            var form = forms[i];
            /** register on send header */
            form.setAttribute('onSubmit', "return E2('onFormSubmit', '" + this.widgetId
                    + "', this);");

            /** find all submitting elements (submit-inputs and buttons) */
            var arrSubmitter = form.getElements('input[type=submit]');
            // add buttons with type submit (explicitly set or default)
            // note: IE sets type='button' as default when no type is provided
            var btnElems = form.getElements('button');
            for ( var j = 0; j < btnElems.length; j++) {
                var typeProp = btnElems[j].getProperty('type');
                if (!typeProp || typeProp == 'submit') {
                    arrSubmitter.push(btnElems[j]);
                }
            }
            // TODO uaah this is ugly. Just do not support onclick.
            arrSubmitter.each(function(objSubmit) {
                var attribute = objSubmit.getAttribute('onclick');
                if (attribute != null) {
                    attribute = attribute.toString();

                    if (Browser.name === 'ie') {
                        /**
                         * fix for ie bug -
                         * http://tobielangel.com/2007/1/11/attribute-nightmare-in-ie
                         */
                        attribute = attribute.replace("function anonymous()\n{\n", "");
                        /** fix for IE8 - value is function onclick... */
                        attribute = attribute.replace("function onclick()\n{\n", "");
                        if (attribute.lastIndexOf("\n}") == attribute.length - 2) {
                            attribute = attribute.substring(0, attribute.lastIndexOf("\n}"));
                        }
                    }
                }
                if (attribute != null) {
                    if (attribute.match(/;$/) == null) {
                        attribute += ";";
                    }
                }
                /** set the updated onclick event handler - setAttribute does not work on IE7 */
                objSubmit.onclick = function() {
                    if (attribute)
                        eval(attribute);
                    return E2('onFormSubmitButtonClick', this.widgetId, this);
                };
            });
        }
    },

    /**
     * Is called when a submit succeeded (i.e. applicationSuccess in response and true). Directly
     * invoked after 'onWidgetRefreshComplete' event. Default implementation does nothing.
     */
    onSubmitSuccess: function() {
    },

    /**
     * Is called when a submit failed (i.e. applicationSuccess in response and false). Directly
     * invoked after 'onWidgetRefreshComplete' event. Default implementation does nothing.
     */
    onSubmitFailure: function() {
    },

    /**
     * @override
     * @ignore
     */
    refreshComplete: function(responseMetadata) {

        if (this.refreshCausedBySubmit) {
            /** reset submit flag */
            this.refreshCausedBySubmit = false;

            this.prepareForms();
            /** load existing sub widgets */
            // TODO this is not a good idea because when submitting this widget the sub widgets are lost
            this.widgetController.findWidgets(this.domNode);
            if (responseMetadata.applicationSuccess) {
                this.onSubmitSuccess();
            } else {
                this.onSubmitFailure();
            }
        } else {
            this.prepareForms();
            /** load existing sub widgets */
            // TODO this is not a good idea because when submitting this widget the sub widgets are lost
            this.widgetController.findWidgets(this.domNode);
        }

    },

    getListeningEvents: function() {

        return [ 'onFormSubmit', 'onFormSubmitButtonClick' ];
    },

    onFormSubmit: function(params) {
        var objForm = null;
        var replaceHtmlWithResponce = true;
        if (typeOf(params) == 'array') {
            objForm = params[0];
            replaceHtmlWithResponce = params[1];
        } else {
            objForm = params || this.getWidgetForm();
        }

        /** update only "own" form */
        if (objForm.getAttribute('id').indexOf(this.getWidgetId() + '_form') < 0) {
            return false;
        }

        /** flag to differentiate submit types */
        this.refreshCausedBySubmit = true;
        /** register values */
        this.serializedForm = objForm.toQueryString();

        /** submit */
        return this.submit(replaceHtmlWithResponce);
    },

    onFormSubmitButtonClick: function(objSubmit) {
        var objForm = objSubmit.form;

        /** update only "own" form */
        if (objForm.getAttribute('id').indexOf(this.getWidgetId() + '_form') < 0) {
            return false;
        }
        /** flag to differentiate submit types */
        this.refreshCausedBySubmit = true;
        /** register values and consider pressed submit button */
        this.serializedForm = objForm.toQueryString() + "&" + objSubmit.name + "="
                + objSubmit.value;

        /** refresh widget = submit form */
        return this.submit(true);
    },

    /* submit me */
    submit: function(replaceHtmlWithResponce) {
        if (replaceHtmlWithResponce == null) {
            replaceHtmlWithResponce = true;
        }
        this.widgetController.refreshWidgetByMethod(this, true, replaceHtmlWithResponce);
    },

    getQueryString: function(isSubmit) {
        // TODO this is awkward
        if (isSubmit) {
            return this.serializedForm;
        } else {
            return this.parent();
        }
    }
});

var SpringTSWidget = C_FormWidget;
