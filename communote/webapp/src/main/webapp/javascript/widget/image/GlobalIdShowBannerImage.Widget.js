(function(namespace) {
    var GlobalIdShowBannerImageWidget = new Class({
        Extends: C_FilterWidget,
        widgetGroup: "image",

        observedFilterParams: [ 'contextId', 'tagIds', 'userId', 'targetBlogId' ],

        setup: function() {
            this.parent();
            this.copyStaticParameter('title');
            this.responsiveDesignEnabled = namespace.utils && namespace.utils.responsiveUtils && namespace.utils.responsiveUtils.responsiveDesignEnabled();
            if (this.responsiveDesignEnabled) {
                this.adaptToViewportWidth();
            }
        },
        
        adaptToViewportWidth: function() {
            var renderOnlyTitle = namespace.utils.responsiveUtils.testResponsiveAttribute({
                name: 'viewportWidth', value: 'tiny'});
            if (this.getFilterParameter('renderOnlyTitle') != renderOnlyTitle) {
                this.setFilterParameter('renderOnlyTitle', renderOnlyTitle);
                return true;
            }
            return false;            
        },

        getListeningEvents: function() {
            var events = this.parent().combine([ 'onGlobalIdImageChanged', 'onUserProfileChanged' ]);
            if (this.responsiveDesignEnabled) {
                events.push('onResponsiveAttributesChanged');
            }
            return events;
        },

        onGlobalIdImageChanged: function(data) {
            if (data.imageType != 'banner') {
                return;
            }
            if (data.entityId == 'topic.'
                    + this.filterParamStore.getFilterParameter('targetBlogId')
                    || data.entityId == 'user.'
                            + this.filterParamStore.getFilterParameter('userId')) {
                this.domNode.getElement('img').set('src', data.imageUrl);
            }
        },
        
        onUserProfileChanged: function() {
            this.refresh();
        },
        
        onResponsiveAttributesChanged: function(changedAttributes) {
            if (changedAttributes.indexOf('viewportWidth') > -1 && this.adaptToViewportWidth()) {
                this.refresh();
            }
        },
        
        refreshComplete: function(responseMetadata) {
            var utils;
            var removedEntity = responseMetadata && responseMetadata.entityNotFound;
            if (removedEntity) {
                E('onEntityNotFound', removedEntity);
                // TODO because the utils cannot handle the event we have to notify them manually
                if (removedEntity.type == 'topic') {
                    utils = window.blogUtils;
                    if (utils) {
                        utils.entityChanged(removedEntity.id);
                    }
                } else if (removedEntity.type == 'user') {
                    utils = window.userUtils;
                    if (utils) {
                        utils.entityChanged(removedEntity.id);
                    }
                } else if (removedEntity.type == 'tag') {
                    utils = window.noteTagUtils;
                    if (utils) {
                        utils.entityChanged(removedEntity.id);
                    }
                }
            }
        }

    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('GlobalIdShowBannerImageWidget', GlobalIdShowBannerImageWidget);
    } else {
        window.GlobalIdShowBannerImageWidget = GlobalIdShowBannerImageWidget;
    }
})(window.runtimeNamespace);