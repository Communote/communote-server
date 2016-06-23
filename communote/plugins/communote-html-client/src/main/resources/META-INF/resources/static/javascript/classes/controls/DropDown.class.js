/**
 * @class
 * @augments communote.widget.classes.controls.Control
 */
communote.widget.classes.controls.DropDown = communote.widget.classes.controls.Control.extend(
/** 
 * @lends communote.widget.classes.controls.DropDown.prototype
 */
{
    name: 'DropDown',
    noContainer: true,

    registerListeners: function() {
        this.base();
        this.listenTo('selectorOpen');
        this.listenTo('selectorOpen', this.widget.channel, 'selectorClose');
        this.listenTo('selectorClose');
        this.listenTo('selectValue');
    },

    bindEvents: function() {
        var defaultValue;
        var $ = communote.jQuery;
        var self = this;
        var widgetChannel = this.widget.channel;
        var controlChannel = this.channel;
        var domNode = this.getDomNode();
        $('.cntwSelectorOpen', domNode).click(function() {
            self.fireEvent('selectorOpen', widgetChannel, {});
            self.fireEvent('selectorOpen', controlChannel, {});
        });
        $('.cntwSelectorClose', domNode).click(function() {
            self.fireEvent('selectorClose', controlChannel, {});
        });
        $('.cntwOption > *', domNode).click(function() {
            self.fireEvent('selectValue', controlChannel, {
                value: $(this)
            });
        });
        //select first value als default
        defaultValue = $('.cntwOption', domNode).first().children().first();
        this.fireEvent('selectValue', controlChannel, {
            value: defaultValue
        });
    },

    selectorOpen: function() {
        var $ = communote.jQuery;
        var select = this.getDomNode();
        var options = $('.cntwOptions', select);
        options.css('width', select.width());
//        options.css('top', select.position().top + 'px');
//        options.css('left', select.position().left + 'px');
        options.show();
    },

    selectorClose: function() {
        var $ = communote.jQuery;
        var select = this.getDomNode();
        var options = $('.cntwOptions', select);
        options.hide();
    },

    selectValue: function(data) {
        var $ = communote.jQuery;
        var domNode = this.getDomNode();
        var value = data.value;
        var valueField = $('.cntwSelectorValue', domNode);
        valueField.children().remove();
        value.clone().appendTo(valueField);
        this.fireEvent('selectorClose', this.channel, {});
    }
});
