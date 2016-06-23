/**
 * The Basic Control Class, every Control extends this class it provides: - event registration -
 * rendering - data loading - event binding - error handling - popup messages (inline and floating) -
 * loading indicator
 * @class
 * @name communote.widget.classes.controls.Control
 */
communote.widget.classes.controls.Control = communote.Base.extend(
/** 
 * @lends communote.widget.classes.controls.Control.prototype
 */	
{
    rendered: false, // if rendering cycle has passed
    errorText: 'htmlclient.control.error.noDataToDisplay', // simple error label (will be passed to 'getLabel')
    // TODO fgr: please add a comment what this attribute is good for!
    // noContainer = true will prevent the Control to surround itself with an additional <div> tag 
    //(containerTag can be configured per control) see also: addTemplateToContainer
    // a surrounding container should prevent pure to override the container it renders to.. but actually it doesnt matters
    // because the container is set to new for every render.. also reRender
    noContainer: false,
    // on true, control will igonore rendering cycle
    noRender: false,
    filterParametersToIgnore: null,
    css: [],
    moreNotesAvailable: false,

    /**
     * all events that are registered through listenTo() are listed here so that they can be
     * unregistered on deconstruction
     */
    registeredListeners: [],

    /**
     * This will init the control, set a bunch of attribute and link it with the widget config may
     * look like ( everything beside slot and type is optional ) config = { slot = 'cssSelector',
     * type = 'ClassName', template = 'myTemplateKey', will be used downcase as filename name =
     * 'MyName', css = ['cssClass', 'cssClass'], will be merged with class default styles data = {}
     * object will be available in class by this.configData noContainer = boolean override the
     * default noContainer value parent = ControlObject will set the parent of the control
     * controls = [] a set of subcontrol_configs_ only relevant for ContainerControl }
     * 
     * it will also call registerListeners,
     */
    constructor: function(id, config, widget) {
        var $ = communote.jQuery;

        this.registeredListeners = []; // this line is essential! see KENMEI-4130

        this.id = id;
        this.type = config.type;
        this.name = config.name || config.type;
        this.channel = 'ch' + widget.id + '_' + id;
        this.widget = widget;
        this.widgetDomNode = this.widget.getDomNode();
        this['is' + config.type] = true;
        this.template = config.template || this.template || config.type;
        if (!$.isArray(this.css)) {
            this.css = [];
        } else {
            this.css = $.merge([], this.css); // create new array, otherwise all classes would share the same array
        }
        if (config.css && $.isArray(config.css)) {
            this.css = $.merge(this.css, config.css);
        }
        if (config.noContainer != undefined){this.noContainer = config.noContainer;}
        this.nodeSelector = config.slot;
        this.configData = config.data || {};
        this.config = config;
        this.parent = config.parent;
        this.controller = widget.controller;
        if (!this.isContainerControl){this.registerListeners();}
        this.enabled = true; // REFACTOR: i think this is not really used right now
    },

    /**
     * Returns an array filter parameter names that should be ignored when creating a query string
     * to fetch the latest data to refresh the view. By default, this method returns the member
     * filterParametersToIgnore which is set to null. Subclasses usually only have to override this
     * member within the constructor.
     * 
     * This method will only be evaluated if #includeFilterParameters returns true.
     * 
     * @returns {Array} an array filter parameter names to ignore or null if no parameters should be
     *          ignored
     */
    getFilterParametersToIgnore: function() {
        return this.filterParametersToIgnore;
    },

    /**
     * Returns whether to include the shared filter parameters when creating a query string to fetch
     * the latest data to refresh the controls view. By default, this method returns false.
     * 
     * @returns {boolean} true if the currently set filter parameters should be included false
     *          otherwise
     */
    // TODO better name
    includeFilterParameters: function() {
        return false;
    },

    /**
     * All event registrations should be done here, but it is possible to do anywhere..
     */
    registerListeners: function() {
        this.listenTo('dataReady', this.channel, 'insertValues');
        this.listenTo('renderingDone', this.channel, 'bindEvents');
        this.listenTo('error', this.channel, 'errorValue');
        this.listenTo('success');
    },

    /**
     * @param {string} label - the language property key
     * @param {object} args - {0: 'awesome'}
     * @return {string} - a localized text REFACTOR: should be named similar to the method in the
     *         i18n controller e.g. getLocalizedText
     */
    getLabel: function(label, args) {
        if (!args) {
            return this.controller.I18nController.getText(label);
        } else {
            return this.controller.I18nController.getTextFormatted(label, args);
        }
    },

    /**
     * Returns the css class name for the container of the control
     */
    getContainerClass: function() {
        return 'cntwContainer' + this.name;
    },

    /**
     * Returns the id string for the control
     */
    getDomId: function() {
        if (!this.domId) {
            this.domId = 'cntw' + this.widget.id + '_' + this.id;
        }
        return this.domId;
    },

    /**
     * attach the DomId the the passed domNode
     * 
     * @param {jQueryObject} domNode - the domNode which will be enhanced with the domId
     */
    setIdAttribute: function(domNode) {
        var id = domNode.attr('id');
        if ((id == undefined) || id === "") {
            this.domId = this.getDomId();
            domNode.attr('id', this.domId);
        }
    },

    /**
     * returns the active jQueryNode of the control
     * 
     * @return {jQueryObject}
     */
    getDomNode: function() {
        var parentComponent;
        if (!this.domNode || (this.domNode.attr('domNodeId') != this.getDomId())) {
            parentComponent = this.parent || this.widget;
            this.domNode = parentComponent.getDomNode().find('#' + this.getDomId());
            this.domNode.attr('domNodeId', this.getDomId());
        }
        return this.domNode;
    },

    /**
     * returns an key-value-object with additional filter values for ajax request
     * 
     * @return {object}
     */
    getRenderParameters: function() {
        return {};
    },

    /**
     * start the rendering cycle for this control this will get the template, trigger the creation
     * of the container and start the dataLoading process
     */
    render: function() {
        if (this.enabled && !this.noRender && !this.rendered) {
            var template;
            // 'no template' is a configuration to not use an template
            template = (this.template != 'no template') ? this.controller.TemplateController
                    .getTemplate(this.template, this) : '';
            this.addTemplatePrefix();
            this.addTemplateToContainer(template);
            if (this.template !== "Base") {
                this.startWorking();
            }
            this.loadData();
        }
    },

    /**
     * This method is called before the rendering starts.This makes it possible to write an object
     * before the content. This method is used by the note-class.
     */

    addTemplatePrefix: function() {

    },

    /**
     * this will set (and may create) the container of the control add the template to this
     * container and set the domId as attribute to this container
     * 
     * @param {string} template - the template which will be used
     * 
     * REFACTOR: * move the template loading in this function from render() * split the container
     * creation in an other function
     */
    addTemplateToContainer: function(template) {
        var tag, newNode;
        var $ = communote.jQuery;
        var self = this;
        // the baseContainer has no parent - it will use the widget domNode as parent
        var parentComponent = this.parent || this.widget;
        var parentDomNode = parentComponent.getDomNode();
        var parentContainer = parentDomNode.find(this.nodeSelector).first();

        if (!this.noContainer) {
            this.container = this.getDomNode();
            if (this.container.length <= 0) {
                tag = this.containerTag || 'div';
                newNode = $('<' + tag + ' class="' + this.getContainerClass() + '"></' + tag + '>',
                        parentContainer[0].ownerDocument);
                parentContainer.append(newNode);
                this.container = newNode;
            }
        } else {
            //if noContainer is true, the selector/slot will be used as a container
            this.container = parentContainer;
        }

        this.container.append(template);
        this.setIdAttribute(this.container);
        $.each(this.css, function(index, cssClass) {
            self.getDomNode().addClass(cssClass);
        });
    },

    /**
     * will force the container to rerender this will remove everything inside the domNode and start
     * the usual rendering (including dataloading)
     */
    reRender: function() {
        this.getDomNode().children().remove();
        this.rendered = false;
        this.onError = false;
        this.startWorking();
        this.render();
    },

    /**
     * this will trigger the api to load data for this control - the api will get the whol contorl
     * and will look up for the resource attribute (and depending on the loader also additional
     * stuff) onError it will push a error event back to the control
     * 
     * if this control has no 'resource' attribute, the dataloading step will be skipped
     */
    loadData: function() {
        if (this.resource) {
            this.controller.ApiController.getData(this);
        } else {
            if (this.getDomNode().length > 0) {
                this.insertValues({});
            }
        }
    },

    /**
     * this will insert all data and labels to the template/domNode this function uses pure to
     * enhance the domNode with a directive and the data
     * 
     * @param {object} - the dataobject that will be passed to pure (after it got enhanced by
     *            parseData)
     * @param {boolean} [skipRenderingDone] - this will prevent the function to trigger the
     *            renderingDone() if this is triggered, it has to be called manually
     */
    insertValues: function(data, skipRenderingDone) {
        var domNode = this.getDomNode();
        var directives = this.getDirectives();
        data = data || {};
        this.parseData(data);
        if (domNode.length > 0) {
            if (data && directives) {
                try {
                    domNode.render(data, directives);
                } catch (e) {
                    // this.log('catched error: ', e);
                    var msg = communote.utils.printError("rendering error", e);
                    this.fireEvent('error', this.channel, {
                        message: 'rendering error<br />' + communote.utils.nl2br(msg),
                        type: 'error',
                        error: e
                    });
                }
                this.domNode = undefined; // force for domNode update
                //REFACTOR: right now the domNode will be selected everytime again, it is 
                //          not cached anymore this might slow down everything and should
                //          be redone => see getDomNode()
            }
        }
        if (!skipRenderingDone){this.renderingDone();}
    },

    /**
     * this function is called before the data passed to insertValues() is passed to pure, it allows
     * to make the directive a lot 'better looking'
     * 
     * @param {object} data - the object that should be enhanced
     * @return undefined - because data is modified as a reference
     */
    parseData: function(data) {
    },

    /**
     * the final step of the rendering cycle it will fire the Event 'renderingDone', which will
     * trigger the 'bindEvents()'
     * 
     * if the parent is allready finished with rendering ( that should be only possible after the
     * initial rendering, on a rerender of this control or sth similar) it also fires a SizeChanged
     * to resize the widgetframe
     */
    renderingDone: function() {
        if (!this.rendered) {
            this.rendered = true;
            // this has to be evaluated before the renderDone is fired, because the event 
            // triggers the 'renderingDone' of the parent
            this.controller.EventController.fireEvent('renderingDone', this.channel, {}, this);
            if (!this.parent || (this.parent && this.parent.rendered)) {
                this.controller.EventController.fireEvent('sizeChanged', this.widget.channel, {},
                        this);
            }
            this.endWorking();
        }
    },

    /**
     * this will move the widget to a 'working' state mainly this will trigger the workingIndicator,
     * which avoid the user see any 'moving content'
     * 
     * if the parent of this control already works, the indicator will not be created
     */
    startWorking: function() {
        if (!this.working) {
            this.working = true;
            if (!this.parent || (this.parent && !this.parent.working)) {
                this.showWorkingIndicator();
            }
        }
    },

    /**
     * this will move the widget to a 'non-working' state this will also hide the working indicator
     */
    endWorking: function() {
        if (this.working) {
            this.working = false;
            if (!this.parent || (this.parent && !this.parent.working)) {
                this.hideWorkingIndicator();
            }
        }
    },

    /**
     * This will insert a working indicator to the domnode of the control it will be a huge white
     * plain, that covers the whol control and show a loading icon
     * 
     * it also shows the amount of 'to load'controls and already loaded ones the indicator is
     * position absolute floating over the control
     */
    showWorkingIndicator: function() {
        var $ = communote.jQuery;
        var node = this.getDomNode();
        var indicator = $('<div></div>', node[0].ownerDocument);
        indicator.addClass('cntwWorkingIndicator');
        node.append(indicator);
    },

    /**
     * hides the working indicator
     */
    hideWorkingIndicator: function() {
        var node = this.getDomNode();
        var indicator = node.parent().find('.cntwWorkingIndicator');
        indicator.remove();
    },

    /**
     * REFACTOR: rename it to 'onError' or 'showError' or sth similar this function displays a erro
     * message next to the control for complete detail, please look up showMessage() it will force
     * the message.type = 'error'
     * 
     * after showing this message, it will call renderingDone REFACTOR: maybe cancel all
     * subcontrols?
     * 
     * @param {object} errorObj - a complex 'message Object'
     */
    errorValue: function(obj) {
        if (this.onError) {
            return;
        }
        this.onError = true;
        obj.type = 'error';
        obj.isRemoveable = !(obj.isRemoveable === false) ? true : false;
        this.showMessage(obj);
        this.renderingDone();
    },

    /**
     * similar to errorValue, it will use showMessage() to display a message and force the type to
     * 'success'
     */
    success: function(obj) {
        if(!obj.type){
            obj.type = 'success';
        }
        this.showMessage(obj);
    },

    /**
     * returns the domNode where the message should appear in this control can be override by
     * extending controls to change this (dont call this.base() ;D )
     */
    getMessageNode: function() {
        return this.getDomNode();
    },

    /**
     * This will show a message inside the control (error, info or success) - message it may be
     * inline or floating (positioned absolute) - it may be disappear after a few seconds or has be
     * removed by click on the message - message may be interpreted as a label (and will be
     * localized)
     * 
     * @param {object} obj - the message object may look like this: obj = { message: string, // the
     *            message type: string, // error|success (inserted as css class) isLabel: boolean, //
     *            will force the message to be used as property key isRemoveable: boolean, //
     *            disappear the message after a time, or removed by click isInline: boolean, // make
     *            the message appear inline or float it centered submitText: string, // text for
     *            submit button submitClass: string, // css class for submit button (used as
     *            identifier) cancelText: string // text for cancel button (remove the request by
     *            click) }
     */
    showMessage: function(obj) {
        var $, self, node, isInline, type, infoBox, msgBox, txtBox, offset, doc;
        var msg = obj.isLabel || false ? this.getLabel(obj.message) : obj.message;
        if ((msg != undefined) && (msg.length > 0)) {
            self = this;
            $ = communote.jQuery;
            node = this.getMessageNode();
            doc = node[0].ownerDocument;
            // all messages have to be inline, if the requirement will change, just change this line
            isInline = !(obj.isInline === false) ? true : false;
            type = obj.type || 'info';
            infoBox = $('<div></div>', doc);
            infoBox.addClass('cntwInfoBox');
            msgBox = $('<div></div>', doc);
            msgBox.addClass(type);
            if (obj.messageClass){msgBox.addClass(obj.messageClass);}
            txtBox = $('<span></span>', doc);
            txtBox.html(msg);
            msgBox.append(txtBox);
            if (obj.submitText || obj.CancelText) {
                buttonBox = $('<div class="cntwButtonBar"></div>', doc);
                if (obj.submitText) {
                    submitBox = $('<div class="cntwSubmitButton"></div>', doc);
                    submitBox.text(obj.submitText);
                    if (obj.submitClass){submitBox.addClass(obj.submitClass);}
                    buttonBox.append(submitBox);
                }
                if (obj.cancelText) {
                    cancelBox = $('<div class="cntwCancelButton"></div>', doc);
                    cancelBox.text(obj.cancelText);
                    buttonBox.append(cancelBox);
                }
                if (obj.cancelText && obj.submitText) {
                    cancelBox.addClass("cntwFirst");
                    submitBox.addClass("cntwLast");
                }
                msgBox.append(buttonBox);
            }
            infoBox.append(msgBox);
            node.append(infoBox);
            if (isInline) {
                this.fireEvent('sizeChanged', this.widget.channel);
                infoBox.css('position', 'static');
            } else {
                offset = node.offset() || {};
                offset.top = offset.top + (node.outerHeight() - infoBox.outerHeight()) / 2;
                offset.left = offset.left + (node.outerWidth() - infoBox.outerWidth()) / 2;
                infoBox.css('max-width', node.innerWidth() + 'px');
                infoBox.css('width', (node.innerWidth() - 2 * offset.left) + 'px');
                infoBox.css(offset);
                infoBox.addClass('cntwFloatingBox');
            }
            var removeBox = function() {
                communote.jQuery(infoBox).fadeOut("fast", function() {infoBox.remove();} );
                if (type == 'error') {
                    self.onError = false;
                }
                self.fireEvent('sizeChanged', self.widget.channel);
            };
            if (obj.isRemoveable && !obj.cancelText) {
                msgBox.click(removeBox);
            } else if (obj.cancelText) {
                 cancelBox.click(removeBox);
            } else {
                setTimeout(removeBox, 5000);
            }
            infoBox.show();
        }
        return infoBox;
    },

    /**
     * Returns a directive the will be used in insertValues() by pure
     */
    getDirectives: function() {
        return null;
    },

    /**
     * In this function all 'event binding' should be done, if done earlier in the lifecycle the
     * events on domnode may be killed by pure
     */
    bindEvents: function() {
    },

    /**
     * Shortcut to EventController.registerListener
     * 
     * @param {string} eventName - the event for which the control register
     * @param {string} [channel] - the channel on which the event have to be fired
     * @param {string} [listenerFunction] - the function that should be called, when the event got
     *            triggered ( by default the eventName is used )
     * 
     * @return undefined
     */
    listenTo: function(eventName, channel, listenerFunction) {
        if (!this.controller) {
            return;
        }
        if (channel === undefined) {
            channel = this.channel;
        }

        // append to local copy
        var listener = {
            eventName: eventName,
            channel: channel,
            listener: this
        };
        this.registeredListeners[this.registeredListeners.length] = listener;

        // register event
        this.controller.EventController
                .registerListener(eventName, channel, this, listenerFunction);
    },

    /**
     * Shortcut to EventController.fireEvent
     * 
     * @param {string} eventName - the event that will be fired
     * @param {string} [channel] - the channel on which the event will be fired
     * @param {object} [data] - the data that will be passed by the event
     * 
     * @return undefined
     */
    fireEvent: function(eventName, channel, data) {
        if (!this.controller) {
            return;
        }
        if (channel === undefined) {
            channel = this.channel;
        }
        if (data === undefined) {
            data = {};
        }

        this.controller.EventController.fireEvent(eventName, channel, data, this);
    },

    /**
     * Unregisters all events that where registered through this object's listenTo method.
     * 
     * Attention: theses are not necessarily all registered events for this Control, because calling
     * the EventManager directly is always possible (not through listenTo).
     * 
     * @return void
     */
    unregisterListeners: function() {

        var listeners = this.registeredListeners;
        for ( var i = 0; i < listeners.length; i++) {

            this.controller.EventController.unregisterListener(listeners[i].eventName,
                    listeners[i].channel, listeners[i].listener);
        }
        this.registeredListeners = [];
    },

    /**
     * Internal method that removes all listeners and the whole channel for this Control.
     */
    beforeDestroy: function() {

        // unregister listeners and deactivate the whole control channel
        this.unregisterListeners();
        this.controller.EventController.unregisterChannel(this.channel);
    },

    /**
     * Destroy method to call when the Control is removed.
     */
    destroy: function() {
        var domNode;
        if (this.parent !== undefined && this.parent.removeSubControl !== undefined) {
            this.parent.removeSubControl(this);
        } else {
            this.beforeDestroy();
        }
        // remove child elements of container node and clear id attribute
        domNode = this.getDomNode();
        domNode.empty();
        domNode.attr('id', '');
    },

    /**
     * Method to call when to destroy only one specific child control of this Control. Calling
     * destroy() on the child results in a call to this method implicitly.
     * 
     * @param subControl {Control} the child control to destroy
     */
    removeSubControl: function(subControl) {
        subControl.beforeDestroy();
    }

});
