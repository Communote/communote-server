/**
 * Utility class that provides functions to work with Communote users.
 */
var UserUtils = new Class({

    Implements: Options,

    options: {
        cacheOptions: {
            limit: 25,
            timeToLive: 3600
        }
    },

    apiAccessor: undefined,
    cache: undefined,
    navigationItemResourceBase: undefined,
    observationsResourceBase: undefined,
    preferenceResource: undefined,

    /**
     * Create a new instance.
     * 
     * @param {RestApiAccessor} apiAccessor The RestApiAccessor instance to use for the API requests
     * @param {Object} options An object with settings that should overwrite the default options
     */
    initialize: function(apiAccessor, options) {
        var currentUserId = communote.currentUser.id;
        this.setOptions(options);
        this.apiAccessor = apiAccessor;
        if (this.options.cacheOptions) {
            this.cache = new Cache(this.options.cacheOptions);
        }
        this.navigationItemResourceBase = 'users/' + currentUserId + '/navigationItems/';
        this.observationsResourceBase = 'users/' + currentUserId + '/observations/';
        this.preferenceResource = 'users/' + currentUserId + '/userPreferences/0';
    },

    /**
     * Create the signature of a user the same way the backend does.
     * 
     */
    buildFullUserSignature: function(salutation, firstName, lastName, alias) {
        var signature = '';
        if (salutation) {
            signature += salutation + ' ';
        }
        if (firstName) {
            signature += firstName + ' ';
        }
        if (lastName) {
            signature += lastName + ' ';
        }
        if (signature.length) {
            signature += '(@' + alias + ')';
        } else {
            signature = '@' + alias;
        }
        return signature;
    },
    
    buildPhoneNumber: function(countryCode, areaCode, number) {
        var phoneNumber = '';
        if (countryCode) {
            phoneNumber = '+' + countryCode + ' ';
        }
        if (areaCode) {
            phoneNumber += '(' + areaCode + ') ';
        }
        if (number) {
            phoneNumber += number;
        }
        return phoneNumber.trim();
    },
    
    buildShortUserSignature: function(firstName, lastName, alias) {
        var signature = '';
        if (firstName) {
            signature = firstName;
            if (lastName) {
                signature += ' ' + lastName;
            }
        } else if (lastName) {
            signature = lastName;
        } else {
            signature = '@' + alias;
        }
        return signature;
    },

    /**
     * Helper success callback which puts the result into the cache before calling the provided
     * success callback.
     * 
     * @param {String} cacheKey The key to use for caching
     * @param {Function} orgSuccessCallback The original, provided callback to invoke after caching
     * @param {Object} response The response returned by the server
     */
    cachingSuccessCallback: function(cacheKey, orgSuccessCallback, response) {
        this.cache.put(cacheKey, response);
        orgSuccessCallback.call(null, response);
    },

    createNavigationItem: function(navigationItemData, successCallback, errorCallback, options) {
        options = Object.append({}, options);
        options.data = this.prepareNavigationItemDataForSend(navigationItemData);
        this.apiAccessor.doApiRequest('post', this.navigationItemResourceBase, options,
                successCallback, errorCallback);
    },

    /**
     * Create a permanent link for a user tag.
     * 
     * @param {Object} tag A tag resource as returned by the REST API
     */
    createTagPermalink: function(tag) {
        return buildRequestUrl('/portal/users?tagId=' + tag.tagId);
    },
    
    defaultErrorCallback: function(response) {
        showNotification(NOTIFICATION_BOX_TYPES.error, null, response.message, {
            duration: ''
        });
    },

    deleteNavigationItem: function(id, successCallback, errorCallback, options) {
        this.apiAccessor.doApiRequest('DELETE', this.navigationItemResourceBase + id, options,
                successCallback, errorCallback);
    },

    /**
     * Method to be called when a user, that is any of its properties, changed.
     * 
     * @param {String|Number} id The ID of the user that changed
     */
    entityChanged: function(id) {
        // invalidate the cache by removing the item
        this.cache.remove('user' + id);
    },

    // TODO put somewhere else?
    /**
     * Get the REST API information resource.
     * 
     * @param {Function} successCallback A function to be called after the information resource was loaded
     *            successfully. The function will be passed the API response object containing the
     *            information resource.
     * @param {Function} [errorCallback] A function to be called when the request failed. This
     *            function will be passed the error object returned by the REST API which usually
     *            contains a 'message' field and might contain an 'errors' element with detailed
     *            error descriptions. If callback is not provided there won't be no error feedback.
     * @param {Object} [options] An object with additional options to be passed to the doApiRequest
     *            method of the APIAccessor
     * @return {Boolean} return whether the response could be loaded from cache
     */
    getInformation: function(successCallback, errorCallback, options) {
        if (!options) {
            options = {};
        }
        // no apiVersion since information resource unversioned 
        options.apiVersion = false;
        this.apiAccessor.doApiRequest('get', 'information', options,
                successCallback, errorCallback);
        return false;
    },
    
    /**
     * Get all navigation items of the current user.
     * 
     * @param {Function} successCallback A function to be called after the items were loaded
     *            successfully. The function will be passed the API response object containing the
     *            navigation items.
     * @param {Function} [errorCallback] A function to be called when the request failed. This
     *            function will be passed the error object returned by the REST API which usually
     *            contains a 'message' field and might contain an 'errors' element with detailed
     *            error descriptions. If callback is not provided there won't be no error feedback.
     * @param {Object} [options] An object with additional options to be passed to the doApiRequest
     *            method
     * @return {Boolean} return whether the response could be loaded from cache
     */
    getNavigationItems: function(successCallback, errorCallback, options) {
        this.apiAccessor.doApiRequest('get', this.navigationItemResourceBase, options,
                successCallback, errorCallback);
        return false;
    },

    getObservations: function(itemIds, successCallback, errorCallback, options) {
        var requestOptions = Object.merge({}, options);
        if (itemIds) {
            if (!requestOptions.data) {
                requestOptions.data = {};
            }
            if (typeOf(itemIds) == 'array') {
                itemIds = itemIds.join(',');
            }
            requestOptions.data.f_observations = itemIds;
        }
        this.apiAccessor.doApiRequest('get', this.observationsResourceBase, requestOptions,
                successCallback, errorCallback);
        return false;
    },

    /**
     * Get the additional parameters which should be included when fetching the observations of the current user.
     * @return {Object[]} array of objects with key and value members which describe the request parameters
     */
    getObservationsAdditionalParameters: function() {
        return [{key: this.apiAccessor.options.disableBasicAuthParameter, value: true}];
    },
    
    /**
     * @return {String} the relative URL to fetch the observations of the current user
     */
    getObservationsUrl: function() {
        return this.apiAccessor.getResourceUrl(this.observationsResourceBase);
    },

    /**
     * Get a REST API User resource by its ID.
     * 
     * @param {Number} id The ID of the user to retrieve
     * @param {Function} successCallback A function to be called after the User was loaded
     *            successfully. The function will be passed the API response object containing the
     *            user resource.
     * @param {Function} [errorCallback] A function to be called when the request failed. This
     *            function will be passed the error object returned by the REST API which usually
     *            contains a 'message' field and might contain an 'errors' element with detailed
     *            error descriptions. If callback is not provided there won't be no error feedback.
     * @param {Object} [options] An object with additional options to be passed to the doApiRequest
     *            method of the APIAccessor
     * @return {Boolean} return whether the response could be loaded from cache
     */
    getUser: function(id, successCallback, errorCallback, options) {
        var cached, cacheKey;
        var cache = this.cache;
        if (cache) {
            cacheKey = 'user' + id;
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
        this.apiAccessor
                .doApiRequest('get', 'users/' + id, options, successCallback, errorCallback);
        return false;
    },

    prepareNavigationItemDataForSend: function(navigationItemData) {
        var unlinkedData = Object.merge({}, navigationItemData);
        if (navigationItemData.data && typeof navigationItemData.data != 'string') {
            unlinkedData.data = JSON.encode(navigationItemData.data);
        }
        return unlinkedData;
    },

    renameNavigationItem: function(id, newName, successCallback, errorCallback, options) {
        var navigationItemData = {
            name: newName
        };
        this.updateNavigationItem(id, navigationItemData, successCallback, errorCallback, options);
    },
    
    moveNavigationItem: function(id, newIndex, successCallback, errorCallback, options) {
        var navigationItemData = {
            index: newIndex
        };
        this.updateNavigationItem(id, navigationItemData, successCallback, errorCallback, options);
    },

    updateNavigationItem: function(id, navigationItemData, successCallback, errorCallback, options) {
        options = Object.append({}, options);
        options.data = this.prepareNavigationItemDataForSend(navigationItemData);
        this.apiAccessor.doApiRequest('put', this.navigationItemResourceBase + id, options,
                successCallback, errorCallback);
    },

    updateNavigationItemLastCheckDate: function(id, newTimestamp, successCallback, errorCallback,
            options) {
        var navigationItemData = {
            lastAccessDate: newTimestamp
        };
        this.updateNavigationItem(id, navigationItemData, successCallback, errorCallback, options);
    },

    /**
     * Update a UserPreference using the REST API.
     * 
     * @param {String} preferenceType The name of the class/type of the preference to update
     * @param {Object} preferences Object with keys representing the fields of the preference type
     *            and values which hold the new values to set
     * @param {Function} successCallback A function to be called after the Preferences were updated
     *            successfully. The function will be passed the API response object containing the
     *            user resource.
     * @param {Function} [errorCallback] A function to be called when the request failed. This
     *            function will be passed the error object returned by the REST API which usually
     *            contains a 'message' field and might contain an 'errors' element with detailed
     *            error descriptions. If callback is not provided a message popup will show the
     *            error message contained in the REST API response.
     * @param {Object} [options] An object with additional options to be passed to the doApiRequest
     *            method
     * 
     */
    updateUserPreference: function(preferenceType, preferences, successCallback, errorCallback,
            options) {
        var key;
        var preferenceResources = [];
        for (key in preferences) {
            if (preferences.hasOwnProperty(key)) {
                preferenceResources.push({
                    key: key,
                    value: preferences[key]
                });
            }
        }
        options = Object.append({}, options);
        options.data = {
            className: preferenceType,
            preferences: preferenceResources
        };
        this.apiAccessor.doApiRequest('put', this.preferenceResource, options, successCallback,
                errorCallback || this.defaultErrorCallback);
    }
});