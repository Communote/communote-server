var ExpandingTextarea = new Class( {
    
    Implements: Options,
    
    options: {
        additionalPixels: -1, // additional space in px at bottom of textarea, takes precedence over additionalLines if >= 0
        additionalLines: 1, // additional empty lines at bottom of textarea
        checkDelay: 300, // delay in ms after which to check for expand after paste event 
        checkInterval: 400, // interval in ms for periodically checking for expand (opera only)
        minLines: 0, // values lesser than 1 will be ignored
        minHeight: 0, // will be ignored if minLines > 0 or less than lineHeight
        maxHeight: 0,
        disableBrowserResize: true // set to true to remove the resize control that is rendered to a textarea by newer browsers
    },
    
    additionalPixels: 0,
    cssHeightOffset: 0,
    surrogate: null,
    textarea: null,
    minHeight: 0,
    pasteEventHandler: null,
    
    initialize: function(textarea, options) {
        var surrogate, height, lineHeight, cssHeightOffset, boxSizingStyle, borderBoxModel;
        var elem = document.id(textarea);
        if (!elem || elem.type != 'textarea') {
            throw new Error('This class can only be applied to textarea elements');
        }
        // read some element attributes to init the options
        this.options.maxHeight = parseInt(elem.getStyle('max-height'), 10) || -1;
        
        // apply options -> override elem styles
        this.setOptions(options);
        
        // create surrogate for measuring the size and ensure that there is no padding or border
        // at top or bottom because we need the pure size
        surrogate = new Element('div', {
            styles: {
                position: 'absolute',
                top: '0px',
                left: '-9999px',
                'overflow-x': 'hidden',
                'word-wrap': 'break-word'
            }
        });
        // apply styles that influence the height and space for text
        surrogate.setStyles(elem.getStyles('font-size', 'font-family', 'font-weigth', 
                'line-height', 'padding', 'width', 'border-top', 'border-bottom', 
                'border-left', 'border-right', 'white-space', 'vertical-align'));
        // check for border box model (border and padding included in weight/height)
        boxSizingStyle = this.getBoxSizingStyle(elem);
        if (boxSizingStyle) {
            borderBoxModel = boxSizingStyle.value === 'border-box';
            surrogate.setStyle(boxSizingStyle.name, boxSizingStyle.value);
        }
        surrogate.inject(elem, 'after');
        
        // calculate height of one line and CSS offsets in height that are included when calling getSize()
        // calculate it by adding an M and measuring the height
        surrogate.set('html', 'M');
        height = surrogate.getSize().y;
        // remove top and bottom padding and border
        surrogate.setStyles({
            'padding-top': '0px',
            'padding-bottom': '0px',
            'border-top': '0px',
            'border-bottom': '0px'
        });
        lineHeight = surrogate.getSize().y;
        surrogate.empty();
        if (this.options.additionalPixels > 0) {
            this.additionalPixels = this.options.additionalPixels;
        } else {
            if (this.options.additionalLines > 0) {
                this.additionalPixels = this.options.additionalLines * lineHeight; 
            }
        }
        if (this.options.minLines > 0) {
            this.minHeight = lineHeight * this.options.minLines;
        } else if (this.options.minHeight > lineHeight) {
            this.minHeight = this.options.minHeight;
        } else {
            // at least one line as minimum
            this.minHeight = lineHeight;
        }
        if (this.options.disableBrowserResize) {
            elem.setStyle('resize', 'none');
        }
        if (borderBoxModel) {
            // in border box model padding and border are important when calculating height but
            // cssHeightOffset must be ignored
            surrogate.setStyles(elem.getStyles('padding-top', 'padding-bottom', 
                    'border-top', 'border-bottom'));
        } else {
            this.cssHeightOffset = height - lineHeight;
        }
        this.surrogate = surrogate;
        this.textarea = elem;
        this.expand(this.minHeight - 1);
    },
    
    getBoxSizingStyle: function(elem) {
        var i;
        var styleNames = ['box-sizing', '-moz-box-sizing', '-webkit-box-sizing', '-ms-box-sizing'];
        var style = {};
        for (i = 0; i < styleNames.length; i++) {
            style.value = elem.getStyle(styleNames[i]);
            if (style.value != undefined && style.value !== '') {
                style.name = styleNames[i];
                break;
            }
        }
        return style.name ? style : null;
    },
    
    startAutoExpand: function() {
        this.textarea.setStyle('overflow', 'hidden');
        this.checkExpand();
        this.keyupHandler = this.checkExpand.bind(this);
        this.textarea.addEvent('keyup', this.keyupHandler);
        //this.textarea.addEvent('paste', function(){this.timeoutId = this.checkExpand.delay(this.options.checkDelay, this)});
        if (this.textarea.addEventListener) {
            this.pasteEventHandler = this.textarea.addEventListener('paste', function(e) {
                this.timeoutId = this.checkExpand.delay(1, this);
            }.bind(this));
        } else {
            // TODO check if we can use mootools paste event with current mootools version
            this.textarea.onpaste = function(){
                this.timeoutId = this.checkExpand.delay(this.options.checkDelay, this);
            }.bind(this);
        }
    },
    
    stopAutoExpand: function() {
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
        }
        if (this.keyupHandler) {
            this.textarea.onpaste = null;
            this.textarea.removeEvent('keyup', this.keyupHandler);
            if (this.pasteEventHandler) {
                this.textarea.removeEventListener('paste', this.pasteEventHandler);
            }
        }
        this.textarea.setStyle('overflow', '');
    },
    
    expand: function(height) {
        var overflow = 'hidden';
        if (height < this.minHeight) {
            height = this.minHeight;
        } else if (this.options.maxHeight > 0 && height > this.options.maxHeight) {
            height = this.options.maxHeight;
            overflow = 'auto';
        }
        this.textarea.setStyle('height', height + 'px');
        this.textarea.setStyle('overflow', overflow);
    },
    
    checkExpand: function() {
        var content, previousContent, targetHeight;
        content = this.textarea.value.replace(/&/g,'&amp;').replace(/  /g, ' &nbsp;').replace(/<|>/g, '&gt;').replace(/\n/g, '<br />');
        previousContent = this.surrogate.get('html');
        
        if (previousContent != content + '&nbsp;') {
            // update surrogate content to measure the new height
            // add extra space to cover case when caret is at the end of the line
            this.surrogate.set('html', content + '&nbsp;');
            targetHeight = this.surrogate.getSize().y + this.additionalPixels;
            if (Math.abs(targetHeight - (this.textarea.getSize().y - this.cssHeightOffset)) > 3) {
                this.expand(targetHeight);
            }
        }
    },
    
    destroy: function() {
        this.stopAutoExpand();
        this.surrogate.dispose();
    }
    
});