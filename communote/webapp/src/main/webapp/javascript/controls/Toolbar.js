(function(namespace) {
    if (!namespace) {
        namespace = window;
    }
    var Toolbar = new Class({
        Implements: Options,

        options: {
            showCssClass: false,
            hideCssClass: false,
            cssClass: 'toolbar'
        },

        element: undefined,
        hidden: undefined,
        idBase: undefined,

        tools: null,
        menuItems: null,
        toolCount: 0,
        menuItemCount: 0,

        initialize: function(baseNode, options) {
            var elem;
            this.setOptions(options);
            this.tools = {};
            this.menuItems = {};
            this.idBase = String.uniqueID();
            elem = new Element('div');
            if (this.options.cssClass) {
                elem.addClass(this.options.cssClass);
            }
            baseNode.grab(elem);
            this.element = elem;
            this.hide();
        },
        
        addItemToMenu: function(menuToolId, itemConfig) {
            var menuElem;
            var menuTool = this.tools[menuToolId];
            if (menuTool && menuTool.type === 'menu') {
                menuElem = this.element.getElementById(menuToolId);
                this.addItemToMenuList(menuToolId, menuTool, menuElem.getElement('.cn-menu-list'), itemConfig);
            }
        },
        
        addItemToMenuByRole: function(role, itemConfig) {
            var menuToolId = this.getToolIdByRole(role);
            if (menuToolId) {
                this.addItemToMenu(menuToolId, itemConfig);
            }
        },

        addItemToMenuList: function(menuToolId, menuTool, menuList, itemConfig) {
            var elem, menuItemId, liElem;
            if (itemConfig.type == 'toggle') {
                elem = this.createToggleElem(itemConfig);
            } else if (itemConfig.type == 'action') {
                elem = this.createActionElem(itemConfig);
            }
            if (elem) {
                this.menuItemCount++;
                menuItemId = this.idBase + '_menuitem_' + this.menuItemCount;
                liElem = new Element('li');
                elem.setProperty('id', menuItemId);
                if (itemConfig.hidden) {
                    elem.setStyle('display', 'none');
                }
                liElem.grab(elem);
                menuList.grab(liElem);
                this.menuItems[menuItemId] = {
                    menuToolId: menuToolId,
                    role: itemConfig.role,
                    index: menuTool.items.length
                };
                menuTool.items.push({
                    id: menuItemId,
                    config: itemConfig
                });
            }
        },

        addMenu: function(menuConfig) {
            var toolId, menuListElem, menuTool, i;
            var menuElem = new Element('ul', {
                'class': 'cn-menu',
                'aria-haspopup': 'true'
            });
            var openLabel = menuConfig.openLabel || '';
            var closeLabel = menuConfig.closeLabel || '';
            var openLabelCssClass = menuConfig.openLabelCssClass || 'cn-icon cn-arrow';
            var closeLabelCssClass = menuConfig.closeLabelCssClass || 'cn-icon cn-arrow';
            var cssClass = menuConfig.cssClass ? menuConfig.cssClass + ' ' : '';
            // TODO make classes configurable
            var html = '<li><a class="' + cssClass
                    + 'cn-link" href="javascript:;"><span class="cn-icon"></span><span class="'
                    + openLabelCssClass + '">' + openLabel + '</span></a></li>';
            html += '<li><a class="'
                    + cssClass
                    + 'cn-link open cn-hidden" href="javascript:;"><span class="cn-icon"></span><span class="'
                    + closeLabelCssClass + '">' + closeLabel + '</span></a></li>';
            html += '<ul class="cn-menu-list"></ul>';
            menuElem.set('html', html);
            menuTool = {
                role: menuConfig.role,
                type: 'menu',
                items: [],
                hidden: menuConfig.hidden
            };
            toolId = this.internalAddTool(menuElem, menuTool);
            menuListElem = menuElem.getElement('.cn-menu-list');
            for (i = 0; i < menuConfig.items.length; i++) {
                this.addItemToMenuList(toolId, menuTool, menuListElem, menuConfig.items[i]);
            }
            return toolId;
        },

        addToggle: function(toggleConfig) {
            var elem = this.createToggleElem(toggleConfig);
            return this.internalAddTool(elem, toggleConfig);
        },

        addTool: function(toolConfig) {
            if (toolConfig.type == 'toggle') {
                return this.addToggle(toolConfig);
            } else if (toolConfig.type == 'menu') {
                return this.addMenu(toolConfig);
            }
        },

        applyToggleToolState: function(elem, oldState, newState) {
            elem.setProperty('title', newState.title || '');
            if (oldState && oldState.cssClass) {
                elem.removeClass(oldState.cssClass);
            }
            if (newState.cssClass) {
                elem.addClass(newState.cssClass);
            }
            elem.set('text', newState.label || '');
        },

        createActionElem: function(actionConfig) {
            var elem = new Element('a', {
                href: actionConfig.url || 'javascript:;'
            });
            if (actionConfig.cssClass) {
                elem.addClass(actionConfig.cssClass);
            }
            if (actionConfig.clickAction) {
                elem.addEvent('click', function() {
                    actionConfig.clickAction.call(null, elem, actionConfig)
                });
            }
            if (actionConfig.url && actionConfig.newWindow) {
                elem.setAttribute('target', '_blank');
            }
            elem.setProperty('title', actionConfig.title || '');
            elem.set('text', actionConfig.label || '');
            return elem;
        },

        createToggleElem: function(toggleConfig) {
            var elem = new Element('a', {
                href: 'javascript:;'
            });
            if (toggleConfig.cssClass) {
                elem.addClass(toggleConfig.cssClass);
            }
            elem.addEvent('click', this.toggleToolClicked.bind(this));
            this.applyToggleToolState(elem, null, toggleConfig.isOn ? toggleConfig.toggledOn
                    : toggleConfig.toggledOff);
            return elem;
        },

        destroy: function() {
            this.element.destroy();
        },

        getToolIdByRole: function(role) {
            var foundToolId = null;
            Object.each(this.tools, function(tool, toolId){
                if (tool.role == role) {
                    foundToolId = toolId;
                    return;
                }
            });
            return foundToolId;
        },
        
        getTool: function(toolId) {
            return this.tools[toolId];
        },

        getToolOrMenuItem: function(id) {
            var menuItemDescr, menuTool;
            if (id.contains(this.idBase + '_tool_')) {
                return this.tools[id];
            } else {
                menuItemDescr = this.menuItems[id];
                if (menuItemDescr) {
                    menuTool = this.tools[menuItemDescr.menuToolId];
                    if (menuTool) {
                        return menuTool.items[menuItemDescr.index].config;
                    }
                }
            }
            return null;
        },

        hide: function() {
            var setStyle = true;
            if (!this.hidden) {
                if (this.options.showCssClass) {
                    this.element.removeClass(this.options.showCssClass);
                    setStyle = false;
                }
                if (this.options.hideCssClass) {
                    this.element.addClass(this.options.hideCssClass);
                    setStyle = false;
                }
                if (setStyle) {
                    this.element.setStyle('display', 'none');
                }
                this.hidden = true;
            }
        },
        
        hideTool: function(toolId) {
            var elem;
            var tool = this.getToolOrMenuItem(toolId);
            if (tool && !tool.hidden) {
                elem = this.element.getElementById(toolId);
                if (elem) {
                    elem.setStyle('display', 'none');
                }
                tool.hidden = true;
            }
        },

        internalAddTool: function(elem, toolDetails) {
            var toolId;
            this.toolCount++;
            toolId = this.idBase + '_tool_' + this.toolCount;
            elem.set('id', toolId);
            if (toolDetails.hidden) {
                elem.setStyle('display', 'none');
            }
            this.tools[toolId] = toolDetails;
            this.element.grab(elem);
            return toolId;
        }.protect(),
        
        

        show: function() {
            var setStyle = true;
            if (this.hidden !== false) {
                if (this.options.hideCssClass) {
                    this.element.removeClass(this.options.hideCssClass);
                    setStyle = false;
                }
                if (this.options.showCssClass) {
                    this.element.addClass(this.options.showCssClass);
                    setStyle = false;
                }
                if (setStyle) {
                    this.element.setStyle('display', '');
                }
                this.hidden = false;
            }
        },
        
        showTool: function(toolId) {
            var elem;
            var tool = this.getToolOrMenuItem(toolId);
            if (tool && tool.hidden) {
                elem = this.element.getElementById(toolId);
                if (elem) {
                    elem.setStyle('display', '');
                }
                tool.hidden = false;
            }
        },

        toggleToolClicked: function(event) {
            var newState, oldState;
            var elem = event.target;
            this.toggleToolSwitchState(elem.id, null, true, elem);
            return false;
        },

        toggleToolSwitchState: function(id, activate, callAction, elem) {
            var newState, oldState;
            var toolConfig = this.getToolOrMenuItem(id);
            if (!toolConfig) {
                return false;
            }
            if (activate == null) {
                activate = !toolConfig.isOn;
            } else if (activate == toolConfig.isOn) {
                return false;
            }
            if (activate) {
                newState = toolConfig.toggledOn;
                oldState = toolConfig.toggledOff
            } else {
                newState = toolConfig.toggledOff;
                oldState = toolConfig.toggledOn;
            }
            if (!elem) {
                elem = this.element.getElementById(id);
            }
            if (!callAction || !oldState.action
                    || oldState.action.call(null, elem, oldState) !== false) {
                this.applyToggleToolState(elem, oldState, newState);
                toolConfig.isOn = activate;
                return true;
            }
            return false;
        },

        updateActionElement: function(elem, oldConfig, newConfig) {
            // update defined values
            if (newConfig.url !== undefined && newConfig.url != oldConfig.url) {
                elem.setProperty('href', newConfig.url || 'javascript:;');
                oldConfig.url = newConfig.url;
            }
            if (newConfig.title !== undefined && newConfig.title != oldConfig.title) {
                elem.setProperty('title', newConfig.title || '');
                oldConfig.title = newConfig.title;
            }
            if (newConfig.label !== undefined && newConfig.label != oldConfig.label) {
                elem.set('text', newConfig.label || '');
                oldConfig.label = newConfig.label;
            }
            // TODO other properties
        },

        updateMenuItem: function(menuToolId, index, newItemConfig) {
            var item;
            var tool = this.getTool(menuToolId);
            if (tool && tool.items) {
                item = tool.items[index - 1];
                // currently only supporting action items
                if (newItemConfig.type == 'action') {
                    this.updateActionElement(this.element.getElementById(item.id), item.config,
                            newItemConfig);
                }
            }
        },

        /**
         * Update the state of a toggle item to a new state.
         * 
         * @param {String} toolId ID of toggle tool that was returned by addToogle
         * @param {boolean} activate True to activate the toggledOn state or false to activate the
         *            toggledOf state
         * @param {boolean} callAction Whether to invoke the action of the old state if the new
         *            state is not the current state
         * @return {boolean} Whether the new state was set
         */
        updateToggleItemState: function(toolId, activate, callAction) {
            return this.toggleToolSwitchState(toolId, activate, callAction, null);
        }

    });

    if (namespace.addConstructor) {
        namespace.addConstructor('Toolbar', Toolbar);
    } else {
        namespace.Toolbar = Toolbar;
    }
})(window.runtimeNamespace);