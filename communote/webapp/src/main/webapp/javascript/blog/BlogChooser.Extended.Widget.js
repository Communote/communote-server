var BlogChooserExtendedWidget = new Class({
    Extends: BlogChooserWidget,
    Implements: [ C_FilterParameterListener, FilterParamsHandlerStrategyByName ],

    /**
     * Default action of FilterParamsHandlerStrategyByName removes the blog filter, thus there is no
     * need to break.
     */
    breakAfterDefaultAction: false,
    filterParameterStore: null,

    observedParams: [ 'targetBlogId' ],

    // stores details about the categories displayed in the chooser
    categories: {},
    // prefix of element IDs of the category items
    categoryIdPrefix: '',

    // slider instance
    chooserSlide: null,

    // the ID of the currently selected blog
    selectedBlogId: null,
    //OverText object that is the placeholder for the input element
    noBlogPlaceholder: null,

    init: function() {
        this.parent();

        // get filter parameter store of filterWidgetGroup
        var filterWidgetGroupId = this.staticParams.filterWidgetGroupId;
        var filterWidgetGroup = filterWidgetGroupRepo[filterWidgetGroupId];
        this.filterParameterStore = filterWidgetGroup.getParameterStore();
        var initialBlogId = this.filterParameterStore.getFilterParameter('targetBlogId');
        if (initialBlogId != null) {
            this.selectedBlogId = initialBlogId;
        }
        this.filterParameterStore.attachListener(this);
    },

    setup: function() {
        var applicationUrl = communote.server.applicationUrl;
        this.categoryIdPrefix = this.widgetId + '_category_';
        this.categories.myBlogs = {
            elementId: this.categoryIdPrefix + 'myBlogs',
            dirty: true,
            refreshUrl: insertSessionId(applicationUrl + '/blog/control/getMyBlogs.do'),
            slide: null,
            openByDefault: false
        };
        this.categories.mostUsedBlogs = {
            elementId: this.categoryIdPrefix + 'mostUsedBlogs',
            dirty: true,
            refreshUrl: insertSessionId(applicationUrl + '/blog/control/getMostUsedBlogs.do'),
            refreshParams: {
                sortByTitle: true
            },
            slide: null,
            openByDefault: true
        };
        this.categories.lastUsedBlogs = {
            elementId: this.categoryIdPrefix + 'lastUsedBlogs',
            dirty: true,
            refreshUrl: insertSessionId(applicationUrl + '/blog/control/getLastUsedBlogs.do'),
            slide: null,
            openByDefault: false
        };
    },

    beforeRemove: function() {
        this.filterParameterStore.removeListener(this);
    },

    getObservedFilterParameters: function() {
        return this.observedParams;
    },

    targetBlogIdChanged: function() {
        // close if still open
        if (this.chooserSlide) {
            this.closeChooserSlide();
        }
        var blogId = this.filterParameterStore.getFilterParameter('targetBlogId');
        if (this.selectedBlogId != blogId) {
            this.selectedBlogId = blogId;
            this.updateInputField();
            this.updateDefaultBlogMarker();
        }
    },

    getListeningEvents: function() {
        return this.parent().combine(
                [ 'onBlogUpdate', 'onCurrentUserBlogRoleChanged', 'onCurrentBlogAccessLost',
                        'onBlogDelete', 'onBlogAdd', 'onReloadPostList' ]);
    },

    onBlogDelete: function() {
        // in case one of the shown blogs got deleted
        this.markAllCategoriesDirty();
    },

    onBlogAdd: function() {
        // TODO shouldn't it be enough to refresh the myBlogs?
        this.markAllCategoriesDirty();
    },

    /**
     * Event handler that is triggered by a blog update.
     * 
     * @param {Object} blogData object with blog describing members id, newTitle, newDescription
     */
    onBlogUpdate: function(blogData) {
        // blog might be shown in any category, better refresh all
        this.markAllCategoriesDirty();
        /* refresh input line if title of current blog changed */
        if (blogData != null) {
            if (this.selectedBlogId == blogData.id) {
                if (blogData.newTitle) {
                    this.updateInputField(blogData.newTitle);
                }
            }
        }
    },

    onCurrentUserBlogRoleChanged: function() {
        this.markCategoryDirty(this.categories.myBlogs);
    },
    onCurrentBlogAccessLost: function() {
        this.markAllCategoriesDirty();
    },
    onReloadPostList: function() {
        // in case someone made current user to the manager of a blog
        this.markCategoryDirty(this.categories.myBlogs);
    },
    onRefresh: function(sender) {
        this.markAllCategoriesDirty();
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
            // refresh the element, if it is open
            if (category.slide && category.slide.open) {
                this.refreshCategory(category);
            }
        }
    },

    /**
     * Refreshes one of the menu categories
     * 
     * @param {String} id The item ID of the category
     */
    refreshCategoryById: function(id) {
        var category = this.categories[id];
        if (!category) {
            return;
        }
        this.refreshCategory(category);
    },

    /**
     * Refreshes one of the menu categories
     * 
     * @param {String} category The category to refresh
     */
    refreshCategory: function(category) {
        var elem, request;
        elem = document.id(category.elementId);
        elem.addClass(this.widgetController.markLoadingCssClass);
        request = new Request.JSON({
            url: category.refreshUrl,
            onComplete: function(data) {
                this.createBlogList(data, elem.getElement('ul'));
                // clear dirty status
                category.dirty = false;
                elem.removeClass(this.widgetController.markLoadingCssClass);
            }.bind(this)
        });
        request.post(category.refreshParams);
    },

    // creates a blog list inside a node
    createBlogList: function(blogList, parent) {
        var curBlogId, blogItem, i;
        var blogCount = 0;
        var blogElements = parent.getElements('li');

        if (blogList != null && blogList.length > 0) {
            blogCount = blogList.length;
            // currently selected blog
            curBlogId = this.selectedBlogId;
            blogList.each(function(blogItem, index) {
                var newElem, newActionElem, blogData;
                newElem = new Element('li');
                newActionElem = new Element('a', {
                    'text': blogItem.title,
                    'title': blogItem.description,
                    'href': 'javascript:;'
                });
                blogData = {
                    type: 'blog',
                    key: blogItem.id,
                    title: blogItem.title
                };
                newActionElem.addEvent('click', function(event) {
                    event.stop();
                    this.closeChooserSlide();
                    // send event to all groups
                    E2G('onBlogClick', null, blogData.key, blogData);
                }.bind(this));
                newElem.grab(newActionElem);
                if (curBlogId == blogItem.id) {
                    newElem.addClass('selected');
                }
                if (blogElements != null && index < blogElements.length) {
                    newElem.replaces(blogElements[index]);
                } else {
                    newElem.inject(parent);
                }
            }, this);
        }
        if (blogElements != null) {
            for (i = blogCount; i < blogElements.length; i++) {
                blogElements[i].dispose();
            }
        }
    },

    updateInputField: function(title) {
        var blogId, blog, defaultBlog, defaultBlogId;
        // if not yet ready do nothing
        if (!this.inputElement)
            return;
        if (!title) {
            /* check for current blog ID */
            blogId = this.selectedBlogId;
            if (blogId != null) {
                blog = this.widgetController.getDataStore().get('blog', blogId);
                if (blog) {
                    defaultBlog = blogUtils.getDefaultBlog();
                    defaultBlogId = defaultBlog && defaultBlog.id;
                    title = blogId == defaultBlogId ? getJSMessage('custom.message.default.blog',
                            [ blog.title ]) : blog.title;
                }
            }
        }
        // TODO accessing overtext is ugly
        // the OverText placeholder doesn't recognize the set value
        if (this.noBlogPlaceholder.overtext) {
            // call hide or show directly and not assert, because we set the text afterwards which looks better
            if (title) {
                this.noBlogPlaceholder.overtext.hide(true);
                this.inputElement.set('value', title);
            } else {
                this.inputElement.set('value', '');
                this.noBlogPlaceholder.refresh();
            }
        } else {
            // set empty value if no title is defined so that the placeholder is triggered
            this.inputElement.set('value', title || '');
        }
    },

    updateDefaultBlogMarker: function() {
        var elem = this.domNode.getElement('.cn-chooser-info');
        // ignore update requessts while the DOM is not ready yet (e.g. events before the first refresh finished) 
        if (elem) {
            if (this.selectedBlogId != null) {
                elem.removeClass('cn-default-blog-selected');
            } else {
                elem.addClass('cn-default-blog-selected');
            }
        }
    },

    /**
     * Called when the blog chooser is about to show or hide some choices to select from. The
     * choices cover the menu categories and the autocompleter suggestions.
     * 
     * @param {boolean} show true if the choices are shown and false if they are hidden
     * 
     */
    chooserShowsHidesChoices: function(show) {
        var inputContainer = this.inputElement.getParent();
        if (show) {
            inputContainer.addClass('cn-chooser-input-select');
        } else {
            inputContainer.removeClass('cn-chooser-input-select');
        }
    },

    closeChooserSlide: function() {
        this.chooserSlide.hide();
        this.chooserShowsHidesChoices(false);
    },

    menuToggle: function() {
        this.chooserSlide.toggle();
    },

    menuSlideStart: function() {
        if (!this.chooserSlide.open) {
            this.chooserShowsHidesChoices(true);
        }
    },

    menuSlideComplete: function() {
        if (!this.chooserSlide.open) {
            this.chooserShowsHidesChoices(false);
        }
    },

    categoryClicked: function() {
        this.slide.toggle();
    },

    categorySlideStart: function(category) {
        if (category.dirty) {
            this.refreshCategory(category);
        }
        document.id(category.elementId).toggleClass('cn-open');
    },

    refreshComplete: function(responseMetadata) {
        var categoryElems, i, categoryId, category, elem, overtext;
        var chooserMenuWrapper = this.domNode.getElement('.cn-chooser-select-wrapper');
        var chooserMenu = this.domNode.getElement('.cn-chooser-select');
        this.chooserSlide = new Fx.Slide(chooserMenu, {
            wrapper: chooserMenuWrapper,
            resetHeight: true
        });
        // FX.slide opens the slider by default but we want it to be closed
        this.chooserSlide.hide();
        this.chooserSlide.addEvent('complete', this.menuSlideComplete.bind(this));
        this.chooserSlide.addEvent('start', this.menuSlideStart.bind(this));
        // remove position style which is applied by Fx.Slide because it overrides the CSS
        // definitions; also remove visibility style which is used to hide the element while
        // the page is loading
        this.chooserSlide.wrapper.setStyles({
            position: '',
            visibility: ''
        });
        // add slides to menu items
        categoryElems = this.domNode.getElements('.cn-chooser-select-category');
        for (i = 0; i < categoryElems.length; i++) {
            elem = categoryElems[i];
            categoryId = elem.id.substring(this.categoryIdPrefix.length);
            category = this.categories[categoryId];
            if (category) {
                category.slide = new Fx.Slide(elem.getElement('ul'), {
                    resetHeight: true
                });
                // if the category should be open by default do not close it but simulate open
                // action
                if (category.openByDefault) {
                    this.categorySlideStart(category);
                } else {
                    category.slide.slideOut();
                }
                category.slide.addEvent('start', this.categorySlideStart.bind(this, category));
                elem.getElement('a').addEvent('click', this.categoryClicked.bind(category));
            }
        }
        this.inputElement = this.domNode.getElement('input');
        this.attachAutocompleter();
        this.noBlogPlaceholder = communote.utils.attachPlaceholders(this.inputElement)[0];
        this.updateInputField();
        this.updateDefaultBlogMarker();
    },

    attachAutocompleter: function() {
        this.parent();

        this.autocompleter.addEvent('show', function() {
            // do not call this.closeChooserSlide() because only the slide should be closed,
            // further operations are not necessary here
            this.chooserSlide.hide();
            this.chooserShowsHidesChoices(true);
        }.bind(this));
        this.autocompleter.addEvent('hide', function() {
            this.chooserShowsHidesChoices(false);
            if (!this.autocompleter.isInputElementFocused()) {
                this.updateInputField();
            }
        }.bind(this));
        this.autocompleter.addEvent('blur', this.updateInputField.bind(this, null));
    }
});
