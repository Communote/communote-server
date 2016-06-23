/**
 * <p>
 * Factory to ease the creation of frequently used Autocompleter types.
 * </p>
 * Note: A factory method that creates a new Autocompleter should have the following signature:
 * inputElement (the input element that should be enhanced by the Autocompleter), options (an object
 * with options which will be merged with the local default autocompleterOptions and then passed to
 * the Autocompleter constructor), postData (object with request parameters that should be passed to
 * the DataSource). These arguments can be followed by an arbitrary amount of additional arguments.
 * When conforming to this signature the factory function can be used by generic widgets like the
 * SearchBox.
 * 
 * @author Communote GmbH
 * @copyright Author
 */
// TODO move file to another location because it is not part of the actual autocompleter
var AutocompleterFactory = new Class({
    Implements: [ Options ],

    // default options for all autocompleter instances, can be extended and overridden
    // on initialization of the factory
    autocompleterOptions: {
        dataSourceConstructor: AutocompleterRequestDataSource
    },
    dataSourceOptions: {
        postData: {
            maxCount: 10,
            offset: 0
        }
    },
    inputFieldOptions: {},
    suggestionsOptions: {
        flipAround: true,
        selectFirst: true,
        showLoadingFeedback: true,
        width: 'inherit'
    },

    // options to control autocompleter creation
    options: {
        choiceSummaryCssClass: 'cn-choice-summary',
        loadingFeedbackHintKey: 'autosuggest.searching',
        restApiVersion: '3.0',
        summaryAtTop: true
    },
    restApiBaseUrl: null,

    initialize: function(defaultOptions, defaultAutocompleterOptions, defaultInputFieldOptions,
            defaultDataSourceOptions, defaultSuggestionsOptions) {
        var baseUrl;
        this.setOptions(defaultOptions);
        if (this.options.loadingFeedbackHintKey) {
            this.suggestionsOptions.loadingFeedbackHintText = getJSMessage(this.options.loadingFeedbackHintKey);
        }
        Object.merge(this.autocompleterOptions, defaultAutocompleterOptions);
        Object.merge(this.dataSourceOptions, defaultDataSourceOptions);
        Object.merge(this.inputFieldOptions, defaultInputFieldOptions);
        Object.merge(this.suggestionsOptions, defaultSuggestionsOptions);
        baseUrl = '/web/rest/' + this.options.restApiVersion;
        if (baseUrl.charAt(baseUrl.length - 1) !== '/') {
            baseUrl += '/';
        }
        this.restApiBaseUrl = baseUrl;
    },

    arrayWithContainedSummaryResultCallback: function(isUpdate, categoryId, response) {
        var metaData, i, suggestions;
        if (!response) {
            // fake an empty response
            response = [];
            response.push({
                resultsReturned: 0
            });
        }
        suggestions = [];
        // check if there is an entry with resultsReturned member and add it to the metaData
        for (i = 0; i < response.length; i++) {
            if (response[i].resultsReturned != undefined) {
                metaData = response[i];
            } else {
                suggestions.push(response[i]);
            }
        }
        this.fireEvent('queryComplete', [ isUpdate, categoryId, suggestions, metaData ]);
        return metaData ? metaData.resultsReturned > 0 : true;
    },

    /**
     * Generic injectChoiceContentCallback which injects the value of a configurable member of the
     * JSON object as suggestion content. The member to use is defined by the contentAttribute which
     * can be a member of the category or the suggestion options . The value of the member must be a
     * string. The query value will be highlighted in the choice. With an additional attribute named
     * inputValueAttribute it is possible to define the member of the JSON object that should be
     * returned as the input value, that is the value to insert when selecting a suggestion. In case
     * this option is not defined the value of the member defined by contentAttribute option will be
     * returned.
     * 
     * This method must be run in the context of an autocompleter instance.
     * 
     * @param {Object} category The category to inject into
     * @param {Element} elem The choice element into which the content will be injected
     * @param {Object} token The JSON object representing the choice that should be processed
     * @param {Object} [metaData] Is ignored
     * @returns {String} The string value to insert into the input field
     */
    choiceContentFromJsonAttributeCallback: function(category, elem, token, metaData) {
        var value, inputValueAttribute, inputValue;
        value = token[category.contentAttribute || this.options.contentAttribute];
        inputValueAttribute = category.inputValueAttribute || this.options.inputValueAttribute;
        if (inputValueAttribute) {
            inputValue = token[inputValueAttribute];
        } else {
            inputValue = value;
        }
        elem.set('html', this.markQueryValue(value));
        return inputValue;
    },
    /**
     * Similar to choiceContentFromJsonAttributeCallback but also inserts an image. The members to
     * use for the content text and the return value are also the contentAttribute and
     * inputValueAttribute category members/suggestion options. The member to use for the URL to the
     * image is defined by the imagePathAttribute category members/suggestion option. The values of
     * both members must be strings and that for the image must be a relative URL. If the token does
     * not have the image member but a noImageCssClass option exists in the options/category a SPAN
     * with that class will be rendered.
     * 
     * This method must be run in the context of a AutocompleterSuggestions instance.
     * 
     * @param {Object} category The category to inject into
     * @param {Element} elem The choice element into which the content will be injected
     * @param {Object} token The JSON object representing the choice that should be processed
     * @param {Object} [metaData] Is ignored
     * @returns {String} The string value to insert into the input field
     */
    choiceContentWithImageFromJsonAttributeCallback: function(category, elem, token, metaData) {
        var contentElem, imageElem, inputValueAttribute, inputValue, imagePathAttribute, imagePath;
        var noImageCssClass;
        var value = token[category.contentAttribute || this.options.contentAttribute];
        inputValueAttribute = category.inputValueAttribute || this.options.inputValueAttribute;
        if (inputValueAttribute) {
            inputValue = token[inputValueAttribute];
        } else {
            inputValue = value;
        }
        imagePathAttribute = category.imagePathAttribute || this.options.imagePathAttribute;
        if (imagePathAttribute) {
            imagePath = token[imagePathAttribute];
            if (imagePath) {
                imageElem = new Element('img', {
                    'src': buildRequestUrl(imagePath)
                });
            }
        }
        if (!imageElem) {
            noImageCssClass = category.noImageCssClass || this.options.noImageCssClass;
            if (noImageCssClass) {
                imageElem = new Element('span', {
                    'class': noImageCssClass
                });
            }
        }
        if (imageElem) {
            elem.grab(imageElem);
        }
        contentElem = new Element('span');
        contentElem.set('html', this.markQueryValue(value));
        elem.grab(contentElem);
        return inputValue;
    },

    createAtAtSuggestionToken: function(definition) {
        var atatValue = '@' + definition.inputValue;
        // fake a user object that looks like those returned by server requests, but also include
        // inputValue and suggestion for creating the suggestion entry
        return {
            inputValue: definition.inputValue,
            suggestion: definition.suggestion,
            alias: atatValue,
            longName: definition.suggestion
        };
    },

    /**
     * Created an autocompleter from the prepared options.
     * 
     * @param {Element} inputElem The input element that should be enhanced by the Autocompleter
     * @param {Object} autocompleterOptions Object with options that should be passed to the
     *            Autocompleter constructor. This object must not contain the fields
     *            dataSourceOptions, inputFieldOptions and suggestionsOptions because they are added
     *            in this method.
     * @param {AutocompleterDataSource|AutocompleterDataSource[]} dataSources DataSources to use to
     *            retrieve the suggestions
     * @param {Object} inputFieldOptions Options to be passed to the InputField constructor
     * @param {Object} suggestionsOptions Options to be passed to the Suggestions constructor
     * @return {Autocompleter} the created Autocompleter
     */
    createAutocompleter: function(inputElem, autocompleterOptions, dataSources, inputFieldOptions,
            suggestionsOptions) {
        autocompleterOptions.dataSources = dataSources;
        autocompleterOptions.inputFieldOptions = inputFieldOptions;
        autocompleterOptions.suggestionsOptions = suggestionsOptions;
        return new Autocompleter(inputElem, autocompleterOptions);
    },

    /**
     * Create a generic autocompleter that fetches suggestion via AJAX.
     * 
     * @param {Element} inputElem The input element that should be enhanced by the Autocompleter
     * @param {Object} options An object which can contain an autocompleterOptions object, an
     *            inputFieldOptions Object, a dataSourceOptions object and a suggestionsOptions
     *            object which will be merged with the defaults provided by this instance before
     *            passing them to the {@link Autocompleter}, DataSource, Suggestions and InputField
     *            constructors.
     * @param {Object} postData An object with request parameters as key-value pairs that should be
     *            passed to the {@link DataSource} of the Autocompleter.
     * @param {String} url The URL to use when searching for suggestions
     * @param {String} queryParam The name of the request parameter that should be set to the
     *            current search term for retrieving suggestions
     * @return {Autocompleter} the created Autocompleter
     */
    createGenericAutocompleter: function(inputElem, options, postData, url, queryParam) {
        var initializedOptions = this.initOptionsFromDefaults(options);
        initializedOptions.dataSources.push(this.createRequestDataSource(url, queryParam, postData,
                initializedOptions.suggestionsOptions.categories, options
                        && options.dataSourceOptions));
        return this.createAutocompleter(inputElem, initializedOptions.autocompleterOptions,
                initializedOptions.dataSources, initializedOptions.inputFieldOptions,
                initializedOptions.suggestionsOptions);
    },

    createGenericSummary: function(category, choicesWrapper, queryValue, tokens, metaData) {
        var message, resultsReturned;
        if (metaData && !category.summaryDisabled) {
            resultsReturned = metaData.resultsReturned;
            if (resultsReturned == 0) {
                message = getJSMessage('autosuggest.found.nothing', [ queryValue ]);
            } else if (metaData.moreResults) {
                message = getJSMessage('autosuggest.found.more', [ resultsReturned, queryValue ]);
            } else {
                message = getJSMessage('autosuggest.found.all', [ resultsReturned, queryValue ]);
            }
            this.injectSummaryElement(choicesWrapper, message, false, this.options.summaryAtTop);
        }
    },

    /**
     * Create an autocompleter that creates categorized suggestions for the Communote entities which
     * are topics, users and tags.
     * 
     * @param {Element} inputElem The input element that should be enhanced by the Autocompleter
     * @param {Object} [options] Options object that can contain options for the Autocompleter
     *            instance (autocompleterOptions member), options to be passed to the InputField
     *            instance (inputFieldOptions), options to be passed to the Suggestions instance
     *            (suggestionsOptions member) and DataSource options for each entity (contained in
     *            members userDataSourceOptions, tagDataSourceOptions and topicDataSourceOptions).
     *            Finally a tagCategories member can be defined to provide an array of tag
     *            categories.
     * @param {Object} postData An object which can contain several objects with request parameters
     *            as key-value pairs to be passed to the entity {@link DataSource} instances. It is
     *            possible to provide common DataSource post data for all types in a member named
     *            common and individual post data in members named topic, user and tag.
     */
    createEntityAutocompleter: function(inputElem, options, postData) {
        var preparedOptions = this.prepareEntityAutocompleterOptions(options, postData);
        return this.createAutocompleter(inputElem, preparedOptions.autocompleterOptions,
                preparedOptions.dataSources, preparedOptions.inputFieldOptions,
                preparedOptions.suggestionsOptions);
    },

    /**
     * Create an autocompleter that searches for mentions to be added when creating a note. The
     * mentions include other users and some predefined terms that start with @@ like
     * @@all.
     * 
     * @param {Element} inputElem The element the autocompleter should be attached to
     * @param {Object} [options] Options object that can contain Autocompleter, InputField,
     *            Suggestions and two different DataSource options to override defaults. The
     *            DataSource option members are requestDataSourceOptions to override options of the
     * request DataSource and staticDataSourceOptions to override settings of the @@ suggestions
     *            providing DataSource.
     * @param {Object} [postData] additional key value pairs that should be sent as request
     *            parameters when searching for user suggestions
     * @param {Boolean} showImage If true the user image will be rendered for each found user.
     * @return {Autocompleter} the created autocompleter instance
     */
    createMentionAutocompleter: function(inputElem, options, postData, showImage) {
        var preparedOptions = this.prepareMentionAutocompleterOptions(options, postData, showImage);
        return this.createAutocompleter(inputElem, preparedOptions.autocompleterOptions,
                preparedOptions.dataSources, preparedOptions.inputFieldOptions,
                preparedOptions.suggestionsOptions);
    },

    /**
     * Function that creates the autocompleter summary for the @ mentions suggestions.
     */
    createMentionsQuerySummary: function(category, choicesWrapper, queryValue, tokens, metaData) {
        if (metaData) {
            if (category.id == 'userSuggestions') {
                // no summary if no results were returned and the query started with @ (for @@ mentions)
                if (metaData.resultsReturned > 0 || metaData.resultsReturned == 0
                        && queryValue.length && queryValue.charAt(0) != '@') {
                    this.createGenericSummary(category, choicesWrapper, queryValue, tokens,
                            metaData);
                }
            } else {
                // summary only when the query started with an @ and no results were found
                if (metaData.resultsReturned == 0 && queryValue.charAt(0) == '@') {
                    if (queryValue.charAt(1) != '@') {
                        queryValue = '@' + queryValue;
                    }
                    this.createGenericSummary(category, choicesWrapper, queryValue, tokens,
                            metaData);
                }
            }
        }
    },

    createStaticSuggestionDefinition: function(msgKey, inputValue, matchEmtpyString) {
        var additionalMatchesString, additionalMatches, i, term;
        var def = {};
        var i18n = communote.i18n;
        def.matchEmptyString = matchEmtpyString;
        def.inputValue = inputValue;
        def.suggestion = i18n.getMessage(msgKey);
        additionalMatchesString = i18n.getMessage(msgKey + '.terms', null, '');
        if (additionalMatchesString != '') {
            def.additionalMatches = [];
            additionalMatches = additionalMatchesString.split(',');
            for (i = 0; i < additionalMatches.length; i++) {
                term = additionalMatches[i].trim();
                if (term) {
                    def.additionalMatches.push(term);
                }
            }
        }
        return def;
    },

    /**
     * Create a tag Autocompleter that uses the REST API, more precisely the tagSuggestionLists
     * resource, to retrieve tag suggestions.
     * 
     * @param {Element} inputElem The input element that should be enhanced by the Autocompleter
     * @param {Object} [options] An object which can contain an autocompleterOptions object, an
     *            inputFieldOptions Object, a dataSourceOptions object and a suggestionsOptions
     *            object which will be merged with the defaults provided by this instance before
     *            passing them to the {@link Autocompleter}, DataSource, Suggestions and InputField
     *            constructors.
     * @param {Object} postData An object with request parameters as key-value pairs that should be
     *            passed to the {@link DataSource} of the Autocompleter.
     * @param {String} type Defines the type of the tag store to look for tag suggestions. Supported
     *            values are 'ENTITY' (user tags), 'BLOG' (topic tags) and 'NOTE' (note tags).
     * @param {Boolean} assignedTagsOnly If true only suggestions of tags that are already assigned
     *            to Communote entities (notes, topics or users as defined by the type parameter)
     *            have to be be returned. If false, the tags don't have to be assigned to Communote
     *            entities.
     * @param {Boolean} multiple Convenience argument to create an Autocompleter in multiple mode.
     * @return {Autocompleter} the created Autocompleter
     */
    createTagAutocompleter: function(inputElem, options, postData, type, assignedTagsOnly, multiple) {
        var preparedOptions = this.prepareTagAutocompleterOptions(options, postData, type,
                assignedTagsOnly, multiple);
        return this.createAutocompleter(inputElem, preparedOptions.autocompleterOptions,
                preparedOptions.dataSources, preparedOptions.inputFieldOptions,
                preparedOptions.suggestionsOptions);
    },

    /**
     * Create an Autocompleter that searches for blog/topic suggestions.
     * 
     * @param {Element} inputElem The input element that should be enhanced by the Autocompleter
     * @param {Object} options An object which can contain an autocompleterOptions object, an
     *            inputFieldOptions Object, a dataSourceOptions object and a suggestionsOptions
     *            object which will be merged with the defaults provided by this instance before
     *            passing them to the {@link Autocompleter}, DataSource, Suggestions and InputField
     *            constructors.
     * @param {Object} postData An object with request parameters as key-value pairs that should be
     *            passed to the {@link DataSource} of the Autocompleter.
     * @param {boolean} timeline Whether to get the timeline topics or just any topic
     * @param {String} accessLevel Restricts the suggested topics to a certain access level.
     *            Supported values are 'read', 'write' and 'manager', where the first will fetch all
     *            matching topics the current user is allowed to read. The second will only return
     *            those the current user is allowed to write to and the last only those the user can
     *            manage.
     * @return {Autocompleter} the created Autocompleter
     */
    createTopicAutocompleter: function(inputElem, options, postData, timeline, accessLevel) {
        var suggestionsOptions;
        var preparedOptions = this.initOptionsFromDefaults(options);
        suggestionsOptions = preparedOptions.suggestionsOptions;
        // add defaults if not provided via options
        if (!suggestionsOptions.injectChoiceContentCallback) {
            suggestionsOptions.injectChoiceContentCallback = this.choiceContentWithImageFromJsonAttributeCallback;
        }
        if (!suggestionsOptions.createCategorySummaryCallback) {
            suggestionsOptions.createCategorySummaryCallback = this.createGenericSummary.bind(this);
        }
        suggestionsOptions.categories = [ {} ];
        preparedOptions.dataSources.push(this.prepareTopicCategoryAndDataSource(
                suggestionsOptions.categories[0], options && options.dataSourceOptions, postData,
                timeline, accessLevel));
        return this.createAutocompleter(inputElem, preparedOptions.autocompleterOptions,
                preparedOptions.dataSources, preparedOptions.inputFieldOptions, suggestionsOptions);
    },

    /**
     * Create an autocompleter that searches for users and optionally also for groups.
     * 
     * @param {Element} inputElem The element the autocompleter should be attached to
     * @param {Object} [options] An object which can contain an autocompleterOptions object, an
     *            inputFieldOptions Object, a dataSourceOptions object and a suggestionsOptions
     *            object which will be merged with the defaults provided by this instance before
     *            passing them to the {@link Autocompleter}, DataSource, Suggestions and InputField
     *            constructors.
     * @param {Object} [postData] additional key value pairs that should be sent as request
     *            parameters when searching for suggestions
     * @param {String} type Defines what should be searched. Supported values are 'USER', 'ENTITY',
     *            'AUTHOR', 'MANAGER' and 'READER'. When using the first type only users will be
     *            returned. The users do not have to be authors of notes. The second type is similar
     *            to 'USER' but will also include matching groups. When setting the type to 'AUTHOR'
     *            only authors of notes of blogs the current user can read will be retrieved. The
     *            'READER' type will return all users with read access to the currently selected
     *            blog. 'MANAGER' means, only topic managers will be received.
     * @param {Boolean} showImage If true images will be rendered before each found entity. For
     *            groups a default image will be used.
     * @return {Autocompleter} the created autocompleter instance
     */
    createUserAutocompleter: function(inputElem, options, postData, type, showImage) {
        var preparedOptions = this.prepareUserAutocompleterOptions(options, postData, type,
                showImage);
        return this.createAutocompleter(inputElem, preparedOptions.autocompleterOptions,
                preparedOptions.dataSources, preparedOptions.inputFieldOptions,
                preparedOptions.suggestionsOptions);
    },

    /**
     * Get the default (built-in) tag suggestion category for the given type.
     * 
     * @param {String} type Defines the type of the tag store to look for tag suggestions. Supported
     *            values are 'ENTITY' (user tags), 'BLOG' (topic tags) and 'NOTE' (note tags).
     */
    getDefaultTagSuggestionCategory: function(type) {
        var defaultStoreName = 'Default' + type.charAt(0) + type.substr(1).toLowerCase()
                + 'TagStore';
        return {
            id: defaultStoreName,
            provider: defaultStoreName,
            title: 'Communote',
            contentAttribute: 'name',
            cssClass: 'autocompleter-category-tags autocompleter-category-' + defaultStoreName
        };
    },

    initOptionsFromDefaults: function(options) {
        var result = {};
        var merge = Object.merge;
        result.dataSources = [];
        result.autocompleterOptions = merge({}, this.autocompleterOptions);
        result.inputFieldOptions = merge({}, this.inputFieldOptions);
        result.suggestionsOptions = merge({}, this.suggestionsOptions);
        if (options) {
            merge(result.autocompleterOptions, options.autocompleterOptions);
            merge(result.inputFieldOptions, options.inputFieldOptions);
            merge(result.suggestionsOptions, options.suggestionsOptions);
        }
        return result;
    },

    /**
     * Injects a found @@ suggestion and takes care of correct highlighting within the suggestion.
     * 
     * Note: is run in AutocompleterSuggestions instance context
     */
    injectAtAtMentionSuggestion: function(category, elem, token, metaData) {
        var value = token.suggestion;
        var inputValue = token.inputValue;
        var queryValue = this.queryValue;
        if (queryValue.length) {
            // if the inputValue starts with the queryValue, prepend another @ to highlight
            // this alias in the suggestion string. Otherwise remove the leading @ to highlight
            // other search terms.
            if (inputValue.indexOf(queryValue) == 0) {
                queryValue = '@' + queryValue;
            } else if (queryValue.charAt(0) == '@' && queryValue.charAt(1) != '@') {
                queryValue = queryValue.substring(1);
            }
        }
        elem.set('html', this.markQueryValue(value, queryValue));
        return inputValue;
    },

    /**
     * Inject the query value suggestion.
     * 
     * Note: is run in AutocompleterSuggestions instance context
     */
    injectQueryValueSuggestion: function(category, elem, token, metaData) {
        var noImageCssClass, imageElem, contentElem;
        var markedQueryValue = this.markQueryValue(token.suggestion);
        if (category.wrapSuggestionMessageKey) {
            markedQueryValue = getJSMessage(category.wrapSuggestionMessageKey, [ markedQueryValue ]);
        }
        noImageCssClass = category.noImageCssClass || this.options.noImageCssClass;
        if (noImageCssClass) {
            imageElem = new Element('span', {
                'class': noImageCssClass
            });
            elem.grab(imageElem);
        }
        contentElem = new Element('span');
        contentElem.set('html', markedQueryValue);
        elem.grab(contentElem);
        return token.inputValue;
    },

    injectSummaryElement: function(choicesWrapper, message, isHtml, top) {
        var where = top ? 'top' : 'bottom';
        var elem = new Element('li', {
            'class': this.options.choiceSummaryCssClass
        });
        if (isHtml) {
            elem.set('html', message);
        } else {
            elem.set('text', message);
        }
        choicesWrapper.grab(elem, where);
        return elem;
    },

    prepareAtAtSuggestionsDataSource: function(category, providedDataSourceOptions) {
        var dataSourceOptions = {};
        var definitions = [];
        definitions.push(this.createStaticSuggestionDefinition('autosuggest.atat.discussion',
                '@discussion', true));
        definitions.push(this.createStaticSuggestionDefinition('autosuggest.atat.authors',
                '@authors', true));
        definitions.push(this
                .createStaticSuggestionDefinition('autosuggest.atat.all', '@all', true));
        definitions.push(this.createStaticSuggestionDefinition('autosuggest.atat.managers',
                '@managers', true));
        category.id = 'staticSuggestions';
        category.injectChoiceContentCallback = this.injectAtAtMentionSuggestion;
        dataSourceOptions.prefilterSearchCallback = this.stripLeadingAtCharacter;
        dataSourceOptions.createSuggestionTokenCallback = this.createAtAtSuggestionToken;
        Object.merge(dataSourceOptions, providedDataSourceOptions);
        return new AutocompleterStaticDataSource(definitions, category.id, dataSourceOptions);
    },

    prepareEntityAutocompleterOptions: function(options, postData) {
        var preparedOptions, suggestionsOptions, commonDataSourceOptions, commonPostData, i;
        var dataSources, category, finalDataSourceOptions, finalPostData, tagCategories;
        if (!options) {
            options = {};
        }
        // extract defaults
        preparedOptions = this.initOptionsFromDefaults(null);
        dataSources = preparedOptions.dataSources;
        suggestionsOptions = preparedOptions.suggestionsOptions;
        commonDataSourceOptions = options.dataSourceOptions;
        if (!postData) {
            postData = {};
        }
        commonPostData = Object.merge({}, postData.common);
        suggestionsOptions.categories = [];
        if (!options.searchValueSuggestionDisabled) {
            finalDataSourceOptions = Object.merge({}, options.searchDataSourceOptions);
            suggestionsOptions.categories.push({});
            dataSources.push(this.prepareQueryValueCategoryAndDataSource(
                    suggestionsOptions.categories[0], finalDataSourceOptions));
            // disable delay in InputField but enable it for the request DataSources to get direct
            // feedback of the typed term in the search suggestion
            preparedOptions.inputFieldOptions.delay = false;
            commonDataSourceOptions.delay = 400;
        }
        // prepare topic category and add DataSource
        if (!options.topicSuggestionDisabled) {
            // apply provided custom options for the topic DataSource
            finalDataSourceOptions = Object.merge({}, commonDataSourceOptions,
                    options.topicDataSourceOptions);
            finalPostData = Object.merge({}, commonPostData, postData.topic);
            category = {};
            category.title = getJSMessage('autosuggest.title.topics');
            suggestionsOptions.categories.push(category);
            dataSources.push(this.prepareTopicCategoryAndDataSource(category,
                    finalDataSourceOptions, finalPostData, options.topicSuggestionTimeline,
                    'VIEWER'));
        }
        // prepare user category
        if (!options.userSuggestionDisabled) {
            finalDataSourceOptions = Object.merge({}, commonDataSourceOptions,
                    options.userDataSourceOptions);
            finalPostData = Object.merge({}, commonPostData, postData.user);
            category = {};
            category.title = getJSMessage('autosuggest.title.users');
            suggestionsOptions.categories.push(category);
            dataSources.push(this.prepareUserCategoryAndDataSource(category,
                    finalDataSourceOptions, finalPostData, options.userSuggestionType, true));
        }
        // add categories and TagDataSource
        if (!options.tagSuggestionDisabled) {
            finalDataSourceOptions = Object.merge({}, commonDataSourceOptions,
                    options.tagDataSourceOptions);
            finalPostData = Object.merge({}, commonPostData, postData.tag);
            tagCategories = options.tagCategories || [];
            dataSources.push(this.prepareTagCategoriesAndDataSource(tagCategories,
                    finalDataSourceOptions, finalPostData, options.tagSuggestionType, true));
            suggestionsOptions.categories.append(tagCategories);
        }

        suggestionsOptions.showCategoryTitle = true;
        suggestionsOptions.createCategorySummaryCallback = this.createGenericSummary.bind(this);
        suggestionsOptions.injectChoiceContentCallback = this.choiceContentWithImageFromJsonAttributeCallback;
        suggestionsOptions.noImageCssClass = 'cn-icon';

        // merge remaining options
        Object.merge(preparedOptions.autocompleterOptions, options.autocompleterOptions);
        Object.merge(preparedOptions.suggestionsOptions, options.suggestionsOptions);
        Object.merge(preparedOptions.inputFieldOptions, options.inputFieldOptions);
        return preparedOptions;
    },

    /**
     * Prepare the options object that contains autocompleter options and dataSourceOptions,
     * suggestionsOptions and inputFieldOptions objects to pass to the autocompleter constructor to
     * create an autocompleter for mention suggestions.
     * 
     * @param {Element} inputElem The element the autocompleter should be attached to
     * @param {Object} [options] Options object that can contain Autocompleter, InputField,
     *            Suggestions and two different DataSource options to override defaults. The
     *            DataSource option members are dataSourceOptions to override options of the request
     * DataSource and staticDataSourceOptions to override settings of the @@ suggestions providing
     *            DataSource.
     * @param {Object} [postData] additional key value pairs that should be sent as request
     *            parameters when searching for user suggestions
     * @param {Boolean} showImage If true the user image will be rendered for each found user.
     * @return {Object} the prepared options
     */
    prepareMentionAutocompleterOptions: function(options, postData, showImage) {
        var preparedOptions = this.initOptionsFromDefaults(null);
        var suggestionsOptions = preparedOptions.suggestionsOptions;
        if (!options) {
            options = {};
        }
        suggestionsOptions.categories = [ {}, {} ];
        // add @@ static DataSource
        preparedOptions.dataSources.push(this.prepareAtAtSuggestionsDataSource(
                suggestionsOptions.categories[0], options.staticDataSourceOptions));
        // add reader request DataSource
        preparedOptions.dataSources.push(this.prepareUserCategoryAndDataSource(
                suggestionsOptions.categories[1], options.dataSourceOptions, postData, 'READER',
                showImage));

        suggestionsOptions.showCategoryTitle = false;
        suggestionsOptions.minLength = 0;
        // mentions query is a prefix query matching against several words (first, last name and
        // alias) so we highlight the start of the words
        suggestionsOptions.markQueryWordStart = true;
        suggestionsOptions.markQueryWordStartPattern = '^|[\\s\[\(]@*?';
        suggestionsOptions.createCategorySummaryCallback = this.createMentionsQuerySummary
                .bind(this);
        if (showImage) {
            suggestionsOptions.injectChoiceContentCallback = this.choiceContentWithImageFromJsonAttributeCallback;
        } else {
            suggestionsOptions.injectChoiceContentCallback = this.choiceContentFromJsonAttributeCallback;
        }
        // merge remaining options, excluding already used options
        Object.merge(preparedOptions.autocompleterOptions, options.autocompleterOptions);
        Object.merge(suggestionsOptions, options.suggestionsOptions);
        Object.merge(preparedOptions.inputFieldOptions, options.inputFieldOptions);
        return preparedOptions;
    },

    prepareQueryValueCategoryAndDataSource: function(category, dataSourceOptions) {
        category.id = 'queryValueSuggestion';
        category.cssClass = 'autocompleter-category-search';
        category.injectChoiceContentCallback = this.injectQueryValueSuggestion;
        category.wrapSuggestionMessageKey = 'autosuggest.search.suggestion';
        // disable title
        category.title = false;
        return new AutocompleterQueryEchoingDataSource(category.id, dataSourceOptions);
    },

    createRequestDataSource: function(url, queryParam, postData, categories,
            providedDataSourceOptions) {
        // init from defaults
        var dataSourceOptions = Object.merge({}, this.dataSourceOptions);
        dataSourceOptions.queryParam = queryParam;
        if (!dataSourceOptions.postData) {
            dataSourceOptions.postData = {};
        }
        Object.merge(dataSourceOptions.postData, postData);
        Object.merge(dataSourceOptions, providedDataSourceOptions);
        return new AutocompleterRequestDataSource(buildRequestUrl(url), categories, dataSourceOptions);
    },

    prepareTopicCategoryAndDataSource: function(category, providedDataSourceOptions, postData,
            timeline, accessLevel) {
        var url = timeline ? '/blog/control/findTimelineTopics.do' : '/blog/control/findBlogs.do';
        category.id = 'topicSuggestions';
        category.contentAttribute = 'title';
        category.imagePathAttribute = 'imagePath';
        category.cssClass = 'autocompleter-category-topics';
        if (!providedDataSourceOptions) {
            providedDataSourceOptions = {};
        }
        providedDataSourceOptions.requestCompleteCallback = this.arrayWithContainedSummaryResultCallback;
        if (accessLevel) {
            if (!postData) {
                postData = {};
            }
            postData.blogAccess = accessLevel;
        }
        return this.createRequestDataSource(url, 'pattern', postData, category,
                providedDataSourceOptions);
    },

    /**
     * Prepare the options object that contains autocompleter Options, suggestionsOptions,
     * inputFieldOptions Objects and the dataSources array with TagSuggestionDataSource instance.
     * This options object can be passed to the autocompleter constructor to create an autocompleter
     * for tag suggestions.
     * 
     * @param {Object} [options] Options object that can contain Autocompleter, InputField,
     *            Suggestions and DataSource options to override defaults
     * @param {Object} postData An object with request parameters as key-value pairs that should be
     *            passed to the {@link DataSource} of the Autocompleter.
     * @param {String} type Defines the type of the tag store to look for tag suggestions. Supported
     *            values are 'ENTITY' (user tags), 'BLOG' (topic tags) and 'NOTE' (note tags).
     * @param {Boolean} assignedTagsOnly If true only suggestions of tags that are already assigned
     *            to Communote entities (notes, topics or users as defined by the type parameter)
     *            have to be be returned. If false, the tags don't have to be assigned to Communote
     *            entities.
     * @param {Boolean} multiple Convenience argument to create an Autocompleter in multiple mode.
     * @return {Object} the prepared options
     */
    prepareTagAutocompleterOptions: function(options, postData, type, assignedTagsOnly, multiple) {
        var suggestionsOptions, categories;
        var preparedOptions = this.initOptionsFromDefaults(options);
        preparedOptions.inputFieldOptions.multiple = !!multiple;
        suggestionsOptions = preparedOptions.suggestionsOptions;
        if (!suggestionsOptions.injectChoiceContentCallback) {
            suggestionsOptions.injectChoiceContentCallback = this.choiceContentFromJsonAttributeCallback;
        }
        if (!suggestionsOptions.createCategorySummaryCallback) {
            suggestionsOptions.createCategorySummaryCallback = this.createGenericSummary.bind(this);
        }
        if (!suggestionsOptions.categories) {
            suggestionsOptions.categories = [];
        }
        preparedOptions.dataSources.push(this.prepareTagCategoriesAndDataSource(
                suggestionsOptions.categories, options && options.dataSourceOptions, postData,
                type, assignedTagsOnly));
        return preparedOptions;
    },

    /**
     * Prepare the categories and create the DataSource for the tag suggestions of the given type.
     * 
     * @param {Object[]} categories Array of categories to use end extend with some additional
     *            options. If empty the default category will be used and returned.
     * @param {Object} providedDataSourceOptions Object with options to be passed to the
     *            TagSuggestionDataSource constructor which is used to override default settings
     * @param {Object} postData An object with request parameters as key-value pairs that should be
     *            passed to the {@link DataSource}. This argument is an alternative to provide the
     *            postData as part of the DataSource options.
     * @param {String} type Defines the type of the tag store to look for tag suggestions. Supported
     *            values are 'ENTITY' (user tags), 'BLOG' (topic tags) and 'NOTE' (note tags).
     * @param {Boolean} assignedTagsOnly If true only suggestions of tags that are already assigned
     *            to Communote entities (notes, topics or users as defined by the type parameter)
     *            have to be be returned. If false, the tags don't have to be assigned to Communote
     *            entities.
     * @return {TagSuggestionDataSource} The created DataSource to retrieve the TagSuggestions
     */
    prepareTagCategoriesAndDataSource: function(categories, providedDataSourceOptions, postData,
            type, assignedTagsOnly) {
        var i, category, dataSourceOptions;
        // in case categories are missing create default that uses the default tagstore of the type
        if (categories.length == 0) {
            categories.push(this.getDefaultTagSuggestionCategory(type));
        } else {
            for (i = 0; i < categories.length; i++) {
                category = categories[i];
                category.contentAttribute = 'name';
                category.cssClass = 'autocompleter-category-tags autocompleter-category-'
                        + categories[i].id;
            }
        }
        dataSourceOptions = Object.merge({}, this.dataSourceOptions);
        if (!dataSourceOptions.postData) {
            dataSourceOptions.postData = {};
        }
        dataSourceOptions.postData.tagStoreType = type;
        dataSourceOptions.postData.nameProvider = 'WIDGET';
        dataSourceOptions.postData.assignedTagsOnly = (assignedTagsOnly === true);
        Object.merge(dataSourceOptions.postData, postData);
        Object.merge(dataSourceOptions, providedDataSourceOptions);
        return new TagSuggestionDataSource(buildRequestUrl(this.restApiBaseUrl
                + 'tagSuggestionLists'), categories, dataSourceOptions);
    },

    /**
     * Prepare the options object that contains autocompleter options and dataSourceOptions,
     * suggestionsOptions and inputFieldOptions objects to pass to the autocompleter constructor to
     * create an autocompleter for user suggestions.
     * 
     * @param {Object} [options] Options object that can contain Autocompleter, InputField,
     *            Suggestions and DataSource options to override defaults
     * @param {Object} [postData] additional key value pairs that should be sent as request
     *            parameters when searching for suggestions
     * @param {String} type Defines what should be searched. Supported values are 'USER', 'ENTITY',
     *            'AUTHOR', 'MANAGER' and 'READER'. When using the first type only users will be
     *            returned. The users do not have to be authors of notes. The second type is similar
     *            to 'USER' but will also include matching groups. When setting the type to 'AUTHOR'
     *            only authors of notes of blogs the current user can read will be retrieved. The
     *            'READER' type will return all users with read access to the currently selected
     *            blog. 'MANAGER' means, only topic managers will be received.
     * @param {Boolean} showImage If true images will be rendered before each found entity. For
     *            groups a default image will be used.
     * @return {Object} the prepared options
     */
    prepareUserAutocompleterOptions: function(options, postData, type, showImage) {
        var preparedOptions = this.initOptionsFromDefaults(options);
        var suggestionsOptions = preparedOptions.suggestionsOptions;
        suggestionsOptions.categories = [ {} ];
        preparedOptions.dataSources.push(this.prepareUserCategoryAndDataSource(
                suggestionsOptions.categories[0], options && options.dataSourceOptions, postData,
                type, showImage));
        if (!suggestionsOptions.injectChoiceContentCallback) {
            if (showImage) {
                suggestionsOptions.injectChoiceContentCallback = this.choiceContentWithImageFromJsonAttributeCallback;
            } else {
                suggestionsOptions.injectChoiceContentCallback = this.choiceContentFromJsonAttributeCallback;
            }
        }
        if (!suggestionsOptions.createCategorySummaryCallback) {
            suggestionsOptions.createCategorySummaryCallback = this.createGenericSummary.bind(this);
        }
        return preparedOptions;
    },

    prepareUserCategoryAndDataSource: function(category, providedDataSourceOptions, postData, type,
            showImage) {
        var action, includeGroups;
        var finalPostData = {};
        category.id = 'userSuggestions';
        category.cssClass = 'autocompleter-category-users';

        type = type.toUpperCase();
        switch (type) {
        case 'MANAGER':
            action = 'queryManager';
            break;
        case 'AUTHOR':
            action = 'queryAuthor';
            break;
        case 'READER':
            action = 'queryReader';
            break;
        case 'ENTITY':
            action = 'queryEntity';
            includeGroups = true;
            break;
        default:
            action = 'queryUser';
        }
        category.contentAttribute = 'longName';
        if (showImage) {
            category.imagePathAttribute = 'imagePath';
            finalPostData['imageSize'] = 'SMALL';
            if (includeGroups) {
                // add a CSS class that will be used when there is no image, since all users have
                // images this only applies to groups 
                category.noImageCssClass = 'cn-icon';
            }
        }
        Object.merge(finalPostData, postData);
        if (!providedDataSourceOptions) {
            providedDataSourceOptions = {};
        }
        providedDataSourceOptions.requestCompleteCallback = this.arrayWithContainedSummaryResultCallback;
        return this.createRequestDataSource('/user/search/' + action + '.do', 'searchString',
                finalPostData, category, providedDataSourceOptions);
    },

    stripLeadingAtCharacter: function(text) {
        if (text.length) {
            // ignore all queries that do not start with an @
            if (text.charAt(0) != '@') {
                return null;
            } else if (text.length > 1 && text.charAt(1) == '@') {
                return text.substring(1);
            }
        }
        return text;
    }
});

/**
 * Special DataSource which groups update requests with the same TagSuggestionProvider to reduce the
 * number of requests.
 */
var TagSuggestionDataSource = new Class({
    Extends: AutocompleterRequestDataSource,

    options: {
        categoryParam: 'f_suggestionAliases',
        extendSupported: false,
        queryParam: 'tagPrefix'
    },
    // request descriptors for updates
    updateRequestDescriptors: null,
    requestedMaxCount: false,

    initialize: function(url, categories, options) {
        var maxCount;
        this.parent(url, categories, options);
        this.prepareForExtend(categories);
        if (this.options.postData) {
            maxCount = this.options.postData.maxCount;
            // API response has no indication if there are more results. workaround: increase
            // the maxCount and check whether the server returns the additional element
            if (maxCount > 0) {
                this.requestedMaxCount = maxCount;
                this.options.postData.maxCount++;
            }
        }
    },
    /**
     * @override
     */
    prepareForUpdate: function(categories) {
        var descriptors, i, descriptor, provider, category;
        // create one request per TagSuggestionProvider 
        descriptors = {};
        for (i = 0; i < categories.length; i++) {
            category = categories[i];
            provider = category.provider;
            if (provider && descriptors[provider] == undefined) {
                // only set provider alias to get all suggestions from this provider
                // because one provider can return several categories, we pass an array
                descriptor = this.createRequestDescriptorForCategory([ category.id ],
                        'f_suggestionProviderAliases', provider, true);
                descriptors[provider] = descriptor;
            } else {
                // just add the categoryId
                descriptor = descriptors[provider];
                descriptor.categoryId.push(category.id);
            }
        }
        this.updateRequestDescriptors = descriptors;
    },

    prepareForExtend: function(categories) {
        var descriptors, i, descriptor, categoryId, categoryParam, category;
        // create one request descriptor for each category
        if (this.options.extendSupported) {
            descriptors = {};
            categoryParam = this.options.categoryParam;
            for (i = 0; i < categories.length; i++) {
                category = categories[i];
                categoryId = category.id;
                descriptor = this.createRequestDescriptorForCategory(categoryId, categoryParam,
                        categoryId, false);
                // add provider, not sure if this needed
                descriptor.data['f_suggestionProviderAliases'] = category.provider;
                descriptors[categoryId] = descriptor;
            }
            // use the requestDescriptors member so that queryForExtend can be reused
            this.requestDescriptors = descriptors;
        }
    },
    /**
     * @override
     */
    doQueryForUpdate: function() {
        var providerAlias, descr;
        // send requests for all providers
        for (providerAlias in this.updateRequestDescriptors) {
            descr = this.updateRequestDescriptors[providerAlias];
            this.sendRequest(descr, true, this.currentQueryValue, null);
        }
    },
    /**
     * @override Special result handler that supports the response of the TagSuggestionList
     *           resource.
     * @param {Boolean} isUpdate Whether the request was an update or an extend
     * @param {String|String[]} categoryId The categoryIds for which suggestions were requested
     * @param {Object} the JSON response
     * @return {boolean} whether results where found
     */
    handleResult: function(isUpdate, categoryId, response) {
        var i, tagSuggestion, tokens, resultCount;
        var metaData = {};
        // result member of response holds an array of tag suggestions, which itself hold
        // the tags and an alias of the suggestion that is the category ID
        if (response.status === "OK" && response.result.length > 0) {
            resultCount = 0;
            for (i = 0; i < response.result.length; i++) {
                tagSuggestion = response.result[i];
                tokens = tagSuggestion.tags;
                if (tokens && this.requestedMaxCount && tokens.length > this.requestedMaxCount) {
                    metaData.moreResults = true;
                    tokens.pop();
                } else {
                    metaData.moreResults = false;
                }
                metaData.resultsReturned = tokens ? tokens.length : 0;
                resultCount += metaData.resultsReturned;
                this
                        .fireEvent('queryComplete', [ isUpdate, tagSuggestion.alias, tokens,
                                metaData ]);
            }
            return (resultCount > 0);
        } else {
            // treat error as 0 results, but return true to retry
            tokens = [];
            metaData.resultsReturned = 0;
            metaData.moreResults = false;
            if (isUpdate) {
                // categoryId is an array
                for (i = 0; i < categoryId.length; i++) {
                    this.fireEvent('queryComplete', [ true, categoryId[i], tokens, metaData ]);
                }
            } else {
                this.fireEvent('queryComplete', [ false, categoryId, tokens, metaData ]);
            }
            return true;
        }
    }
});