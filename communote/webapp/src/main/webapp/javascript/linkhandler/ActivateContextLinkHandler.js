(function(namespace) {
    // TODO processXxxData handlers in this class isn't the cleanest way, but adding subclasses for each type isn't better
    var ActivateContextLinkHandler = new Class({

        Extends: namespace.getConstructor('ApplicationLinkHandler'),

        options: {
            // the name of the request parameter that provides the ID of the view to activate
            viewIdParameter: 'viewId',
            // the name of the group to send the activateContext event to
            activateContextEventGroup: 'mainPageContextManagement',
            // function to call before activating the context. This function will be passed the
            // context descriptor object that is provided as argument to the activateContext event.
            contextDescriptorPostProcessor: undefined,
            // the handler to use to interpret the data passed to the open method. Can be a function
            // or a string that identifiers a local method. The method must take 3 parameters which
            // are the data object, the object to receive the context filter parameters and the filter
            // parameters to be published in the filter group of the activated context. The should return
            // false if the data does not contain the required information.
            processDataHandler: undefined
        },
        applicationPath: undefined,
        contextId: undefined,

        initialize: function(applicationPath, contextId, options) {
            this.parent(applicationPath, options);
            // assert data handler refers to a local function if it is a string
            if (typeof this.options.processDataHandler == 'string') {
                if (typeof this[this.options.processDataHandler] != 'function') {
                    delete this.options.processDataHandler;
                }
            }
            this.contextId = contextId;
        },

        extractViewId: function(params) {
            var viewId = params[this.options.viewIdParameter];
            if (viewId) {
                delete params[this.options.viewIdParameter];
            }
            return viewId;
        },

        processUserData: function(data, contextFilterParameters, filterParameters) {
            var dataStore;
            var userId = data && data.userId;
            if (userId == undefined) {
                return false;
            }
            // put userId into the context parameters and assert that the filterParameters
            // do not contain a userId filter. TODO should we do the latter in MainPageContextManager?
            contextFilterParameters.userId = userId;
            delete filterParameters['userId'];
            // publish the user details if contained
            if (data.userLongName && data.userShortName) {
                // TODO using the datastore isn't the best way. Better solution would be to use the userUtils!
                dataStore = communote.widgetController.getDataStore();
                dataStore.put({
                    type: 'user',
                    key: userId,
                    shortName: data.userShortName,
                    longName: data.userLongName
                });
            }
            return true;
        },

        processTopicData: function(data, contextFilterParameters, filterParameters) {
            var dataStore, title;
            var topicId = data && (data.blogId || data.topicId);
            if (topicId == undefined) {
                return false;
            }
            // TODO should we use topicId as parameter name instead?
            contextFilterParameters.targetBlogId = topicId;
            delete filterParameters['blogId'];
            delete filterParameters['targetBlogId'];
            dataStore = communote.widgetController.getDataStore();
            // TODO InitialFilterParameters use blog-prefix, entity-details of CPL and activities
            // use REST-API names which is for a topic only title, for a note entity it's
            // topicTitle. Kind of confusing.
            title = data.blogTitle || data.title || data.topicTitle;
            if (title) {
                dataStore.put({
                    type: 'blog',
                    key: topicId,
                    title: title
                });
            }
            if (data.blogRole) {
                dataStore.put({
                    type: 'blogRole',
                    key: topicId,
                    role: data.blogRole
                });
            }
            // can also contain note details to show a certain note of the topic
            if (data.noteId) {
                filterParameters.noteId = data.noteId;
                dataStore.put({
                    type: 'note',
                    key: data.noteId,
                    title: data.noteTitle
                });
            }
            return true;
        },

        processTagData: function(data, contextFilterParameters, filterParameters) {
            var dataStore;
            var tagId = data && data.tagId;
            if (tagId == undefined) {
                return false;
            }
            // TODO should we use tagId as parameter name instead?
            contextFilterParameters.tagIds = tagId;
            delete filterParameters['tagIds'];
            this.publishTagDetails(tagId, data.tagName);
            return true;
        },

        /**
         * Check for the filter parameters for a tagId filter and if set publish tag details
         * contained in data
         */
        processTagFilterData: function(data, contextFilterParameters, filterParameters) {
            var tagId;
            var paramName = this.options.filterParameterMapping['tagId'] || 'tagId';
            if (filterParameters[paramName]) {
                // TODO should fail if tagId filter is set but no data is provided?
                tagId = data && data.tagId;
                if (tagId != undefined) {
                    this.publishTagDetails(tagId, data.tagName);
                }
            }
            return true;
        },

        publishTagDetails: function(tagId, tagName) {
            var dataStore;
            if (tagName) {
                dataStore = communote.widgetController.getDataStore();
                dataStore.put({
                    type: 'tag',
                    key: tagId,
                    title: tagName
                });
            }
        },

        open: function(path, paramString, hashString, data) {
            var dataHandler, dataValid;
            var params = this.getObjectFromQueryString(paramString);
            var viewId = this.extractViewId(params);
            var contextDescr = {};
            contextDescr.contextId = this.contextId;
            if (viewId) {
                contextDescr.viewId = viewId;
            }
            contextDescr.options = {};
            contextDescr.options.filterParams = params;
            dataHandler = this.options.processDataHandler;
            if (dataHandler) {
                contextDescr.contextFilterParameters = {};
                if (typeof dataHandler == 'string') {
                    dataValid = this[dataHandler].call(this, data,
                            contextDescr.contextFilterParameters, params);
                } else {
                    dataValid = dataHandler.call(null, data, contextDescr.contextFilterParameters,
                            params);
                }
            } else {
                dataValid = true;
            }
            if (dataValid) {
                if (this.options.contextDescriptorPostProcessor) {
                    this.options.contextDescriptorPostProcessor.call(null, contextDescr);
                }
                E2G('activateContext', this.options.activateContextEventGroup, contextDescr);
            }
            return dataValid;
        },

        processLocation: function(path, paramString, hashString) {
            this.open(path, paramString, hashString, namespace.initialFilterParameters);
        }
    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('ActivateContextLinkHandler', ActivateContextLinkHandler);
    } else {
        window.ActivateContextLinkHandler = ActivateContextLinkHandler;
    }
})(window.runtimeNamespace);