(function(namespace) {
    var AboutBlogWidget = new Class({

        Extends: C_FilterWidget,

        widgetGroup: "blog",
        topicUtils: null,
        blogIdFilterParamName: null,
        // URL to open when openEditMode is called. The URL can contain the strings
        // ALIAS_PLACEHOLDER and ID_PLACEHOLDER which will be replaced with the alias and ID of the
        // current topic respectively
        editUrl: null,
        // whether to remove this widget when openEditMode is called and no editUrl is specified.
        // If the editEvent is defined the widget will be removed after firing the event.
        removeOnEdit: false,
        // widget event to be fired when openEditMode is called and no editUrl is specified. The
        // event will receive a parameter object that contains the blogId and the widgetId of this
        // widget. The event is called onBlogManagementStart by default.
        editEvent: null,

        init: function() {
            var blogIdFilterParameterName;
            this.parent();
            if (this.filterWidgetGroup) {
                blogIdFilterParameterName = this.getStaticParameter('blogIdFilterParameterName')
                        || 'blogId';
                // register for blogId changes by observing the provided filter parameter name
                this.observedFilterParams = [ blogIdFilterParameterName ];
                this.blogIdFilterParamName = blogIdFilterParameterName;
            } else {
                // get reference to topicUtils if available to take newest blog ID from there
                this.topicUtils = window.blogUtils;
            }
            this.removeOnEdit = !!this.getStaticParameter('removeOnEdit');
            this.editEvent = this.getStaticParameter('editEvent') || 'onBlogManagementStart';
            this.copyStaticParameter('showFollowAction', true);
        },

        /**
         * @override
         */
        refreshStart: function() {
            // set the blogId parameter to the current value
            this.setFilterParameter('blogId', this.getCurrentBlogId());
            this.currentBlogAlias = null;
        },

        refreshComplete: function(responseMetadata) {
            var url;
            init_tips(this.domNode);
            url = this.getStaticParameter('editUrl');
            if (url) {
                url = url.replace('ID_PLACEHOLDER', this.getCurrentBlogId());
                url = url.replace('ALIAS_PLACEHOLDER', responseMetadata.blogAlias);
                this.editUrl = url;
            }
        },

        getListeningEvents: function() {
            return this.parent().combine([ 'onItemFollowed', 'onItemUnfollowed' ]);
        },

        onItemFollowed: function(params) {
            this.followStatusChanged(true, params);
        },

        onItemUnfollowed: function(params) {
            this.followStatusChanged(false, params);
        },

        followStatusChanged: function(follow, params) {
            if (params.blogId != this.getCurrentBlogId()) {
                return;
            }
            // TODO what now, change a css class or something?
        },
        getCurrentBlogId: function() {
            var blogId;
            if (this.filterWidgetGroup) {
                // take blog ID from observed filter parameter store
                blogId = this.filterParamStore.getFilterParameter(this.blogIdFilterParamName);
            } else {
                // use topicUtils to get newest blogId otherwise take from static parameters
                if (this.topicUtils) {
                    blogId = this.topicUtils.getCurrentBlogId();
                } else {
                    blogId = this.getStaticParameter('blogId');
                }
            }
            return blogId;
        },
        openEditMode: function() {
            if (this.editUrl) {
                location.href = buildRequestUrl(this.editUrl);
            } else {
                if (this.editEvent) {
                    this.widgetController.sendEvent(this.editEvent, null, {
                        widgetId: this.widgetId,
                        blogId: this.getCurrentBlogId()
                    });
                }
                if (this.removeOnEdit) {
                    this.widgetController.removeWidget(this);
                }
            }
        }
    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('AboutBlogWidget', AboutBlogWidget);
    } else {
        window.AboutBlogWidget = AboutBlogWidget;
    }
})(window.runtimeNamespace);
