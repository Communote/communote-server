(function() {

    var pluginDef = {

        settingDefinitions: null,
        settingIds: null,
        buttonCssClass: null,
        buttonImageUrl: null,
        id: 0,
        textProperty: null,
        editor: null,
        /**
         * Initializes the plugin, this will be executed after the plugin has been created. This
         * call is done before the editor instance has finished it's initialization so use the
         * onInit event of the editor instance to intercept that event.
         * 
         * @param {tinymce.Editor} editor Editor instance that the plugin is initialized in.
         * @param {string} url Absolute URL to where the plugin is located.
         */
        init: function(editor, url) {
            var i;
            var options = editor.getParam('changesettings_options');
            if (options) {
                this.buttonImageUrl = options.buttonImageUrl;
                this.buttonCssClass = options.buttonCssClass;
                this.settingIds = [];
                this.settingDefinitions = {};
                this.parseDefinitions(options.settingDefinitions);
            }
            this.editor = editor;
            this.textProperty = (document.createElement('div').textContent == null) ? 'innerText'
                    : 'textContent';
        },

        parseDefinitions: function(settingDefinitions) {
            var i, id, settingDef;
            if (!settingDefinitions) {
                return;
            }
            for (i = 0; i < settingDefinitions.length; i++) {
                settingDef = this.buildSettingDefinition(settingDefinitions[i]);
                if (settingDef) {
                    id = 'mceChangeSettingItem_' + this.id;
                    settingDef.id = id;
                    this.settingIds.push(id);
                    this.settingDefinitions[id] = settingDef;
                    this.id++;
                }
            }
        },

        buildSettingDefinition: function(options) {
            var setting;
            if (!options.label || options.type != 'toggle' && options.type != 'action') {
                return null;
            }
            setting = {};
            setting.type = options.type;
            setting.label = options.label;
            setting.disableInFullscreen = !!options.disableInFullscreen;
            setting.cssClass = 'mceChangeSetting-' + setting.type;
            if (options.cssClass) {
                setting.cssClass += ' ' + options.cssClass;
            }
            setting.clickCallback = options.clickCallback;
            if (setting.type == 'toggle') {
                setting.labelToggledOff = options.labelToggledOff || options.label;
                setting.toggledOff = !!options.toggledOff;
            }
            return setting;
        },

        actionSettingClicked: function(element) {
            var splitButton, menuItem;
            var def = this.settingDefinitions[element.id];
            if (def.clickCallback) {
                splitButton = this.editor.controlManager.get('changesettings');
                menuItem = splitButton.menu.items[def.id];
                def.clickCallback.call(null, this.editor, menuItem, element, def.toggledOff);
            }
        },
        toggleSettingClicked: function(element) {
            var textElem, splitButton, menuItem, newLabel;
            var def = this.settingDefinitions[element.id];
            splitButton = this.editor.controlManager.get('changesettings');
            menuItem = splitButton.menu.items[def.id];
            def.toggledOff = !def.toggledOff;
            if (def.clickCallback) {
                def.clickCallback.call(null, this.editor, menuItem, element, def.toggledOff);
            }
            textElem = tinymce.DOM.select('.mceText', element)[0];
            newLabel = def.toggledOff ? def.labelToggledOff : def.label;
            textElem.title = newLabel;
            textElem[this.textProperty] = newLabel;
            this.setToggledOffStateOfSetting(menuItem, def.toggledOff);
            return false;
        },

        setToggledOffStateOfSetting: function(menuItem, off) {
            menuItem.setState('ToggledOff', off);
        },

        renderSettingsMenu: function(splitButton, menu) {
            var i, def, options, menuItem;
            // override default onHideMenu handler because it focuses the button element which
            // cannot be undone in chrome
            menu.onHideMenu.addToTop(function() {
                splitButton.hideMenu();
                return false;
            });
            for (i = 0; i < this.settingIds.length; i++) {
                def = this.settingDefinitions[this.settingIds[i]];
                options = {};
                options['class'] = def.cssClass;
                options.id = def.id;
                if (def.type != 'toggle' || !def.toggledOff) {
                    options.title = def.label;
                } else {
                    options.title = def.labelToggledOff;
                }
                options.onclick = def.type == 'toggle' ? this.toggleSettingClicked
                        : this.actionSettingClicked;
                options.onclick = options.onclick.bind(this);
                menuItem = menu.add(options);
                if (def.toggledOff) {
                    this.setToggledOffStateOfSetting(menuItem, true);
                }
                if (def.disableInFullscreen && this.editor.id == 'mce_fullscreen') {
                    menuItem.setDisabled(true);
                    // state isn't set correctly by setDisabled, do it manually
                    menuItem.setState('Disabled', true);
                }
            }
        },

        /**
         * Creates control instances based in the incomming name. This method is normally not needed
         * since the addButton method of the tinymce.Editor class is a more easy way of adding
         * buttons but you sometimes need to create more complex controls like listboxes, split
         * buttons etc then this method can be used to create those.
         * 
         * @param {String} controlName Name of the control to create.
         * @param {tinymce.ControlManager} controlManager Control manager to use inorder to create
         *            new control.
         * @return {tinymce.ui.Control} New control instance or null if no control was created.
         */
        createControl: function(controlName, controlManager) {
            var button, i, disableButton;
            switch (controlName) {
            case 'changesettings':
                button = controlManager.createSplitButton('changesettings', {
                    title: 'changesettings.settings_desc',
                    'class': this.buttonCssClass,
                    image: this.buttonImageUrl,
                    onclick: function() {
                        var splitButton = controlManager.get('changesettings');
                        // toggle the menu
                        if (splitButton.isMenuVisible) {
                            splitButton.hideMenu();
                        } else {
                            splitButton.showMenu();
                        }
                        return true;
                    }
                });
            }
            if (button) {
                // add menu items when the menu is rendered for the first time
                button.onRenderMenu.add(this.renderSettingsMenu, this);
                disableButton = (this.settingIds.length == 0);
                if (!disableButton && this.editor.id == 'mce_fullscreen') {
                    disableButton = true;
                    // check if there is at least one active setting
                    for (i = 0; i < this.settingIds.length; i++) {
                        if (!this.settingDefinitions[this.settingIds[i]].disableInFullscreen) {
                            disableButton = false;
                            break;
                        }
                    }
                }
                // set button state after rendering is done since it does not work early
                this.editor.onPostRender.add(function(ed, cm) {
                    button.disabled = !disableButton;
                    button.setDisabled(disableButton);
                });
            }
            return button;
        },

        /**
         * Returns information about the plugin as a name/value array. The current keys are
         * longname, author, authorurl, infourl and version.
         * 
         * @return {Object} Name/value array containing information about the plugin.
         */
        getInfo: function() {
            return {
                longname: 'Change settings plugin',
                author: 'rwi',
                authorurl: '',
                infourl: '',
                version: "0.1"
            };
        }
    };
    // use tool for external plugins which handles delayed registration after tinymce is loaded
    if (window.tinymceExternalPluginInitializer) {
        tinymceExternalPluginInitializer.add('tinymce.plugins.ChangeSettingsPlugin',
                'changesettings', pluginDef);
    } else {
        tinymce.create('tinymce.plugins.ChangeSettingsPlugin', pluginDef);
        // Register plugin
        tinymce.PluginManager.add('changesettings', tinymce.plugins.ChangeSettingsPlugin);
    }
})();