/**
 * @class
 * @augments communote.widget.classes.controls.ContainerControl
 */
communote.widget.classes.controls.Slider = communote.widget.classes.controls.ContainerControl
        .extend(
        /** 
         * @lends communote.widget.classes.controls.Slider.prototype
         */ 	
        {
            name: 'Slider',
            noContainer: true,
            sliderOpenClass: 'cntwSliderOpen',
            sliderCloseClass: 'cntwSliderClose',
            registerListeners: function() {
                this.listenTo('sliderOpen');
                this.listenTo('sliderClose');
                this.base();
            },

            bindEvents: function() {
                var node;
                var $ = communote.jQuery;
                var self = this;
                var domNode = this.getDomNode();
                this.sliderOpenClass = this.configData.sliderOpenClass || this.sliderOpenClass;
                this.sliderCloseClass = this.configData.sliderCloseClass || this.sliderCloseClass;
                node = $('.' + this.sliderOpenClass, domNode);
                node.click(function() {
                    self.fireEvent('sliderOpen');
                    self.fireEvent("sizeChanged", self.widget.channel);
                });
                node = $('.' + this.sliderCloseClass, domNode);
                node.click(function() {
                    self.fireEvent('sliderClose');
                    self.fireEvent("sizeChanged", self.widget.channel);
                });
                if (this.configData.open){setTimeout(function() {
		    self.fireEvent('sliderOpen');
		}, 300);}
            },

            getDirectives: function() {
                var self = this;
                var labelClose = this.configData.sliderCloseLabel;
                var labelOpen = this.configData.sliderOpenLabel;
                var dir = {};
                if (labelClose){dir['.' + this.sliderCloseClass] = function() {
		    return self.getLabel(labelClose);
		};}
                if (labelOpen){dir['.' + this.sliderOpenClass] = function() {
		    return self.getLabel(labelOpen);
		};}
                return dir;
            },
            sliderOpen: function() {
                var $ = communote.jQuery;
                var domNode = this.getDomNode();
                domNode.addClass('cntwOpenSlider');
                $('.' + this.sliderOpenClass, domNode).hide();
                $('.' + this.sliderCloseClass, domNode).show();
                $('.cntwFloatingSliderArea', domNode).show();
            },

            sliderClose: function() {
                var $ = communote.jQuery;
                var domNode = this.getDomNode();
                domNode.removeClass('cntwOpenSlider');
                $('.' + this.sliderCloseClass, domNode).hide();
                $('.cntwFloatingSliderArea', domNode).hide();
                $('.' + this.sliderOpenClass, domNode).show();
            }

        });
