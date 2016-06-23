/**
 * This Control is mainly for grouping other controls it provides: - loading subcontrol ( and
 * trigger create ) - trigger rendering those - update the working indicator to show the actual
 * number of loaded ones it also ensures that all subcontrols has to be rendered, until it claims
 * itself as rendered
 * @class 
 * @name communote.widget.classes.controls.ContainerControl
 * @augments communote.widget.classes.controls.Control
 */
communote.widget.classes.controls.ContainerControl = communote.widget.classes.controls.Control.extend(
/** 
 * @lends communote.widget.classes.controls.ContainerControl.prototype
 */        	
{
            isContainerControl: true,
            noChildsLabel: false, // {string} will lead to display this label, when no controls are availabel

            /**
             * this constructor will call its base constructor and will push the controls from the
             * config to the own controls after this it will load all other controls
             * 
             * because it is in the constructor, it will result in the _complete_ tree of all
             * subcontrols at the end of the constructor
             */
            constructor: function(id, config, widget) {
                this.base(id, config, widget);
                var i;
                var $ = communote.jQuery;
                if (this.controls == undefined) {
                    this.controls = [];
                } else {
                    this.controls = $.merge([], this.controls); // create own instance
                }
                if (config.controls){for (i = 0; i< config.controls.length; i++) {this.controls.push(config.controls[i]);}}
                this.controlList = [];
                this.loadControls();
                this.registerListeners();
            },

            /**
             * This will load (== create) all controls configured in this.controls and store them in
             * this.controlList
             * 
             * if the controlList is not empty at the beginning, it will use the controlList.length
             * as an offset on the contols (this enables the possibility of adding control-configs
             * later and still be able to call this function)
             * 
             */
            loadControls: function() {
                var i;
                if (this.controls != undefined) {
                    // if loadControls is called to load controls, that are not present at the beginning
                    for (i = this.controlList.length; i < this.controls.length; i++) {
                        this.loadControl(this.controls[i]);
                    }
                }
            },

            /**
             * This will load (== create) a control for this config and store it in the controlList
             * 
             * it will not create controls that are filtered by the 'disabeledByConfig()', which
             * will check the widget configuration for some flags
             * 
             * to create the link between parent and child, the parent object (this) is pushed into
             * the config
             * 
             */
            loadControl: function(config) {
                var control;
                if (this.disabeledByConfig(config)) {
                    return null;
                }
                // TODO fgr: why do you modify the configuration? Couldn't the details be passed as a method argument?
                // by fgr:   maybe it would be better to do so.. i just took the easier way ;D
                config.parent = this;
                control = communote.widget.ControlFactory.getControl(config, this.widget);
                if (control) {
                    this.addSubControl(control);
                }
                return control;
            },

            /**
             * This function decides if a flag will force a component to not be used
             * 
             * @param {object} config - standard control config
             * @return {boolean} - true, if the control should not be used REFACTOR: may evalute
             *         better ways of configure those things
             */
            // TODO let controls decide whether they should be rendered (constructor could throw ControlDisabledExcpetion)
            disabeledByConfig: function(config) {
                var wConfig = this.widget.configuration;
                var result = ((config.type == "WriteContainer") && !wConfig.edShowCreate)
                        || ((config.type == "WriteField") && this.confirmAction === "create" && !wConfig.edShowCreate)
                        || ((config.type == "SearchField") && !wConfig.fiShowSearch)
                        || ((config.type == "TopicFilter") && !wConfig.fiShowTopic)
                        || ((config.type == "TagFilter") && !wConfig.fiShowTagCloud)
                        || ((config.type == "UserFilter") && !wConfig.fiShowAuthor)
                        || ((config.type == "FilterContainer") && !(wConfig.fiShowFilter && ((wConfig.fiShowTopic||wConfig.fiShowAuthor||wConfig.fiShowTagCloud||wConfig.fiShowSearch))) );
                // console.info(config.type,result);
                
                return result;
            },

            /**
             * adds a control to the controlList and register on its renderingDone event
             */
            addSubControl: function(control) {
                this.listenTo('renderingDone', control.channel);
                this.controlList.push(control);
            },

            /**
             * this will get an subcontrol of this control by any given attribute, that fits the
             * value for example this.getSubControl('name', 'OptionList'); or
             * this.getSubControl('nodeSelector', '.cntwHeader'); it will return the first match
             * 
             * @param {string} attribute - the name of the attribute
             * @param {object} value - the object value that will be compared
             * 
             * @return {Control} - the control that fitted the search or undefined
             */
            getSubControl: function(attribute, value) {
                var i, subControl;
                for (i = 0; i < this.controlList.length; i++) {
                    if (this.controlList[i][attribute] == value) {
                        return this.controlList[i];
                    }
                    if (this.controlList[i].isContainerControl) {
                        subControl = this.controlList[i].getSubControl(attribute, value);
                        if (subControl) {
                            return subControl;
                        }
                    }
                }
                return null;
            },

            /**
             * this extend the Control.reRender by the remove of all childs REFACTOR: make real
             * destructor, that also has to be recursive
             */
            reRender: function() {
                var i;
                this.rendered = false;
                for (i = 0; i < this.controlList.length; i++) {
                    this.controlList[i].getDomNode().remove();
                }
                this.removeAllSubControls();
                this.controlList = [];
                this.base();
            },

            /**
             * this will extend the Control.insertValues by the rendering of all subControls
             * 
             * it skips the renderingDone if it has subControls, to it may like to bind events and
             * has to wait for their rendering cycle
             */
            insertValues: function(data, skipRenderingDone) {
                if (skipRenderingDone === undefined) {
                    skipRenderingDone = (this.controls.length > 0);
                }
                this.base(data, skipRenderingDone);
                this.renderSubControls();
            },

            /**
             * this will trigger als subcontrols to render or display a label that there are no
             * subcontrols to render
             */
            renderSubControls: function() {
                var i;
                if (this.controlList.length <= 0) {
                    if (this.noChildsLabel){this.getDomNode()
		            .append('<p>' + this.getLabel(this.noChildsLabel) + '</p>');}
                    //this.renderingDone();
                    return;
                }
                for (i = 0; i < this.controlList.length; i++) {
                    this.controlList[i].render();
                }
            },

            /**
             * extends the Control.renderingDone will check all subcontrols if they are rendered and
             * will be only continue if all subcontrols are rendered
             * 
             * will update the counter on the working indicator
             */
            renderingDone: function() {
                var i;
                var done = true;
                var notRendered = [];
                for (i = 0; i < this.controlList.length; i++) {
                    if (!this.controlList[i].rendered) {
                        notRendered.push(this.controlList[i].name + '('
                                + this.controlList[i].channel + ')');
                        done = false;
                    }
                }
                this.readyCount = (this.controlList.length - notRendered.length);
                if (done || this.onError) {
                    this.base();
                }
            },

            /**
             * counts all ready subcontrols (and their controls)
             */
            getReadyCount: function() {
                var $ = communote.jQuery;
                var count = this.readyCount || 0;
                $.each(this.controlList.length, function(index, child) {
                    if ($.isFunction(child.getReadyCount)) {
                        count += child.getReadyCount();
                    }
                });
                return count;
            },

            /**
             * counts the number of all subcontrols and their subcontrols
             */
            getSubControlCount: function() {
                var $ = communote.jQuery;
                var count = this.controlList.length;
                $.each(this.controlList.length, function(index, child) {
                    if ($.isFunction(child.getSubControlCount)) {
                        count += child.getSubControlCount();
                    }
                });
                return count;
            },

            /**
             * Internal method. Remove the sub control at the specified index in the controlList.
             * 
             * @param idx {integer} the index to destroy the sub control at
             */
            removeSubControlByIndex: function(idx) {
                var subControl = this.controlList[idx];
                if (subControl !== undefined) {
                    subControl.beforeDestroy();
                    this.controlList.splice(idx, 1);
                }
            },

            /**
             * Method to call when to destroy only one specific child control of this Control.
             * Calling destroy() on the child results in a call to this method implicitly.
             * 
             * This method delegates the actual destroy call to the removeSubControlByIndex()
             * method.
             * 
             * @param subControl {Control} the child control to destroy
             */
            removeSubControl: function(subControl) {
                var i;
                for (i = 0; i < this.controlList.length; i++) {
                    if (this.controlList[i] === subControl) {
                        this.removeSubControlByIndex(i);
                        return;
                    }
                }

                throw "Control unknown. Control could not be deleted.";
            },

            /**
             * Destroys all registered sub controls.
             */
            removeAllSubControls: function() {
                while (this.controlList.length > 0) {
                    this.removeSubControlByIndex(0);
                }
            },

            /**
             * Internal method. When calling destroy on this ContainerControl, that method is
             * called. This method removes all sub controls and finally destroys this
             * ContainerControl.
             */
            beforeDestroy: function() {
                this.removeAllSubControls();
                this.base();
            }
        });
