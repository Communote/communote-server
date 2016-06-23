(function(namespace) {
    var ListExternalObjectsWidget = new Class({

        Extends: C_FilterWidget,

        widgetGroup: 'blog',
        topicUtils: null,
        blogIdFilterParamName: null,

        removeExternalObjectQueryUrlBase: '/web/v1.1.3/external/deleteExternalObject/INTERNAL_ID.json',

        init: function() {
            this.parent();
            if (this.filterWidgetGroup) {
                blogIdFilterParameterName = this.getStaticParameter('blogIdFilterParameterName')
                        || 'blogId';
                // register for blogId changes by observing the provided filter parameter name
                this.observedFilterParams = [ blogIdFilterParameterName ];
                this.blogIdFilterParamName = blogIdFilterParameterName;
            } else {
                // get reference to blogUtils if available to take newest blog ID from there
                this.topicUtils = window.blogUtils;
            }
            this.copyStaticParameter('editMode');
            this.copyStaticParameter('silentIfEmpty');
        },

        /**
         * @override
         */
        refreshStart: function() {
            // set the blogId parameter to the current value
            var blogId;
            if (this.filterWidgetGroup) {
                blogId = this.filterParamStore.getFilterParameter(this.blogIdFilterParamName);
            } else {
                if (this.topicUtils) {
                    blogId = this.topicUtils.getCurrentBlogId();
                } else {
                    blogId = this.getStaticParameter('blogId');
                }
            }
            this.setFilterParameter('blogId', blogId);
        },

        /**
         * Delete the external object assigned to the blog. Ask the user before actually doing it.
         * 
         * @param {String} internlExternalObjectId The internal ID of of the external object
         */
        removeExternalObject: function(internlExternalObjectId) {
            var buttons = [];
            buttons.push({
                type: 'yes',
                action: function() {
                    this.performRemoveExternalObject(internlExternalObjectId);
                }.bind(this)
            });
            buttons.push({
                type: 'no'
            });
            showDialog(getJSMessage('blog.member.management.remove.external.object'),
                    getJSMessage('blog.member.management.remove.external.object.question'), buttons);

        },

        /**
         * Delete the external object assign to the blog
         * 
         * @param internlExternalObjectId the internal of of the external object
         */
        performRemoveExternalObject: function(internlExternalObjectId) {
            new Request.JSON({
                url: buildRequestUrl(this.removeExternalObjectQueryUrlBase.replace('INTERNAL_ID',
                        internlExternalObjectId)),
                'onComplete': function(response) {
                    if (response.status == 'ERROR') {
                        showNotification(NOTIFICATION_BOX_TYPES.failure, null, response.message,
                                null);
                    } else {
                        showNotification(NOTIFICATION_BOX_TYPES.success, null, response.message,
                                null);
                        E('onExternalObjectDelete');
                        this.refresh();
                    }
                }.bind(this)
            }).post({});
        },

        /**
         * Open the edit mode under the navigation point "Integration" on the BlogManagement site
         * 
         */
        openEditMode: function() {
            var blogId = this.getFilterParameter("blogId");
            var url = buildRequestUrl("/portal/topic-edit?blogId=" + blogId + "&viewId=integration");
            location.href = url;
        }
    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('ListExternalObjectsWidget', ListExternalObjectsWidget);
    } else {
        window.ListExternalObjectsWidget = ListExternalObjectsWidget;
    }
})(window.runtimeNamespace);