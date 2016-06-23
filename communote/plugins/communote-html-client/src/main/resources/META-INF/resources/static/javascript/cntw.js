if (typeof communote == 'undefined') {
    var communote = {};
    if (typeof console == 'undefined'){console = {};}
    if (typeof console.log == 'undefined'){console.log = function() {
    };}
    if (typeof console.info == 'undefined'){console.info = function() {
    };}
    if (typeof console.warn == 'undefined'){console.warn = function() {
    };}
    if (typeof console.error == 'undefined'){console.error = function() {
    };}
    if (typeof console.debug == 'undefined'){console.debug = function() {
    };}

    (function() {
        if (typeof cntw == 'undefined') {
            if (true) {
                //set namespace
                communote.utils = {};
                communote.widget = {};
                communote.widget.classes = {};
                communote.widget.classes.data = {};
                communote.widget.classes.controllers = {};
                communote.widget.classes.controls = {};
 
                //set toolbase
                communote.Base = cntwBase();
                // using deep nonconflict mode to restore jQuery and $ variables
                communote.jQuery = jQuery.noConflict(true);
                communote.Pure = cntwPure(communote.jQuery);
            }
        }
    })();
}
