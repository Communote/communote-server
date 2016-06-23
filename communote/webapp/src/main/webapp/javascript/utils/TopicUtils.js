/**
 * Class with helper functions for working with blogs. It for instance returns the current blogId of
 * an associated FilterParameterStore. This is useful for all widgets that are only interested in
 * the current blog ID and do not need the overhead of a listener registration. Moreover, this class
 * gives access to the default blog if active and the role the current user has for that blog.
 */
var TopicUtils = new Class({

    Implements: Options,

    options: {
        cacheOptions: {
            limit: 50,
            timeToLive: 3600
        },
        blogIdFilterParameter: 'targetBlogId'
    },

    filterParameterStore: null,
    staticBlogId: null,
    defaultBlogEnabled: false,
    defaultBlog: null,
    apiAccessor: null,
    cache: null,
    topicAccessUrl: null,

    /**
     * @param {RestApiAccessor} apiAccessor The RestApiAccessor instance to get topic details via
     *            REST API calls.
     * @param {FilterParameterStore|String} blogIdResource Can be a FilterParameterStore instance to
     *            get the current blog from the targetBlogId filter parameter. If it is not a
     *            FilterParameterStore the argument is interpreted as the current blog ID.
     * @param {Object} defaultBlog An object with details about the default blog. Can be null if
     *            there is no default blog or the current user has no access.
     * @param {Object} [options] Additional options object to overwrite default settings.
     */
    initialize: function(apiAccessor, blogIdResource, defaultBlog, options) {
        this.setOptions(options);
        this.apiAccessor = apiAccessor;
        if (typeof blogIdResource === 'object' && blogIdResource.getFilterParameter) {
            this.filterParameterStore = blogIdResource;
        } else {
            this.staticBlogId = blogIdResource;
        }
        if (defaultBlog) {
            this.defaultBlog = Object.clone(defaultBlog);
            this.defaultBlogEnabled = true;
        }
        if (this.options.cacheOptions) {
            this.cache = new Cache(this.options.cacheOptions);
        }
        this.topicAccessUrl = buildRequestUrl('/blog/control/findBlogs.do');
    },

    /**
     * Method to be called when a topic, that is any of its properties, changed.
     * 
     * @param {String|Number} id The ID of the topic that changed
     */
    entityChanged: function(id) {
        // invalidate the cache by removing the item
        this.cache.remove('topic' + id);
    },

    getCachedTopicRole: function(id) {
        var cache = this.cache;
        if (cache) {
            cacheKey = 'topic' + id;
            cached = cache.get(cacheKey);
            if (cached) {
                return cached.result.userRole || 'NONE';
            }
        }
        return null;
    },
    /**
     * Get a REST API topic resource by its ID.
     * 
     * @param {Number} id The ID of the topic to retrieve
     * @param {Function} successCallback A function to be called after the topic was loaded
     *            successfully. The function will be passed the API response object containing the
     *            topic resource.
     * @param {Function} [errorCallback] A function to be called when the request failed. This
     *            function will be passed the error object returned by the REST API which usually
     *            contains a 'message' field and might contain an 'errors' element with detailed
     *            error descriptions. If callback is not provided there won't be no error feedback.
     * @param {Object} [options] An object with additional options to be passed to the doApiRequest
     *            method
     * @return {Boolean} return whether the response could be loaded from cache. If true is returned
     *         the successCallback has already been invoked.
     */
    getTopic: function(id, successCallback, errorCallback, options) {
        var cached, cacheKey;
        var cache = this.cache;
        if (cache) {
            cacheKey = 'topic' + id;
            cached = cache.get(cacheKey);
        }
        if (cached) {
            successCallback.call(null, cached);
            return true;
        }
        // TODO cache error case?
        if (cache) {
            successCallback = this.cachingSuccessCallback.bind(this, cacheKey, successCallback);
        }
        this.apiAccessor.doApiRequest('get', 'topics/' + id, options, successCallback,
                errorCallback);
        return false;
    },

    /**
     * Provide an object that contains only the ID and the title of a topic.
     * 
     * @param {Number|String} id The ID of the topic to retrieve
     * @param {Function} successCallback The function to pass the retrieved topic info. The function
     *            is called with an object that contains the members topicId and title.
     * @param {Function} errorCallback The function to call in case of an error. [errorCallback] A
     *            function to be called when the request failed. This function will be passed the
     *            error object returned by the REST API which usually contains a 'message' field and
     *            might contain an 'errors' element with detailed error descriptions. If callback is
     *            not provided there won't be no error feedback.
     * @param {Object} [options] An object with additional options to be passed to the doApiRequest
     *            method
     * @return {Boolean} return whether the response could be loaded from cache. If true is returned
     *         the successCallback has already been invoked.
     */
    // TODO include alias in info.
    // TODO add a method publishTopicInfo which puts the topicInfo into a cache and invalidates the
    // cached topic resource if content differs. The publishTopicInfo should than be used instead of the DataStore 
    getTopicInfo: function(id, successCallback, errorCallback, options) {
        var dataStore, cached, topicInfo;
        // check deprecated dataStore
        dataStore = communote.widgetController.getDataStore();
        cached = dataStore.get('blog', id);
        if (cached) {
            topicInfo = {};
            topicInfo.title = cached.title;
            topicInfo.topicId = id;
            successCallback.call(null, topicInfo);
            return true;
        }
        successCallback = this.getTopicInfoSuccessCallback.bind(this, successCallback);
        return this.getTopic(id, successCallback, errorCallback, options);
    },

    getTopicInfoSuccessCallback: function(orgSuccessCallback, response) {
        var topicInfo = {};
        topicInfo.topicId = response.result.topicId;
        topicInfo.title = response.result.title;
        // TODO should contain alias too, but currently not possible because dataStore stuff
        orgSuccessCallback.call(null, topicInfo);
    },

    cachingSuccessCallback: function(cacheKey, orgSuccessCallback, response) {
        this.cache.put(cacheKey, response);
        orgSuccessCallback.call(null, response);
    },

    /**
     * Create a permanent link for a topic tag.
     * 
     * @param {Object} tag A tag resource as returned by the REST API
     */
    createTagPermalink: function(tag) {
        return buildRequestUrl('/portal/topics?tagId=' + tag.tagId);
    },

    /**
     * Checks whether the current user has write access to the provided blog. This method will check
     * the defaultBlog role, if available, and the dataStore for the role of the blog.
     * 
     * @param {string|number} blogId The ID of the blog to check
     * @returns true if the user has access, false otherwise
     */
    blogWriteAccess: function(blogId) {
        var storedEntry, role = 'NONE';
        if (blogId != null) {
            if (this.defaultBlog && this.defaultBlog.id == blogId) {
                role = this.defaultBlog.role;
            } else {
                // TODO get rid of dataStore and use local cache
                storedEntry = communote.widgetController.getDataStore().get('blogRole', blogId);
                if (storedEntry) {
                    role = storedEntry.role;
                } else {
                    role = this.getCachedTopicRole(blogId);
                    if (!role) {
                        return this.checkWriteAccessForTopic(blogId);
                    }
                }
            }
        }
        return !role || (role != 'NONE' && role != 'VIEWER');
    },
    
    checkWriteAccessForTopic: function(topicId) {
        var request, options;
        var access = false;
        var data = {};
        data.blogId = topicId;
        data.blogAccess = 'write';
        // fetch subtopics and put them into cache
        if (this.cache) {
            data.includeChildTopics = true;
        }
        data.noSummary = true;
        options = {};
        options.url = this.topicAccessUrl;
        options.data = data;
        options.async = false;
        request = new Request.JSON(options);
        request.addEvent('complete', function(topics) {
            var currentTopicFound = false;
            var writableSubtopics = [];
            var i = 0;
            for (i; i < topics.length; i++) {
                if (topics[i].id == topicId) {
                    currentTopicFound = true;
                } else {
                    writableSubtopics.push(topics[i]);
                }
            }
            if (this.cache) {
                this.cache.put('writableSubtopics' + topicId, writableSubtopics);
            }
            access = currentTopicFound;
        }.bind(this));
        request.get();
        return access;
    }.protect(),

    getWritableSubtopics: function(topicId) {
        var cacheKey, subtopics, request, options, data;
        var cache = this.cache;
        if (cache) {
            cacheKey = 'writableSubtopics' + topicId;
            subtopics = cache.get(cacheKey);
        }
        if (subtopics) {
            return subtopics;
        }
        data = {};
        data.blogId = topicId;
        data.blogAccess = 'write';
        // only get subtopics
        data.blogIdsToExclude = topicId;
        data.includeChildTopics = true;
        data.noSummary = true;
        options = {};
        options.url = this.topicAccessUrl;
        options.data = data;
        options.async = false;
        request = new Request.JSON(options);
        request.addEvent('complete', function(topics) {
            if (this.cache) {
                this.cache.put(cacheKey, topics);
            }
            subtopics = topics;
        }.bind(this));
        request.get();
        return subtopics;
    },
    
    /**
     * @returns {boolean} true if the default blog is enabled and the current user has read access,
     *          false otherwise
     */
    isDefaultBlogEnabled: function() {
        return this.defaultBlogEnabled;
    },

    /**
     * @return {string} A String describing the blog role of the current user in the default blog.
     *         Will be null if the default blog is disabled or the user has no access
     */
    getDefaultBlogRole: function() {
        return this.defaultBlog && this.defaultBlog.role;
    },

    /**
     * @returns {object} Object with details about the default blog. This includes the 'alias',
     *          'title' and 'id'. Returns null if the default blog is disabled or the current user
     *          has no access.
     */
    getDefaultBlog: function() {
        return this.defaultBlog;
    },
    /**
     * Return the current blog ID. This will be the value of the parameter 'targetBlogId' if this
     * object was constructed with a FilterParameterStore or the value passed as blogResource to the
     * constructor. In case the blog ID is an array the first entry will be returned only if the
     * length is 1.
     * 
     * @return {String|Number|Object} The blog ID, can be null, for instance if no blog is set.
     */
    getCurrentBlogId: function() {
        var blogId;
        if (this.filterParameterStore) {
            blogId = this.filterParameterStore
                    .getFilterParameter(this.options.blogIdFilterParameter);
        } else {
            blogId = this.staticBlogId;
        }
        if (blogId != null) {
            if (typeOf(blogId) == 'array') {
                if (blogId.length == 1) {
                    blogId = blogId[0];
                } else
                    blogId = null;
            }
        }
        return blogId;
    },

    /**
     * Returns a query string containing the current blogId. This string will be empty if more than
     * one blog or no blog is selected.
     * 
     * @param {String} [prefix] Another query string which will be prepended to the result.
     * @return {String} The URI encoded query string.
     */
    getQueryString: function(prefix) {
        var blogId;
        var qs = [];
        if (prefix) {
            qs.push(prefix);
        }
        blogId = this.getCurrentBlogId();
        if (blogId) {
            qs.push('blogId=' + encodeURIComponent(blogId));
        }
        return qs.join('&');
    },

    /**
     * Get the role of the current user for the given topic.
     */
    getTopicRole: function(topicId) {
        var foundRole;
        // do an synchronous request since we want an immediate result
        this.getTopic(topicId, function(response) {
            foundRole = response.result.userRole;
        }, null, {
            async: false
        });
        return foundRole || 'NONE';
    },
    /**
     * Create a URL pointing to the profile image of a topic.
     * @param {TopicResource} topic A topic resource as returned by the rest api
     * @return {String} the URL
     */
    getTopicProfileImageUrl: function(topic) {
        return buildRequestUrl('/image/entity-profile.do?id=' + topic.profileImageId
                + '&size=MEDIUM&lastModified=' + topic.profileImageVersion);
    },

    /**
     * Method to set or remove a role from a topic for a given entity.
     * 
     * @param {Long} [entityId] Id of the entity to set the role for.
     * @param {Long} [topicId] Id of the topic to set the role for.
     * @param {String} role The role to set. If empty the user will removed from the topic.
     * @param {Function} successCallback An optional callback for a successful response.
     * @param {Function} errorCallback An optional call, when the request fails.
     */
    setTopicRole: function(entityId, topicId, role, successCallback, errorCallback) {
        var options = {
            data: {
                'role': role,
                'entityId': entityId
            }
        };
        if (!successCallback) {
            successCallback = this.defaultSuccessCallback;
        }
        if (!errorCallback) {
            errorCallback = this.defaultErrorCallback;
        }
        this.apiAccessor.doApiRequest('post', 'topics/' + topicId + '/roles', options,
                successCallback, errorCallback);
    },

    defaultSuccessCallback: function(response) {
        showNotification(NOTIFICATION_BOX_TYPES.success, null, response.message);
    },

    defaultErrorCallback: function(response) {
        showNotification(NOTIFICATION_BOX_TYPES.error, null, response.message, {
            duration: ''
        });
    }
});
