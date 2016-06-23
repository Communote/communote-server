(function(namespace) {

    /**
     * Get the index of the item in the provided array that has the given navigationItemId
     * 
     * @param {Object[]} items Array of objects that have a navigationItemId member
     * @param {Number} navigationItemId ID of the navigation item
     * @return {Number} the index position inside the array or -1
     */
    function getIndexOfPersistedNavigationItemId(items, navigationItemId) {
        var i;
        for (i = 0; i < items.length; i++) {
            if (items[i].navigationItemId == navigationItemId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Private helper class that handles the checks of the persisted navigation items for new notes
     * by using REST API and AutoRefreshers.
     */
    var ObservationManager = new Class({
        activeObservedNavigationItemId: false,
        autoRefreshers: null,
        checkUrl: null,
        firstAutomaticObservationCheck: true,
        observations: null,
        noteCountChangedCallback: null,
        noteCountLimit: 0,
        userUtils: null,

        initialize: function(noteCountLimit, noteCountChangedCallback) {
            this.observations = [];
            this.autoRefreshers = [];
            this.noteCountChangedCallback = noteCountChangedCallback;
            this.userUtils = userUtils;
            this.checkUrl = this.userUtils.getObservationsUrl();
            this.noteCountLimit = noteCountLimit;
        },
        activeObservedNavigationItemChanged: function(newActiveNavigationItemId) {
            var oldActiveObservedNavigationItemId = this.activeObservedNavigationItemId;
            var updateObservationChecks = false;
            // stop new
            if (newActiveNavigationItemId !== false) {
                idx = getIndexOfPersistedNavigationItemId(this.observations,
                        newActiveNavigationItemId);
                if (this.observations[idx].action != 'remove') {
                    this.observations[idx].action = 'stop';
                }
                if (this.observations[idx].running) {
                    updateObservationChecks = true;
                }
            }
            // (re-) start previous, assume it has been reset in the meanwhile
            if (oldActiveObservedNavigationItemId !== false) {
                idx = getIndexOfPersistedNavigationItemId(this.observations,
                        oldActiveObservedNavigationItemId);
                if (this.observations[idx].action != 'remove') {
                    this.observations[idx].action = 'start';
                }
                updateObservationChecks = true;
            }
            if (updateObservationChecks) {
                this.updateAutomaticObservationChecks();
            }
            this.activeObservedNavigationItemId = newActiveNavigationItemId;
        },
        addObservation: function(navigationItemId, startChecking) {
            this.observations.push({
                action: 'start',
                dirty: false,
                lastCheckUpdateRunning: false,
                navigationItemId: navigationItemId,
                newNoteCount: 0,
                running: false
            });
            if (startChecking) {
                this.updateAutomaticObservationChecks();
            }
        },

        cleanup: function() {
            var i;
            this.observations = [];
            for (i = 0; i < this.autoRefreshers.length; i++) {
                this.autoRefreshers[i].stop();
            }
            this.autoRefreshers = [];
            this.firstAutomaticObservationCheck = true;
        },
        observationAutoRefresherCallback: function(response) {
            var i, observationsResponse, idx, observation, updateRefresher;
            observationsResponse = response.result;
            if (observationsResponse && observationsResponse.length > 0) {
                for (i = 0; i < observationsResponse.length; i++) {
                    idx = getIndexOfPersistedNavigationItemId(this.observations,
                            observationsResponse[i].observationId);
                    if (idx != -1) {
                        observation = this.observations[idx];
                        if (observation.newNoteCount != observationsResponse[i].count) {
                            this.noteCountChangedCallback(observation.navigationItemId,
                                    observationsResponse[i].count, observation.newNoteCount);
                            observation.newNoteCount = observationsResponse[i].count;
                            if (observation.newNoteCount > this.noteCountLimit) {
                                observation.action = 'stop';
                                updateRefresher = true;
                            }
                            // mark dirty if the lastCheck date is currently being updated to force a restart
                            // of the observation as soon as the update operation completed
                            observation.dirty = observation.lastCheckUpdateRunning;
                        }
                    }
                }
                if (updateRefresher) {
                    this.updateAutomaticObservationChecks();
                    return;
                }
            }
            this.optimizeAndStartAutomaticObservationChecks();
        },

        observedNavigationItemNotesLoaded: function(navigationItemId, firstNoteCreationTimestamp) {
            var idx = getIndexOfPersistedNavigationItemId(this.observations, navigationItemId);
            var observation = this.observations[idx];
            // mark that the lastCheck date is being updated
            observation.lastCheckUpdateRunning = true;
            this.userUtils.updateNavigationItemLastCheckDate(navigationItemId,
                    firstNoteCreationTimestamp,
                    this.updateNavigationItemLastCheckDateSuccessCallback.bind(this, observation),
                    function(response) {
                        observation.lastCheckUpdateRunning = false;
                        showNotification(NOTIFICATION_BOX_TYPES.error, '', response.message, {
                            duration: ''
                        });
                    }.bind(this));
        },
        /**
         * Try to optimize the automatic observation checks by reducing the number of server
         * requests through grouping of AutoRefreshers into one. Afterwards start all AutoRefreshers
         * that are not running to start the automatic checks.
         */
        optimizeAndStartAutomaticObservationChecks: function() {
            var i, aggregatingRefresher, aggregatedObservedIds, stats, mapping, key, length;
            var curIncrement, driftCount, prevNextCheck, autoRefreshersToRemove;
            var autoRefreshers = this.autoRefreshers;
            if (autoRefreshers.length > 1) {
                i = 0;
                // aggregate all autorefreshers that reached the limit into one
                while (i < autoRefreshers.length) {
                    if (autoRefreshers[i].isTimerLimitReached()) {
                        if (aggregatedObservedIds) {
                            autoRefreshers[i].stop();
                            aggregatedObservedIds.append(autoRefreshers[i]
                                    .getAdditionalParameter('f_observations'));
                            aggregatingRefresher.markDirty();
                            // remove autorefresher and continue without incrementing the counter
                            autoRefreshers.splice(i, 1);
                            continue;
                        } else {
                            aggregatingRefresher = autoRefreshers[i];
                            aggregatedObservedIds = aggregatingRefresher
                                    .getAdditionalParameter('f_observations');
                        }
                    }
                    i++;
                }
                // group autoRefreshers to reduce the number of requests if the timeouts only
                // differ by a small fraction: the following aggregates those that have the
                // same number of increments and will check with a difference of less than 10s
                mapping = {};
                curIncrement = -1;
                // first collect refreshers with same increment
                for (i = 0; i < autoRefreshers.length; i++) {
                    if (!autoRefreshers[i].isTimerLimitReached()) {
                        stats = autoRefreshers[i].getStatistics();
                        if (curIncrement != stats.timerIncrementCount) {
                            curIncrement = stats.timerIncrementCount;
                            driftCount = 0;
                            prevNextCheck = stats.nextCheck;
                        } else if (stats.nextCheck - prevNextCheck > 10000) {
                            driftCount++;
                        }
                        key = 'i' + stats.timerIncrementCount + '_' + driftCount;
                        if (!mapping[key]) {
                            mapping[key] = [];
                        }
                        mapping[key].push({
                            idx: i,
                            stats: stats
                        });
                    }
                }
                autoRefreshersToRemove = [];
                for (key in mapping) {
                    length = mapping[key].length;
                    if (length > 1) {
                        // use first for aggregating as it has the shortest waiting time
                        aggregatingRefresher = autoRefreshers[mapping[key][0].idx];
                        aggregatedObservedIds = aggregatingRefresher
                                .getAdditionalParameter('f_observations');
                        for (i = 1; i < length; i++) {
                            stats = mapping[key][i];
                            aggregatedObservedIds.append(autoRefreshers[stats.idx]
                                    .getAdditionalParameter('f_observations'));
                            autoRefreshers[stats.idx].stop();
                            autoRefreshersToRemove.push(autoRefreshers[stats.idx]);
                        }
                        aggregatingRefresher.markDirty();
                    }
                }
                for (i = 0; i < autoRefreshersToRemove.length; i++) {
                    autoRefreshers.erase(autoRefreshersToRemove[i]);
                }
            }
            // start existing autoRefreshers, starting an already started refresher won't do no harm
            for (i = 0; i < autoRefreshers.length; i++) {
                // force an immediate check if this is the first check, usually after widget refreshs
                autoRefreshers[i].start(this.firstAutomaticObservationCheck, false);
            }
            this.firstAutomaticObservationCheck = false;
        },

        removeObservation: function(navigationItemId, stopChecking) {
            var idx = getIndexOfPersistedNavigationItemId(this.observations, navigationItemId);
            if (idx != -1) {
                // mark observation for removal
                this.observations[idx].action = 'remove';
            }
            if (stopChecking) {
                this.updateAutomaticObservationChecks();
            }
        },

        /**
         * Check the action fields of the observations and remove, stop or start them.
         */
        updateAutomaticObservationChecks: function() {
            var i, j, observationsToRemove, idsToStopObserving, idsToStartObserving, observation;
            var observedIds, oldLength, newAutoRefresher, observationParams;
            var observations = this.observations;
            if (observations.length == 0) {
                return;
            }
            idsToStopObserving = [];
            idsToStartObserving = [];
            i = 0;
            while (i < observations.length) {
                observation = observations[i];
                if (observation.action == 'remove') {
                    if (observation.running) {
                        idsToStopObserving.push(observation.navigationItemId);
                    }
                    // remove and continue without incrementing
                    observations.splice(i, 1);
                    continue;
                } else if (observation.running && observation.action == 'stop') {
                    idsToStopObserving.push(observation.navigationItemId);
                    observation.running = false;
                } else if (!observation.running && observation.action == 'start') {
                    idsToStartObserving.push(observation.navigationItemId);
                    observation.running = true;
                } else if (observation.action == 'restart') {
                    idsToStopObserving.push(observation.navigationItemId);
                    idsToStartObserving.push(observation.navigationItemId);
                    observation.action = 'start';
                    observation.running = true;
                }
                i++;
            }
            for (i = 0; i < idsToStopObserving.length; i++) {
                for (j = 0; j < this.autoRefreshers.length; j++) {
                    observedIds = this.autoRefreshers[j].getAdditionalParameter('f_observations');
                    oldLength = observedIds.length;
                    if (observedIds.erase(idsToStopObserving[i]).length != oldLength) {
                        this.autoRefreshers[j].stop();
                        // can stop here because only one autoRefresher handles this ID, can also
                        // remove the autoRefresher if no IDs are left
                        if (observedIds.length == 0) {
                            this.autoRefreshers.splice(j, 1);
                        }
                        break;
                    }
                }
            }
            if (idsToStartObserving.length > 0) {
                // create a new auto refresher for the observations to restart
                newAutoRefresher = new AutoRefresher({
                    functionToCall: this.observationAutoRefresherCallback.bind(this),
                    timerInMillis: 5000,
                    timerUpperLimitReachedCallback: this.optimizeAndStartAutomaticObservationChecks
                            .bind(this),
                    url: this.checkUrl
                });
                newAutoRefresher.setAdditionalParameter('f_observations', idsToStartObserving);
                observationParams = this.userUtils.getObservationsAdditionalParameters();
                if (observationParams) {
                    for (i = 0; i < observationParams.length; i++) {
                        newAutoRefresher.setAdditionalParameter(observationParams[i].key,
                                observationParams[i].value);
                    }
                }
                this.autoRefreshers.push(newAutoRefresher);
            }
            this.optimizeAndStartAutomaticObservationChecks();
        },
        updateNavigationItemLastCheckDateSuccessCallback: function(observation, response) {
            observation.lastCheckUpdateRunning = false;
            if (observation.dirty) {
                // observation was updated while we were updating the last check date, but we
                // cannot know if the current note count is correct so we force a new check if running
                if (this.activeObservedNavigationItemId != observation.navigationItemId
                        && (observation.running || observation.newNoteCount > this.noteCountLimit)) {
                    observation.action = 'restart';
                    this.updateAutomaticObservationChecks();
                }
            } else {
                this.noteCountChangedCallback(observation.navigationItemId, 0,
                        observation.newNoteCount);
                // restart observation if it was stopped due to the limit beeing reached
                if (observation.newNoteCount > this.noteCountLimit) {
                    observation.action = 'start';
                }
                observation.newNoteCount = 0;
                // manually start if the observation does not belong to the current navigation item
                if (this.activeObservedNavigationItemId != observation.navigationItemId
                        && !observation.running) {
                    this.updateAutomaticObservationChecks();
                }
            }
        }
    });

    var VerticalNavigationWidget = new Class({
        Extends: C_FilterWidget,

        widgetGroup: 'navigation',

        observedFilterParams: [ 'contextId', 'viewId', 'targetBlogId', 'tagIds', 'userId' ],

        activeItemCssClass: 'active',
        activePersistedNavigationItemId: false,
        contextManager: null,
        followingNavigationItem: null,
        lastSelectedEntity: null,
        mentionsNavigationItem: null,
        observationCountLimit: 10,
        observationManager: null,
        persistedNavigationItems: null,
        persistedNavigationItemsAdded: false,
        // flag indicating that the persisted navigation items are currently or just have been sorted
        persistedNavigationItemsBeenSorted: false,
        persistedNavigationItemsConfigName: null,
        persistedNavigationItemsSortable: false,
        placeholders: null,
        searchItemClasses: null,
        userUtils: null,
        sortables: null,
        oldNavigationItemList: null,

        init: function() {
            var cssClass;
            this.parent();
            this.contextManager = this.getStaticParameter('contextManager')
                    || namespace.contextManager;
            cssClass = this.getStaticParameter('activeItemCssClass');
            if (cssClass != null) {
                if (cssClass) {
                    this.activeItemCssClass = cssClass;
                } else {
                    // disable highlighting if cssClass is blank or false
                    this.activeItemCssClass = false;
                }
            }
            this.persistedNavigationItems = [];
            this.persistedNavigationItemsConfigName = this
                    .getStaticParameter('persistedNavigationItemsConfigName')
                    || 'persistedNavigationItems';
            this.observationManager = new ObservationManager(this.observationCountLimit, this.updatePersistedNavigationItemNoteCount
                    .bind(this));
            this.userUtils = userUtils;
            // if the browser supports touch it's not possible to use the sort function
            this.persistedNavigationItemsSortable = (!Modernizr.touch);
        },

        /**
         * Fire the event that reloads the context and filters of the provided persisted navigation
         * item.
         * 
         * @param {Object} item The persisted navigation item to activate
         */
        activatePersistedNavigatonItem: function(item) {
            var contextDescriptor, paramData;
            // optimization: do nothing if already active
            if (this.activePersistedNavigationItemId === item.navigationItemId) {
                return;
            }
            // build a context descriptor
            contextDescriptor = {};
            contextDescriptor.contextId = item.data.contextType;
            contextDescriptor.viewId = this.getNotesViewId(contextDescriptor.contextId);
            contextDescriptor.contextFilterParameters = {};
            // get name of context filter parameter, but take value from navigation item
            paramData = this.getFilterParameterNameAndValue(contextDescriptor.contextId);
            contextDescriptor.contextFilterParameters[paramData.name] = item.data.contextId;
            // if the filters of the navigation item have more than one value provide them as filterParameters
            if (item.data.filters && Object.keys(item.data.filters).length > 1) {
                contextDescriptor.options = {};
                contextDescriptor.options.filterParams = Object.merge({}, item.data.filters);
            }
            this.sendFilterGroupEvent('activateContext', contextDescriptor);
        },

        /**
         * Add previously persisted navigation items to the navigation.
         * 
         * @param {Object[]} items An array of navigation item objects as returned by the REST API
         *            NavigationItem topic sub-resource. The data member can be string or already
         *            converted into an object.
         * @param {boolean} top Whether to add the items to the top or bottom of the persisted
         *            navigation items
         * @param {boolean} updateObservationChecks Whether the observation manager should start the
         *            observations after adding them. Passing false can be useful for optimization
         *            for instance if the following operation would stop the added observation
         *            again.
         */
        addPersistedNavigationItems: function(items, top, startObservationChecks) {
            var i, templateElem, wrapperElem, wasEmpty, navigationItem, entityId;
            wasEmpty = this.persistedNavigationItems.length == 0;
            templateElem = this.domNode.getElementById(this.widgetId
                    + '_navigation_favorites_template');
            wrapperElem = this.domNode.getElementById(this.widgetId
                    + '_navigation_favorites_wrapper');
            for (i = 0; i < items.length; i++) {
                navigationItem = items[i];
                // normalize properties
                if (typeof navigationItem.data == 'string') {
                    navigationItem.data = JSON.decode(navigationItem.data);
                }
                // entity IDs are typically numbers -> convert them because filter parameter store is type sensitive
                entityId = parseInt(navigationItem.data.contextId, 10);
                if (!isNaN(entityId)) {
                    navigationItem.data.contextId = entityId;
                }
                // check whether immutable or mutable item and only add latter to favorites
                if (navigationItem.index < 0) {
                    if (navigationItem.name == 'mentions') {
                        this.mentionsNavigationItem = navigationItem;
                    } else if (navigationItem.name == 'following') {
                        this.followingNavigationItem = navigationItem;
                    } else {
                        // ignore
                        continue;
                    }
                } else {
                    this.addPersistedNavigationItemToFavorites(navigationItem, templateElem,
                            wrapperElem, top);
                }
                // add a new observation and mark it to be started
                this.observationManager.addObservation(navigationItem.navigationItemId,
                        startObservationChecks && i == items.length - 1);
            }
            if (wasEmpty && this.persistedNavigationItems.length) {
                this.domNode.getElementById(this.widgetId + '_navigation_favorites').setStyle(
                        'display', '');
            }
        },

        /**
         * Add the provided navigation item to the favorites.
         * 
         * @param {Object} navigationItem The item to add
         * @param {Element} templateElement The element to clone and create the favorite element
         *            from
         * @param {Element} wrapperElement The favorites wrapping element
         * @param {boolean} top Whether to add the favorite element to the top or bottom of the
         *            favorites
         */
        addPersistedNavigationItemToFavorites: function(navigationItem, templateElem, wrapperElem,
                top) {
            var itemElem = templateElem.clone();
            this.setNavigationItemLabelText(itemElem, navigationItem.name);
            itemElem.setStyle('display', '');
            itemElem.setProperty('data-cnt-navigationItem-id', navigationItem.navigationItemId);
            itemElem.getElement('a')
                    .addEvent(
                            'click',
                            this.persistedNavigationItemClicked.bind(this,
                                    navigationItem.navigationItemId));
            wrapperElem.grab(itemElem, top ? 'top' : 'bottom');
            itemElem.getElement('.control-navigation-action-remove').addEvent(
                    'click',
                    this.persistedNavigationItemRemoveClicked.bind(this,
                            navigationItem.navigationItemId));
            itemElem.addClass(navigationItem.data.contextType);
            if (this.persistedNavigationItemsSortable) {
                // clear the beenSorted flag on mousedown to not block normal clicks. If mousedown
                // leads to a DnD the beenSorted flag will be set to true again and the mouse click
                // handler that is triggered when dropping while still hovering the sorted element
                // will do nothing. Actually only required for FF as preventDefault and/or 
                // stopPropagation have no effect.
                itemElem.addEvent('mousedown', this.clearBeenSortedFlag.bind(this));
            }
            if (top) {
                this.persistedNavigationItems.unshift(navigationItem);
            } else {
                this.persistedNavigationItems.push(navigationItem);
            }
            if (this.sortables) {
                this.sortables.addItems(itemElem);
                this.updateNavigationList();
            }
        },

        /**
         * @override
         */
        beforeRemove: function() {
            this.parent();
            this.cleanup();
        },

        /**
         * Cleanup any resources held.
         */
        cleanup: function() {
            if (this.placeholders) {
                this.placeholders.destroy();
            }
            // remove the navigation items to free there resources (e.g. click handler)
            this.domNode.getElements('[data-cnt-navigationItem-id]').destroy();
            // reset added persisted navigation items to let them be reloaded/readded
            this.persistedNavigationItemsAdded = false;
            this.persistedNavigationItems = [];
            if (this.observationManager) {
                this.observationManager.cleanup();
            }
            if (this.sortables) {
                this.sortables.detach();
            }
        },

        clearBeenSortedFlag: function() {
            this.persistedNavigationItemsBeenSorted = false;
        },

        /**
         * @override
         */
        filterParametersChanged: function(changedParams) {
            // do nothing until the first refresh completed and the persisted items were added to
            // not create a search item where highlighting a navigation item would be correct  
            if (this.firstDOMLoadDone && this.persistedNavigationItemsAdded) {
                this.highlightActiveNavigationItem();
            }
        },

        /**
         * Search for a persisted navigation item for the provided context that does not have
         * additional filters.
         * 
         * @param {String} contextId The ID of the context
         * @param {String} entityId The ID of the entity selected in the context, in other words:
         *            the value of the unresetable filter parameter of the context
         * @return {Object} the persisted navigation item or null if it does not exist
         */
        findMatchingPersistedNavigationItem: function(contextId, entityId) {
            var i, item, paramStore;
            for (i = 0; i < this.persistedNavigationItems.length; i++) {
                item = this.persistedNavigationItems[i];
                if (item.data.contextType == contextId && item.data.contextId == entityId) {
                    // compare the filter parameters of the item with the unresetable parameters
                    paramStore = this.contextManager.getFilterParameterStoreForContext(contextId,
                            this.getNotesViewId(contextId));
                    if (paramStore
                            && namespace.utils.compareFilterParameters(paramStore
                                    .getCurrentUnrestableFilterParameters(), item.data.filters)) {
                        return item;
                    }
                }
            }
            return null;
        },

        /**
         * @return {String} the current context ID
         */
        getContextId: function() {
            return this.filterParamStore.getFilterParameter('contextId');
        },

        getFilterParameterNameAndValue: function(contextId) {
            var filterParamName;
            if (contextId == 'tagSelected') {
                filterParamName = 'tagIds';
            } else if (contextId == 'userSelected') {
                filterParamName = 'userId';
            } else if (contextId == 'topicSelected') {
                filterParamName = 'targetBlogId';
            }
            if (filterParamName) {
                return {
                    name: filterParamName,
                    value: this.filterParamStore.getFilterParameter(filterParamName)
                };
            }
            return null;
        },

        /**
         * @override
         */
        getListeningEvents: function() {
            return this.parent().combine([ 'onNotesLoaded', 'onEntityNotFound' ]);
        },

        /**
         * @return {String[]} an array of the navigation item IDs, ordered as they are currently
         *         rendered
         */
        getNavigationItemsIdsSorted: function() {
            return this.sortables.serialize(function(element, index) {
                return element.getProperty('data-cnt-navigationItem-id');
            });
        },

        /**
         * Return the viewId that shows notes within the provided context.
         */
        getNotesViewId: function(contextId) {
            // TODO provide a staticParameter to provide a custom mapping
            // TODO only works for xxSelected contexts!
            return 'notes';
        },

        getPersistedNavigationItemElement: function(navigationItemId, startNode) {
            // check for the built-in mentions and following navigation items
            if (this.mentionsNavigationItem
                    && this.mentionsNavigationItem.navigationItemId == navigationItemId) {
                return this.domNode.getElement('.navigation-observation-item.mentions');
            } else if (this.followingNavigationItem
                    && this.followingNavigationItem.navigationItemId == navigationItemId) {
                return this.domNode.getElement('.navigation-observation-item.following');
            }
            if (!startNode) {
                startNode = this.domNode;
            }
            return startNode.getElement('[data-cnt-navigationItem-id=' + navigationItemId + ']');
        },

        /**
         * @return {String} the current view ID
         */
        getViewId: function() {
            return this.filterParamStore.getFilterParameter('viewId');
        },

        /**
         * Highlight the active navigation item by adding the configured CSS class
         * (activeItemCssClass) and removing that class from the previously active item.
         * 
         */
        highlightActiveNavigationItem: function() {
            var navigationItem, contextId, viewId, elem, viewElem, filterParamData;
            var idx, oldActivePersistedNavigationItemId;
            if (!this.activeItemCssClass) {
                return;
            }
            oldActivePersistedNavigationItemId = this.activePersistedNavigationItemId;
            this.activePersistedNavigationItemId = false;
            elem = this.domNode.getElements('.' + this.activeItemCssClass);
            elem.removeClass(this.activeItemCssClass);
            contextId = this.getContextId();
            if (!contextId) {
                return;
            }
            filterParamData = this.getFilterParameterNameAndValue(contextId);
            navigationItem = filterParamData
                    && this.findMatchingPersistedNavigationItem(contextId, filterParamData.value);
            if (navigationItem != undefined) {
                // highlight persisted navigation item
                elem = this.getPersistedNavigationItemElement(navigationItem.navigationItemId);
                this.activePersistedNavigationItemId = navigationItem.navigationItemId;
            } else {
                if (contextId.test('[[a-z]+Overview')) {
                    // navigation items for overview contexts are fix
                    elem = this.domNode.getElement('#' + this.widgetId + '_navigation_item_'
                            + contextId);
                    if (elem && contextId === 'notesOverview') {
                        viewId = this.getViewId();
                        if (viewId) {
                            viewElem = elem.getElement('.' + viewId);
                            if (viewElem) {
                                viewElem.addClass(this.activeItemCssClass);
                            }
                            if (this[viewId + 'NavigationItem']) {
                                this.activePersistedNavigationItemId = this[viewId
                                        + 'NavigationItem'].navigationItemId;
                            }
                        }
                    }
                } else {
                    if (filterParamData) {
                        elem = this.updateSearchItem(contextId, filterParamData);
                    }
                }
            }
            if (elem) {
                elem.addClass(this.activeItemCssClass);
            }
            if (oldActivePersistedNavigationItemId !== this.activePersistedNavigationItemId) {
                this.observationManager
                        .activeObservedNavigationItemChanged(this.activePersistedNavigationItemId);
            }
        },

        initSortNavigationItems: function() {
            var navItemsWrapper = this.domNode.getElementById(this.widgetId
                    + '_navigation_favorites_wrapper');
            this.sortables = new Sortables(navItemsWrapper, {
                onComplete: this.sortNavigationItemCompleteCallback.bind(this),
                onStart: this.sortNavigationItemStartCallback.bind(this)
            });
            this.updateNavigationList();
        },

        /**
         * Event handler for the onEntityNotFound widget event which checks whether there is a
         * persisted navigation item that is filtered by that entity. If such an item exists a
         * confirm dialog to remove that item is shown.
         * 
         * @param {Object} details The event data including the type and id of the entity
         */
        onEntityNotFound: function(details) {
            var navigationItem, title, content, args;
            if (details.type === 'topic' || details.type === 'user' || details.type === 'tag') {
                // TODO the item will only be found if the context was active before
                navigationItem = this.findMatchingPersistedNavigationItem(
                        details.type + 'Selected', details.id);
                if (navigationItem && !navigationItem.askedForRemoval) {
                    navigationItem.askedForRemoval = true;
                    title = getJSMessage('widget.mainPageVerticalNavigation.favorites.remove.missing.title');
                    args = [ navigationItem.name ];
                    content = [];
                    content.push(getJSMessage(
                            'widget.mainPageVerticalNavigation.favorites.remove.missing.details.'
                                    + details.type, args));
                    content.push(getJSMessage(
                            'widget.mainPageVerticalNavigation.favorites.remove.missing.confirm',
                            args));
                    showConfirmDialog(title, content, this.removePersistedNavigationItem.bind(this,
                            navigationItem.navigationItemId, false));
                }
            }
        },

        /**
         * Event handler for the onNotesLoaded widget event which updates the observation of the
         * current persisted navigation item, if there is one.
         * 
         * @param {Object} data The event data including filter parameters and the timestamps of the
         *            first and last rendered note.
         */
        onNotesLoaded: function(data) {
            var idx, navigationItem;
            var activeItemId = this.activePersistedNavigationItemId;
            if (data.firstNoteCreationTimestamp && activeItemId) {
                if (this.mentionsNavigationItem
                        && this.mentionsNavigationItem.navigationItemId == activeItemId) {
                    navigationItem = this.mentionsNavigationItem;
                } else if (this.followingNavigationItem
                        && this.followingNavigationItem.navigationItemId == activeItemId) {
                    navigationItem = this.followingNavigationItem;
                } else {
                    idx = getIndexOfPersistedNavigationItemId(this.persistedNavigationItems,
                            activeItemId);
                    navigationItem = this.persistedNavigationItems[idx];
                }
                if (namespace.utils.compareFilterParameters(data.filterParameters,
                        navigationItem.data.filters)) {
                    this.observationManager.observedNavigationItemNotesLoaded(
                            navigationItem.navigationItemId, data.firstNoteCreationTimestamp);
                }
            }
        },

        /**
         * Callback that is invoked when one of the persisted navigation items is clicked by the
         * user.
         * 
         * @param {Number} navigationItemId ID of the persisted navigation item.
         */
        persistedNavigationItemClicked: function(navigationItemId) {
            var idx;
            if (!this.persistedNavigationItemsBeenSorted) {
                idx = getIndexOfPersistedNavigationItemId(this.persistedNavigationItems,
                        navigationItemId);
                if (idx != -1) {
                    this.activatePersistedNavigatonItem(this.persistedNavigationItems[idx]);
                }
            }
        },
        /**
         * Callback that is invoked when the remove action of one of the persisted navigation items
         * is clicked by the user.
         * 
         * @param {Number} navigationItemId ID of the persisted navigation item.
         */
        persistedNavigationItemRemoveClicked: function(navigationItemId) {
            this.removePersistedNavigationItem(navigationItemId, true);
            return false;
        },

        /**
         * Callback that is invoked when the persisted navigation items are loaded by REST API and
         * that request failed.
         * 
         * @param {Object} response The REST API response
         */
        persistedNavigationItemsLoadFailedCallback: function(response) {
            // mark that items were also it failed. So that the filter parameters are changed correctly.
            this.persistedNavigationItemsAdded = true;
            this.restApiRequestFailedCallback(response);
        },

        /**
         * Callback that is invoked when the persisted navigation items are loaded by REST API and
         * that request succeeded.
         * 
         * @param {Object} response The REST API response where the result member contains an array
         *            of NavigationItem resources
         */
        persistedNavigationItemsLoadSuccessCallback: function(response) {
            // do not start checking after adding because highlightActiveNaviItem might activate a
            // persisted navigation item which would restart the checker (without that item) again 
            this.addPersistedNavigationItems(response.result, false, false);
            this.stopLoadingFeedback();
            this.persistedNavigationItemsAdded = true;
            this.highlightActiveNavigationItem();
            // manually start checking
            this.observationManager.updateAutomaticObservationChecks();
            if (this.persistedNavigationItemsSortable) {
                this.initSortNavigationItems();
            }
        },

        /**
         * @override
         */
        refreshComplete: function(responseMetadata) {
            var searchInputElem, persistedItems;
            this.parent(responseMetadata);

            this.searchItemClasses = this.domNode.getElementById(this.widgetId
                    + '_navigation_item_search').className;

            searchInputElem = this.domNode.getElement('input[type=text]');
            this.placeholders = communote.utils.attachPlaceholders(searchInputElem);
            // add the persisted navigation items, first check if they are contained in the
            // configuration and if they are not load them with REST API
            if (namespace && namespace.configuration) {
                persistedItems = namespace.configuration[this.persistedNavigationItemsConfigName];
            }
            if (persistedItems) {
                this.addPersistedNavigationItems(persistedItems, false, false);
                this.persistedNavigationItemsAdded = true;
                this.highlightActiveNavigationItem();
                this.observationManager.updateAutomaticObservationChecks();
                if (this.persistedNavigationItemsSortable) {
                    this.initSortNavigationItems();
                }
            } else {
                // restart loading feedback
                this.startLoadingFeedback();
                this.userUtils.getNavigationItems(this.persistedNavigationItemsLoadSuccessCallback
                        .bind(this), this.persistedNavigationItemsLoadFailedCallback.bind(this));
            }
        },

        /**
         * @override
         */
        refreshStart: function() {
            this.cleanup();
        },

        /**
         * Remove a persisted navigation item.
         * 
         * @param {Number} navigationItemId ID of the persisted navigation item.
         * @param {boolean} addToSearchItem Whether to add the removed navigation item to the last
         *            selected search after removing it
         */
        removePersistedNavigationItem: function(navigationItemId, addToSearchItem) {
            this.startLoadingFeedback();
            this.userUtils.deleteNavigationItem(navigationItemId,
                    this.removePersistedNavigationItemSuccessCallback.bind(this, navigationItemId,
                            addToSearchItem), this.restApiRequestFailedCallback.bind(this));
        },

        /**
         * REST API callback that is invoked when a persisted navigation item was successfully
         * removed.
         * 
         * @param {Number} navigationItemId ID of the persisted navigation item.
         * @param {boolean} addToSearchItem Whether to add the removed navigation item to the last
         *            selected search
         * @param {Object} response REST API response object not containing any useful data
         */
        removePersistedNavigationItemSuccessCallback: function(navigationItemId, addToSearchItem,
                response) {
            var navigationItem, wrapperElem, contextId, filterParamData, idx, navigationItemElem;
            // not stopping when the active item is removed because highlightActiveNavigationItem 
            // will notify the manager in that case
            this.observationManager.removeObservation(navigationItemId,
                    this.activePersistedNavigationItemId != navigationItemId);
            idx = getIndexOfPersistedNavigationItemId(this.persistedNavigationItems,
                    navigationItemId);
            if (idx != -1) {
                navigationItem = this.persistedNavigationItems.splice(idx, 1)[0];
                // remove from dom
                wrapperElem = this.domNode.getElementById(this.widgetId + '_navigation_favorites');
                navigationItemElem = this.getPersistedNavigationItemElement(navigationItemId,
                        wrapperElem);
                if (this.sortables) {
                    this.sortables.removeItems(navigationItemElem);
                    this.updateNavigationList();
                }
                navigationItemElem.destroy();
                if (this.persistedNavigationItems.length == 0) {
                    wrapperElem.setStyle('display', 'none');
                }
                if (addToSearchItem) {
                    // update search item with removed navigation item. Provide searchTitle since the data
                    // might not be available in the DataStore
                    contextId = navigationItem.data.contextType;
                    filterParamData = this.getFilterParameterNameAndValue(contextId);
                    // take value from navigation item
                    filterParamData.value = navigationItem.data.contextId;
                    this.updateSearchItem(contextId, filterParamData, navigationItem.name);
                }
                this.stopLoadingFeedback();
                if (navigationItemId == this.activePersistedNavigationItemId) {
                    if (!addToSearchItem) {
                        this.sendFilterGroupEvent('activateContext', {
                            contextId: this.contextManager.getDefaultContextId()
                        });
                    } else {
                        // just highlight and don't fire an event because the context did not change
                        this.highlightActiveNavigationItem();
                    }
                }
            }
        },

        renamePersistedNavigationItem: function(navigationItemId) {
            var contentElem, html, buttons, inputElem;
            var idx = getIndexOfPersistedNavigationItemId(this.persistedNavigationItems,
                    navigationItemId);
            if (idx != -1) {
                // show a dialog that allows the user to change the name
                contentElem = new Element('div');
                html = '<p>'
                        + getJSMessage('widget.mainPageVerticalNavigation.dialog.rename.hint')
                        + '</p><div class="cn-border"><input type="text" data-cnt-dialog-focus="true" /></div>';
                contentElem.set('html', html);
                inputElem = contentElem.getElement('input');
                inputElem.value = this.persistedNavigationItems[idx].name;
                buttons = [];
                buttons.push({
                    type: 'ok',
                    action: this.renamePersistedNavigationItemOkClicked.bind(this, idx)
                });
                buttons.push({
                    type: 'cancel'
                });
                showDialog(getJSMessage('widget.mainPageVerticalNavigation.dialog.rename.title'),
                        contentElem, buttons);
            }
        },

        renamePersistedNavigationItemOkClicked: function(navigationItemIdx, dialogContainer) {
            var newName;
            var inputElem = dialogContainer.getElement('input[type=text]');
            var navigationItem = this.persistedNavigationItems[navigationItemIdx];
            if (inputElem && navigationItem) {
                newName = inputElem.value.trim();
                if (navigationItem.name != newName) {
                    this.startLoadingFeedback();
                    this.userUtils.renameNavigationItem(navigationItem.navigationItemId, newName,
                            this.renamePersistedNavigationItemSuccessCallback.bind(this,
                                    navigationItem.navigationItemId, newName),
                            this.restApiRequestFailedCallback.bind(this));
                }
            }
        },

        renamePersistedNavigationItemSuccessCallback: function(navigationItemId, newName) {
            var idx = getIndexOfPersistedNavigationItemId(this.persistedNavigationItems,
                    navigationItemId);
            if (idx != -1) {
                this.setNavigationItemLabelText(this
                        .getPersistedNavigationItemElement(navigationItemId), newName);
                this.persistedNavigationItems[idx].name = newName;
            }
            this.stopLoadingFeedback();
        },

        /**
         * Reset and hide the navigation item showing the last selected search suggestion.
         */
        resetSearchItem: function() {
            var naviWrapperElem = this.domNode.getElementById(this.widgetId + '_navigation_search');
            naviWrapperElem.setStyle('display', 'none');
            if (this.persistedNavigationItems.length > 0) {
                this.domNode.getElementById(this.widgetId + '_helper').setStyle('display', 'none');
            }
            this.setNavigationItemLabelText(naviWrapperElem, '');
            this.lastSelectedEntity = null;
        },

        /**
         * Save the last selected search suggestion as a persisted navigation item. If there is no
         * such search nothing will happen.
         */
        saveSearch: function() {
            var navigationItemData, paramData;
            var contextId = this.lastSelectedEntity && this.lastSelectedEntity.contextId;
            if (contextId) {
                paramData = this.getFilterParameterNameAndValue(contextId);
                this.startLoadingFeedback();
                navigationItemData = {};
                navigationItemData.name = this.domNode.getElement(
                        '#' + this.widgetId + '_navigation_search .control-navigation-item-label')
                        .get('text');
                navigationItemData.index = 0;
                navigationItemData.data = {};
                // names in API are slightly different to how they are used in FE
                navigationItemData.data.contextType = contextId;
                // take value which must be a string from last selected entity
                navigationItemData.data.contextId = this.lastSelectedEntity.contextFilterParameters[paramData.name]
                        .toString();
                navigationItemData.data.filters = this.lastSelectedEntity.unresetableFilters;
                this.userUtils.createNavigationItem(navigationItemData,
                        this.saveSearchSuccessCallback.bind(this, navigationItemData),
                        this.restApiRequestFailedCallback.bind(this));
            }
        },

        /**
         * Callback that is invoked when the navigation item created from the last selected search
         * suggestion was successfully persisted on the server.
         * 
         * @param {Object} navigationItemData Object holding the details of the navigation item in
         *            the format of the REST API NavigationItem Topic sub-resource
         * @param {Object} response REST API response object whose result member contains the ID of
         *            the persisted navigation item
         */
        saveSearchSuccessCallback: function(navigationItemData, response) {
            this.stopLoadingFeedback();
            // add ID from result to data
            navigationItemData.navigationItemId = response.result;
            // not starting the observation since the item will be highlighted and thus stoped
            this.addPersistedNavigationItems([ navigationItemData ], true, false);
            this.highlightActiveNavigationItem();
            this.resetSearchItem();
        },

        /**
         * Generic handler for failed API requests which stops the loading feedback and shows an
         * error message.
         */
        restApiRequestFailedCallback: function(response) {
            this.stopLoadingFeedback();
            showNotification(NOTIFICATION_BOX_TYPES.error, '', response.message, {
                duration: ''
            });
        },

        /**
         * Callback that is invoked when the navigation item showing the last selected search
         * suggestion is clicked.
         */
        searchItemClicked: function() {
            if (this.lastSelectedEntity) {
                // TODO ignore if it is already focused or let click reset view?
                this.sendFilterGroupEvent('activateContext', this.lastSelectedEntity);
            }
        },

        setNavigationItemLabelText: function(naviElem, text) {
            var labelElem = naviElem.getElement('.control-navigation-item-label');
            labelElem.set('text', text);
            labelElem.setProperty('title', text);
        },

        sortNavigationItemCompleteCallback: function(sortedElement) {
            var sortedNavigationItems, sortedItemId, newIndex;
            this.domNode.getElementById(this.widgetId + '_navigation_favorites_wrapper')
                    .removeClass('cn-ui-action cn-ui-action-move');
            sortedElement.removeClass('cn-sort');
            sortedNavigationItems = this.getNavigationItemsIdsSorted();
            sortedItemId = sortedElement.getProperty('data-cnt-navigationItem-id');
            newIndex = sortedNavigationItems.indexOf(sortedItemId);
            if (this.oldNavigationItemList.indexOf(sortedItemId) != newIndex) {
                this.userUtils.moveNavigationItem(sortedItemId, newIndex, function() {
                    //TODO maybe unblock sort function, if blocked when request starts?
                }, function(response) {
                    showNotification(NOTIFICATION_BOX_TYPES.error, '',
                            getJSMessage('widget.mainPageVerticalNavigation.favorites.sort.error'),
                            {});
                }, null);
                this.updateNavigationList();
            }
        },

        sortNavigationItemStartCallback: function(sortedElement) {
            this.domNode.getElementById(this.widgetId + '_navigation_favorites_wrapper').addClass(
                    'cn-ui-action cn-ui-action-move');
            sortedElement.addClass('cn-sort');
            this.persistedNavigationItemsBeenSorted = true;
        },

        updateNavigationList: function() {
            this.oldNavigationItemList = this.getNavigationItemsIdsSorted();
        },

        /**
         * Update the element that shows the number of the new notes for a given persisted
         * navigation item.
         * 
         * @param {String} navigationItemId ID of the persisted navigation item
         * @param {Number} newCount The new number of notes that should be visualized
         * @param {Number} oldCount The previous number of notes
         */
        updatePersistedNavigationItemNoteCount: function(navigationItemId, newCount, oldCount) {
            var value;
            var itemElem = this.getPersistedNavigationItemElement(navigationItemId, null);
            var countElem = itemElem.getElement('.control-navigation-item-new-note-count');
            if (oldCount == 0 && newCount != 0) {
                itemElem.addClass('navigation-item-has-new-notes');
            } else if (newCount == 0) {
                itemElem.removeClass('navigation-item-has-new-notes');
            }
            if (newCount > this.observationCountLimit) {
                value = this.observationCountLimit + '+';
            } else {
                value = newCount;
            }
            countElem.set('text', value);
        },

        /**
         * Update the navigation item showing the last selected search suggestion with the new data
         * and show it if necessary.
         * 
         * @param {String} contextId ID of the context of the selected search suggestion
         * @param {Object} filterParamData Object containing name and value of the filter parameter
         *            that identifies the selected search suggestion
         * @param {String} [searchItemText] The text to set for the search item. If omitted the text
         *            will be taken from the DataStore
         */
        updateSearchItem: function(contextId, filterParamData, searchItemText) {
            var dataStore, paramStore;
            var naviElem = this.domNode.getElementById(this.widgetId + '_navigation_item_search');
            // do nothing if last selected is just reactivated
            if (this.lastSelectedEntity
                    && this.lastSelectedEntity.contextId == contextId
                    && this.lastSelectedEntity.contextFilterParameters[filterParamData.name] == filterParamData.value) {
                return naviElem;
            }
            dataStore = this.widgetController.getDataStore();
            if (!searchItemText) {
                if (contextId == 'tagSelected') {
                    searchItemText = dataStore.get('tag', filterParamData.value).title;
                } else if (contextId == 'userSelected') {
                    searchItemText = dataStore.get('user', filterParamData.value).longName;
                } else if (contextId == 'topicSelected') {
                    searchItemText = dataStore.get('blog', filterParamData.value).title;
                }
            }
            if (searchItemText) {
                naviElem.getParent('#' + this.widgetId + '_navigation_search').setStyle('display',
                        '');
                this.setNavigationItemLabelText(naviElem, searchItemText);
            }

            naviElem.className = this.searchItemClasses + ' ' + contextId;

            // save the last selected entity to be able to reload it when click on search tab
            this.lastSelectedEntity = {
                contextId: contextId,
                contextFilterParameters: {}
            };
            this.lastSelectedEntity.contextFilterParameters[filterParamData.name] = filterParamData.value;
            // save the filters from the unresetable parameters of the filter parameter store
            // as it might contain more parameters which are set during filter group creation
            paramStore = this.contextManager.getFilterParameterStoreForContext(contextId, this
                    .getNotesViewId(contextId));
            this.lastSelectedEntity.unresetableFilters = Object.merge({}, paramStore
                    .getCurrentUnrestableFilterParameters());

            if (this.persistedNavigationItems.length == 0) {
                this.domNode.getElementById(this.widgetId + '_helper').setStyle('display', '');
            }
            return naviElem;
        }

    });
    namespace.addConstructor('MainPageVerticalNavigationWidget', VerticalNavigationWidget);
})(window.runtimeNamespace);
