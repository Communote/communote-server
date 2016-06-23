/**
 * Utility class that provides functions to work with Communote note tags.
 */
var NoteTagUtils = new Class({

    Implements: Options,

    options: {
        cacheOptions: {
            limit: 25,
            timeToLive: 3600
        }
    },

    apiAccessor: null,
    cache: null,

    /**
     * Create a new instance.
     * 
     * @param {RestApiAccessor} apiAccessor The RestApiAccessor instance to use for the API requests
     * @param {Object} options An object with settings that should overwrite the default options
     */
    initialize: function(apiAccessor, options) {
        this.setOptions(options);
        this.apiAccessor = apiAccessor;
        if (this.options.cacheOptions) {
            this.cache = new Cache(this.options.cacheOptions);
        }
    },

    /**
     * Create a permanent link for a note tag.
     * 
     * @param {Object} tag A tag resource as returned by the REST API
     */
    createTagPermalink: function(tag) {
        return buildRequestUrl('/portal/tags/' + tag.tagId + '/' + encodeURIComponent(tag.name));
    },

    /**
     * Method to be called when a tag, that is any of its properties, changed.
     * 
     * @param {String|Number} id The ID of the tag that changed
     */
    entityChanged: function(id) {
        // invalidate the cache by removing the item
        this.cache.remove('tag' + id);
    },

    /**
     * Get a REST API tag resource by its ID.
     * 
     * @param {Number} id The ID of the tag to retrieve
     * @param {Function} successCallback A function to be called after the tag was loaded
     *            successfully. The function will be passed the API response object containing the
     *            user resource.
     * @param {Function} [errorCallback] A function to be called when the request failed. This
     *            function will be passed the error object returned by the REST API which usually
     *            contains a 'message' field and might contain an 'errors' element with detailed
     *            error descriptions. If callback is not provided there won't be no error feedback.
     * @param {Object} [options] An object with additional options to be passed to the doApiRequest
     *            method
     * @return {Boolean} return whether the response could be loaded from cache
     */
    getTag: function(id, successCallback, errorCallback, options) {
        var cached, cacheKey;
        var cache = this.cache;
        if (cache) {
            cacheKey = 'tag' + id;
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
        this.apiAccessor.doApiRequest('get', 'tags/' + id, options, successCallback, errorCallback);
        return false;
    },

    cachingSuccessCallback: function(cacheKey, orgSuccessCallback, response) {
        this.cache.put(cacheKey, response);
        orgSuccessCallback.call(null, response);
    },

    /**
     * Method to delete and optionally merge a tag.
     * 
     * @param {Number} id Id of the tag to delete.
     * @param {Number} newTagId                   Id of the new tag.
     */
    deleteTag: function(id, newTagId, successCallback, errorCallback) {
        this.entityChanged(id);
        this.apiAccessor.doApiRequest('delete', 'tags/' + id, {
            data: {
                newTagId: newTagId
            }
        }, successCallback, errorCallback);
    },

    /**
     * Method to delete and optionally merge a tag.
     *
     * @param {Number} id Id of the tag to rename.
     * @param {String} name New name of the tag.
     */
    renameTag: function(id, name, successCallback, errorCallback) {
        this.entityChanged(id);
        this.apiAccessor.doApiRequest('put', 'tags/' + id, {
            data: {
                renameTag: true,
                defaultName: name
            }
        }, successCallback, errorCallback);
    }
});