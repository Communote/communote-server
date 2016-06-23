var TagHoverCard = new Class({

    Extends: HoverCard,

    options: {
        selector: '*[data-cnt-tag-id]',
        hoverCardCssClass: 'tag-hover-card',
        lazyTipsIdExtractor: 'data-cnt-tag-id'
    },

    /**
     * @override
     */
    loadDataCallback: function(id, successCallback, errorCallback) {
        return noteTagUtils.getTag(id, successCallback, errorCallback);
    },

    /**
     * @override
     */
    buildCardContent: function() {
        var contentElem = new Element('div', {
            'class': 'hovercard-content-wrapper'
        });
        var moreAction = getJSMessage('common.more');
        var body = '<div class="hovercard-info"><div class="hovercard-image">'
                + '<a href="" data-cnt-hovercard-accessor="fillPermalink"><img src="'
                + buildResourceUrl('/themes/core/images/main/hash-large.png')
                + '" /></a></div>'
                + '<div class="hovercard-details">'
                + '<div><a href="" class="hovercard-heading" data-cnt-hovercard-accessor="fillTagName"></a></div>'
                + '</div><span class="cn-clear"></span></div>'
                + '<div class="hovercard-actions"><div class="hover-card-follow">'
                + '<a href="javascript:;" class="cn-button-look" data-cnt-hovercard-accessor="prepareFollowButton">'
                + '<span class="cn-icon"></span>'
                + '<span class="cn-icon-label">'
                + getJSMessage('follow.link.follow')
                + '</span></a></div>'
                + '<div class="hover-card-more">'
                + '<ul class="cn-menu"><li class="cn-more-actions"><a class="cn-button-look" href="javascript:;"><span class="cn-icon-label">'
                + moreAction
                + ' </span><span class="cn-icon cn-arrow"><!-- &#9660; --></span></a></li>'
                + '<li><ul class="cn-menu-list">'
                + '<li><a href="" data-cnt-hovercard-accessor="fillPermalink" class="cn-link cn-permalink">'
                + getJSMessage('hovercard.actions.menu.permalink')
                + '</a></li>'
                + '</ul></li></ul></div><span class="cn-clear"></span></div>';
        contentElem.set('html', body);
        contentElem.getElement('.hover-card-follow a').addEvent('click', this.followUnfollowTag);
        return contentElem;
    },

    followUnfollowTag: function(event) {
        var elem, id, isFollow;
        if (window.followUtils) {
            elem = document.id(this);
            id = elem.getProperty('data-cnt-follow-id');
            isFollow = elem.getProperty('data-cnt-follow-state');
            function successCallback(message, params, isFollow) {
                // check if hovercard is still showing the same content
                if (elem.getProperty('data-cnt-follow-id') == params.tag) {
                    elem.setProperty('data-cnt-follow-state', isFollow);
                    if(isFollow) {
                        elem.addClass('active');
                    } else {
                        elem.removeClass('active');
                    }
                }
            }
            if (isFollow == 'true') {
                followUtils.unfollowTag(id, successCallback);
            } else {
                followUtils.followTag(id, successCallback);
            }
        }
        return false;
    },

    fillPermalink: function(elem, tagData) {
        elem.setProperty('href', noteTagUtils.createTagPermalink(tagData));
        if (this.options.openLinksInNewWindow) {
            elem.setProperty('target', '_blank');
        }
    },

    fillTagName: function(elem, tagData) {
        elem.set('text', tagData.name);
        this.fillPermalink(elem, tagData)
    },

    prepareFollowButton: function(elem, tagData) {
        var followClass = '';
        if (tagData.isFollow === true) {
            followClass = 'active';
        }
        elem.className = 'cn-button-look cn-follow ' + followClass;
        elem.setProperty('data-cnt-follow-id', tagData.tagId);
        elem.setProperty('data-cnt-follow-state', String(!!tagData.isFollow));
    }

});