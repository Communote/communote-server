(function(namespace) {
    /**
     * Filter event handler for the user and blog overview pages.
     */
    var BlogsAndUsersFilterEventHandler = new Class({
        Extends: C_FilterEventHandler,

        options: {
            // the context the handler is used in, can be 'user' or 'topic'
            context: 'user'
        },

        getHandledEvents: function() {
            return this.parent().combine(
                    [ 'onChangeViewType', 'onTagClick', 'onTagIdClick', 'onTagSuggestionSelected', 'onSetTextFilter',
                            'onTagSearchKeyUp', 'onEntitySuggestionSelected' ]);
        },
        
        onChangeViewType: function(details) {
            if (details.preferenceId) {
                communoteLocalStorage.setItem(details.preferenceId + '-selectedViewType', details.viewType);
            }
            if (details.viewType == 'CLASSIC') {
                this.filterParameterStore.setUnresetableFilterParameter('showOnlyRootTopics', false);
            } else {
                this.filterParameterStore.setUnresetableFilterParameter('showOnlyRootTopics', true);
            }
            return ['viewType'];
        },
        onEntitySuggestionSelected: function(token) {
            // inspect token to find the entity that was selected and delegate to correct handler
            if (token.suggestion) {
                return this.onSetTextFilter(token.suggestion);
            } else if (token.tagId != undefined) {
                return this.onTagSuggestionSelected(token);
            }
        },

        /**
         * called when issuing a text string search in user attributes at or in case the context of
         * the handler is 'topic' in the title of the topic
         */
        onSetTextFilter: function(searchString) {
            var filterParamName;
            if (this.options.context == 'user') {
                filterParamName = 'searchString'; 
            } else if (this.options.context == 'topic') {
                filterParamName = 'pattern';
            }
            if (filterParamName) {
                if (this.filterParameterStore.toggleFilterParameter(filterParamName, searchString)) {
                    return [ filterParamName ];
                }
            }
        },

        onTagClick: function(tag) {
            var remove = false;
            if (typeOf(tag) == 'array') {
                remove = tag[1];
                tag = tag[0];
            }
            if (this.filterParameterStore.appendFilterParameterValue('filter', tag, remove)) {
                return [ 'filter' ];
            }
        },

        onTagIdClick: function(tagId) {
            if (this.filterParameterStore.appendFilterParameterValue('tagIds', tagId[0], tagId[2])) {
                return [ 'tagIds' ];
            }
        },

        onTagSearchKeyUp: function(prefix) {
            if (this.filterParameterStore.toggleFilterParameter('tagPrefix', prefix)) {
                return [ 'tagPrefix' ];
            }
        },

        onTagSuggestionSelected: function(tagSuggestion) {
            if (this.filterParameterStore.appendFilterParameterValue('tagIds', tagSuggestion.tagId,
                    false)) {
                widgetController.getDataStore().put({
                    type: 'tag',
                    key: tagSuggestion.tagId,
                    title: tagSuggestion.name
                });
                return [ 'tagIds' ];
            }
        }
    });
    namespace.addConstructor('BlogsAndUsersFilterEventHandler', BlogsAndUsersFilterEventHandler);
})(window.runtimeNamespace);