/**
 * ExpandingTextarea
 */
communote.widget.classes.ExpandingTextarea = communote.Base.extend(
/** @lends communote.widget.classes.ExpandingTextarea.prototype */	
{

    name: 'ExpandingTextarea', // for logging
    options: {
        additionalPixels: -1, // additional space in px at bottom of textarea, takes precedence over additionalLines if >= 0
        additionalLines: 0, // additional empty lines at bottom of textarea
        checkDelay: 300, // delay in ms after which to check for expand after paste event
        checkInterval: 400, // interval in ms for periodically checking for expand (opera only)
        minLines: 1, // values lesser than 1 will be ignored
        maxHeight: 0
    },

    additionalPixels: 0,
    cssHeightOffset: 0,
    surrogate: null,
    surrogateOuterHeight : null,
    textarea: null,
    minHeight: 0,
    document: null,

    /**
     * @constructs
     * @param elem
     * @param options
     */
    constructor: function(elem, options) {
        var surrogate, $;  
        options = options || {};
        $ = communote.jQuery;
        // elem = textarea;
        elem.tagName = elem[0].tagName.toLowerCase();
        if (!elem || (elem.tagName != 'textarea')) {
            throw new Error('This class can only be applied to textarea elements');
        }
        this.document = this.getDocumentNode(elem[0]);
        // read some element attributes to init the options

        // cssMaxHeight = elem.css('max-height');
        // if (cssMaxHeight == undefined || cssMaxHeight == 'none')
            // cssMaxHeight = '0';
        // this.options.maxHeight = eval(cssMaxHeight.replace('px', '')) || -1;
        this.options.maxHeight = parseFloat(elem.css('max-height')) || 0;

        // apply options -> override elem styles
        this.setOptions(options);
        // create surrogate for measuring the size and ensure that there is no padding or border
        // at top or bottom because we need the pure size
        surrogate = this.createElement('div', {
            styles: {
                'position': 'absolute',
                'top': '0',
                'left': '-9999px',
                'overflow-x': 'hidden',
                'word-wrap': 'break-word',
                'padding-top': '0',
                'padding-bottom': '0',
                'border-top': '0',
                'border-bottom': '0',
                'background-color': '#ddd' 
            }
        });
        // apply styles that influence the height and space for text
        this.copyStyles(elem, surrogate, [ 'font-size', 'font-family', 'font-weigth',
                'line-height', 'padding', 'border-top', 'border-bottom', 'border-left',
                'border-right', 'white-space', 'vertical-align' ]);
        elem.after(surrogate);
        surrogate = $(surrogate);
        // calculate height of one line and CSS offsets in height that are included when calling getSize()
        // calculate it by adding an M and measuring the height
        surrogate.html('M');
        this.surrogateOuterHeight = surrogate.outerHeight();
        this.surrogate = surrogate;
        this.calculateHeight();
        this.textarea = elem;
    },

    calculateHeight: function() {
        var height, lineHeight;
        height = this.surrogateOuterHeight;
        lineHeight = this.surrogateOuterHeight;
        if (this.options.additionalPixels > 0) {
            this.additionalPixels = this.options.additionalPixels;
        } else {
            if (this.options.additionalLines > 0) {
                this.additionalPixels = this.options.additionalLines * lineHeight;
            }
        }
        if (this.options.minLines > 0) {
            this.minHeight = lineHeight * this.options.minLines;
        } else {
            // at least one line as minimum
            this.minHeight = lineHeight;
        }
        this.cssHeightOffset = height - lineHeight;
    },

    /**
     * @method
     * return the next document over the given element
     * @param {dom object} node
     */
    getDocumentNode: function(node) {
        if (node.nodeName != "#document") {
            node = this.getDocumentNode(node.parentNode);
        }
        return node;
    },
    
    startAutoExpand: function() {
        var self;
        self = this;
        this.textarea.css('overflow', 'hidden');
        this.checkExpand();
        this.keyupHandler = function() {
            self.checkExpand();
        };
        this.textarea.keyup(this.keyupHandler);
        this.pasteHandler = function() {
            this.timeoutId = self.checkExpand(); //.delay(self.options.checkDelay, self)
        };
        this.textarea.bind('paste', this.pasteHandler);
    },

    stopAutoExpand: function() {
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
        }
        if (this.keyupHandler) {
            this.textarea.unbind('past', this.pastHandler);
            this.textarea.unbind('keyup', this.keyupHandler);
        }
    },

    expand: function(height) {
        var overflow;
        overflow = 'hidden';
        if (height < this.minHeight) {
            height = this.minHeight;
        } else if ((this.options.maxHeight > 0) && (height > this.options.maxHeight)) {
            height = this.options.maxHeight;
            overflow = 'auto';
        }
        this.textarea.css('height', height + 'px');
        this.textarea.css('overflow', overflow);
    },

    checkExpand: function() {
        var content, previousContent, targetHeight;
        if (this.minHeight <= 0) {
            this.calculateHeight();
        }
        this.surrogate.css("width", this.textarea.css("width"));
        content = this.textarea.attr('value').replace(/&/g, '&amp;').replace(/^ .*?/g, "&nbsp;")
                .replace(/  /g, ' &nbsp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(
                        /\n/g, '<br />');
        previousContent = this.surrogate.html();
        if (previousContent != content + '&nbsp;') {
            // update surrogate content to measure the new height
            // add extra space to cover case when caret is at the end of the line
            this.surrogate.html(content + '&nbsp;');
            targetHeight = this.surrogate.outerHeight() + this.additionalPixels;
            if (Math.abs(targetHeight - (this.textarea.outerHeight() - this.cssHeightOffset)) > 3) {
                this.expand(targetHeight);
            }
        }
    },

    setOptions: function(options) {
        var key;
        for (key in options) {
            this.options[key] = options[key];
        }
    },

    createElement: function(tagName, options) {
        var $, html, node, key;
        $ = communote.jQuery;
        html = '<' + tagName + '></' + tagName + '>';
        node = $(html, this.document);
        if (options.styles) {
            for (key in options.styles) {
                node.css(key, options.styles[key]);
            }
        }
        return node;
    },

    copyStyles: function(from, to, styles) {
        var style, index;
        if ((from == undefined) || (to == undefined) || (styles == undefined)) {
            return;
        }
        for (index = 0; index <  styles.length; index++) {
            style = styles[index];
            to.css(style, from.css(style));
        }
    },

    delay: function(func, time, repeat) {
        var id;
        id = Math.random();
        if (!this.delayedFunctions){this.delayedFunctions = [];}
        this.delayedFunctions.push(id);
        this.delayLoop(id, func, time);
        return id;
    },

    delayLoop: function(id, func, time) {

    },

    destroy: function() {
        this.stopAutoExpand();
        this.surrogate.dispose();
    }

});
