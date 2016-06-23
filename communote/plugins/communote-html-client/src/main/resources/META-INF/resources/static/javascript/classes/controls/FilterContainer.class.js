/**
 *  @class
 *  @augments communote.widget.classes.controls.ContainerControl
 */
communote.widget.classes.controls.FilterContainer = communote.widget.classes.controls.ContainerControl
        .extend(
/** 
 * @lends communote.widget.classes.controls.FilterContainer.prototype 
 */        	
{
            name: "FilterContainer",
            controls: [ {
                type: "Slider",
                slot: ".cntwSlider",
                template: "no template",
                data: {
                    open: false
                },
                controls: [ {
                    type: "OptionList",
                    slot: ".cntwFilterToggle",
                    data: {
                        items: [ {
                            label: "htmlclient.filtercontainer.showFilter",
                            css: "cntwAction cntwRight cntwSliderOpen"
                        }, {
                            label: "htmlclient.filtercontainer.hideFilter",
                            css: "cntwAction cntwRight cntwSliderClose cntwHidden"
                        } ]
                    }
                } ]
            }, {
                type: "OptionList",
                slot: ".cntwFilterTypeList",
                data: {
                    pipeList: true,
                    items: [ {
                        label: "htmlclient.filtercontainer.text",
                        css: "cntwText"
                    }, {
                        label: "htmlclient.filtercontainer.tags",
                        css: "cntwTags"
                    }, {
                        label: "htmlclient.filtercontainer.authors",
                        css: "cntwUserText"
                    } ],
                    showSelection: true
                }
            }, {
                type: "SearchField",
                slot: ".cntwFilterField",
                data: {
                    infoTextLabel: "htmlclient.filtercontainer.inputfield.placeholder"
                }
            }, {
                slot: ".cntwFloatingSliderArea",
                type: "TopicFilter"
            }, {
                slot: ".cntwFloatingSliderArea",
                type: "TagFilter"
            }, {
                slot: ".cntwFloatingSliderArea",
                type: "UserFilter"
            }, {
                slot: ".cntwFilterSummaryArea",
                type: "FilterSummary"
            } ],

            disabeledByConfig: function(config) {
                var wConfig = this.widget.configuration;
                if ((config.slot === ".cntwFilterTypeList" && !wConfig.fiShowSearch)
                        || (config.slot === ".cntwFilterToggle" && (!wConfig.fiShowTopic
                                && !wConfig.fiShowTagCloud && !wConfig.fiShowAuthor))) {
                    return true;
                }
                // hide the filter link, if all filters disabled
                if (config.slot === ".cntwSlider" && (!wConfig.fiShowTopic 
                        && !wConfig.fiShowTagCloud && !wConfig.fiShowAuthor)) {
                    if (config.controls && config.controls.length) {
                        var ctrl = config.controls[0];
                        if (ctrl.slot && (ctrl.slot == ".cntwFilterToggle")) {
                            config.controls.splice(0, 1);
                        }
                    }
                }

                return this.base(config);
            },

            showWorkingIndicator: function() {
               //TODO show indicator inside filter area
            }
        });
