(function(namespace) {
    if (!namespace) {
        namespace = window;
    }

    var ChangedHandler = new Class({
        paramName: null,
        handlerFunction: null,
        currentValue: null,
        filterParameterStore: null,
        filterGroup: null,

        /**
         * Creates a new instance.
         * 
         * @param {String} groupId The ID of the FilterGroup to attach to.
         * @param {String} paramName The name of the parameter to observe for changes.
         * @param {Function} handlerFunction The function to be called when the parameter changed.
         *            The function will be passed to arguments, the first is the old and the 2nd the
         *            new value.
         * @param {String} [options.filterGroupRepoName] The name of the namespace member that holds
         *            the filter group repository (mapping from group name to group). If not
         *            provided 'filterGroupRepo' is used.
         * @constructs
         * @class A helper to easily observe changes of a filter parameter in a given filter group.
         *        If the parameter changes a configurable handler function will be called with the
         *        old and the new value.
         */
        initialize: function(groupId, paramName, handlerFunction, options) {
            var groupRepoName, groupRepo, filterGroup;
            this.paramName = paramName;
            this.handlerFunction = handlerFunction;
            groupRepoName = (options && options.filterGroupRepoName) || 'filterGroupRepo';
            groupRepo = namespace[groupRepoName];
            if (!groupRepo) {
                throw 'FilterGroup repository ' + groupRepoName + ' does not exist'
            }
            filterGroup = groupRepo[groupId];
            if (!filterGroup) {
                throw 'FilterGroup ' + groupId + ' does not exist'
            }
            this.filterGroup = filterGroup;
            filterGroup.addMember(this);
            this.filterParameterStore = filterGroup.getParameterStore();
            this.currentValue = this.filterParameterStore.getFilterParameter(paramName);
        },
        
        destroy: function() {
            this.filterGroup.removeMember(this);
        },
        /**
         * Implementation of the FilterParameterListener function which calls the configured handler
         * function.
         */
        filterParametersChanged: function(changedParams) {
            var oldValue = this.currentValue;
            this.currentValue = this.filterParameterStore.getFilterParameter(this.paramName);
            this.handlerFunction.call(null, oldValue, this.currentValue);
        },
        getCurrentValue: function() {
            return this.currentValue;
        },
        getFilterParameter: function(paramName) {
            return this.filterParameterStore.getFilterParameter(paramName);
        },
        /**
         * Implementation of the FilterParameterListener function which returns the configured
         * parameter name.
         */
        getObservedFilterParameters: function() {
            return [ this.paramName ];
        }
    });
    if (namespace.addConstructor) {
        namespace.addConstructor('FilterParameterChangedHandler', ChangedHandler);
    } else {
        window.FilterParameterChangedHandler = ChangedHandler;
    }
})(window.runtimeNamespace);