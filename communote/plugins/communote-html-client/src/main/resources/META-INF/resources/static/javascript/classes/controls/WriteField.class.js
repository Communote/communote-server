/**
 * @class
 * @augments communote.widget.classes.controls.InputField
 */
communote.widget.classes.controls.WriteField = communote.widget.classes.controls.InputField
        .extend(
        /** 
         * @lends communote.widget.classes.controls.WriteField.prototype
         */	
        {
            inputField: "<textarea class=\"cntwTextarea\" cols=\"25\" rows=\"1\"></textarea>",
            autoExpand: true,
            confirmOnEnter: false,

            /**
             * 
             */
            registerListeners: function() {
                this.base();
                this.listenTo("noteServiceSuccessEvent");
            },

            bindEvents: function() {
            	var self = this;
                this.base();
                var elem = communote.jQuery(".cntwTextarea", this.domNode);
                var h = parseFloat(elem.css("line-height"))
                        || (parseFloat(elem.css("font-size")) || 0) * 1.3
                        || 16;
                var lines = this.widget.configuration.edInitLines || 2;
                elem.css("min-height", (lines * h) + "px");
                elem.bind('keydown', function(){
                	self.setDirty();
                });
                
                elem.bind('blur', function(){
                	if(elem.hasClass('cntwEmpty')){
                    	self.setClean();
                	}
                });
            },

            /** 
             * clear input field after successfull sending
             */
            noteServiceSuccessEvent: function() {
                this.insertInfoText(true); 
                this.setClean();
            },

            /**
             * 
             */
            confirmValue: function(value) {
                if ((this.getInfoText() == value) || (this.configData.defaultText == value)){value = '';}
                this.fireEvent('sendNote', this.parent.channel, {
                    value: value
                });
            },

            /**
             * overwrite the render method in the Control class
             */
            render: function() {
                // the count of lines defined in Configuration
                this.inputField = this.inputField.replace(/rows=".*?"/, "rows=\""
                        + this.widget.configuration.edInitLines + "\"");
                this.base();
            },
            
            /**
             * appends the cntwDirty class for beforeunload
             */
            setDirty: function(){
            	communote.jQuery(this.getInput()).addClass('cntwDirty');             	
            },
            
            /**
             * removes the cntwDirty class
             */
            setClean: function(){
            	communote.jQuery(this.getInput()).removeClass('cntwDirty');            	
            }

        });
