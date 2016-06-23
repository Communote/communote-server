/**
 * @class 
 * @augments communote.widget.classes.controllers.Controller
 */
communote.widget.classes.controllers.ApiController = communote.widget.classes.controllers.Controller.extend(
/** 
 * @lends communote.widget.classes.controllers.ApiController.prototype
 */
{
    // TODO nja: optimization create local reference to eventcontroller in constructor
    // @rwi: I think this isn't useful, because of different bind possibilities for success/error handlers
    // @nja: when the default handlers are called the bind must be (and currently is) an instance of this
    // class (otherwise your error handler fail!)
    name: 'ApiController',

    /** the configuration */
    configuration: null,

    /** the internal CommunoteApiAccessor */
    apiAccessor: null,
    
    /** the autocomplete runOnceId */
    autocompleteId: 'ApiController#autocomplete',

    /**
     * ApiController Class that manages all accesses on the Communote REST API. by
     *             providing an interface for the HTML client.
     *
     * @param {Configuration} configuration the Configuration to use
     * @param {CommunoteApiAccessor} apiAccessor the CommunoteApiAccessor to use
     */
    constructor: function(configuration, apiAccessor) {

        this.configuration = configuration;
        this.apiAccessor = apiAccessor;
    },

    /**
     * Prepares an API request and performs the request by calling the doJsonRequest method of the
     * internal CommunoteApiAccessor instance. The requestDescriptor object may have the following
     * properties:
     *   - urlPart (REQUIRED):
     *          the URL suffix of the API to call (translation via the
     *          apiAccessor.getMappedRessource() method)
     *   - httpMethod (optional):
     *          the HTTP method (GET, POST, PUT, DELETE), defaults to GET
     *   - data (REQUIRED):
     *          the data object to send with the request
     *   - async (optional):
     *          send asynchronous request? defaults to true
     *   - runOnceId (optional):
     *          if set skips repeated requests with the same runOnceId while one of those requests
     *          is running
     *   - eventChannel (optional):
     *          event channel to send the success etc. events to; defaults to global channel
     *   - successHandler (optional):
     *          the success handler method, defaults to this.onApiRequestSuccessDefault
     *   - errorHandler (optional):
     *          the error handler method, defaults to this.onApiRequestErrorDefault
     *   - bind (optional):
     *          the object that becomes "this" within the success/error handler methods
     *   - resource (REQUIRED):
     *          if required by calling code (data is available under this key in the
     *          resulting object), legacy compatibility
     *
     * @param {Object} requestDescriptor
     * @returns void
     */
    doApiRequest: function(requestDescriptor) {

        // handle default values
        if (requestDescriptor.async == undefined) {
            requestDescriptor.async = true;
        } else {
            requestDescriptor.async = !!requestDescriptor.async;
        }

        requestDescriptor.successHandler = requestDescriptor.successHandler
                || this.onApiRequestSuccessDefault;
        requestDescriptor.errorHandler = requestDescriptor.errorHandler
                || this.onApiRequestErrorDefault;

        // call underlying JSON
        this.apiAccessor.doJsonRequest(requestDescriptor);
    },

    /**
     * Default success handler method for JSON requests. Fires the events "success" and "dataReady"
     * on the given channel.
     *
     * @param {Object} result the result data object
     * @param {String} message the message from the API
     * @param {String} channel the channel to fire possible events at
     * @returns void
     */
    onApiRequestSuccessDefault: function(result, message, channel) {
        // TODO: change event name
        var data = {
            result: result,
            message: message
        };
        communote.widget.EventController.fireEvent('success', channel, data);
        communote.widget.EventController.fireEvent('success', "global", data);

        communote.widget.EventController.fireEvent('dataReady', channel, result);
    },

    /**
     * Default error handler method for JSON requests. Fires an "error" event with a meaningful
     * message
     *
     * @param {Object} jqXHR jQuery's XHR object
     * @param {String} textStatus jQuery's textStatus
     * @param {Object} errorThrown the thrown error; may be null
     * @param {String} errorClass 'error' (possible connection problem) or 'apiError' (error
     *            produced by the API, see message in JSON response
     * @param {String} channel the channel to fire possible events at
     * @returns void
     */
    onApiRequestErrorDefault: function(jqXHR, textStatus, errorThrown, errorClass, channel) {
        var json, i;
        // status 0 is being set when connnection is aborted by client
        if ((errorClass == 'error') && (jqXHR.status > 0)) {
            var message = communote.widget.I18nController.getText('htmlclient.common.connectionError');
            // try to parse json response
            try {
                json = communote.jQuery.parseJSON(jqXHR.responseText);
                if (json.errors && json.errors.length > 0) {
                    message = "<ul>";
                    for (i = 0; i < json.errors.length; i++) {
                        message += "<li>" + json.errors[i].message + "</li>";
                    }
                    message += "</ul>";
                } else {
                    message = json.message;
                }
            } catch (e) {
                if(jqXHR.status && (jqXHR.status > 200)){
                    message += "<br/>Status " + jqXHR.status + ': ' + textStatus;
                }
                if(jqXHR.responseText){
                    message += "<br/>" + jqXHR.responseText;
                }
                message += "<br/>" + communote.widget.I18nController.getText('htmlclient.common.connectionError.hint');
            }
            // TODO: change event name
            communote.widget.EventController.fireEvent('error', channel, {
                message: message,
                errorThrown: errorThrown,
                errorClass: errorClass
            });
        }
        else{
            console.error("internal error: ApiController.onApiRequestErrorDefault() - ErrorClass: " + errorClass + " Status: " + jqXHR.status);
        }

    },

    /**
     * This function wraps for the resource attribute of a control the request to an other function
     *
     * first it looks up if there is function that is called like the resourceAttribute if this
     * fails it looks up if the resource attribute is in the mapping var and if this fails it throws
     * an error
     *
     * @param {Control} control - the data requesting control
     * @returns void
     */
    getData: function(control) {
        var resource = control.resource;
        if (this[resource] && communote.jQuery.isFunction(this[resource])) {
            this[resource](control);
            return;
        }
        if (this.apiAccessor.getMappedRessource(resource)) {
            this.getDataByResource(resource, control);
            return;
        }

        this.onApiRequestErrorDefault(null, 'Resource for control "' + control.getDomId()
                + '" not found.', null, 'error', control.channel);
    },

    /**
     * This function is called, if a resource attribute mapping is available and no function for the
     * resource itself is defined
     *
     * it build up a simple request this request fill be filtered by addFilterParameters the
     * attributes 'maxCount' and 'offset' of the control will be used, if present. If not defaults
     * to 20 resp. 0.
     *
     * @param {String} resource - the resource key to load
     * @param {Control} control - the requesting control
     */
    getDataByResource: function(resource, control) {

        var requestDescriptor, renderParameters, paramName, paramValue;

        // build data object
        var data = {};
        data.offset = control.offset || 0;
        // max count
        data.maxCount = control.maxCount;
        if (control.maxCount === undefined) {
            data.maxCount = 20;
        }

        // append topicListType
        if (control.topicListType) {
            data.topicListType = control.topicListType;
        } else {
            if (control.canWrite) {
                data.topicListType = (control.canWrite) ? 'WRITE' : 'READ';
            }
        }
        renderParameters = control.getRenderParameters();
        for (paramName in renderParameters) {
            paramValue = this.flattenFilterArray(paramName, renderParameters[paramName]);
            if (paramValue != null) {
                data[this.apiAccessor.getFilterParameterName(paramName)] = paramValue;
            }
        }

        // add filter params to data
        this.addFilterParameters(control, data);

        // build ressource descriptor
        // TODO nja: shouldn't control.id be enough as runOnceId?
        // @rwi: I don't think so; one control may send several independent requests
        requestDescriptor = {
            resource: resource,
            data: data,
            urlPart: this.apiAccessor.getMappedRessource(resource),
            runOnceId: 'ApiController#' + control.getDomId() + '#' + resource,
            eventChannel: control.channel
        };

        this.doApiRequest(requestDescriptor);
    },

    /**
     * read the data for the given noteId
     *
     * @param {string} singleNoteId
     * @param {function} successHandler
     * @param {object} bind
     */
    plainNote: function(singleNoteId, successHandler, bind) {
        var requestDescriptor = {
            urlPart: this.apiAccessor.getMappedRessource('notes') + '/' + singleNoteId,
            data: {
                filterHtml: true
            },
            resource: "notes",
            successHandler: successHandler,
            bind: bind
        };
        this.doApiRequest(requestDescriptor);
    },

    /**
     * a special function for answers - this is directly called in the NoteList not via getData it
     * is similar to the simple data request, but: - it will not be filtered - it uses a
     * f_parentNoteId filter
     *
     * @param {integer} parentNoteId the note id of the parent note
     * @param {Control}
     * @returns
     */
    answers: function(parentNoteId, control) {

        var requestDescriptor; 

        // build data
        var data = {};

        if (control.discussionId !== undefined){data[this.apiAccessor.getFilterParameterName('discussionId')] = control.discussionId;} else {data[this.apiAccessor.getFilterParameterName('parentNoteId')] = parentNoteId;}

        // build request descriptor
        requestDescriptor = {
            resource: 'timeLineNotes',
            urlPart: this.apiAccessor.getMappedRessource('timeLineNotes'),
            data: data,
            eventChannel: control.channel
        };

        this.doApiRequest(requestDescriptor);
    },

    /**
     * @method favoriteNote store the active state of favorite
     * @param {Object} successHandler
     * @param {Object} bind - context
     * @param {Object} noteData
     * @returns void
     */
    favoriteNote: function(successHandler, bind, noteData) {
        this.doApiRequest({
            urlPart: this.apiAccessor.getMappedRessource('notes') + '/' + noteData.noteId
                    + '/favorites',
            httpMethod: "POST",
            data: {
                favorite: noteData.isFavorite
            },
            resource: "notes",
            successHandler: successHandler,
            bind: bind
        });
    },

    /**
     * @method likeNote store the active state of like
     * @param {Object} successHandler
     * @param {Object} bind - context
     * @param {Object} noteData
     * @returns void
     */
    likeNote: function(successHandler, bind, noteData) {
        this.doApiRequest({
            urlPart: this.apiAccessor.getMappedRessource('notes') + '/' + noteData.noteId
                    + '/likes',
            httpMethod: "POST",
            data: {
                like: noteData.isLike
            },
            resource: "notes",
            successHandler: successHandler,
            bind: bind
        });
    },

    /**
     * @method getNoteLikers request a liked users by note id
     * @param {Object} successHandler
     * @param {Object} bind - context
     * @param {Object} noteId
     * @returns void
     */
    getNoteLikers: function(successHandler, bind, noteId) {
        this.doApiRequest({
            urlPart: this.apiAccessor.getMappedRessource('notes') + '/' + noteId
                    + '/likes',
            resource: "notes",
            successHandler: successHandler,
            bind: bind
        });

    },

    /**
     * request the note with offset = 0 (last time note))
     *
     * @param {object} successandler
     * @param {object} bind
     * @param {object} control - the data requesting control
     */
    lastTimeNote: function(successHandler, bind, control) {
        var requestDescriptor = {
            urlPart: this.apiAccessor.getMappedRessource('timeLineNotes'),
            data: {
                offset: 0,
                maxCount: 1,
                bypassSession: true
            },
            resource: "notes",
            successHandler: successHandler,
            bind: bind
        };
        this.addFilterParameters(control, requestDescriptor.data);
        this.doApiRequest(requestDescriptor);
    },

    /**
     * @method followTopic store the follow state for a topic
     * @param {object} successHandler
     * @param {object} bind
     * @param {string} topicAlias
     * @return void
     */
    followTopic: function(successHandler, bind, topicData) {
        var requestDescriptor = {
            urlPart: this.apiAccessor.getMappedRessource('writableTopics') + '/' + topicData.topicId
                    + '/follows',
            httpMethod: "POST",
            data: {
                followId: topicData.topicId,
                follow: !topicData.isFollow
            },
            resource: "writableTopics",
            successHandler: successHandler,
            bind: bind
        };
        this.doApiRequest(requestDescriptor);
    },

    /**
     * @method getTopicByAlias request a topic by topic alias
     * @param {object} successHandler
     * @param {object} bind
     * @param {string} topicAlias
     * @return void
     */
    getTopicByAlias: function(successHandler, bind, topicAlias) {

        var requestDescriptor = {
            urlPart: this.apiAccessor.getMappedRessource('writableTopics'),
            data: {
                topicListType: "READ",
                f_topicAliases: topicAlias
            },
            resource: "writableTopics",
            successHandler: successHandler,
            bind: bind
        };
        this.doApiRequest(requestDescriptor);
    },

    /**
     * Create a new Note.
     *
     * @param {integer} topicId integer identifies the topic where the note should created
     * @param {string} text the message should be noteed
     * @param {?} tags
     * @param {Control} control the control which triggers the creation
     * @param {numeric} attachmentUploadSessionId
     * @param {array} attachmentIds
     *
     * @result none
     *
     * events: Error when the ajax request fails, this message will be sent to the control
     * noteServiceSuccessEvent when the ajax request succeed, this message will be sent to the
     * control
     */
    createNote: function(topicId, text, tags, control, attachmentUploadSessionId, attachmentIds) {
        var urlPart, requestDescriptor;
        var data = {};
        // build data
        data.topicId = topicId;
        data.text = text;
        data.tags = tags || null;
        data.attachmentUploadSessionId = attachmentUploadSessionId || 0;
        data.attachmentIds = attachmentIds || [];
        //data.isDirectMessage = false;

        // build request descriptor and call API
        urlPart = this.apiAccessor.getMappedRessource('notes');
        requestDescriptor = this.buildNoteRequestDescriptor(urlPart, JSON.stringify(data), 'POST',
                [ control.channel, control.widget.channel ]);
        requestDescriptor.contentType = 'application/json';
        this.doApiRequest(requestDescriptor);
    },

    /**
     * Create an answer for a note
     *
     * @param {integer} topicId the id of the target topic
     * @param {integer} parentNoteId the id of the parent note
     * @param {String} text the text of the note
     * @param {?} tags the tags to attach to the note
     * @param {Control} control the control that is used to create the note
     * @returns void
     */
    answerNote: function(topicId, parentNoteId, text, tags, isDirectMessage, control,
            attachmentUploadSessionId, attachmentIds) {
        var urlPart, requestDescriptor;
        var data = {};
        // build data
        data.topicId = topicId;
        data.parentNoteId = parentNoteId;
        text = communote.jQuery.trim(text);
        if(isDirectMessage && text && (text.indexOf("d") != 0) ){
            text = 'd ' + text;
        }
        data.text = text;
        data.tags = tags || null;
        data.attachmentUploadSessionId = attachmentUploadSessionId || 0;
        data.attachmentIds = attachmentIds || [];
        // not needed but 'd' must be at the beginning
        // data.isDirectMessage = isDirectMessage || false;

        // build request descriptor and call API
        urlPart = this.apiAccessor.getMappedRessource('notes');
        requestDescriptor = this.buildNoteRequestDescriptor(urlPart, JSON.stringify(data), 'POST',
                [ control.channel, control.widget.channel ]);
        requestDescriptor.contentType = 'application/json';
        requestDescriptor.runOnceId = "ApiController#Answers";

        this.doApiRequest(requestDescriptor);
    },

    /**
     * Store a new text for a note
     *
     * @param {integer} topicId the id of the target topic
     * @param {integer} noteId the id of the note
     * @param {String} text the new text of the note
     * @param {?} tags the new tags to attach to the note
     * @param {Control} control the control that is used to create the note
     * @returns void
     */
    editNote: function(topicId, noteId, text, tags, control, attachmentUploadSessionId,
            attachmentIds) {
        var urlPart, requestDescriptor;
        var data = {};

        // build data
        data.topicId = topicId;
        data.noteId = noteId;
        data.text = text;
        data.tags = tags || null;
        data.attachmentUploadSessionId = attachmentUploadSessionId || 0;
        data.attachmentIds = attachmentIds || [];

        // build request descriptor and call API
        urlPart = this.apiAccessor.getMappedRessource('notes') + '/' + noteId;
        requestDescriptor = this.buildNoteRequestDescriptor(urlPart, JSON.stringify(data), 'PUT', [
                control.channel, control.widget.channel ]);
        requestDescriptor.contentType = 'application/json';
        this.doApiRequest(requestDescriptor);
    },

    /**
     * Delete a note
     *
     * @param {integer} noteId the id of note
     * @param {Control} control the control that is used to create the note
     * @returns void
     */
    deleteNote: function(noteId, control) {
        var urlPart, requestDescriptor;
        var data = {};

        // build request descriptor and call API
        urlPart = this.apiAccessor.getMappedRessource('notes') + '/' + noteId;
        requestDescriptor = this.buildNoteRequestDescriptor(urlPart, data, 'DELETE', [
                control.channel, control.widget.channel ]);
        this.doApiRequest(requestDescriptor);
    },

    /**
     * Helper-method that builds the requestDescriptor for note manipulation actions (create, edit,
     * answer, delete). The success handler is overridden (the default handler is not used). This
     * handler fires the events "success" and "noteServiceSuccessEvent".
     *
     * @param {String} urlPart the url part
     * @param {Object} data the data to send
     * @param {string} httpMethod the HTTP method to use for the request
     * @param {string} eventChannel the channel to fire the events on
     * @returns {object} the built request descriptor object
     */
    buildNoteRequestDescriptor: function(urlPart, data, httpMethod, eventChannel) {

        var requestDescriptor = {

            urlPart: urlPart,
            data: data,
            httpMethod: httpMethod,
            eventChannel: eventChannel,
            bind: this,

            successHandler: function(result, message, channels) {

                var type = null;
                if(result.origResult.status == "WARNING"){
                    type = "warn";
                }

                communote.widget.EventController.fireEvent('success', channels, {
                    message: message,
                    type: type
                });

                communote.widget.EventController.fireEvent('noteServiceSuccessEvent', channels, {},
                        this);
            }
        };

        return requestDescriptor;
    },

    /**
     * Add filter parameters of the given control to the given data object
     *
     * @param {Control} control the control
     * @param {Object} data the data object to be transferred via AJAX
     */
    addFilterParameters: function(control, data) {
        var jQuery, filterParams, paramName, finalParamName, value;
        var paramNamesToIgnore;
        // check if the control wants the parameters to be included
        if (control.includeFilterParameters()) {
            jQuery = communote.jQuery;
            filterParams = control.widget.getCurrentFilterParameters();
            paramNamesToIgnore = control.getFilterParametersToIgnore();

            for (paramName in filterParams) {
                // do not add parameters that should be ignored
                if ((paramNamesToIgnore == null)
                        || (jQuery.inArray(paramName, paramNamesToIgnore) == -1)) {
                    value = this.flattenFilterArray(paramName, filterParams[paramName]);
                    // add filter data to request descriptor if not null
                    if (value != null) {
                        // translate filter parameter
                        finalParamName = this.apiAccessor.getFilterParameterName(paramName);
                        data[finalParamName] = value;
                    }
                }
            }
        }
    },

    /**
     * Flattens an array of filter parameter values to a string.
     *
     * @param {String} paramName The name of the parameter
     * @param {Array|Number|String} filterValue The value of a filter parameter to process
     * @returns {Number|String} a string representation of the array or null if the array
     *      is empty. If filterValue is not an array it is returned unchanged.
     */
    flattenFilterArray: function(paramName, filterValue) {
        var separator;
        if (!communote.jQuery.isArray(filterValue)) {
            return filterValue;
        }
        if (filterValue.length == 0) {
            return null;
        }
        separator = this.apiAccessor.getFilterParameterValueSeparator(paramName);
        return filterValue.join(separator);
    },

    /**
     * return a list of topic data objects
     *
     * @param {string} aliases; may be undefined
     * @param {string} type, like 'WRITE', 'READ', 'MOST_USED'
     * @param {integer} maxCount The upper limit of topics to return
     * @param {function} successHandler
     * @param {object} bind
     * @param {boolean} async Asynchronous flag (true = async, false = sync)
     */
    getTopics: function(aliases, type, maxCount, successHandler, bind, async) {

        var requestDescriptor = {
            urlPart: this.apiAccessor.getMappedRessource('writableTopics'),
            data: {
                offset: 0,
                maxCount: maxCount,
                topicListType: type
            },
            successHandler: successHandler,
            bind: bind
        };

        if (aliases) {
            requestDescriptor.data.f_topicAliases = aliases;
        }
        if (async !== undefined) {
            requestDescriptor.async = async;
        }

        this.doApiRequest(requestDescriptor);
    },

    /**
     * Search for writable topics via their title
     *
     * @param {string} titleSearchString The string to find topics
     * @param {integer} maxCount Maximum number of topics to find
     * @param {string} [topicAliases] Comma separated list of topic aliases to filter the result
     * @param {Function} [successHandler] The handler to call when the query succeeded
     * @param {Object} [bind] The this-bind in the successHandler
     * @param {boolean} [autocompleter] if the request is from autocompleters to cancel previous autocompleter request
     */
    searchWritableTopics: function(titleSearchString, maxCount, topicAliases, successHandler, bind, autocompleter) {
        var runOnceId, requestDescriptor;

        runOnceId = autocompleter?this.autocompleteId:'ApiController#writableTopics';
        requestDescriptor = {
            urlPart: this.apiAccessor.getMappedRessource('writableTopics'),
            data: {
                offset: 0,
                maxCount: maxCount,
                topicListType: "WRITE",
                f_titleSearchString: titleSearchString
            },
            successHandler: successHandler,
            runOnceId: runOnceId,
            bind: bind
        };

        if (topicAliases) {
            requestDescriptor.data.f_topicAliases = topicAliases;
        }

        this.doApiRequest(requestDescriptor);
    },

    /**
     * builds the URL to the user-image of the specified user.
     *
     * @param {user} the user
     * @param {size} SMALL|MEDIUM|LARGE|undefined
     * @return {String} the URL
     */
    getUserImageUrl: function(user, size) {
        var imageUrl;
        size = size || 'MEDIUM';
        // TODO this is ugly as it introduces a dependency to sharepoint
        if (this.configuration.useSharePointProfilePictures) {
            imageUrl = window.location.protocol + '//' + window.location.host
                    + '/_vti_bin/communardo.communote/communotemanagement.svc/'
                    + 'GetImage'
                    + '?imageSize=' + size + '&alias=' + user.alias;
        } else {
            imageUrl = this.apiAccessor.buildUserImageUrl(user.userId, size);
        }
        return imageUrl;
    },
    /**
     * builds the URL for the attachment with the specified id.
     *
     * @param {string} [id] the attachment id
     * @return {String} the URL
     */
    getFileUploadUrl: function(id){
        var data = {
                attachmentUploadSessionId:id
        };
        if (this.configuration.jsessionId && !this.configuration.usingProxy) {
            data.jsessionid=this.configuration.jsessionId;
        }
        this.apiAccessor.buildRequestUrl();
        var url = this.apiAccessor.buildDataBaseUrl() + this.apiAccessor.buildRequestUrl('attachments.html', data);
        
        return url;
    },

    /**
     * This method returns the list of suggestions for all tags starting with the given prefix
     * filtered by the optional given currentFilter.
     * 
     * @param {string} [tagPrefix] The prefix for the tags to be returned
     * @param {Function} [successHandler] The handler to call when the query succeeded
     * @param {Object} [bind] The this-bind in the successHandler
     * @param {boolean} [autocompleter] if the request is from autocompleters to cancel previous autocompleter request
     * @param {Object} [currentFilter] the used filters for the search request
     * @param {boolean} [preselected] if preselected Filters are used
     * @param {Boolean} [assignedTagsOnly] only assigned Tags are returned
     * @param {int} [count] the maximum count of returned elements
     */

    getTagSuggestionList: function(tagPrefix, successHandler, bind, autocompleter, currentFilter, preselected, assignedTagsOnly, count) {
        var requestDescriptor, maxCount, runOnceId;
        maxCount = count || 20; //21 is the Value from communote FE
        
        runOnceId = autocompleter?this.autocompleteId:'ApiController#TagSuggestionList';

        requestDescriptor = {
            urlPart: this.apiAccessor.getMappedRessource('tagSuggestion'),
            data: {
                f_tagPrefix: tagPrefix,
                maxCount: maxCount
            },
            successHandler: successHandler,
            runOnceId: runOnceId,
            bind: bind
        };
        if (currentFilter) {
            requestDescriptor.data = this.appendCurrentFilter(requestDescriptor.data,
                    currentFilter,preselected);
        }
        
        if (assignedTagsOnly) {
            requestDescriptor.data.assignedTagsOnly = assignedTagsOnly;
        }
        
        this.doApiRequest(requestDescriptor);
    },
    
    /**
     * This method returns the list of users for all users starting with the given prefix filtered
     * by the optional given currentFilter.
     * 
     * @param {string} [userPrefix] The prefix for the users to be returned
     * @param {Function} [successHandler] The handler to call when the query succeeded
     * @param {Object} [bind] The this-bind in the successHandler
     * @param {boolean} [autocompleter] if the request is from autocompleters to cancel previous autocompleter request
     * @param {Object} [currentFilter] the used filters for the search request
     * @param {boolean} [preselected] if preselected Filters are used
     * @param {int} [count] the maximum count of returned elements 
     */

    getTimelineUserList: function(userPrefix, successHandler, bind, autocompleter, currentFilter, preselected, count) {
        var requestDescriptor, maxCount, runOnceId;

        maxCount = count || 20; //20 is the Value from communote FE

        runOnceId = autocompleter?this.autocompleteId:'ApiController#TimelineUserList';
        
        requestDescriptor = {
            urlPart: this.apiAccessor.getMappedRessource('users'),
            data: {
                f_userSearchString: userPrefix,
                maxCount: maxCount
            },
            successHandler: successHandler,
            runOnceId: runOnceId,
            bind: bind
        };
        if (currentFilter) {
            requestDescriptor.data = this.appendCurrentFilter(requestDescriptor.data,
                    currentFilter,preselected);
        }
        this.doApiRequest(requestDescriptor);
    },
    
    /**
     * This method returns the given data-object extended by the given filter parameters.
     * 
     * @param {Object} [data] data object that is extended by the parameters
     * @param {Object} [currentFilter] the used filters for the search request
     * @param {boolean} [preselectedFilter] if preselectedFilters are used
     */

    appendCurrentFilter: function(data, currentFilter, preselectedFilter) {
        var i, filterString, presel;
        preselectedFilter= preselectedFilter== undefined?true:false;

        if(preselectedFilter){
            presel=this.controller.WidgetController.widgets[0].getCurrentFilterParameters();
        } else {
            presel = {};
        }
               
        if (currentFilter.topicIds || presel.topicIds) {
            filterString = '';
            
            if(presel.topicIds){
                for (i = 0; i < presel.topicIds.length; i++) {
                    if (i > 0) {
                        filterString = filterString + ',';
                    }
                    filterString = filterString + presel.topicIds[i];    
                }
            } else {            
                for (i = 0; i < currentFilter.topicIds.length; i++) {
                    if (i > 0) {
                        filterString = filterString + ',';
                    }
                    filterString = filterString + currentFilter.topicIds[i];
    
                }
                 
            }
            data.f_topicIds = filterString;
        }

        if (currentFilter.tagIds|| presel.tagIds) {
            filterString = '';
            if(presel.tagIds){
                for (i = 0; i < presel.tagIds.length; i++) {
                    if (i > 0) {
                        filterString = filterString + ',';
                    }
                    filterString = filterString + presel.tagIds[i];    
                }
            } else {
                for (i = 0; i < currentFilter.tagIds.length; i++) {
                    if (i > 0) {
                        filterString = filterString + ',';
                    }
                    filterString = filterString + currentFilter.tagIds[i];
                }
            }
            data.f_tagIds = filterString;
        }

        if (currentFilter.userIds || presel.userIds) {
            filterString = '';  
            if(presel.userIds){
                for (i = 0; i < presel.userIds.length; i++) {
                    if (filterString.length > 0) {
                        filterString = filterString + ',';
                    }
                    filterString = filterString + presel.userIds[i];    
                }
            } else {
                for (i = 0; i < currentFilter.userIds.length; i++) {
                    if (i > 0) {
                        filterString = filterString + ',';
                    }
                    filterString = filterString + currentFilter.userIds[i];
    
                }
            }
            data.f_userIds = filterString;
        }
        
        if (presel.userAliases) {
            filterString = '';
            for (i = 0; i < presel.userAliases.length; i++) {
                if (i > 0) {
                    filterString = filterString + ',';
                }
                filterString = filterString + presel.userAliases[i];

            }
            data.f_userAliases = filterString;
        }
        
        if (presel.topicAliases) {
            filterString = '';
            for (i = 0; i < presel.topicAliases.length; i++) {
                if (i > 0) {
                    filterString = filterString + ',';
                }
                filterString = filterString + presel.topicAliases[i];

            }
            data.f_topicAliases = filterString;
        }
        
        return data;
    },
    
    abortAutocompleteRequests: function(){
        this.apiAccessor.abortJsonRequests(this.autocompleteId);
    }

});

