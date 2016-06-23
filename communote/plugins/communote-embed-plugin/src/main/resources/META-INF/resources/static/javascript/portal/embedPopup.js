/**
 * This javascript file is for the communote main page (portal) and will add the embed menu to the
 * tools and show the popup with the embed code.
 */
(function(window) {

    //store the current width and height shown in the snippet code
    var currentEmbedWidth, currentEmbedHeight;

    /**
     * add handler to change the snippet code if height or length is provided
     */
    function addLengthInputHandler(dialogContainer) {
        dialogContainer.getElementById('embedWidth')
                .addEvent('blur', snippetCodeChange);
        dialogContainer.getElementById('embedHeight').addEvent('blur',
                snippetCodeChange);
    }

    /**
     * Parse the given length as to be rendered in the snippet code
     */
    function parseLength(length) {
        var lengthNumber;
        if (length == null) {
            return null;
        }
        length = length.trim();
        lengthNumber = parseInt(length); 
        if (isNaN(lengthNumber)) {
            length = null;
        } else {
            //if length only contains numbers add a 'px' otherwise take the value as provided
            if (('' + lengthNumber) == length) {
                length += 'px';
            }
        }
        return length;
    }

    /**
     * Build the snippet string base on the current set view of Communote. For possible options see cnt-embed.js.
     * 
     * @param width {String} the width as to be rendered as minWidth option for the snippet.
     * @param height {String} the height as to be rendered as minHeight option for the snippet. 
     */
    function buildSnippetString(width, height) {
        var topicId, userId, tagIds, snippetString, contextId, viewId, activeParamStore;
        var communote = window.communote;
        var contextManager = communote.contextManager;
        var mainFilterParamStore = contextManager.getFilterGroup().getParameterStore();
        var baseUrl = communote.server.applicationUrl;
        var idx = baseUrl.indexOf('/microblog/');
        var params = {};
        params.server = baseUrl.substring(0, idx);
        params.communoteId = baseUrl.substring(idx + 11);

        topicId = mainFilterParamStore.getFilterParameter('targetBlogId');
        userId = mainFilterParamStore.getFilterParameter('userId');
        tagIds = mainFilterParamStore.getFilterParameter('tagIds');

        if (topicId != null) {
            params.edPreselectedTopicId = topicId;
            params.fiPreselectedTopicIds = topicId;
        }
        if (userId != null) {
            params.fiPreselectedAuthorIds = userId;
        }
        if (tagIds != null) {
            params.fiPreselectedTagIds = tagIds;
        }
        if (width) {
            params.minWidth = width;
        }
        if (height) {
            params.minHeight = height;
        }
        // current view related settings
        viewId = mainFilterParamStore.getFilterParameter('viewId');
        contextId = mainFilterParamStore.getFilterParameter('contextId');
        activeParamStore = contextManager.getFilterParameterStoreForContext(contextId, viewId);
        if (activeParamStore) {
            if (activeParamStore.getFilterParameter('showPostsForMe') === true) {
                params.msgViewSelected = 'mentions';
            } else if (activeParamStore.getFilterParameter('showFollowedItems') === true) {
                params.msgViewSelected = 'following';
            } else if (activeParamStore.getFilterParameter('showFavorites') === true) {
                params.msgViewSelected = 'favorites';
            } else {
                params.msgViewSelected = 'all';
            }
        }

        snippetString = '<div id="embed-communote"></div>\n\n';
        snippetString += '<script type="text/javascript" src="'
                + buildRequestUrl('/javascripts/packed.js?category=cnt-load-embed&'
                        + communote.server.resourceUrlParam) + '"></script>\n';
        snippetString += '<script type="text/javascript">\n';
        snippetString += 'communote.embedCommunote("#embed-communote", ';
        if (JSON.stringify) {
            // use string beautifier if available
            snippetString += JSON.stringify(params, null, 4);
        } else {
            snippetString += '\n' + JSON.decode(params) + '\n';
        }
        snippetString += ');\n</script>'

        return snippetString;
    }

    /**
     * Checks if the height and weight entered changed and changes the snippet code accordingly
     */
    function snippetCodeChange() {
        var width, height;
        
        if (this.id === 'embedWidth') {
            width = parseLength(this.value);
            if (currentEmbedWidth != width) {
                height = currentEmbedHeight;
                currentEmbedWidth = width; 
            } else {
                return false;
            }
        } else {
            height = parseLength(this.value);
            if (currentEmbedHeight != height) {
                currentEmbedHeight = height;
                width = currentEmbedWidth;
            } else {
                return false;
            }
        }
        //update snippet code
        document.getElementById('embed-communote-snippet-pre').set('text', buildSnippetString(
                currentEmbedWidth, currentEmbedHeight));
        return false;
    }

    /**
     * Setups and shows the popup with the snippet code
     */
    function showEmbedPopup() {
        var element, fullElement, textinput, buttons;
        var textinput;
        // reset
        currentEmbedWidth = null;
        currentEmbedHeight = null;
        
        textinput = new Element('div');
        textinput
                .set(
                        'html',
                        '<div class="cn-form-container"><div class="cn-field-50">' +
                        '<label class="cn-label" for="embedWidth">'+ getJSMessage('plugins.embed.snippet.popup.width') + '</label>' +
                        '<input id="embedWidth" class="cn-inputTxt" name="embedWidth" type="text" size="5" maxlength="10"/></div>' +
                        '<div class="cn-field-50 cn-last"><label class="cn-label" for="embedHeight">'+ getJSMessage('plugins.embed.snippet.popup.height') + '</label>' +
                        '<input id="embedHeight" class="cn-inputTxt" name="embedHeight" type="text" size="5" maxlength="10"/></div><span class="cn-clear"></span></div>');

        textinput.set('id', 'embed-communote-settings');
        element = new Element('div');
        element.set('id', 'embed-communote-snippet');
        element.grab(new Element('pre', {
            id: 'embed-communote-snippet-pre',
            text: buildSnippetString()
        }));

        fullElement = new Element('div');
        fullElement.set('html', '<p>' + getJSMessage('plugins.embed.snippet.popup.explanation')
                + '</p>');
        fullElement.set('title', getJSMessage('plugins.embed.snippet.popup.code.title'));
        fullElement.appendChild(element);
        fullElement.appendChild(textinput);

        buttons = [ {
            type: 'ok'
        }, {
            type: 'select',
            label: getJSMessage('plugins.embed.snippet.popup.select.button'),
            action: function() {
                communote.utils.selectTextOfElement('embed-communote-snippet-pre');
                return false;
            }
        } ];
        showDialog(getJSMessage('plugins.embed.snippet.popup.title'), fullElement, buttons, {
            onShowCallback: addLengthInputHandler
        });
    }

    /**
     * Init stuff to add the embed menu to the tools
     */
    window.communote.initializer
            .addBeforeWidgetScanCallback(function() {
                var embededToolProvider, mainPageContextManagementFilterGroup;
                var communote = window.communote;
                if (!communote || !communote.contextManager) {
                    return;
                }
                mainPageContextManagementFilterGroup = communote.contextManager.getFilterGroup();
                if (!mainPageContextManagementFilterGroup) {
                    return;
                }
                embededToolProvider = {
                    horizontalNavigationWidgetId: false,
                    paramStore: mainPageContextManagementFilterGroup.getParameterStore(),
                    knownViews: [],

                    /**
                     * Implementation of a method defined by the widget event listener interface.
                     */
                    getWidgetListenerGroupId: function() {
                        return undefined;
                    },
                    /**
                     * Implementation of a method defined by the widget event listener interface.
                     */
                    handleEvent: function(eventName, params) {
                        var viewId;
                        // only interested in the refreshcomplete of the MainPageHorizontalNavigation widgets
                        if (eventName == 'onWidgetRefreshComplete') {
                            if (params.widgetType == 'MainPageHorizontalNavigationWidget') {
                                if (!this.horizontalNavigationWidgetId) {
                                    this.horizontalNavigationWidgetId = params.widgetId;
                                } else if (this.horizontalNavigationWidgetId != params.widgetId) {
                                    return;
                                }
                                viewId = this.getViewId();
                                // reset views that were already activated for current context
                                this.knownViews = [ viewId ];
                                this.addEmbedMenu(viewId);
                            }
                        }
                    },

                    getObservedFilterParameters: function() {
                        return [ 'viewId' ];
                    },

                    filterParametersChanged: function(changedParams) {
                        var viewId;
                        if (changedParams.contains('viewId')) {
                            // ignore viewId changes that go along with contextId changes since the naviWidget will refresh
                            if (!changedParams.contains('contextId')) {
                                viewId = this.getViewId();
                                if (!this.knownViews.contains(viewId)) {
                                    this.knownViews.push(viewId);
                                    this.addEmbedMenu(viewId);
                                }
                            }
                        }
                    },
                    getViewId: function() {
                        return this.paramStore.getFilterParameter('viewId');
                    },
                    addEmbedMenu: function(viewId) {
                        var horizontalNavWidget, itemConfig, contextId;
                        contextId = this.paramStore.getFilterParameter('contextId');
                        // only add to notesOverview_all and the <entity-type>Selected_notes views
                        if (this.horizontalNavigationWidgetId
                                && ((contextId === 'notesOverview') || viewId === 'notes')) {
                            horizontalNavWidget = widgetController
                                    .getWidget(this.horizontalNavigationWidgetId);
                            if (horizontalNavWidget) {
                                itemConfig = {
                                    type: 'action',
                                    role: 'embedFeed',
                                    clickAction: showEmbedPopup,
                                    label: getJSMessage('plugins.embed.snippet.menu.title'),
                                    cssClass: 'cn-link'
                                };
                                horizontalNavWidget.addItemToMenuByRole(viewId, 'moreOptions',
                                        itemConfig);
                            }
                        }
                    }
                };

                communote.widgetController.registerWidgetEventListener('onWidgetRefreshComplete', embededToolProvider);
                mainPageContextManagementFilterGroup.addMember(embededToolProvider);
            });

})(this);
