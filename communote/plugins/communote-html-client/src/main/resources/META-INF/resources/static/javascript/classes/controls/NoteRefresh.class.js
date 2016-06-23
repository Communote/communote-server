/**
 * @class
 * @augments communote.widget.classes.controls.Control
 */
communote.widget.classes.controls.NoteRefresh = communote.widget.classes.controls.Control.extend(
/** 
 * @lends communote.widget.classes.controls.NoteRefresh.prototype
 */
{
    name: 'NoteRefresh',
    refreshInterval: 30000,
    hdlTimeoutFocusOrBlur: null,
    hdlTimeoutRefresh: null,
    isWFocus: false,
    isIFocus: false,
    hasFocusLastState: false,
    jQuery: null,

    /**
     * @param id
     * @param config
     * @param widget
     */
    constructor: function(id, config, widget) {
        this.base(id, config, widget);
        var self = this;
        this.jQuery = communote.jQuery;

        this.refreshInterval = this.widget.configuration.activeRefreshInterval || 60000;
        this.timeout();

        // the DOM must be rendered for assign
        setTimeout((function(){
            var wElem = self.jQuery(window);
            wElem.blur(function() {
                self.isWFocus = false;
                self.isFocusOrBlur();
            });
            wElem.focus(function() {
                self.isWFocus = true;
                self.isFocusOrBlur();
            });
            var iElem;
            if(self.widget.configuration.useIframe){
                 var iElem = self.jQuery(self.jQuery(".cntwIframe")[0].contentWindow);
            } else {
                iElem = window;
            }
            iElem.blur(function() {
                self.isIFocus = false;
                self.isFocusOrBlur();
            });
            iElem.focus(function() {
                self.isIFocus = true;
                self.isFocusOrBlur();
            });

            var elem = self.jQuery(".cntwNoteRefreshButton", self.domNode);
            elem.click(function() {
                elem.addClass("cntwHidden");
                self.timeout();
                self.controller.EventController.fireEvent('OnClickLoadNewNotes');
            });

        }), 0);
    },

    /**
     * overwritten
     */
    beforeDestroy: function() {
        clearTimeout(this.hdlTimeoutFocusOrBlur);
        clearTimeout(this.hdlTimeoutRefresh);
        this.base();
    },

    /**
     * overwritten
     */
    getDirectives: function() {
        return {
            'input@value': 'title'
        };
    },

    /**
     * @method ifNewNotesAvailable
     * trigger the load button
     * @param {object} data
     */
    ifNewNotesAvailable: function(data) {
        if ((data != undefined) && (data.notes[0] != undefined) && (this.widget.firstNoteCreationDate < data.notes[0].creationDate)) {
            communote.jQuery(".cntwNoteRefreshButton", this.getDomNode()).removeClass("cntwHidden");
        } else {
            this.timeout();
        }
    },

    /**
     * overwritten
     */
    includeFilterParameters: function() {
        return true;
    },

    /**
     * @method isFocusOrBlur
     * browser window get or lost the focus
     */
    isFocusOrBlur: function() {
        var self = this;
        if (this.hdlTimeoutFocusOrBlur) {
            clearTimeout(this.hdlTimeoutFocusOrBlur);
        }
        this.hdlTimeoutFocusOrBlur = setTimeout((function() {
            var hasFocus = self.isWFocus || self.isIFocus;
            if (hasFocus) {
                /** execute only, if last state was false */
                if (!self.hasFocusLastState) {
                    if (self.hdlTimeoutRefresh){clearTimeout(self.hdlTimeoutRefresh);}
                    /** set a new interval time, if the browser becomes the focus */
                    self.refreshInterval = self.widget.configuration.activeRefreshInterval || 30000;
                    self.timeout();
                }
            } else {
                /** change the interval time, if the browser leave the focus */
                self.refreshInterval = self.widget.configuration.inactiveRefreshInterval || 150000;
            }
            self.hasFocusLastState = hasFocus;
        }), 200);
    },

    /**
     * overwritten
     */
    parseData: function(data) {
        data.title = this.getLabel('htmlclient.notelist.refresh');
    },

    /**
     * overwritten
     */
    registerListeners: function() {
    },

    /**
     * @method requestForNewNotes
     * requests the API
     */
    requestForNewNotes: function() {
        this.controller.ApiController.lastTimeNote(this.ifNewNotesAvailable, this, this);
    },

    /**
     * @method timeout
     * init a new refresh time interval, time is setting in configuration (refreshInterval)
     */
    timeout: function() {
        var self = this;
        this.hdlTimeoutRefresh = setTimeout( function() {
            self.requestForNewNotes();
        }, this.refreshInterval);
    },

    eof: true
});
