(function(namespace, window) {
    if (!namespace) {
        namespace = window;
    } else {
        namespace = namespace.classes || namespace;
    }
    /**
     * <p>
     * Extension to the AdvancedTips class which allows filling the tip content lazily by invoking a
     * provided callback function or doing an asynchronous JSON request. The callback function can
     * also work asynchronously. While the tip content is loading a feedback message can be shown.
     * </p>
     * <p>
     * The callback or request are invoked with an ID which is by default extracted from the rel
     * attribute of the element for which the tooltip should be shown. This default behavior can be
     * changed to reading another attribute or executing a custom function.
     * </p>
     * <p>
     * This class can use a simple FIFO cache with configurable size (Cache class) to avoid calling
     * the callback or doing the request every time the tip is shown. This cache is enabled if
     * option cacheOptions is set. The cacheOptions object is passed to the Cache constructor.
     * </p>
     */
    namespace.LazyTips = new Class({
        Extends: namespace.AdvancedTips,

        options: {
            cacheOptions: null,
            jsonRequestUrl: null,
            loadDataCallback: null,
            loadDataFailedMessage: 'Loading the data failed',
            parseDataCallback: null,
            idExtractor: 'rel',
            dataFieldText: 'text',
            dataFieldTitle: 'title',
            errorFieldMessage: 'message',
            loadingFeedbackMessage: 'Loading ...',
            loadingFeedbackCssClass: 'tip-loading'
        },
        cache: null,
        currentId: null,
        currentElement: null,

        initialize: function(options, elements) {

            this.parent(options, elements);
            // normalize and validate options
            if (typeOf(this.options.loadDataCallback) !== 'function') {
                this.options.loadDataCallback = null;
            }
            if (typeOf(this.options.parseDataCallback) !== 'function') {
                this.options.parseDataCallback = null;
            }
            if (typeOf(this.options.idExtractor) !== 'function'
                    && typeOf(this.options.idExtractor) !== 'string') {
                throw "Unsupported idExtractor option";
            }
            if (!this.options.loadDataCallback && this.options.jsonRequestUrl) {
                throw "One of the options loadDataCallback and jsonRequestUrl has to specified";
            }
            if (typeOf(this.options.cacheOptions) == 'object') {
                this.cache = new Cache(this.options.cacheOptions);
            }
        },

        clearCache: function() {
            if (this.cache) {
                this.cache.removeAll();
            }
        },

        addToCache: function(id, title, text) {
            var cache = this.cache;
            if (cache) {
                cache.put(id.toString(), {
                    title: title,
                    text: text
                });
            }
        },

        getFromCache: function(id) {
            var cache = this.cache;
            if (cache) {
                return cache.get(id.toString());
            }
        },

        removeFromCache: function(id) {
            var cache = this.cache;
            if (cache) {
                cache.remove(id.toString());
            }
        },

        loadData: function(id) {
            var request;
            if (this.options.loadDataCallback) {
                this.options.loadDataCallback.call(null, id, this.loadDataSuccess.bind(this, id),
                        this.loadDataFailure.bind(this, id));
            } else if (this.options.jsonRequestUrl) {
                request = new Request.JSON({
                    url: this.options.jsonRequestUrl + id,
                    noCache: true,
                    onSuccess: function(response) {
                        this.loadDataSuccess(id, response);
                    }.bind(this),
                    onFailure: function(xhr) {
                        this.loadDataFailure(null);
                    }.bind(this)
                });
                request.send();
            }
        },

        loadDataSuccess: function(id, result) {
            var parsedResult;
            var options = this.options;
            if (options.parseDataCallback) {
                parsedResult = options.parseDataCallback.call(null, result);
            } else {
                parsedResult = {
                    text: result[options.dataFieldText],
                    title: result[options.dataFieldTitle]
                };
            }
            if (parsedResult) {
                this.addToCache(id, parsedResult.title, parsedResult.text);
                if (id == this.currentId) {
                    this.container.removeClass(this.options.loadingFeedbackCssClass);
                    this.setTitle(parsedResult.title);
                    this.setText(parsedResult.text);

                    // reposition in fixed mode since size might have changed
                    this.repositionFixedTip();
                }
            } else {
                this.loadDataFailure(id, null);
            }
        },

        loadDataFailure: function(id, message) {
            if (id == this.currentId) {
                this.container.removeClass(this.options.loadingFeedbackCssClass);
                if (typeof message == 'object') {
                    if (this.options.errorFieldMessage) {
                        message = message[this.options.errorFieldMessage];
                    } else {
                        message = null;
                    }
                }
                if (!message) {
                    message = this.options.loadDataFailedMessage;
                }
                this.setTitle(null);
                this.setText(message);
                // reposition in fixed mode since size might have changed
                this.repositionFixedTip();
            }
        },

        /**
         * Reposition the tip if the 'fixed' option is set. This method should be called if the
         * content of the tip changed.
         */
        repositionFixedTip: function() {
            if (this.options.fixed && this.shown) {
                this.position(this.currentElement);
            }
        }.protect(),

        /**
         * @override
         */
        updateTip: function(element) {
            var id, cached;
            if (typeof this.options.idExtractor == 'string') {
                id = element.getProperty(this.options.idExtractor);
            } else {
                // assume it is a function
                id = this.options.idExtractor.call(null, element);
            }
            if (id) {
                this.currentId = id;
                this.currentElement = element;
                cached = this.getFromCache(id);
                if (cached) {
                    this.container.removeClass(this.options.loadingFeedbackCssClass);
                    this.setTitle(cached.title);
                    this.setText(cached.text);
                } else {
                    this.container.addClass(this.options.loadingFeedbackCssClass);
                    this.setTitle(null);
                    this.setText(this.options.loadingFeedbackMessage);
                    this.loadData(id);
                }
                return true;
            }
            return false;
        }
    });
})(this.runtimeNamespace, this);