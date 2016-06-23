/**
 * @class
 * @augments communote.widget.classes.controls.Control
 */
communote.widget.classes.controls.ViewFilter = communote.widget.classes.controls.Control.extend(
/** 
 * @lends communote.widget.classes.controls.ViewFilter.prototype
 */	
{
    name: 'ViewFilter',
    shown: false,
    availableViews: null,
    selectedViewId: null,
    supportedViewIds: null,
    showActivityFilter: true,
    showActivities: true,
    
    /**
     * @param id
     * @param config
     * @param widget
     */
    constructor: function(id, config, widget) {
        this.base(id, config, widget);

        var i, viewId, i18nController, preselectedView;
        var showViews = widget.configuration.msgShowViews;
        this.showActivities = widget.configuration.acShow;
        this.showActivityFilter = widget.configuration.acShowFilter;
        
        if (showViews) {
            showViews = showViews.split(',');
        }
        if (showViews && (showViews.length > 1) && communote.currentUser) {
            this.shown = true;
            i18nController = this.controller.I18nController;
            // save names of views in order
            this.availableViews = {};
            for (i = 0; i < showViews.length; i++) {
                viewId = showViews[i];
                this.availableViews[viewId] = i18nController.getText('htmlclient.viewfilter.viewlabel.'
                        + viewId);
                if (i === 0) {
                    this.selectedViewId = viewId;
                }
            }
            
            // ordered list of supported views
            this.supportedViewIds = ['all', 'me', 'favorites', 'following'];
            
            preselectedView = widget.configuration.msgViewSelected;
            if (preselectedView && this.availableViews[preselectedView] 
                && communote.jQuery.inArray(preselectedView, this.supportedViewIds)) {
                this.selectedViewId = preselectedView;
            }
            
        } else {
            // TODO throw exception that disabled or add a ControlEnabledChecker
            throw new communote.widget.classes.ControlDisabledException(this.name);
        }
    },

    registerListeners: function() {
        this.base();
        this.listenTo('filterChanged', this.widget.channel);
        this.listenTo("IsRenderedNoteList", "global");
        this.listenTo("notNoteElements", "global");
    },

    /**
     * @method notNoteElements
     * eventHandler for event 'notNoteElements', event fired if the notelist is empty
     * hide the filterView container
     */
    notNoteElements: function() {
        if (this.selectedViewId == this.supportedViewIds[0]) {
            this.getDomNode().find('.cntwViewFilterSelection').hide();
        }

        
    },

    filterChanged: function(data) {
        // set the filterView container to viewable
        this.domNode.show();
        // only interested in viewFilter
        if (data.paramName == 'viewFilter') {
            this.selectedViewId = data.value;
            this.viewMenu.markDirty();
            this.getDomNode().find('.cntwViewFilterSelection').text(data.label);
        }
    },
    
    /**
     * @override
     */
    bindEvents: function() {
        var domNode, self, i18nController;
        
        i18nController = this.controller.I18nController;
        
        if (this.shown) {
            domNode = this.getDomNode();
            this.viewMenu = domNode.find('.cntwViewFilterSelection').overlaymenu({
                appendTo: '.cntwViewFilter',
                growHorizontally: true,
                selectionCallback: function(view) {
                    communote.widget.EventController.fireEvent('filterEvent', self.widget.channel, {
                        paramName: 'viewFilter',
                        value: view.id,
                        label: view.label
                    });
                },
                loadData: this.getViewMenuEntries,
                loadDataBind: this
            }).data('overlaymenu');
            
            self = this;
            domNode.find('.cntwViewFilterSelection').click(function(event) {
                self.viewMenu.toggle(event);
            });
            
            var acFilter = domNode.find('.cntwActivityViewFilter');
            if(this.showActivityFilter){
                acFilter.text(i18nController.getText('htmlclient.viewfilter.activityStream'));
                
                // predefined Filter is set in widget.createFilterParameterStore
                if(!this.showActivities){
                    acFilter.removeClass('cntwSelected');                    
                } else {
                    acFilter.addClass('cntwSelected');
                }
                
                acFilter.click(function(event){
                    var $ = communote.jQuery;
                	if($(this).hasClass('cntwSelected')){
                		$(this).removeClass('cntwSelected');
                		communote.widget.EventController.fireEvent('filterEvent', self.widget.channel, {
                            paramName: 'propertyFilter',   
                            value:'value:{["Note","com.communote.plugins.communote-plugin-activity-base","contentTypes.activity","activity","EQUALS","true"]}'
                        });
                		self.showActivities=false;

                	} else {
                		$(this).addClass('cntwSelected');
                		communote.widget.EventController.fireEvent('filterEvent', self.widget.channel, {
                            paramName: 'propertyFilter'
                        });
                        self.showActivities=true;
                	}

                    self.getDomNode().find('.cntwViewFilterSelection').show();
                });
            } else {
                acFilter.hide();
            }
        }
    },
    
    getViewMenuEntries: function() {
        var view, entries, i;
        entries = [];
        // add entries in correct order
        for (i = 0; i < this.supportedViewIds.length; i++) {
            view = this.supportedViewIds[i];
            if ((this.selectedViewId != view) && this.availableViews[view]) {
                entries.push({
                    id: view,
                    label: this.availableViews[view]
                });
            }
        }
        this.viewMenu.dataLoaded(entries);
    },

    /**
     * @override
     */
    parseData: function(data) {
        if (this.shown) {
            data.title = this.getLabel('htmlclient.viewfilter.title');
            data.selectedViewLabel = this.availableViews[this.selectedViewId];
        }
    },

    /**
     * @override
     */
    getDirectives: function() {
        return {
            '.cntwViewFilterSelection': 'selectedViewLabel'
        };
    },

    /**
     * fade in the view menu after NoteList is rendered
     */
    IsRenderedNoteList: function() {
        communote.jQuery(".cntwViewFilter",this.domNode.context).fadeIn("slow");
    }
});