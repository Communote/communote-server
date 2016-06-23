HoverCard = new Class({

    Implements: Options,

    options: {
        // should be set in subclasses/instances to select the items that should get a hovercard
        selector: null,
        // additional css class names to be applied to the lazy tips wrapper apart from hover-card
        hoverCardCssClass: '',
        loadingMessageKey: 'javascript.loading.message',
        // the attribute or function that retrieves the ID for loading the hovercard content from
        // the element that is augmented with the hovercard. This setting is passed to the LazyTips
        // instance.
        lazyTipsIdExtractor: '',
        // selector of an element within the DOM that holds the HTML of the hovercard content. This
        // element will be used directly (thus can contain ID attributes) for rendering. If not 
        // defined the buildCardContent method will be called.
        contentTemplateSelector: null,
        // defines the maximum number of tags that should be rendered when fillTags is called. If
        // 0 or less than 0, all tags will be rendered.
        fillTagsLimitation: 0,
        // whether links added to the HoverCard should have a target='_blank' attribute to force
        // opening in a new window
        openLinksInNewWindow: false
    },

    lazyTips: null,
    cardContentElement: null,

    initialize: function(options) {
        var hoverCardCssClass, tipContainer;
        this.setOptions(options);
        hoverCardCssClass = this.options.hoverCardCssClass || '';
        if (hoverCardCssClass.length) {
            hoverCardCssClass = ' ' + hoverCardCssClass;
        }
        this.lazyTips = new communote.classes.LazyTips({
            className: 'hovercard' + hoverCardCssClass,
            htmlContent: true,
            loadDataCallback: this.loadDataCallback.bind(this),
            parseDataCallback: this.parseDataCallback.bind(this),
            errorFieldMessage: 'message',
            loadingFeedbackMessage: getJSMessage(this.options.loadingMessageKey),
            idExtractor: this.options.lazyTipsIdExtractor,
            // no caching in lazy tips since userUtils cache the api responses
            cacheOptions: false,
            tipHoverable: true,
            showDelay: 800,
            hideDelay: 400,
            offset: {
                x: -40, // offset to position the arrow of the hovercard centered above/below the hovered element 
                y: 5
            },
            horizontalPositionElementCenter: true,
            verticalPositionElementHeight: true
        });
        tipContainer = this.lazyTips.getTipElement()
        // add additional elements to the top and bottom containers for easy styling
        tipContainer.getElement('.tip-top').set('html',
                '<div class="hovercard-border-shadow"></div><div class="hovercard-border"></div>');
        tipContainer.getElement('.tip-bottom').set('html',
                '<div class="hovercard-border-shadow"></div><div class="hovercard-border"></div>');
    },

    /**
     * LazyTips callback that fetches the data that should be shown in the hover card. Should be
     * implemented in subclasses.
     */
    loadDataCallback: function(id, successCallback, errorCallback) {
        return null;
    },

    /**
     * LazyTips callback that parses the loaded data and updates the content element of the
     * hovercard. This generic implementation will check all elements of the cardContent that have
     * the data attribute data-cnt-hovercard-accessor. The value of the attribute will be tested
     * whether it is a local function or a member of the loaded server object (contained in
     * response.result). In case it is not a function and not contained (or null-valued) in the
     * response, the attribute data-cnt-hovercard-missing will be evaluated to decide what to do
     * with the element. This data attribute can have the values 'hide', 'nodisplay' and 'empty',
     * which lead to setting the visibility style to 'hidden', the display style to 'none' or
     * removal of the content of the element respectively.
     * 
     * @param {Object} response The response returned from loading the data. This object is expected
     *            to contain a result member that holds the actual data.
     */
    parseDataCallback: function(response) {
        var fieldElems, i, elem, accessor, missing, style, styleValue, userData, cardContentElement;
        userData = response.result;
        cardContentElement = this.getCardContentElement();
        fieldElems = cardContentElement.getElements('*[data-cnt-hovercard-accessor]');
        for (i = 0; i < fieldElems.length; i++) {
            elem = fieldElems[i];
            accessor = elem.getProperty('data-cnt-hovercard-accessor');
            // check accessor whether it is a local function or userData attribute
            if (this[accessor]) {
                this[accessor](elem, userData);
            } else {
                // check what should be done with missing attributes
                missing = elem.getProperty('data-cnt-hovercard-missing');
                if (missing == 'hide') {
                    style = 'visibility';
                    styleValue = 'hidden';
                } else if (missing == 'nodisplay') {
                    style = 'display';
                    styleValue = 'none';
                } else {
                    style = styleValue = null;
                }
                if (userData[accessor] != null) {
                    elem.set('text', userData[accessor]);
                    if (style) {
                        elem.setStyle(style, '');
                    }
                } else {
                    if (style) {
                        elem.setStyle(style, styleValue);
                    } else {
                        elem.empty();
                    }
                }
            }
        }
        return {
            text: cardContentElement
        }
    },

    getMatchingElements: function(domNode, selector) {
        var elems, i;
        if (!domNode) {
            domNode = document;
        } 
        if (typeOf(domNode) == 'elements') {
            // search elements manually because Elements.getElements returns strange results
            elems = new Elements();
            for (i = 0; i < domNode.length; i++) {
                elems.append(domNode[i].getElements(selector));
            }
        } else {
            elems = domNode.getElements(selector);
        }
        return elems;
    }.protect(),
    
    /**
     * Attach the HoverCard to all child elements of domNode that match the selector option.
     * 
     * @param {Element|Elements} [domNode] Element whose child nodes should be searched for HoverCard
     *            candidates. If not provided the whole DOM will be searched.
     */
    attach: function(domNode) {
        var elems;
        var selector = this.options.selector;
        if (selector) {
            elems = this.getMatchingElements(domNode, selector);
            if (elems.length) {
                this.lazyTips.attach(elems);
            }
        }
    },
    
    /**
     * Detach the HoverCard from all child elements of domNode that match the selector option.
     * Should usually be called before the elements are removed from the DOM because mootools-more
     * tips uses element storage which tends to leak memory when eliminate is not called.
     * 
     * @param {Element|Elements} [domNode] Element whose child nodes should be searched for HoverCard
     *            candidates. If not provided the whole DOM will be searched.
     */
    detach: function(domNode) {
        var elems;
        var selector = this.options.selector;
        if (selector) {
            elems = this.getMatchingElements(domNode, selector);
            if (elems.length) {
                this.lazyTips.hideNow();
                this.lazyTips.detach(elems);
            }
        }
    },

    getCardContentElement: function() {
        var templateSelector, contentElement;
        // TODO clone element?
        if (!this.cardContentElement) {
            templateSelector = this.options.contentTemplateSelector;
            if (templateSelector) {
                contentElement = document.getElement(templateSelector);
            }
            if (!contentElement) {
                contentElement = this.buildCardContent();
            } else {
                // remove template from DOM
                contentElement = contentElement.dispose();
            }
            this.cardContentElement = contentElement;
        }
        return this.cardContentElement;
    },

    /**
     * Can be overridden in subclasses to build the content of the HoverCard. Default implementation
     * does nothing.
     * 
     * @return {Element} the element that represents the content of the HoverCard that will be
     *         filled by parseData
     */
    buildCardContent: function() {
        return null;
    },

    createTagUrl: function(tag) {
        return '';
    },

    fillTags: function(elem, result) {
        var i, tag, tagWrapperElem, tagElem, value, limit;
        var tags = result.tags;
        if (tags && tags.length) {
            tagWrapperElem = elem.getChildren('span.cn-icon-label')[0];
            tagWrapperElem.empty();
            if (this.options.fillTagsLimitation > 0) {
                limit = Math.min(tags.length, this.options.fillTagsLimitation);
            } else {
                limit = tags.length;
            }
            for (i = 0; i < limit; i++) {
                tag = tags[i];
                value = tag.name;
                if (i < tags.length - 1) {
                    value += ', ';
                }
                tagElem = new Element('a', {
                    href: this.createTagUrl(tag),
                    text: value
                });
                if (this.options.openLinksInNewWindow) {
                    tagElem.setProperty('target', '_blank');
                }
                tagWrapperElem.grab(tagElem);
            }
            if (limit < tags.length) {
                tagWrapperElem.appendText('...');
            }
            elem.setStyle('display', '');
        } else {
            elem.setStyle('display', 'none');
        }
    }
});