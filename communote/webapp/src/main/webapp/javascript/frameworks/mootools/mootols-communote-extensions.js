(function(window) {

    if (window.communote) {
        Request.implement('failure', function() {
             if (this.xhr.status == 401 && window.communote.requestFailureUnauthorizedHandler) {
                 if (!window.communote.requestFailureUnauthorizedHandler.call(null, this)) {
                     return;
                 }
             }
             this.onFailure();
        });
    }

})(this);

CommunoteOverText = new Class({
    Extends: OverText,
    boundBlur: null,
    blurTimeout: null,
    
    onBlurDelayed: function() {
        this.blurTimeout = this.onBlur.delay(250, this);
    },
    
    onBlur: function() {
        // check if still unfocused
        if (document.activeElement != this.element) {
            this.assert(false);
        }
    },
    disable: function(){
        clearTimeout(this.blurTimeout);
        this.element.removeEvents({
            focus: this.focus,
            blur: this.boundBlur,
            change: this.assert
        });
        window.removeEvent('resize', this.reposition);
        this.hide(true, true);
        return this;
    },

    enable: function(){
        this.boundBlur = this.onBlurDelayed.bind(this); 
        this.element.addEvents({
            focus: this.focus,
            blur: this.boundBlur,
            change: this.assert
        });
        window.addEvent('resize', this.reposition);
        this.reposition();
        return this;
    },
    test: function() {
        if (document.activeElement == this.element) {
            // force hide when focused
            return false;
        }
        return this.parent();
    }
});
