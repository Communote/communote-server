(function(namespace) {

    /**
     * FilterEventHandler to filter notes.
     */
    var NotesFilterEventHandler = new Class({
        Extends: C_FilterEventHandler,

        /**
         * @override
         */
        getHandledEvents: function() {
            return this.parent().combine(
                    [ 'onBlogToggled', 'onTagSearchKeyUp', 'onTagClick', 'onTagIdClick',
                            'onTagHistoryClick', 'onTagSuggestionSelected', 'onUserSearchKeyUp',
                            'onUserToggled', 'onUserSelected', 'onSetTextFilter',
                            'onShowPostsForMe', 'onShowFavorites', 'onShowFollowedItems',
                            'onSetPeriodFilter', 'onPostDeleted', 'onShowNote', 'onTagClear',
                            'onPropertyFilterChanged', 'onUserSuggestionSelected', 'onRankChanged',
                            'onEntitySuggestionSelected' ]);
        },
        onBlogToggled: function(blogId) {
            if (typeOf(blogId) != 'string')
                blogId = blogId.toString();
            if (this.filterParameterStore.appendFilterParameterValue('blogId', blogId, true)) {
                return [ 'blogId' ];
            }
        },

        onEntitySuggestionSelected: function(token) {
            // inspect token to find the entity that was selected and delegate to correct handler
            if (token.suggestion) {
                return this.onSetTextFilter(token.suggestion);
            } else if (token.longName) {
                return this.onUserSuggestionSelected(token);
            } else if (token.tagId != undefined) {
                return this.onTagSuggestionSelected(token);
            } else if (token.alias != undefined) {
                communote.widgetController.getDataStore().put({
                    type: 'blog',
                    key: token.id,
                    title: token.title
                });
                return this.onBlogToggled(token.id);
            }
        },

        onPostDeleted: function(pId) {
            var currentDiscussionId = this.filterParameterStore.getFilterParameter('discussionId');
            var currentNoteId = this.filterParameterStore.getFilterParameter('noteId');
            if (currentDiscussionId == pId) {
                /* reset currentDiscussionId if root post is deleted */
                this.filterParameterStore.unsetFilterParameter('discussionId');
                return [ 'discussionId' ];
            } else if (currentNoteId == pId) {
                /* reset noteId if it is deleted */
                this.filterParameterStore.unsetFilterParameter('noteId');
                return [ 'noteId' ];
            }
        },

        onPropertyFilterChanged: function(propertyFilter) {
            var value = JSON.encode(propertyFilter.property);
            if (propertyFilter.selected) {
                if (this.filterParameterStore.setFilterParameter('propertyFilter', value)) {
                    return [ 'propertyFilter' ];
                }
            } else {
                if (this.filterParameterStore.appendFilterParameterValue('propertyFilter', value,
                        true)) {
                    return [ 'propertyFilter' ];
                }
            }
        },

        onRankChanged: function(value) {
            if (this.filterParameterStore.setFilterParameter('minRank', value)) {
                return [ 'minRank' ];
            }
        },

        onSetPeriodFilter: function(data) {
            if (data != null) {
                var changedParams = [];
                if (data.end !== undefined) {
                    if (data.end == null) {
                        if (this.filterParameterStore.unsetFilterParameter('endDate')) {
                            changedParams[0] = 'endDate';
                        }
                    } else {
                        if (this.filterParameterStore.setFilterParameter('endDate', data.end)) {
                            changedParams[0] = 'endDate';
                        }
                    }
                }
                if (data.start !== undefined) {
                    if (data.start == null) {
                        if (this.filterParameterStore.unsetFilterParameter('startDate')) {
                            changedParams[changedParams.length] = 'startDate';
                        }
                    } else {
                        if (this.filterParameterStore.setFilterParameter('startDate', data.start)) {
                            changedParams[changedParams.length] = 'startDate';
                        }
                    }
                }
                if (changedParams.length > 0) {
                    return changedParams;
                }
            }
        },

        onSetTextFilter: function(value) {
            if (this.filterParameterStore.toggleFilterParameter('postTextSearchString', value)) {
                return [ 'postTextSearchString' ];
            }
        },

        onShowFavorites: function(value) {
            if (this.filterParameterStore.setFilterParameter('showFavorites', value)) {
                return [ 'showFavorites' ];
            }
        },
        onShowFollowedItems: function(value) {
            if (this.filterParameterStore.setFilterParameter('showFollowedItems', value)) {
                return [ 'showFollowedItems' ];
            }
        },

        onShowPostsForMe: function(value) {
            if (this.filterParameterStore.setFilterParameter('showPostsForMe', value)) {
                return [ 'showPostsForMe' ];
            }
        },

        /**
         * @param {object} params object that containing the following members: noteId - the ID of
         *            the note to filter for blogId - ID of the blog of the note
         */
        onShowNote: function(params) {
            var newParams;
            if (params) {
                newParams = {
                    noteId: params.noteId
                };
                if (this.filterParameterStore.resetCurrentFilterParameters(newParams).length > 0) {
                    return [ 'noteId' ];
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

        onTagHistoryClick: function(tags) {
            var changed = false;
            if (tags == null) {
                changed = this.filterParameterStore.unsetFilterParameter('filter');
            } else {
                changed = this.filterParameterStore.setFilterParameter('filter', tags);
            }
            if (changed)
                return [ 'filter' ];
        },

        /**
         * Callback that modifies the tag filter that is based on the IDs of the tags.
         * 
         * @param {Array} tagData Array where the first element is the ID and the 2nd the name of
         *            the tag to add or remove. If the array contains a third element with value
         *            true, the tag filter for the given ID will be removed if it is already set.
         *            Otherwise the filter will be extended with the ID if it is not yet set.
         * @return {String[]} Array containing the filter parameter name 'tagIds' if the tagIds
         *         filter parameter was modified.
         */
        onTagIdClick: function(tagData) {
            // store as string because the parameter store is type specific
            var tagId = tagData[0].toString();
            var remove = true === tagData[2];
            if (this.filterParameterStore.appendFilterParameterValue('tagIds', tagId, remove)) {
                if (tagData[1]) {
                    communote.widgetController.getDataStore().put({
                        type: 'tag',
                        key: tagId,
                        title: tagData[1]
                    });
                }
                return [ 'tagIds' ];
            }
        },

        onTagSearchKeyUp: function(prefix) {
            if (this.filterParameterStore.toggleFilterParameter('tagPrefix', prefix)) {
                return [ 'tagPrefix' ];
            }
        },

        onTagSuggestionSelected: function(tagSuggestion) {
            // store as string because the parameter store is type specific
            var tagId = tagSuggestion.tagId.toString();
            if (this.filterParameterStore.appendFilterParameterValue('tagIds', tagId, false)) {
                communote.widgetController.getDataStore().put({
                    type: 'tag',
                    key: tagId,
                    title: tagSuggestion.name
                });
                return [ 'tagIds' ];
            }
        },

        onUserSearchKeyUp: function(prefix) {
            if (this.filterParameterStore.toggleFilterParameter('searchString', prefix)) {
                return [ 'searchString' ];
            }
        },

        onUserSelected: function(userId) {
            if (userId) {
                if (typeOf(userId) != 'string')
                    userId = userId.toString();
                if (this.filterParameterStore.appendFilterParameterValue('userId', userId, false)) {
                    return [ 'userId' ];
                }
            }
        },

        onUserSuggestionSelected: function(userData) {
            // save user data in data store and filter by userId
            communote.widgetController.getDataStore().put({
                type: 'user',
                key: userData.id,
                longName: userData.longName,
                shortName: userData.shortName
            });
            return this.onUserSelected(userData.id);
        },

        onUserToggled: function(userId) {
            if (typeOf(userId) != 'string')
                userId = userId.toString();
            if (this.filterParameterStore.appendFilterParameterValue('userId', userId, true)) {
                return [ 'userId' ];
            }
        }
    });
    NotesFilterEventHandler.defaultFilteredUnresetableParams = ['userId', 'blogId'];
    namespace.addConstructor('NotesFilterEventHandler', NotesFilterEventHandler);
})(window.runtimeNamespace);