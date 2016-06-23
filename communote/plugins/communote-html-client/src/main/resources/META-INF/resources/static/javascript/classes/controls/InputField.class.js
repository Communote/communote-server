/**
 * @class
 * @name communote.widget.classes.controls.InputField
 * @augments communote.widget.classes.controls.Control
 */
communote.widget.classes.controls.InputField = communote.widget.classes.controls.Control.extend(
/** 
 * @lends communote.widget.classes.controls.InputField.prototype
 */       
{
    name: 'InputField',
    template: 'InputField',
    noContainer: true,
    inputField: '<input />',
    infoTextLabel: 'htmlclient.writecontainer.inputfield.placeholder',
    confirmOnEnter: true,
    alwaysClear: false,
    keepValue: false,
    autoExpand: false,
    registerListeners: function() {
        this.listenTo('confirmValue');
        this.base();
    },

    insertValues: function(data) {
        this.base(data, true);
        var inputContainer = this.getInputContainer();
        inputContainer.append(this.inputField);
        this.renderingDone();
    },

    bindEvents: function() {
        this.base();
        var forceClear, option;
        var self = this;
        var input = this.getInput();
        var $ = communote.jQuery;
        
        if (this.autoExpand) {
            option = { "minLines": this.widget.configuration.edInitLines };
            this.expander = new communote.widget.classes.ExpandingTextarea(input,option);
            this.expander.startAutoExpand();
        }
        if (this.configData.defaultText){
            this.setText(this.configData.defaultText, true);
        } else {
            this.insertInfoText();
        }
        forceClear = (this.configData.alwaysClear != undefined) ? this.configData.alwaysClear
                : this.alwaysClear;
        this.keepValue = (this.configData.keepValue != undefined) ? this.configData.keepValue
                : this.keepValue;
        
        input.focus(function() {
            self.clearInfoText(forceClear);
        });
        
        input.blur(function() {
            self.insertInfoText();
        }); 
        
        input.keypress(function(event) {
            if (self.confirmOnEnter) {
        		if (event.which == 13){
        		    self.fireEvent('confirmValue', undefined, self.getValue());
        		}
            }
        });
    },

    triggerAutoExpand: function() {
        var self = this;
        if (this.autoExpand) {
            setTimeout(function() {
                self.expander.checkExpand();
            }, 100);
        }
    },

    clearInfoText: function(force) {
        var input = this.getInput();
        var value = this.getValue();
        if (force || (value == '') || (value == this.getInfoText())) {
            this.setValue('');
            input.removeClass('cntwEmpty');
        }
    },

    insertInfoText: function(force) {
        var input = this.getInput();
        var value = this.getValue();
        var keep = (this.keepValue && this.keepText);
        if (force || (value == '') || keep) {
            if (keep) {
                if (this.getValue() === '' || force){
                    this.setValue(this.keepText);
                }
            } else {
                input.addClass('cntwEmpty');
                this.setValue(this.getInfoText());
            }
        }
    },

    setValue: function(value) {
        // TODO fgr: what is requirement for the history. Currently it looks like a memory leak to me.
        var input;
        if (!this.history){this.history = [];}
        this.history.push(value);
        input = this.getInput();
        input.attr('value', value);
        this.triggerAutoExpand();
    },

    setText: function(text, keep) {
        this.clearInfoText(true);
        this.setValue(text);
        this.keepValue = keep;
        this.keepText = text;
    },

    getInfoText: function() {
        if (this.infoText) {
            // dynamic text, that maybe will set on runtime - this is no label, it is a 'real' text
            return this.infoText;
        }
        var label = this.configData.infoTextLabel || this.infoTextLabel;
        return this.getLabel(label);
    },

    getInput: function() {
        return this.getInputContainer().children().first();
    },

    getInputContainer: function() {
        return communote.jQuery('.cntwInputElement', this.getDomNode());
    },

    getValue: function() {
        var input = this.getInput();
        var value = input.attr('value');
        if (value == this.getInfoText()){value = '';}
        return value;
    },

    confirmValue: function() {
    },

    reset: function() {
        this.insertInfoText(true);
    }
});
