(function(namespace, window) {

    function getContentValue(element, retriever, cacheDataAttribute) {
        var value;
        if (cacheDataAttribute) {
            value = element.getProperty(cacheDataAttribute);
        }
        if (!value) {
            if (cacheDataAttribute) {
                value = retriever.call(null, element);
                element.setProperty(cacheDataAttribute, value);
            } else {
                value = element.getProperty(retriever);
            }
        }
        return value;
    }

    if (!namespace) {
        namespace = window;
    } else {
        namespace = namespace.classes || namespace;
    }

    /**
     * Rewrite of the Tips class of mootools-more which provides additional features and has several
     * optimizations. The optimizations include
     * <ul>
     * <li>does not use element.store because it is prone to leaking memory</li>
     * <li>reuses the event handling functions and avoids creation of new functions for every
     * element</li>
     * <li>allows easier overriding of the logic that fills the content of a tip in a subclass</li>
     * </ul>
     * 
     * The additional features are
     * <ul>
     * <li>tips can be hovered with the mouse to allow creation of interactive tips</li>
     * <li>adds appropriate marker classes if a tip is shown above or below the element</li>
     * <li>when positioning the tip the size of the element can be taken into account</li>
     * </ul>
     */
    namespace.AdvancedTips = new Class({
        Implements: [ Events, Options ],

        options: {
            // string defining the value of the id attribute of the tip-container
            id: false,
            /*
             * // event callback that is invoked when a tip is attached to an element
             * 
             * onAttach: function(element){},
             */
            /*
             * // event callback that is invoked when a tip is removed from an element
             * 
             * onDetach: function(element){},
             */
            onShow: function() {
                this.tip.setStyle('display', 'block');
            },
            onHide: function() {
                this.tip.setStyle('display', 'none');
            },
            title: 'title',
            text: function(element) {
                return element.get('rel') || element.get('href');
            },
            showDelay: 100,
            hideDelay: 100,
            className: 'tip-wrap',
            offset: {
                x: 8,
                y: 8
            },
            windowPadding: {
                x: 0,
                y: 0
            },
            // if true the position of the element defines the position of the tip and the tip will
            // not follow the mouse pointer. If false the tip is positioned where the mouse event
            // occurred. The offset option will can be used to add some additional offsets when
            // positioning the tip.
            fixed: true,
            // if true and fixed option is also true, the center of the element is used for
            // determining the position instead of the left edge 
            horizontalPositionElementCenter: false,
            // whether a string value for text or title should be interpreted as HTML or plaintext
            htmlContent: true,
            waiAria: true,
            dataAttributePrefix: 'data-advtips-',
            // whether the tip can be hovered with the mouse without beeing closed. This is useful
            // for interactive tips. If true, the fixed option is forced.
            tipHoverable: false,
            // css class to be applied to the tip element when the 'fixed' option is set and the tip is
            // shown above the element
            tipAboveMarkerClass: 'tip-above',
            // css class to be applied to the tip element when the 'fixed' option is set and the tip is
            // shown below the element
            tipBelowMarkerClass: 'tip-below',
            // if true and fixed option is true the height of the element will be respected when
            // positioning the tip. If the tip is shown below the element the offset.y refers to
            // the bottom of the element
            verticalPositionElementHeight: true
        },
        hoverable: false,
        titleCacheDataAttribute: false,
        textCacheDataAttribute: false,
        contentAttribute: 'html',

        initialize: function(options, elements) {
            this.setOptions(options);
            this.hoverable = this.options.tipHoverable;
            // if hoverable force certain options
            if (this.hoverable) {
                if (!this.options.hideDelay || this.options.hideDelay < 0) {
                    this.options.hideDelay = 800;
                }
                this.options.fixed = true;
            }
            if (elements) {
                this.attach(elements);
            }
            this.container = new Element('div', {
                'class': 'tip'
            });

            if (this.options.id) {
                this.container.set('id', this.options.id);
                if (this.options.waiAria) {
                    this.attachWaiAria();
                }
            }
            if (typeOf(this.options.title) === 'function') {
                this.titleCacheDataAttribute = this.options.dataAttributePrefix + 'title-value';
            }
            if (typeOf(this.options.text) === 'function') {
                this.textCacheDataAttribute = this.options.dataAttributePrefix + 'text-value';
            }
            if (!this.options.htmlContent) {
                this.contentAttribute = 'text';
            }
            this.markerDataAttribute = this.options.dataAttributePrefix + 'attached';
        },

        applyPositionMarker: function(above) {
            if (above) {
                this.tip.removeClass(this.options.tipBelowMarkerClass);
                this.tip.addClass(this.options.tipAboveMarkerClass);
            } else {
                this.tip.removeClass(this.options.tipAboveMarkerClass);
                this.tip.addClass(this.options.tipBelowMarkerClass);
            }
        },

        attach: function(elements) {
            var i, l, type, element, orgTitle, orgTitleAttribute;
            if (elements) {
                type = typeOf(elements);
                if (type === 'element') {
                    elements = Array.from(elements);
                } else if (type !== 'elements') {
                    elements = $$(elements);
                }
                if (!this.boundElementEnter) {
                    this.boundElementEnter = this.elementEnter.bind(this);
                    this.boundElementLeave = this.elementLeave.bind(this);
                    if (!this.options.fixed) {
                        this.boundElementMove = this.elementMove.bind(this);
                    }
                }
                orgTitleAttribute = this.options.dataAttributePrefix + 'org-title';
                for (i = 0, l = elements.length; i < l; i++) {
                    element = elements[i];
                    // skip already attached elements
                    if (element.getProperty(this.markerDataAttribute)) {
                        continue;
                    }
                    element.setProperty(this.markerDataAttribute, 'true');
                    orgTitle = element.get('title');
                    if (orgTitle) {
                        element.setProperty(orgTitleAttribute, orgTitle);
                        element.removeProperty('title');
                    }
                    element.addEvent('mouseenter', this.boundElementEnter);
                    element.addEvent('mouseleave', this.boundElementLeave);
                    if (this.boundElementMove) {
                        element.addEvent('mousemove', this.boundElementMove);
                    }
                    this.fireEvent('attach', [ element ]);
                }
            }
            return this;
        },

        attachWaiAria: function() {
            var id = this.options.id;
            this.container.set('role', 'tooltip');

            if (!this.waiAria) {
                this.waiAria = {
                    show: function(element) {
                        if (id)
                            element.set('aria-describedby', id);
                        this.container.set('aria-hidden', 'false');
                    },
                    hide: function(element) {
                        if (id)
                            element.erase('aria-describedby');
                        this.container.set('aria-hidden', 'true');
                    }
                };
            }
            this.addEvents(this.waiAria);
        },

        detach: function(elements) {
            var i, l, type, element, orgTitle;
            if (elements) {
                type = typeOf(elements);
                if (type === 'element') {
                    elements = Array.from(elements);
                } else if (type !== 'elements') {
                    elements = $$(elements);
                }
                for (i = 0, l = elements.length; i < l; i++) {
                    element = elements[i];
                    element.removeProperty(this.markerDataAttribute);
                    orgTitle = element.get(this.options.dataAttributePrefix + 'org-title');
                    if (orgTitle) {
                        element.removeProperty(this.options.dataAttributePrefix + 'org-title');
                        element.setProperty('title', orgTitle);
                    }
                    element.removeEvent('mouseenter', this.boundElementEnter);
                    element.removeEvent('mouseleave', this.boundElementLeave);
                    if (this.boundElementMove) {
                        element.removeEvent('mousemove', this.boundElementMove);
                    }
                    this.fireEvent('detach', [ element ]);
                }
            }
            return this;
        },

        detachWaiAria: function() {
            if (this.waiAria) {
                this.container.erase('role');
                this.container.erase('aria-hidden');
                this.removeEvents(this.waiAria);
            }
        },

        elementEnter: function(event) {
            clearTimeout(this.timer);
            this.timer = (function() {
                var element = event.target;
                if (this.updateTip(element)) {
                    this.show(element);
                    this.position(element, event.page);
                } else {
                    this.hideNow();
                }
            }).delay(this.options.showDelay, this);
        },

        elementLeave: function(event) {
            clearTimeout(this.timer);
            this.timer = this.hide.delay(this.options.hideDelay, this);
            // TODO really needed?
            // this.fireForParent(event, element);
        },

        elementMove: function(event) {
            this.position(null, event.page);
        },

        fill: function(element, contents) {
            if (typeof contents == 'string') {
                element.set(this.contentAttribute, contents);
            } else {
                element.adopt(contents);
            }
        },
        /**
         * Get the tip element. If it does not yet exist it will be created.
         * 
         * @return {Element} the tip element
         */
        getTipElement: function() {
            var tipElem = this.tip;
            if (tipElem) {
                return tipElem;
            }
            // create tip element
            this.tip = new Element('div', {
                'class': this.options.className,
                styles: {
                    position: 'absolute',
                    top: 0,
                    left: 0
                }
            }).adopt(new Element('div', {
                'class': 'tip-top'
            }), this.container, new Element('div', {
                'class': 'tip-bottom'
            }));
            this._titleElement = new Element('div', {
                'class': 'tip-title'
            }).inject(this.container);
            this._textElement = new Element('div', {
                'class': 'tip-text'
            }).inject(this.container);

            if (this.hoverable) {
                this.tip.addEvent('mouseenter', this.tipEnter.bind(this));
                this.tip.addEvent('mouseleave', this.tipLeave.bind(this));
            }
            // apply one of the position marker classes to get correct height calculation when shown
            // for the first time
            if (this.options.fixed) {
                this.tip.addClass(this.options.tipBelowMarkerClass);
            }
            return this.tip;
        },

        hide: function(element) {
            this.fireEvent('hide', [ this.getTipElement(), element ]);
            this.shown = false;
        },

        hideNow: function() {
            clearTimeout(this.timer);
            this.hide();
        },

        position: function(element, coords) {
            var elemSize;
            var tipElem = this.getTipElement();
            var size = window.getSize();
            var scroll = window.getScroll();
            var tipSize = {
                x: tipElem.offsetWidth,
                y: tipElem.offsetHeight
            };
            var above = false;
            var styles = {};
            var elemHeight = 0;

            if (this.options.fixed) {
                coords = element.getPosition();
                if (this.options.verticalPositionElementHeight) {
                    elemSize = element.getSize();
                    elemHeight = elemSize.y;
                }
                if (this.options.horizontalPositionElementCenter) {
                    coords.x += Math.floor((elemSize ? elemSize.x : element.getSize().x) / 2);
                }
            }
            styles.left = coords.x + this.options.offset.x;
            if ((styles.left + tipSize.x - scroll.x) > size.x - this.options.windowPadding.x) {
                styles.left = coords.x - this.options.offset.x - tipSize.x;
            }
            if (styles.left < this.options.windowPadding.x) {
                styles.left = this.options.windowPadding.x;
            }
            styles.top = coords.y + elemHeight + this.options.offset.y;
            if ((styles.top + tipSize.y - scroll.y) > size.y - this.options.windowPadding.y) {
                styles.top = coords.y - this.options.offset.y - tipSize.y;
                above = true;
            }
            // TODO good idea to show it above, because it could cover the element this way ...
            if (styles.top < this.options.windowPadding.y) {
                styles.top = this.options.windowPadding.y;
            }
            if (this.options.fixed) {
                this.applyPositionMarker(above);
            }
            tipElem.setStyles(styles);
        },

        /**
         * Search for elements matching a given selector and attach a tip to the found elements.
         * 
         * @param {String} selector CSS selector to retrieve the elements to attach
         * @param {Element|String} [container] Selector or element to only scan the children of that
         *            element
         */
        scan: function(selector, container) {
            if (!container) {
                container = document;
                tipElements = document.getElements(selector);
            } else {
                container = document.id(container);
            }
            this.attach(container.getElements(selector));
        },

        setText: function(text) {
            if (!this._textElement) {
                this.getTipElement();
            } else {
                this._textElement.empty();
            }
            if (text) {
                this.fill(this._textElement, text);
            }
            return this;
        },

        setTitle: function(title) {
            if (!this._titleElement) {
                this.getTipElement();
            } else {
                this._titleElement.empty();
            }
            if (title) {
                this.fill(this._titleElement, title);
            }
            return this;
        },

        show: function(element) {
            var tipElem = this.getTipElement();
            if (!tipElem.getParent()) {
                tipElem.inject(document.body);
            }
            this.fireEvent('show', [ tipElem, element ]);
            this.shown = true;
        },

        tipEnter: function() {
            clearTimeout(this.timer);
            // TODO force show if not yet shown (would be necessary if hide event handler uses an animation to hide the tip) 
        },

        tipLeave: function() {
            clearTimeout(this.timer);
            // TODO find a way to pass the element that caused the tooltip to appear
            this.timer = this.hide.delay(this.options.hideDelay, this, null);
        },

        /**
         * Update the tip for the given element.
         * 
         * @param {Element} The hovered element for which a tip should be shown.
         * @return {Boolean} whether the update was successful. If false is returned the tip won't
         *         be shown
         */
        updateTip: function(element) {
            this
                    .setTitle(getContentValue(element, this.options.title,
                            this.titleCacheDataAttribute));
            this.setText(getContentValue(element, this.options.text, this.textCacheDataAttribute));
            return true;
        }

    });
})(this.runtimeNamespace, this);