/**
 * Widget
 */
communote.widget.classes.Widget = communote.Base.extend(
/** @lends communote.widget.classes.Widget.prototype */	
{
    name: "Widget",
    template: "Base",

    id: "",
    channel: "",
    containerNode: {},
    rootNode: null,
    frameNode: {},
    configuration: {},
    controller: {},
    filterParameterStore: null,
    iFrame: null,
    useIframe: false,
    maxHeight: 0,
    maxHeightReached: false,
    cssProps: [ "margin-top", "margin-bottom", "border-top-width", "border-bottom-width",
                 "padding-top", "padding-bottom" ],
    rootMBP: undefined,
    firstNoteCreationDate: undefined,

    registerListeners: function() {
        var eventController = communote.widget.EventController;
        eventController.registerListener("widgetFrameReady", this.channel, this,
                "renderContent");
        eventController.registerListener("sizeChanged", this.channel, this,
                "resizeFrame");
    },

    /**
     * @constructs
     * @param id
     * @param domNode
     * @param configuration
     * @param controller
     */
    constructor: function(id, domNode, configuration, controller) {
        var singleValueParams, preselectedView, availableViews; 
        var namespace = communote.widget.classes;
        this.id = id;
        this.channel = "ch" + this.id + "_global";
        this.containerNode = domNode;
        this.frameNode = domNode;
        this.configuration = configuration;
        this.controller = controller;
        this.filterParameterStore = this.createFilterParameterStore(configuration);
        singleValueParams = [ 'noteSearchString', 'userSearchString', 'tagPrefix' ];
        this.filterEventHandler = new namespace.FilterEventHandler(this.filterParameterStore,
                singleValueParams, this.channel);
        // check for other configurable initial filters
        availableViews = this.evalPreselectFilterParamValue(configuration.msgShowViews, true);
        if (availableViews) {
            // use defined view as preselected view
            preselectedView = this.evalPreselectFilterParamValue(configuration.msgViewSelected, false);
            if (preselectedView && (communote.jQuery.inArray(preselectedView, availableViews) > -1)) {
                communote.widget.EventController.fireEvent('filterEvent', this.channel, {
                    paramName: 'viewFilter',
                    value: preselectedView
                });
            }
        }
        this.useIframe = configuration.useIframe;
        if (this.useIframe && configuration.maxHeight != undefined) {
            this.maxHeight = parseInt(configuration.maxHeight);
            if (!this.maxHeight || this.maxHeight < 0) {
                this.maxHeight = 0;
            }
        }
    },

    /**
     * 
     * @param config
     * @returns {communote.widget.classes.FilterParameterStore}
     */
    createFilterParameterStore: function(config) {
        var preselectedTopics, i;
        var jQuery = communote.jQuery;
        var closedParams = {};
        var extensibleParams = {};
        var closedParamsFound = false;
        var extensibleParamsFound = false;
        var preselectedTopicsFound = false;
        // check for preselected filter parameters
        var preselected = this.evalPreselectFilterParamValue(config.fiPreselectedTopics, true);
        if (preselected) {
            closedParams['topicAliases'] = preselected;
            closedParamsFound = true;
            preselectedTopicsFound = true;
        } else {
            preselected = this.evalPreselectFilterParamValue(config.fiPreselectedTopicIds, true);
            if (preselected) {
                closedParams['topicIds'] = preselected;
                closedParamsFound = true;
                preselectedTopicsFound = true;
            }
        }
        // check edTopicList for topics and add them to fiPreselectedTopics if defined
        if (preselectedTopicsFound) {
            preselected = this.evalPreselectFilterParamValue(config.edTopicList, true);
            if (preselected) {
                preselectedTopics = closedParams['topicAliases'];
                if (preselectedTopics) {
                    // merge
                    for (i = 0; i < preselected.length; i++) {
                        if (jQuery.inArray(preselected[i], preselectedTopics) < 0) {
                            preselectedTopics.push(preselected[i]);
                        }
                    }
                } else {
                    closedParams['topicAliases'] = preselected;
                }
            }
        }
        preselected = this.evalPreselectFilterParamValue(config.fiPreselectedAuthors, true);
        if (preselected) {
            closedParams['userAliases'] = preselected;
            closedParamsFound = true;
        } else {
            preselected = this.evalPreselectFilterParamValue(config.fiPreselectedAuthorIds, true);
            if (preselected) {
                closedParams['userIds'] = preselected;
                closedParamsFound = true;
            }
        }
        preselected = this.evalPreselectFilterParamValue(config.fiPreselectedNoteId, false);
        if (preselected) {
            closedParams['noteId'] = preselected;
            closedParamsFound = true;
        }
        preselected = this.evalPreselectFilterParamValue(config.fiPreselectedTagIds, true);
        if (preselected) {
            extensibleParams['tagIds'] = preselected;
            extensibleParamsFound = true;
        }
        preselected = this.evalPreselectFilterParamValue(config.fiPreselectedSearch, false);
        if (preselected) {
            extensibleParams['noteSearchString'] = preselected;
            extensibleParamsFound = true;
        }        
        // set propertyFilter as a  predefinedParam, only if switching is disabled
        if (!config.acShow && !config.acShowFilter) {
            extensibleParams['propertyFilter'] = 'value:{["Note","com.communote.plugins.communote-plugin-activity-base","contentTypes.activity","activity","EQUALS","true"]}'
            extensibleParamsFound = true;
        }
        if (!closedParamsFound) {
            closedParams = null;
        }
        if (!extensibleParamsFound) {
            extensibleParams = null;
        }
        var paramStore = new communote.widget.classes.FilterParameterStore(extensibleParams, closedParams,
                [['topicAliases', 'topicIds'], ['userAliases', 'userIds']]);
        // this prevents faulty switching of the filter by the user 
        // if the propertyFilter were a predefinedParam, the filter could not be changed,
        // so it has been added as a regular filter parameter
        if (!config.acShow && config.acShowFilter) {
            paramStore.appendFilterParameter('propertyFilter','value:{["Note","com.communote.plugins.communote-plugin-activity-base","contentTypes.activity","activity","EQUALS","true"]}', false)
        }
        return paramStore;
    },

    evalPreselectFilterParamValue: function(value, split) {
        var result = null;
        if (value !== null) {
            // stringify because of type sensitivity
            value = value.toString();

            if (value.length > 0) {
                if (split) {
                    value = value.split(',');
                    if (value.length > 0) {
                        result = value;
                    }
                } else {
                    result = value;
                }
            }
        }
        return result;
    },

    /**
     * @returns {communote.widget.classes.FilterParameterStore} the filter parameter store
     *          associated with this widget instance
     */
    getFilterParameterStore: function() {
        return this.filterParameterStore;
    },

    /**
     * Shortcut to access the current filter parameters of the associated FilterParmeterStore.
     *
     * @returns {Object} a mapping from filter parameter name to values
     * @see communote.widget.classes.FilterParameterStore.getCurrentFilterParameters()
     */
    getCurrentFilterParameters: function() {
        return this.filterParameterStore.getCurrentFilterParameters();
    },

    getDomNode: function() {
        return this.frameNode;
    },

    loadBaseControl: function() {
        var controls = this.configuration.controls;

        // register not rendered manager-controls
        controls.push({
            slot: undefined,
            type: "NoteManager"
        });

        this.baseContainer = this.controller.ControlFactory.getControl({
            name: 'BaseControl',
            type: 'ContainerControl',
            template: this.template,
            noContainer: true,
            controls: controls
        }, this);
    },

    start: function() {
        this.registerListeners();
        this.loadBaseControl();
        this.render();
    },

    render: function() {
        var iFrame, iFrameHtml, url, doc, docIframe;
        var self = this;
        var $ = communote.jQuery;
        var containerNode = this.containerNode;
        if (this.useIframe) {
            iFrame = $('<iframe></iframe>');
            iFrame.attr("frameborder", "0");
            iFrame.css('margin', '0px');
            iFrame.css('border', '0px none');
            iFrame.css('width', '100%');
            iFrame.css('height', '190px');
            //iFrame.css('min-width', '400px');
            //iFrame.css('overflow', 'hidden');
            //iFrame.css('background-color', '#ffc');
            iFrame.addClass('cntwIframe');
            containerNode.append(iFrame);
            iFrameHtml = '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" '
                + '"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">';
            // set overflow:hidden on HTML element to avoid scrollbars around iframe
            iFrameHtml += '<html style="overflow-x:hidden;overflow-y:hidden;"><head><title></title>';
            iFrameHtml += '<meta http-equiv="X-UA-Compatible" content="IE=9" />';
            iFrameHtml += '<meta http-equiv="content-type" content="text/html; charset=UTF-8" />';
            $.each(this.configuration.cssFiles, function(index, cssFile) {
                if (cssFile.search(/^http/g) < 0){url = self.configuration.baseHost + self.configuration.cntPath + cssFile;} else {url = cssFile;}
                iFrameHtml += '<link rel="stylesheet" type="text/css" href="' + url + '" />';
            });
            iFrameHtml += '</head><body style="overflow-x:hidden;overflow-y:hidden;"></body></html>';
            doc = $(".cntwIframe", this.containerNode).contents();
            docIframe = doc[0].open("text/html", "replace");
            docIframe.write(iFrameHtml);
            docIframe.close();
            var bodyElement = $('body', docIframe);
            this.insertWidget(bodyElement);

            // check every 500ms if the body size within the iframe changed
            // if so, resize the frame

            var bodyHeight = bodyElement.height();
            setInterval(function() {

                var currentBodyHeight = bodyElement.height();
                if (currentBodyHeight != bodyHeight) {
                    self.resizeFrame();
                }
                bodyHeight = currentBodyHeight;

            }, 500);
        } else {
            this.insertWidget(containerNode);
        }
    },

    insertWidget: function(container) {
        var head;
        var $ = communote.jQuery;
        var wConfig = this.configuration;
        // if (wConfig.useIframe === false) {
        if (this.useIframe === false) {
            head = $("head");
            $.each(this.configuration.cssFiles, function(index, cssFile) {
                var url, cssLink;
                if (cssFile.search(/^http/g) < 0){url = wConfig.baseHost + wConfig.cntPath + cssFile;} else {url = cssFile;}
                cssLink = $('<link />');
                cssLink.attr('rel', 'stylesheet');
                cssLink.attr('type', 'text/css');
                cssLink.attr('href', url);
                head.append(cssLink);
            });
        }
        this.frameNode = container;
        container.append('<div class="cntwRoot"></div>');
        this.baseContainer.nodeSelector = '.cntwRoot';
        communote.widget.EventController.fireEvent("widgetFrameReady", this.channel);
    },

    /**
     * set the height of the enclosing iframe tag
     */
    resizeFrame: function(atOnce) {

        if (this.useIframe) {
            var compHeight, prop, i;
            var root = this.getRootNode();
            var iframe = this.getIFrame();
            var self = this;
            if (this.rootMBP === undefined) {
                this.rootMBP = 0;
                for (i = 0; i < this.cssProps.length; ++i) {
                    prop = root.css(this.cssProps[i]);
                    this.rootMBP += parseInt(prop) || 0;
                }
            }
            setTimeout(function() {
                var iframeDocument;
                compHeight = root.height() + self.rootMBP;
                /** only IE: correction for iframe */
                /** see: http://www.javascriptkit.com/javatutors/conditionalcompile.shtml */
                /*@cc_on
                    compHeight += 14;
                @*/
                if (self.maxHeight > 0 && compHeight > self.maxHeight) {
                    if (!self.maxHeightReached) {
                        iframeDocument = iframe.contents()[0];
                        iframe.height(self.maxHeight);
                        iframeDocument.body.style.overflowY = '';
                        iframeDocument.documentElement.style.overflowY = ''; // Old IE
                        self.maxHeightReached = true;
                    }
                } else {
                    if (self.maxHeightReached) {
                        iframeDocument = iframe.contents()[0];
                        iframeDocument.body.style.overflowY = 'hidden';
                        iframeDocument.documentElement.style.overflowY = 'hidden'; // Old IE
                        self.maxHeightReached = false;
                    }
                    // do small and grow resizes instantly, other later (if still required)
                    if(atOnce === true || (iframe.height() < compHeight) || (iframe.height() - compHeight < 100)){
                        iframe.height(compHeight);
                    }
                    else{
                        setTimeout(function() { self.resizeFrame(true) ;}, 1000);
                    }
                }
            }, 100);
        }
    },

    renderContent: function() {
        this.baseContainer.render();
    },

    getIFrame : function() {
        if(this.Iframe == null){
            this.Iframe = communote.jQuery(".cntwIframe", this.containerNode);
        }
        return this.Iframe;
    },

    getRootNode : function() {
        if(this.rootNode == null){
           this.rootNode = communote.jQuery(".cntwRoot", this.frameNode).first();
        }
        return this.rootNode;
    }
    
});
