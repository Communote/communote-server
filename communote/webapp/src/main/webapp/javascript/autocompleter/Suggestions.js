var AutocompleterSuggestions = new Class({

    Implements: [ Options ],
    options: {
        // array of objects describing categories, each object must have an id and
        // can have a title. The order of the array defines the order in which the categories are displayed.
        categories: null,
        injectChoiceContentCallback: null,
        dataSourceOptions: {},
        fxOptions: {},
        // if null the the choices container will be added at the end of the body, otherwise it is added
        // after the given element
        insertAfterElement: null,
        // whether the suggestions container should get position fixed or absolute
        fixedPosition: false,
        // whether to start from end or top when navigating through suggestions with keyboard
        flipAround: false,
        overflow: true,
        overflowMargin: 25,
        markQuery: true,
        // whether to encode reserved XML characters, mainly to avoid XSS
        markQueryEncode: true,
        // whether to work case sensitive when highlighting the query value
        markQueryCaseSensitive: false,
        // whether to match the query value against the start of the words in the suggestion when
        // highlighting the query
        markQueryWordStart: false,
        // regex string that is used when markQueryWordStart is true to match the start of a word
        markQueryWordStartPattern: '^|[\\s\[\(]',
        maxChoices: 15,
        minLength: 1,
        // if true and width is set to 'auto' or 'auto-min' the edge for positioning the suggestions
        // will depend on the available size of the viewport. This will also reposition the
        // suggestions with update of one of the categories.
        positionAdaptive: true,
        // if true position the suggestions at the right edge of the positioning source otherwise
        // at the left. If the determine position callback does not return a rightOffset value the
        // suggestions will be placed at the left edge
        positionAtRightEdge: false,
        positionOffset: {
            x: 0,
            y: 0
        },
        // reposition the shown choices when resizing the window
        positionOnWindowResize: true,
        selectFirst: false,
        // whether to show the category titles
        showCategoryTitle: true,
        // whether to show the category title when there is only one category
        showCategoryTitleForSingleCategory: false,
        defaultCategoryTitle: null,
        autocompleterContainerCssClass: null,
        categoryContainerCssClass: null,
        choiceCssClass: null,
        focusedChoiceCssClass: 'autocompleter-choice-focused',
        // if false the choices/loading feedback won't be shown while loading results
        showChoicesWhileLoading: true,
        // if true the choices will be closed when all categories finished loading
        // and no results where found
        hideChoicesWhenEmpty: false,
        categoryTitleCssClass: null,
        showLoadingFeedback: false,
        loadingFeedbackOverlayCssClass: 'autocompleter-loading-overlay',
        loadingFeedbackCssClass: 'autocompleter-loading',
        // optional text to be shown when refreshing a category and there are no choices yet
        loadingFeedbackHintText: null,
        loadingFeedbackHintCssClass: 'autocompleter-loading-hint',
        // scroll choices into view
        visibleChoices: true,
        // can be false to set no width, a number for a fixed width, 'inherit' to take the width from
        // the positioning source if the callback returns the value or 'auto' or 'auto-min' to set
        // width to 'auto' and the later also sets the min-width to the width of the positioning
        // source
        width: 360,
        // whether a max-width should be set. Can be a number, true or false. If true the max-width
        // is set to not exceed the viewport. Will be ignored if width is not auto or auto-min. 
        maxWidth: true,
        zIndex: 42
    },
    dataSources: null,
    // unique prefix for DOM node IDs
    uniqueIdPrefix: '',
    // unique prefix for DOM nodes of category containers
    categoryIdPrefix: '',
    // shown categories as an array, sorted by rendering order, that holds the details,
    // of each category. It will at least contain the default category.
    categories: null,
    // array of DOM node IDs of the category containers, sorted like the categories array 
    categoryNodeIds: null,
    // mapping from the category ID to the index of the category in the categories array
    categoryIdToIndex: null,
    // holds the number of categories that are currently loading
    categoriesLoading: 0,
    // the current query
    queryValue: null,
    // sum of choices of all categories, can be inconsistent to entries shown while
    // categories are being updated  
    choicesFound: 0,
    // CSS classes that are always added
    autocompleterContainerCssClass: 'autocompleter-wrapper',
    categoryContainerCssClass: 'autocompleter-category-wrapper',
    categoryChoicesContainerCssClass: 'autocompleter-choices-wrapper',
    choiceCssClass: 'autocompleter-choice',
    categoryTitleCssClass: 'autocompleter-category-title',
    // DOM node of choice that is currently focused
    focusedChoice: null,

    onChoiceFocusedCallback: null,
    onChoiceSelectedCallback: null,
    onHideCallback: null,
    onShowCallback: null,
    // holds the function to be called when the position of the suggestion needs to be determined
    determinePositionCallback: null,
    onChoicesFoundCallback: null,
    relativePositioning: false,
    boundWindowResizeHandler: null,

    initialize: function(options, dataSources) {
        this.uniqueIdPrefix = 'autocompleter_' + String.uniqueID();
        this.categoryIdPrefix = this.uniqueIdPrefix + '_cat';
        this.setOptions(options);
        if (this.options.width != 'auto' && this.options.width != 'auto-min') {
            this.options.positionAdaptive = false;
            this.options.maxWidth = false; 
        }
        this.parseCategories();
        // create reference to callbacks if defined and functions
        if (typeof this.options.injectChoiceContentCallback == 'function') {
            this.injectChoiceContentCallback = this.options.injectChoiceContentCallback;
        }
        if (typeof this.options.createCategorySummaryCallback == 'function') {
            this.createCategorySummaryCallback = this.options.createCategorySummaryCallback;
        }
        this.build();
        this.dataSources = Array.from(dataSources);
        this.attachEventHandlersToDataSources();
        this.queryValue = null;
    },

    /**
     * Parses and validates the categories. A category is considered valid if it has an id
     * attribute. If no valid categories are defined a default category with id 'default' is added.
     * This method sets the categories member.
     */
    parseCategories: function() {
        var i;
        var categories = this.options.categories;
        this.categories = [];
        this.categoryIdToIndex = {};
        if (categories && categories.length) {
            for (i = 0; i < categories.length; i++) {
                this.addCategory(categories[i]);
            }
        }
        if (this.categories.length === 0) {
            // add default category
            this.addCategory({
                id: 'default',
                title: this.options.defaultCategoryTitle
            });
        }
    },

    /**
     * Adds a category to the categories store (categories member) if it contains contains an id
     * attribute and does not yet exist.
     * 
     * @param {Object} category Object describing the category
     * @returns {boolean} true if the provided category was valid and category was created
     */
    addCategory: function(category) {
        var catId, index, newCategory;
        catId = category.id;
        // ignore invalid or existing categories
        if (catId == null || this.categoryIdToIndex[catId]) {
            return false;
        }
        index = this.categories.length;
        // copy all details of the category because DataSources might find it useful
        newCategory = Object.merge({}, category);
        // set/overwrite details that are required by autocompleter
        newCategory.choices = [];
        newCategory.index = index;
        newCategory.isLoading = false;
        newCategory.showsLoadingFeedback = false;
        // validate callback if defined
        if (newCategory.injectChoiceContentCallback) {
            if (!typeof newCategory.injectChoiceContentCallback == 'function') {
                delete newCategory.injectChoiceContentCallback;
            }
        }
        // save details of category
        this.categories.push(newCategory);
        // save index of category for faster lookup
        this.categoryIdToIndex[catId] = index;
        return true;
    },

    attachEventHandlersToDataSources: function() {
        var dataSource, i;
        var boundCompleteHandler = this.handleDataSourceQueryComplete.bind(this);
        var boundFailureHandler = this.handleDataSourceQueryFailed.bind(this);
        var boundStartingeHandler = this.handleDataSourceQueryStarting.bind(this);
        for (i = 0; i < this.dataSources.length; i++) {
            dataSource = this.dataSources[i];
            dataSource.addEvent('queryComplete', boundCompleteHandler);
            dataSource.addEvent('queryFailed', boundFailureHandler);
            dataSource.addEvent('queryStarting', boundStartingeHandler);
        }
    },

    /**
     * build - Initialize DOM
     * 
     * Builds the html structure for choices and appends the events to the element. Override this
     * function to modify the html generation.
     */
    build: function() {
        var insertAfterElem;
        this.choices = new Element('div', {
            'class': this.autocompleterContainerCssClass,
            'styles': {
                'zIndex': this.options.zIndex,
                'position': this.options.fixedPosition ? 'fixed' : 'absolute',
                'display': 'none'
            }
        });
        if (this.options.autocompleterContainerCssClass) {
            this.choices.addClass(this.options.autocompleterContainerCssClass);
        }

        if (this.options.insertAfterElement) {
            insertAfterElem = document.id(this.options.insertAfterElement);
        }
        if (insertAfterElem) {
            this.choices.inject(insertAfterElem, 'after');
            this.relativePositioning = insertAfterElem.getParent().tagName !== 'BODY';
        } else {
            this.choices.inject(document.body);
        }
        this.buildCategoryContainers();
        this.fx = (!this.options.fxOptions) ? null : new Fx.Tween(this.choices, Object.merge({}, {
            'property': 'opacity',
            'link': 'cancel',
            'duration': 200
        }, this.options.fxOptions)).addEvent('onStart', Chain.prototype.clearChain).set(0);

        // block mousedown event to prevent blur event in input element when clicking scrollbars, a suggestion
        // or just somewhere in the suggestion dropdown. Blur of input is handled
        // TODO when adding scrollbars support for suggestion this must be checked with IE 9 which
        // had same misbehavior like the older IEs
        if (Browser.name === 'ie' && Browser.version <= 9) {
            // need some custom event handling for older IEs. You cannot prevent IE from removing
            // focus from an input so we must refocus it manually. Problem is that IE doesn't remember
            // the caret position in the input and puts it always at the beginning. So we only refocus
            // when focus is lost because of using the scrollbar alongside the suggestions (which isn't
            // supported yet). 
            this.choices.addEvent('mousedown', function(e) {
                if (e.target === this.choices) {
                    this.refocusOnBlur = true;
                    return false;
                }
            }.bind(this));
        } else {
            this.choices.addEvent('mousedown', function(e) {
                return false;
            });
        }
    },

    /**
     * Build the containers for all categories that are stored in the member 'categories'.
     */
    buildCategoryContainers: function() {
        var i, showTitles;
        var categories = this.categories;
        this.categoryNodeIds = [];
        if (this.options.showCategoryTitle) {
            showTitles = this.categories.length > 1
                    || this.options.showCategoryTitleForSingleCategory;
        } else {
            showTitles = false;
        }
        for (i = 0; i < categories.length; i++) {
            this.buildCategoryContainer(categories[i], showTitles);
        }
    },

    /**
     * Builds the HTML for a category. The HTML will contain some wrapper elements, a title element
     * if one should be created and an overlay that is shown when the category is refreshing. The
     * latter is only created if the option showLoadingFeedback is given.
     * 
     * @param {Object} category Object describing the category
     * @param {Boolean} showTitle Whether the category title should be shown
     */
    buildCategoryContainer: function(category, showTitle) {
        var container, choicesWrapper, cssClass, titleElem, overlayElem, nodeId;

        nodeId = this.categoryIdPrefix + category.index;
        container = new Element('div', {
            'id': nodeId,
            'class': this.categoryContainerCssClass
        });
        // CSS class can be defined per category or globally 
        cssClass = category.cssClass || this.options.categoryContainerCssClass;
        if (cssClass) {
            container.addClass(cssClass);
        }
        this.choices.grab(container);
        // show title if defined globally and not disabled for the category
        if (showTitle && category.title !== false) {
            titleElem = new Element('div', {
                'class': this.categoryTitleCssClass
            });
            cssClass = this.options.categoryTitleCssClass;
            if (cssClass) {
                titleElem.addClass(cssClass);
            }
            // set whitespace as title if not defined
            titleElem.set('text', category.title || ' ');
            container.grab(titleElem);
        }
        // add ul which will hold the choices
        choicesWrapper = new Element('ul', {
            'class': this.categoryChoicesContainerCssClass
        });
        container.grab(choicesWrapper);
        if (this.options.showLoadingFeedback) {
            // create overlay container
            overlayElem = new Element('div', {
                'id': nodeId + 'overlay',
                'class': this.options.loadingFeedbackOverlayCssClass,
                'styles': {
                    'position': 'absolute',
                    'display': 'none'
                }
            });
            container.grab(overlayElem);
        }
        // save node ID for fast access
        this.categoryNodeIds.push(nodeId);
    },

    /**
     * Destroy the suggestions element and free other resources hold by this instance. This method
     * should be called when the autocompleter isn't needed anymore.
     */
    destroy: function() {
        this.focusedChoice = null;
        // cancel any fx operation before destroying the element
        if (this.fx) {
            this.fx.cancel();
        }
        // TODO maybe we should call destroy on the dataSource too?
        this.choices = this.choices.destroy();
        if (this.boundWindowResizeHandler) {
            document.window.removeEvent('resize', this.boundWindowResizeHandler);
            this.boundWindowResizeHandler = null;
        }
    },

    handleDataSourceQueryComplete: function(isUpdate, categoryId, tokens, metaData) {
        if (isUpdate) {
            this.updateCategory(categoryId, tokens, metaData);
        } else {
            this.extendCategory(categoryId, tokens, metaData);
        }
    },

    handleDataSourceQueryFailed: function(isUpdate, categoryIds) {
        var i;
        if (typeof categoryIds == 'string') {
            this.hideLoadingFeedbackForCategory(categoryIds);
        } else {
            for (i = 0; i < categoryIds.length; i++) {
                this.hideLoadingFeedbackForCategory(categoryIds[i]);
            }
        }
    },

    handleDataSourceQueryStarting: function(isUpdate, categoryIds) {
        var i;
        if (typeof categoryIds == 'string') {
            this.showLoadingFeedbackForCategory(categoryIds);
        } else {
            for (i = 0; i < categoryIds.length; i++) {
                this.showLoadingFeedbackForCategory(categoryIds[i]);
            }
        }
    },

    /**
     * Resets the internal query cache to issue another query with the current input.
     */
    resetQuery: function() {
        var i;
        this.queryValue = null;
        this.hideChoices();
        for (i = 0; i < this.dataSources.length; i++) {
            this.dataSources[i].resetCaches();
        }
    },

    /**
     * Returns the wrapper element of a category
     * 
     * @param {number} index The index of the category.
     * @returns {Element} the element
     */
    getCategoryContainerElement: function(index) {
        var nodeId = this.categoryNodeIds[index];
        return this.choices.getElement('#' + nodeId);
    },

    /**
     * Searches for the next category that is not loading.
     * 
     * @param {number} startIdx The index to start from with the search. The first category to check
     *            will be the next category, where next is defined by the forward parameter. If the
     *            value is less than 0 the search will start at the boundaries of the category
     *            array.
     * @param {boolean} forward If true, the search will check the categories after startIdx.
     *            Otherwise the search will work backwards and check the categories with a lower
     *            index.
     * @param {boolean} notEmpty If true only categories with choices will be considered
     * @param {boolean} skipLoading If false the search for the next choice will stop as soon as a
     *            loading category is encountered, otherwise these categories will be ignored and
     *            the search continues with the next category.
     * @param {boolean} flipAround Whether to start from end or top when nothing was found
     * @returns {Object} an object holding the details (id, index, choices array, isLoading flag) of
     *          the category or null if none was found
     * 
     */
    findCategory: function(startIdx, forward, notEmpty, skipLoading, flipAround) {
        var i, category, foundCategory;
        if (forward) {
            if (startIdx < 0) {
                startIdx = 0;
            } else {
                startIdx++;
            }
            for (i = startIdx; i < this.categories.length; i++) {
                category = this.categories[i];
                if (!skipLoading && category.isLoading) {
                    break;
                }
                if (!category.isLoading && (!notEmpty || category.choices.length > 0)) {
                    foundCategory = category;
                    break;
                }
            }
        } else {
            if (startIdx < 0) {
                startIdx = this.categories.length - 1;
            } else {
                startIdx--;
            }
            for (i = startIdx; i >= 0; i--) {
                category = this.categories[i];
                if (!skipLoading && category.isLoading) {
                    break;
                }
                if (!category.isLoading && (!notEmpty || category.choices.length > 0)) {
                    foundCategory = category;
                    break;
                }
            }
        }
        // only flip around if not all categories are loading, or if not loading anymore only if more than one choice exists
        if (!foundCategory
                && flipAround
                && ((this.categoriesLoading > 0 && this.categoriesLoading < this.categories.length) || (this.categoriesLoading == 0 && this.choicesFound > 1))) {
            return this.findCategory(-1, forward, notEmpty, skipLoading, false);
        }
        return foundCategory;
    },

    /**
     * Helper to focus the first choice. Won't do anything if the first category is still loading.
     */
    focusFirstChoice: function() {
        if (!this.focusedChoice && this.choicesFound > 0) {
            this.focusNextChoice(false, false);
        }
    },

    /**
     * Focus the next suggestion that is shown after or before the currently focused suggestion. If
     * there is currently no focused suggestion the first or last suggestion will be focused.
     * Suggestions of categories that are still loading will be ignored.
     * 
     * @param {boolean} up If true the suggestion preceding the currently focused suggestion is
     *            focused. If false, the one following the current one is focused.
     * @param {boolean} skipLoading If false the search for the next choice will stop as soon as a
     *            loading category is encountered, otherwise these categories will be ignored and
     *            the search continues with the next category.
     */
    focusNextChoice: function(up, skipLoading) {
        var elemToHighlight, moveAction, categoryContainer, category, catIdx;
        var choicesWrapper, choiceElems, focusedElem;
        // find the next selectable element by checking current and neighboring categories
        if (this.focusedChoice) {
            focusedElem = this.focusedChoice.elem;
            moveOperation = up ? 'getPrevious' : 'getNext';
            elemToHighlight = focusedElem[moveOperation]('.' + this.choiceCssClass);
            if (!elemToHighlight) {
                catIdx = focusedElem.retrieve('categoryIndex');
            }
        } else {
            catIdx = -1;
        }
        if (!elemToHighlight) {
            category = this.findCategory(catIdx, !up, true, skipLoading, this.options.flipAround);
            if (category) {
                categoryContainer = this.getCategoryContainerElement(category.index);
                choicesWrapper = categoryContainer.getFirst('ul');
                choiceElems = choicesWrapper.getChildren('.' + this.choiceCssClass);
                if (up) {
                    elemToHighlight = choiceElems.getLast();
                } else {
                    elemToHighlight = choiceElems[0];
                }
            }
        }
        if (elemToHighlight) {
            this.focusChoice(elemToHighlight, false);
        }
    },

    focusChoice: function(choice, mouseover) {
        var coords, top, height, nextElem, focusedElem, focusedItem, catIdx, choiceIdx;
        focusedElem = this.focusedChoice && this.focusedChoice.elem;
        if (!choice || choice == focusedElem) {
            return;
        }
        if (focusedElem) {
            focusedElem.removeClass(this.options.focusedChoiceCssClass);
        }
        focusedElem = choice.addClass(this.options.focusedChoiceCssClass);
        catIdx = focusedElem.retrieve('categoryIndex');
        choiceIdx = focusedElem.retrieve('choiceIndex');
        focusedItem = this.categories[catIdx].choices[choiceIdx];
        this.focusedChoice = {
            elem: focusedElem,
            item: focusedItem
        };

        // scroll element that was focused by keyboard into view if necessary
        if (this.overflown && !mouseover) {
            coords = focusedElem.getCoordinates(this.choices);
            top = this.choices.scrollTop;
            // height is visible height of the container without scroll offsets
            height = this.choices.clientHeight;
            // top is negative if not in view of scrollable container
            if (coords.top < 0 && top) {
                // check if there is another result to scroll it into view
                nextElem = focusedElem.getPrevious('.' + this.choiceCssClass);
                this.choices.scrollTop = nextElem ? top + coords.top : 0;
            } else if (coords.bottom > height) {
                nextElem = focusedElem.getNext('.' + this.choiceCssClass);
                // in case there is another choice after the current just scroll current 
                // into view otherwise scroll to bottom
                if (nextElem) {
                    this.choices.scrollTop += coords.bottom - height;
                } else {
                    this.choices.scrollTop = this.choices.getScrollSize().y - height;
                }
            }
        }
        if (this.onChoiceFocusedCallback) {
            this.onChoiceFocusedCallback.call(null, focusedElem, focusedItem);
        }
    },

    /**
     * Removes the focus of the currently focused choice.
     */
    unfocusChoice: function() {
        var focusedChoice = this.focusedChoice;
        if (focusedChoice) {
            focusedChoice.elem.removeClass(this.options.focusedChoiceCssClass);
            this.focusedChoice = null;
        }
    },

    /**
     * Select a suggestion element. If this element is not the focused element it will be focused
     * first.
     * 
     * @see selectFocusedChoice
     */
    selectChoice: function(choice) {
        if (choice) {
            this.focusChoice(choice, false);
        }
        this.selectFocusedChoice();
    },

    /**
     * Select the focused suggestion. This will trigger the onChoiceSelected event, close the
     * suggestions and reset the query.
     * 
     * @return {Boolean} whether an element was selected. This will be false if no element was
     *         focused.
     */
    selectFocusedChoice: function() {
        var choiceValue;
        var focusedChoice = this.focusedChoice;
        if (focusedChoice) {
            choiceValue = focusedChoice.item.inputValue;
            if (this.onChoiceSelectedCallback) {
                this.onChoiceSelectedCallback.call(null, focusedChoice.elem,
                        focusedChoice.item.token, choiceValue);
            }
            this.resetQuery();
            return true;
        }
        return false;
    },

    calculateChoicesWidthOffset: function() {
        var offset, styleVal;
        if (!this.choicesWidthOffset) {
            offset = 0;
            styleVal = this.choices.getStyle('border-left').toInt();
            if (!isNaN(styleVal)) {
                offset += styleVal;
            }
            styleVal = this.choices.getStyle('border-right').toInt();
            if (!isNaN(styleVal)) {
                offset += styleVal;
            }
            styleVal = this.choices.getStyle('padding-left').toInt();
            if (!isNaN(styleVal)) {
                offset += styleVal;
            }
            styleVal = this.choices.getStyle('padding-right').toInt();
            if (!isNaN(styleVal)) {
                offset += styleVal;
            }
            this.choicesWidthOffset = offset;
        }
        return this.choicesWidthOffset;
    },

    queryValueChanged: function(query) {
        var clearCategories;
        if (query.length < this.options.minLength) {
            this.hideChoices();
            // the user removed characters thus the shown suggestions are dirty
            this.clearCategoriesBeforeUpdate = true;
            this.minLenghtUnderun = true;
        } else {
            this.minLenghtUnderun = false;
            if (query === this.queryValue) {
                this.showChoices();
            } else {
                // if there is no old query value, e.g. after a reset, clear the categories before
                // updating to avoid showing wrong suggestions. Do the same if the query string changed
                // so that it starts with another value now.
                clearCategories = !this.queryValue
                        || this.clearCategoriesBeforeUpdate
                        || (!this.visible && this.queryValue != query.substring(0,
                                this.queryValue.length));
                this.queryValue = query;
                this.queryForUpdate(clearCategories);
                this.clearCategoriesBeforeUpdate = false;
            }
        }
    },

    repositionChoices: function() {
        if (this.determinePositionCallback && this.visible) {
            this.positionChoices();
        }
    },

    positionChoices: function() {
        var pos, rightEdge, widthOption, width, minWidth, styles, offset, scroll, size, coords;
        var maxWidth;
        pos = this.determinePositionCallback.call(null, this.relativePositioning);
        if (!pos) {
            this.hideChoices();
            return false;
        }
        if (this.options.fixedPosition) {
            // substract scroll
            scroll = document.getScroll();
            pos.left -= scroll.x;
            pos.top -= scroll.y;
        }
        widthOption = this.options.width;
        width = '';
        minWidth = '';
        if (typeof widthOption == 'number') {
            width = widthOption;
        } else if (widthOption == 'inherit' && pos.width > 0) {
            // calculate width respecting border and padding
            width = pos.width - this.calculateChoicesWidthOffset();
        } else if (widthOption == 'auto') {
            width = 'auto';
        } else if (widthOption == 'auto-min') {
            width = 'auto';
            if (pos.width > 0) {
                minWidth = pos.width - this.calculateChoicesWidthOffset();
            }
        }
        offset = this.options.positionOffset;
        styles = {
            'minWidth': minWidth,
            'top': pos.top + offset.y,
            'width': width
        };
        // show for correct width calculation
        this.choices.setStyle('display', '');
        if (this.options.positionAdaptive && pos.rightOffset >= 0 && pos.width > 0) {
            rightEdge = this.choices.getSize().x + offset.x - pos.width > pos.rightOffset
                    && pos.rightOffset < pos.left;
        } else if (this.options.positionAtRightEdge && pos.rightOffset >= 0) {
            rightEdge = true;
        }
        maxWidth = this.options.maxWidth;
        // TODO maxWidth calculation assumes absolute positioning relative to body
        if (rightEdge) {
            styles.right = pos.rightOffset + offset.x;
            styles.left = '';
            if (maxWidth === true) {
                maxWidth = styles.right;
            }
        } else {
            styles.left = pos.left + offset.x;
            styles.right = '';
            if (maxWidth === true) {
                maxWidth = document.getSize().x - styles.left - this.calculateChoicesWidthOffset();
            }
        }
        if (maxWidth !== false) {
            styles.maxWidth = maxWidth;
        }
        this.choices.setStyles(styles);

        // TODO could be problematic with positioning loading feedback overlay
        if (this.fx) {
            this.fx.start(1);
        }
        if (this.onShowCallback) {
            this.onShowCallback.call(null, this.choices);
        }
        // TODO maybe add an option to focus choice if there is only one suggestion?
        if (this.options.selectFirst) {
            this.focusFirstChoice();
        }
        //        var items = this.choices.getChildren(match);
        //        var max = this.options.maxChoices;
        styles = {
            'overflowY': 'hidden',
            'height': ''
        };
        this.overflown = false;
        // TODO iterate over categories and find that one that shows the nth element
        /*
         * if (items.length > max) { var item = items[max - 1]; styles.overflowY = 'scroll';
         * styles.height = item.getCoordinates(this.choices).bottom; this.overflown = true; };
         */
        this.choices.setStyles(styles);
        // scroll choices into view
        if (this.options.visibleChoices) {
            scroll = scroll || document.getScroll();
            size = document.getSize();
            coords = this.choices.getCoordinates();
            if (coords.right > scroll.x + size.x)
                scroll.x = coords.right - size.x;
            if (coords.bottom > scroll.y + size.y)
                scroll.y = coords.bottom - size.y;
            window.scrollTo(Math.min(scroll.x, coords.left), Math.min(scroll.y, coords.top));
        }
        return true;
    }.protect(),

    /**
     * Show the suggestions if not already shown.
     */
    showChoices: function() {
        if (!this.determinePositionCallback
                || this.visible || this.minLenghtUnderun
                || (!this.options.showChoicesWhileLoading && this.categoriesLoading && this.choicesFound === 0)) {
            return;
        }
        this.unfocusChoice();
        if (this.positionChoices()) {
            this.visible = true;
            if (this.options.positionOnWindowResize && !this.boundWindowResizeHandler) {
                this.boundWindowResizeHandler = this.repositionChoices.bind(this);
                document.window.addEvent('resize', this.boundWindowResizeHandler);
            }
        }
    },

    hideChoices: function() {
        var hide;
        if (!this.visible) {
            return;
        }
        if (this.boundWindowResizeHandler) {
            document.window.removeEvent('resize', this.boundWindowResizeHandler);
            this.boundWindowResizeHandler = null;
        }
        this.visible = false;
        this.unfocusChoice();
        hide = function() {
            // must check whether the choices still exist, because this function is delayed
            if (this.choices) {
                this.choices.setStyle('display', 'none');
            }
        }.bind(this);
        if (this.fx) {
            this.fx.start(0).chain(hide);
        } else {
            hide();
        }
        if (this.onHideCallback) {
            this.onHideCallback.call(null, this.choices);
        }
    },

    queryForUpdate: function(clearCategories) {
        var i, category;
        this.unfocusChoice();
        this.categoriesLoading = this.categories.length;
        this.choicesFound = 0;
        this.showChoices();
        for (i = 0; i < this.categories.length; i++) {
            category = this.categories[i];
            if (clearCategories) {
                category.choices.empty();
            }
            category.isLoading = true;
        }
        // note: not passing the categories since the update function might fetch several categories at once
        for (i = 0; i < this.dataSources.length; i++) {
            this.dataSources[i].queryForUpdate(this.queryValue);
        }
    },

    queryForExtend: function(categoryId) {
        var idx, category, i;
        idx = this.categoryIdToIndex[categoryId];
        category = this.categories[idx];
        // ignore extends while still loading
        if (!category.isLoading) {
            for (i = 0; i < this.dataSources.length; i++) {
                if (this.dataSources[i].handlesCategory(categoryId)) {
                    // TODO unfocus focusedChoice or only when showing loading feedback?
                    category.isLoading = true;
                    this.categoriesLoading++;
                    this.dataSources[i].queryForExtend(this.queryValue, category);
                    break;
                }
            }
        }
    },

    /**
     * Shows some loading feedback for the given category if it is not already loading and the
     * showLoadingFeedback option is set. If the category has no choices yet a configurable
     * (loadingFeedbackHintText) hint text will be shown. If there are already choices an overlay
     * will be placed over them so that they are not clickable anymore.
     * 
     * @param {Object|String} category The object holding the details of the category for which the
     *            feedback should be shown or the ID of that category.
     */
    showLoadingFeedbackForCategory: function(category) {
        var categoryElem, hintElem, overlayElem, coords;
        var choicesWrapper, titleElem, titleHeight, catIdx;
        if (!category.id) {
            category = this.categories[this.categoryIdToIndex[category]];
        }
        if (!category.showsLoadingFeedback && this.options.showLoadingFeedback) {
            category.showsLoadingFeedback = true;
            categoryElem = this.getCategoryContainerElement(category.index);
            categoryElem.addClass(this.options.loadingFeedbackCssClass);
            // TODO maybe add a configurable callback to fill the content of the
            // feedback hint LI with something else than text
            if (category.choices.length === 0) {
                choicesWrapper = categoryElem.getFirst('ul');
                // empty in case there is a summary or some old suggestions
                choicesWrapper.getChildren().destroy();
                if (this.options.loadingFeedbackHintText) {
                    hintElem = new Element('li', {
                        'class': this.options.loadingFeedbackHintCssClass
                    });
                    hintElem.set('text', this.options.loadingFeedbackHintText);
                    choicesWrapper.grab(hintElem);
                }
            } else if (category.choices.length > 0) {
                // show and position the overlay, it will cover all elements in choices wrapper
                // this could be done with css but this way it is IE6 compatible
                overlayElem = categoryElem.getLast('div');
                choicesWrapper = categoryElem.getFirst('ul');
                coords = choicesWrapper.getCoordinates(categoryElem);
                overlayElem.setStyles({
                    top: coords.top,
                    width: coords.width,
                    height: coords.height,
                    display: 'block'
                });
                // remove focused element if it is within the category
                if (this.focusedChoice) {
                    catIdx = this.focusedChoice.elem.retrieve('categoryIndex');
                    if (catIdx === category.index) {
                        this.unfocusChoice();
                    }
                }
            }
        }
    },

    /**
     * Removes the loading feedback overlay for the given category if it is loading.
     * 
     * @param {Object|String} category The object holding the details of the category for which the
     *            feedback should be removed or the ID of that category
     * @param {Element} [categoryElem] The element that wraps the category
     */
    hideLoadingFeedbackForCategory: function(category, categoryElem) {
        if (!category.id) {
            category = this.categories[this.categoryIdToIndex[category]];
        }
        if (category.showsLoadingFeedback) {
            if (!categoryElem) {
                categoryElem = this.getCategoryContainerElement(category.index);
            }
            categoryElem.removeClass(this.options.loadingFeedbackCssClass);
            // only hide overlay element, because the hint text will be removed
            // by update routine        
            categoryElem.getLast('div').setStyle('display', 'none');
            category.showsLoadingFeedback = false;
        }
    },

    /**
     * Return an array containing all choices of all categories. Should only be called when all
     * categories finished loading otherwise the result might contain choices returned by different
     * queries.
     * 
     * @return {Object[]} an array of objects that contain the inputValue and the token
     */
    getAggregatedChoices: function() {
        var allChoices, i, j, categoryChoices;
        allChoices = [];
        if (this.choicesFound > 0) {
            for (i = 0; i < this.categories.length; i++) {
                categoryChoices = this.categories[i].choices;
                for (j = 0; j < categoryChoices.length; j++) {
                    allChoices.push(categoryChoices[j]);
                }
            }
        }
        return allChoices;
    }.protect(),

    /**
     * Update the suggestions of a category. Contained entries will be removed.
     * 
     * @param {String} categoryId Identifier of the category
     * @param {Array} tokens Array of strings or objects that represent the suggestions to insert
     * @param {Object} [metaData] Some additional meta data that was returned from the dataSource
     */
    updateCategory: function(categoryId, tokens, metaData) {
        var allChoices;
        // TODO maybe we should call destroy on the dataSource too?
        // do nothing if destroy was called (e.g. dataSource finishes later)
        if (!this.choices) {
            return;
        }
        this.fillCategory(categoryId, tokens, metaData, true);
        if (this.options.hideChoicesWhenEmpty && this.categoriesLoading === 0
                && this.choicesFound === 0) {
            this.hideChoices();
        } else {
            if (!this.visible) {
                this.showChoices();
            } else {
                if (this.options.positionAdaptive) {
                    this.repositionChoices();
                }
                if (this.options.selectFirst) {
                    this.focusFirstChoice();
                }
            }
        }
        // notify callback as soon as all categories finished loading. Note: expect that a 
        // DataSource will only call updateCategory if the results were retrieved with the
        // query passed to the last invocation of its queryForUpdate
        if (this.categoriesLoading === 0 && this.onChoicesFoundCallback) {
            allChoices = this.getAggregatedChoices();
            this.onChoicesFoundCallback.call(null, this.queryValue, allChoices);
        }
    },
    /**
     * Extends the suggestions of a category with additional suggestions.
     * 
     * @param {String} categoryId Identifier of the category
     * @param {Array} tokens Array of strings or objects that represent the suggestions to insert
     * @param {Object} [metaData] Some additional meta data that was returned from the dataSource
     */
    extendCategory: function(categoryId, tokens, metaData) {
        // do nothing if destroy was called (e.g. dataSource finishes later)
        if (!this.choices) {
            return;
        }
        this.fillCategory(categoryId, tokens, metaData, false);
        if (this.options.positionAdaptive) {
            this.repositionChoices();
        }
    },

    /**
     * Fills the suggestions of a category.
     * 
     * @param {String} categoryId Identifier of the category
     * @param {Array} tokens Array of strings or objects that represent the suggestions to insert
     * @param {Object} [metaData] Some additional meta data that was returned from the dataSource
     * @param {boolean} replace If true, the contained suggestions will be removed, otherwise they
     *            will be extended.
     */
    fillCategory: function(categoryId, tokens, metaData, replace) {
        var idx, category, categoryElem, i, choicesWrapper;
        idx = this.categoryIdToIndex[categoryId];
        if (idx === undefined) {
            return;
        }
        category = this.categories[idx];
        if (!category.isLoading) {
            // TODO throw exception?
            return;
        }
        categoryElem = this.getCategoryContainerElement(idx);
        // remove all entries
        choicesWrapper = categoryElem.getFirst('ul');
        if (replace) {
            choicesWrapper.getChildren().destroy();
            category.choices = [];
            category.lastUpdateQueryValue = this.queryValue;
        }
        this.hideLoadingFeedbackForCategory(category, categoryElem);
        category.isLoading = false;
        this.categoriesLoading--;
        if (tokens) {
            this.choicesFound += tokens.length;
        }
        // TODO add option to hide empty category (would need to hide choices
        // when all categories have finished loading!)?
        this.insertChoices(category, choicesWrapper, tokens, metaData);
    },

    /**
     * Inserts the suggestions into a category. The provided suggestions are by default interpreted
     * as strings. This behavior can be changed by implementing your own injectChoiceContentCallback
     * function and adding it to the options. This callback will be invoked to fill the content of
     * the suggestion element. After all suggestions where added the createCategorySummaryCallback
     * will be invoked if defined.
     * 
     * @param {Object} category Object holding the details of the category to fill
     * @param {Element} choicesWrapper The element that wraps the suggestion elements
     * @param {Array} tokens Array of strings or objects that represent the suggestions to insert
     * @param {Object} [metaData] Some additional meta data that was returned from the dataSource
     */
    insertChoices: function(category, choicesWrapper, tokens, metaData) {
        var i, token, elem, injectChoiceCallback, inputValue, catIdx, choiceDetails;
        var offset, lastChoiceElem, insertBeforeElem, cssClass;
        if (tokens) {
            offset = category.choices.length;
            // check if there are already choices and there is an element after the choices like a summary
            if (offset > 0) {
                lastChoiceElem = choicesWrapper.getLast('.' + this.choiceCssClass);
                insertBeforeElem = lastChoiceElem.getNext();
            }
            catIdx = category.index;
            injectChoiceCallback = category.injectChoiceContentCallback
                    || this.injectChoiceContentCallback;
            cssClass = this.choiceCssClass;
            if (this.options.choiceCssClass) {
                cssClass += ' ' + this.options.choiceCssClass;
            }
            for (i = 0; i < tokens.length; i++) {
                token = tokens[i];
                elem = new Element('li', {
                    'class': cssClass
                });
                elem.store('categoryIndex', catIdx);
                elem.store('choiceIndex', offset + i);
                // use callback if defined to fill choice element
                if (injectChoiceCallback) {
                    inputValue = injectChoiceCallback.call(this, category, elem, token, metaData);
                } else {
                    // interprete tokens as strings
                    inputValue = token;
                    elem.set('html', this.markQueryValue(token));
                }
                // ignore null return values
                if (inputValue != null) {
                    choiceDetails = {
                        'token': token,
                        'inputValue': inputValue
                    };
                    category.choices.push(choiceDetails);
                    this.addChoiceEvents(elem);
                    if (insertBeforeElem) {
                        insertBeforeElem.grab(elem, 'before')
                    } else {
                        choicesWrapper.grab(elem);
                    }
                }
            }
        }
        if (!category.noSummary && this.createCategorySummaryCallback) {
            this.createCategorySummaryCallback(category, choicesWrapper, this.queryValue, tokens,
                    metaData);
        }
    },

    /**
     * Marks the queried word in the given string with <span class="autocompleter-queried">*</span>
     * if the option markQuery is given. The queried word will be sought in a case-sensitive manner
     * if the option markQueryCaseSensitive is set. Even if the option markQuery is not set, XML
     * entities will be encoded in the input string if the option markQueryEncode is true. You
     * should usually call this method from your custom injectChoiceContentCallback function.
     * 
     * @param {String} str The string in which the query will be marked
     * @param {String} [queryValue] The query to be marked. If undefined then the current query will
     *            be used.
     * @returns {String} The modified string
     */
    markQueryValue: function(str, queryValue) {
        var regex, regexOptions, replacement;
        if (queryValue == undefined) {
            queryValue = this.queryValue;
        }
        //remove whitespaces before and after the queryValue
        if (queryValue) {
            queryValue = queryValue.trim();
        }

        // encode XML entities
        if (this.options.markQueryEncode) {
            str = str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
            if (this.options.markQuery && queryValue) {
                queryValue = queryValue.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g,
                        '&gt;');
            }
        }
        if (!this.options.markQuery || !queryValue) {
            return str;
        }
        regexOptions = this.options.markQueryCaseSensitive ? '' : 'i';
        if (this.options.markQueryWordStart) {
            regex = new RegExp('(' + this.options.markQueryWordStartPattern + ')('
                    + queryValue.escapeRegExp() + ')', regexOptions);
            replacement = '$1<span class="autocompleter-queried">$2</span>';
        } else {
            regex = new RegExp('(' + queryValue.escapeRegExp() + ')', regexOptions);
            replacement = '<span class="autocompleter-queried">$1</span>';
        }
        return str.replace(regex, replacement);
    },

    /**
     * Attaches a click event handler to an element to trigger an extend/load-more query.
     * 
     * @param {Element} elem The element to which the event should be attached
     * @param {string} categoryId The ID of the category that should be extended
     * @returns {Element} the element passed to the method
     */
    addExtendEvent: function(elem, categoryId) {
        return elem.addEvent('click', this.queryForExtend.bind(this, categoryId));
    },

    /**
     * Attaches the needed event handlers for a suggestion to its element.
     * 
     * @param {Element} elem The suggestion element to process
     * @returns {Element} the element passed to the method
     */
    addChoiceEvents: function(elem) {
        // TODO use event delegation and add events on category container?
        return elem.addEvents({
            'mouseover': this.focusChoice.bind(this, elem, true),
            'click': this.selectChoice.bind(this, elem)
        });
    },

    sanitizeCallback: function(callback) {
        if (typeof callback === 'function') {
            return callback;
        }
        return null;
    }.protect(),

    setOnChoiceFocusedCallback: function(callback) {
        this.onChoiceFocusedCallback = this.sanitizeCallback(callback);
    },

    setOnChoiceSelectedCallback: function(callback) {
        this.onChoiceSelectedCallback = this.sanitizeCallback(callback);
    },

    setOnHideCallback: function(callback) {
        this.onHideCallback = this.sanitizeCallback(callback);
    },

    setOnShowCallback: function(callback) {
        this.onShowCallback = this.sanitizeCallback(callback);
    },

    setDeterminePositionCallback: function(callback) {
        this.determinePositionCallback = this.sanitizeCallback(callback);
    },

    /**
     * Add a callback that should be invoked when an update query for all categories completed. The
     * callback will be passed the queryString and an array with found tokens. The array will be
     * empty when no results were found.
     * 
     * @param {Function} callback The callback function to invoke
     */
    setOnChoicesFoundCallback: function(callback) {
        this.onChoicesFoundCallback = this.sanitizeCallback(callback);
    }

});