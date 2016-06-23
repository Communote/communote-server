(function(namespace) {
    var EntityChooser = new Class({
        Extends: namespace.getConstructor('SearchBoxWidget'),

        categories: undefined,
        categoryCollapsible: true,
        // whether categories are collapsible if there is only one category
        categoryCollapsibleIfSingle: false,
        categoryCollapsibleOpenByDefault: true,
        categoryItemNoImageCssClass: 'cn-icon',
        // event to fire when a category item was selected and the category does not define a custom selectEvent
        categoryItemSelectEvent: 'entitySelected',
        categoryLoadingFeedback: true,
        categoryMarkOpenCssClass: 'open',
        chooserAttachWhere: 'after',
        chooserAttachSelector: '.control-autocompleter-position-source',
        chooserCssClass: 'cn-chooser-menu',
        chooserMarkOpenCssClass: 'open',
        chooserMarkOpenSelector: false,
        chooserSlide: undefined,
        chooserMenuElem: undefined,
        chooserToggleAttachSelector: '.control-autocompleter-position-source',
        chooserToggleAttachWhere: 'bottom',
        chooserToggleCssClass: 'cn-chooser-toggle',
        chooserToggleText: '',
        chooserToggleTitle: '',
        // set to false to render no toggle
        chooserToggleType: 'span',
        chooserWrapperCssClass: 'cn-chooser-menu-wrapper',
        chooserWrapperPosition: 'absolute',
        // z-index to apply to widget DOM node if chooserWrapperPosition is 'absolute' 
        chooserZIndex: 1,
        // just inherit server-side part from parent
        renderWidgetType: 'SearchBoxWidget',

        /**
         * @override
         */
        init: function() {
            var paramsToScan, i, paramName, param;
            this.parent();
            this.prepareCategories();
            paramsToScan = [ 'categoryCollapsible', 'categoryCollapsibleOpenByDefault',
                    'categoryItemNoImageCssClass', 'categoryItemSelectEvent',
                    'categoryMarkOpenCssClass', 'chooserMarkOpenSelector',
                    'chooserToggleAttachSelector', 'chooserToggleAttachWhere',
                    'chooserToggleCssClass', 'chooserToggleText', 'chooserToggleTitle',
                    'chooserZIndex' ];
            for (i = 0; i < paramsToScan.length; i++) {
                paramName = paramsToScan[i];
                param = this.getStaticParameter(paramName);
                if (param != undefined) {
                    this[paramName] = param;
                }
            }
        },

        /**
         * Activate and show the categories belonging to the mode represented by the given mode
         * options. This method must be called while currentMode still holds the old mode.
         * 
         * @param {Object} modeOpts The options of the mode that will be activated
         */
        activateCategories: function(modeOpts) {
            var i, length, categoryId, category, customSettings;
            var previousModeOpts = this.currentMode && this.modeOptions[this.currentMode];
            var previousCategories = previousModeOpts && previousModeOpts.activeCategories
                    && previousModeOpts.activeCategories.length;
            this.closeChooser();
            // TODO optimize by only removing when categories changed
            if (previousCategories) {
                // remove old categories and free slider reference
                this.chooserMenuElem.getChildren().destroy();
                for (i = 0; i < previousModeOpts.activeCategories.length; i++) {
                    category = this.categories[previousModeOpts.activeCategories[i]];
                    if (category) {
                        delete category.slide;
                        delete category.customTitle;
                        category.active = false;
                    }
                }
            }
            length = (modeOpts.activeCategories && modeOpts.activeCategories.length) || 0;
            if (length) {
                // prepare categories for rendering and add in order
                for (i = 0; i < length; i++) {
                    categoryId = modeOpts.activeCategories[i];
                    category = this.categories[categoryId];
                    customSettings = (modeOpts.customSettings && modeOpts.customSettings[categoryId])
                            || {};
                    if (category) {
                        category.active = true;
                        category.dirty = true;
                        category.customTitle = customSettings.title;
                        if (customSettings.collapsible != undefined) {
                            category.collapsible = customSettings.collapsible;
                        } else {
                            // use defaults
                            category.collapsible = length == 1 ? this.categoryCollapsibleIfSingle
                                    : this.categoryCollapsible;
                        }
                        if (category.collapsible) {
                            if (customSettings.openByDefault != undefined) {
                                category.openByDefault = customSettings.openByDefault;
                            } else {
                                category.openByDefault = this.categoryCollapsibleOpenByDefault;
                            }
                        }
                        this.addCategoryToChooser(categoryId, category);
                    }
                }
                // show toggle if necessary
                if (!previousCategories) {
                    this.domNode.getElementById(this.widgetId + '_chooserToggle').setStyle(
                            'display', '');
                }
            } else {
                if (previousCategories) {
                    this.domNode.getElementById(this.widgetId + '_chooserToggle').setStyle(
                            'display', 'none');
                }
            }
        },
        /**
         * Add a category to the chooser by creating the required elements and injecting them into
         * the chooserMenuElem.
         * 
         * @param {String} categoryId The ID of the category
         * @param {Object} category The object holding the category data
         */
        addCategoryToChooser: function(categoryId, category) {
            var categoryWrapper, title, titleElem, itemsWrapper, loadingOverlay;
            categoryWrapper = new Element('div', {
                'id': category.wrapperElemId,
                'class': 'cn-chooser-category'
            });
            if (category.cssClass) {
                categoryWrapper.addClass(category.cssClass);
            }
            if (category.customTitle != undefined) {
                title = category.customTitle;
            } else {
                title = category.title;
            }
            if (category.collapsible && !title) {
                // need title wrapper
                title = ' ';
            }
            if (title) {
                titleElem = new Element('div', {
                    'class': 'cn-chooser-category-title',
                    'text': title
                });
                categoryWrapper.grab(titleElem);
            }
            itemsWrapper = new Element('ul', {
                'class': 'cn-chooser-category-items'
            });
            categoryWrapper.grab(itemsWrapper);
            if (this.categoryLoadingFeedback) {
                loadingOverlay = new Element('div', {
                    'class': this.widgetController.markLoadingCssClass
                });
                loadingOverlay.setStyle('display', 'none');
                loadingOverlay.setStyle('position', 'absolute');
                categoryWrapper.grab(loadingOverlay);
            }
            this.chooserMenuElem.grab(categoryWrapper);
            if (category.collapsible) {
                category.slide = new Fx.Slide(itemsWrapper, {
                    resetHeight: true
                });
                category.slide.addEvent('start', this.categoryStateChanged.bind(this, categoryId));
                titleElem.addEvent('click', this.categoryTitleClicked.bind(category));
                // if the category should be open by default do not close it but simulate open
                // action
                if (category.openByDefault) {
                    this.categoryStateChanged(categoryId, true);
                } else {
                    category.slide.slideOut();
                }
            } else {
                this.categoryStateChanged(categoryId, true);
            }
        },
        /**
         * @override
         */
        autocompleterChoicesShown: function() {
            this.closeChooser();
            this.parent();
        },
        
        beforeRemove: function() {
            var elem = this.domNode.getElementById(this.widgetId + '_chooserToggle');
            if (elem) {
                elem.destroy();
            }
            elem = this.domNode.getElementById(this.widgetId + '_chooserWrapper');
            if (elem) {
                elem.destroy();
            }
            this.parent();
        },
        
        /**
         * Build the entity chooser drop-down menu.
         */
        buildChooserMenu: function() {
            var chooserMenuWrapper, chooserMenu, elem;
            chooserMenuWrapper = new Element('div', {
                'id': this.widgetId + '_chooserWrapper',
                'class': this.chooserWrapperCssClass
            });
            chooserMenuWrapper.setStyle('visibility', 'hidden');
            chooserMenuWrapper.set('html', '<div id="' + this.widgetId + '_chooserMenu" class="'
                    + this.chooserCssClass + '"></div>');
            // add chooser after element found by configurable selector or if not exist after input
            elem = this.domNode.getElement(this.chooserAttachSelector);
            if (elem) {
                chooserMenuWrapper.inject(elem, this.chooserAttachWhere);
            } else {
                chooserMenuWrapper.inject(this.searchInput, 'after');
            }
            if (Browser.name === 'opera') {
                // getStyle(height) might return NaN in opera when no height and no border is set, this
                // leads to a non-re-appearing chooser after it has been closed  
                if (chooserMenuWrapper.getStyle('border-top-style') == 'none') {
                    chooserMenuWrapper.setStyle('border-top-width', 0);
                }
                if (chooserMenuWrapper.getStyle('border-bottom-style') == 'none') {
                    chooserMenuWrapper.setStyle('border-bottom-width', 0);
                }
            }
            chooserMenu = chooserMenuWrapper.getFirst();
            this.chooserSlide = new Fx.Slide(chooserMenu, {
                wrapper: chooserMenuWrapper,
                resetHeight: true
            });
            // FX.slide opens the slider by default but we want it to be closed
            this.chooserSlide.hide();
            this.chooserSlide.addEvent('start', this.chooserSlideStart.bind(this));
            if (this.chooserMarkOpenCssClass) {
                this.chooserSlide.addEvent('complete', this.chooserSlideComplete.bind(this));
            }
            // override position style applied by Fx.Slide with configured value also remove
            // visibility style which is used to hide the element while the page is loading
            this.chooserSlide.wrapper.setStyles({
                position: this.chooserWrapperPosition,
                visibility: ''
            });
            if (this.chooserWrapperPosition == 'absolute' && this.chooserZIndex) {
                // apply a z-index to widget domNode to let chooser overflow siblings of the widget
                if (this.domNode.getStyle('position') == 'static') {
                    this.domNode.setStyle('position', 'relative');
                }
                this.domNode.setStyle('z-index', this.chooserZIndex);
            }
            this.buildChooserToggle();
            this.chooserMenuElem = chooserMenu;
        },

        /**
         * Build the toggle to open and close the entity chooser drop-down menu.
         */
        buildChooserToggle: function() {
            var chooserToggle, elem;
            if (this.chooserToggleType) {
                chooserToggle = new Element(this.chooserToggleType, {
                    'id': this.widgetId + '_chooserToggle',
                    'class': this.chooserToggleCssClass,
                    'title': this.chooserToggleTitle,
                    'text': this.chooserToggleText
                });
                // hide by default
                chooserToggle.setStyle('display', 'none');
                elem = this.domNode.getElement(this.chooserToggleAttachSelector);
                if (elem) {
                    chooserToggle.inject(elem, this.chooserToggleAttachWhere);
                } else {
                    // attach after search input
                    chooserToggle.inject(this.searchInput, 'after');
                }
                chooserToggle.addEvent('click', function() {
                    this.chooserSlide.toggle()
                }.bind(this))
            }
        },
        /**
         * Callback that is invoked when an entry in a category of the entity chooser got clicked.
         * The callback will fire the filter event defined in the category or a globally defined one
         * if the category has none.
         * 
         * @param {Object} category The category of the item that was clicked
         * @param {Object} token The token that should be passed as argument to the event
         */
        categoryItemClicked: function(category, token) {
            var eventName;
            this.closeChooser();
            if (category.active) {
                eventName = category.selectEvent || this.categoryItemSelectEvent;
                if (eventName) {
                    this.sendFilterGroupEvent(eventName, token);
                }
                // clear input when selecting an item
                if (this.clearInputOnSubmit) {
                    this.clearInput();
                }
            }
        },
        /**
         * Callback to be invoked when a category is opened or closed. If the category is dirty it
         * will be refreshed.
         * 
         * @param {String} categoryId ID of the changed category
         * @param {boolean} [newOpenState] If provided it is interpreted as the new state of the
         *            category where true means that the category is now open and false that it is
         *            closed. If omitted the category is considered open if it is not collapsible or
         *            the slide is closed.
         */
        // TODO not that clean, better add callbacks for slide start and complete events and a separate open method 
        categoryStateChanged: function(categoryId, newOpenState) {
            var open, wrapperElem;
            var category = this.categories[categoryId];
            if (category && category.active) {
                if (newOpenState != undefined) {
                    open = newOpenState;
                } else if (category.collapsible) {
                    // bound on start and thus will be closed
                    open = !category.slide.open;
                } else {
                    // always open since not collapsible
                    open = true;
                }
                if (open && category.dirty) {
                    this.refreshCategory(category);
                }
                wrapperElem = this.domNode
                        .getElementById(this.widgetId + '_category_' + categoryId);
                wrapperElem[open ? 'addClass' : 'removeClass'](this.categoryMarkOpenCssClass);
            }
        },

        /**
         * Called when the title of a collapsible category was clicked.
         * 
         * Note: is run in the context of the category
         */
        categoryTitleClicked: function() {
            this.slide.toggle();
        },

        /**
         * Callback that is invoked when the chooser menu finished opening or closing.
         */
        chooserSlideComplete: function() {
            // remove markOpen when closed
            if (!this.chooserSlide.open) {
                this.markChooserOpen(false);
            }
        },

        /**
         * Callback that is invoked when the chooser menu starts opening or closing.
         */
        chooserSlideStart: function() {
            var elem, autocompleter, categoryId, category;
            // close autocompleter if there is one
            autocompleter = this.currentMode && this.autocompleters[this.currentMode];
            if (autocompleter) {
                autocompleter.close();
            }
            // check for dirty open categories and refresh them
            for (categoryId in this.categories) {
                category = this.categories[categoryId];
                if (category.dirty && (!category.slide || category.slide.open)) {
                    this.refreshCategory(category);
                }
            }
            // apply markOpen class if the chooser is going to be opened
            if (!this.chooserSlide.open) {
                this.markChooserOpen(true);
            }
        },

        /**
         * Close the chooser without animation.
         */
        closeChooser: function() {
            if (this.chooserSlide.open) {
                this.chooserSlide.hide();
                this.markChooserOpen(false);
            }
        },

        /**
         * Create an element to be added as entry to the wrapper element of the given category.
         * 
         * @param {Object} category The category the entry belongs to
         * @param {String} label The text label of the category entry
         * @param {String} [title] The text to set as title attribute for the entry
         * @param {String} [imagePath] The relative path to the image of the item. If omitted a span
         *            with CSS class categoryItemNoImageCssClass will be added instead.
         * @param {Object} token The object to be passed to the event that is fired when the entry
         *            is clicked.
         */
        createCategoryItemElement: function(category, label, title, imagePath, token) {
            var elem = new Element('li', {
                'class': 'cn-chooser-category-item'
            });
            if (imagePath) {
                elem.grab(new Element('img', {
                    'src': buildRequestUrl(imagePath)
                }));
            } else if (this.categoryItemNoImageCssClass) {
                elem.grab(new Element('span', {
                    'class': this.categoryItemNoImageCssClass
                }));
            }
            elem.grab(new Element('span', {
                text: label,
                title: title
            }));
            elem.addEvent('click', this.categoryItemClicked.bind(this, category, token));
            return elem;
        },
        /**
         * @override
         */
        getListeningEvents: function() {
            return [ 'onNotesChanged' ];
        },
        /**
         * Mark all menu categories as dirty
         */
        markAllCategoriesDirty: function() {
            var categoryId;
            for (categoryId in this.categories) {
                if (this.categories.hasOwnProperty(categoryId)) {
                    this.markCategoryDirty(this.categories[categoryId]);
                }
            }
        },

        /**
         * Mark a menu category as dirty so it will be refreshed when necessary
         * 
         * @param {Object} category the category to mark dirty
         */
        markCategoryDirty: function(category) {
            if (category) {
                category.dirty = true;
                // refresh the category if active and open
                if (category.active && this.chooserSlide.open
                        && (!category.slide || category.slide.open)) {
                    this.refreshCategory(category);
                }
            }
        },
        /**
         * Mark the chooser as open or closed by applying/removing the configurable CSS class.
         * 
         * @param {Boolean} open True to mark as open, false to mark as closed
         */
        markChooserOpen: function(open) {
            if (this.chooserMarkOpenCssClass) {
                elem = this.domNode.getElement(this.chooserMarkOpenSelector);
                if (elem) {
                    elem[open ? 'addClass' : 'removeClass'](this.chooserMarkOpenCssClass);
                }
            }
        },

        onNotesChanged: function(data) {
            if (data.action != 'edit') {
                // last and most used topics might have changed if comment or new note was created
                this.markCategoryDirty(this.categories.lastUsedTopics);
                this.markCategoryDirty(this.categories.mostUsedTopics);
            }
        },
        /**
         * Prepare the built-in categories and merge with those provided via categories setting.
         */
        prepareCategories: function() {
            var categoryId, category;
            // add default categories
            this.categories = {};
            this.categories.myTopics = {
                cssClass: 'my-topics',
                refreshUrl: buildRequestUrl('/blog/control/getMyBlogs.do'),
                title: 'My Topics',
                type: 'topic'
            };
            this.categories.mostUsedTopics = {
                cssClass: 'most-used-topics',
                refreshUrl: buildRequestUrl('/blog/control/getMostUsedBlogs.do'),
                refreshParams: {
                    sortByTitle: true
                },
                title: 'Most used Topics',
                type: 'topic'
            };
            this.categories.lastUsedTopics = {
                cssClass: 'last-used-topics',
                refreshUrl: buildRequestUrl('/blog/control/getLastUsedBlogs.do'),
                title: 'Last used Topics',
                type: 'topic'
            };
            Object.merge(this.categories, this.getStaticParameter('categories'));
            for (categoryId in this.categories) {
                category = this.categories[categoryId];
                category.active = false;
                category.dirty = true;
                category.wrapperElemId = this.widgetId + '_category_' + categoryId;
            }
        },

        /**
         * Refreshes one of the menu categories
         * 
         * @param {Object} category The category to refresh
         */
        refreshCategory: function(category) {
            var elem, request, loadingOverlay, itemsWrapper, styles;
            if (!category || !category.active || category.refreshing) {
                return;
            }
            category.refreshing = true;
            elem = this.domNode.getElementById(category.wrapperElemId);
            if (this.categoryLoadingFeedback && this.chooserSlide.open) {
                // position loading overlay above category items
                itemsWrapper = elem.getElement('ul');
                styles = itemsWrapper.getCoordinates(itemsWrapper.getOffsetParent());
                loadingOverlay = elem.getLast();
                delete styles.right;
                delete styles.bottom;
                styles.display = '';
                loadingOverlay.setStyles(styles);
            }
            request = new Request.JSON({
                url: category.refreshUrl,
                onComplete: this.refreshCategoryComplete.bind(this, category)
            });
            request.get(category.refreshParams);
        },

        refreshCategoryComplete: function(category, response) {
            var elem;
            category.refreshing = false;
            category.dirty = false;
            elem = this.domNode.getElementById(category.wrapperElemId);
            if (category.type == 'topic') {
                this.updateTopicCategory(category, elem, response);
            }
            if (this.categoryLoadingFeedback) {
                // hide loading overlay
                elem.getLast().setStyle('display', 'none');
            }
        },

        /**
         * @override
         */
        refreshComplete: function(responseMetadata) {
            this.buildChooserMenu();
            this.parent(responseMetadata);
        },
        /**
         * Switch the search mode.
         * 
         * @param {String} mode The new mode to switch to
         */
        switchSearchMode: function(mode) {
            var modeOpts = this.modeOptions[mode];
            if (!modeOpts) {
                return;
            }
            this.activateCategories(modeOpts);
            this.parent(mode);
        },

        /**
         * Update a category showing topic data with the topics contained in the response.
         * 
         * @param {Object} category The category to update
         * @param {Element} wrapperElem The element wrapping the category in the chooser menu
         * @param {Object[]} response Array of objects that provide id, title, imagePath and
         *            optionally the description of the topic
         */
        updateTopicCategory: function(category, wrapperElem, response) {
            var itemsWrapper, i, topicItem, elem, topicData;
            itemsWrapper = wrapperElem.getElement('ul');
            // empty wrapper and free event handler
            itemsWrapper.getChildren().destroy();
            if (response && response.length > 0) {
                for (i = 0; i < response.length; i++) {
                    topicItem = response[i];
                    topicData = {
                        id: topicItem.id,
                        title: topicItem.title
                    };
                    elem = this.createCategoryItemElement(category, topicItem.title,
                            topicItem.description, topicItem.imagePath, topicData);
                    itemsWrapper.grab(elem);
                }
            } else if (category.noContentMessage) {
                elem = new Element('li', {
                    'class': 'cn-chooser-no-items-message',
                    'text': category.noContentMessage
                });
                itemsWrapper.grab(elem);
            }
        }
    });
    namespace.addConstructor('EntityChooserWidget', EntityChooser);
})(window.runtimeNamespace);