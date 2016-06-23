var UserHoverCard = new Class({

    Extends: HoverCard,

    options: {
        selector: '*[data-cnt-user-id]',
        hoverCardCssClass: 'user-hover-card',
        lazyTipsIdExtractor: 'data-cnt-user-id'
    },

    /**
     * @override
     */
    loadDataCallback: function(id, successCallback, errorCallback) {
        return userUtils.getUser(id, successCallback, errorCallback);
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
                + '<a href="" data-cnt-hovercard-accessor="fillUserImage"><img src="" /></a></div>'
                + '<div class="hovercard-details">'
                + '<div class="hovercard-heading"><a href="" data-cnt-hovercard-accessor="fillSignature"></a></div>'
                + '<div id="userHoverCardPosition" class="hovercard-entry" data-cnt-hovercard-accessor="position"></div>'
                + '<div id="userHoverCardPhone" class="hovercard-entry" data-cnt-hovercard-accessor="fillPhoneNumber"></div>'
                + '</div><span class="cn-clear"></span>'
                + '<div class="hovercard-tags" data-cnt-hovercard-accessor="fillTags"><span class="cn-icon"></span><span class="cn-icon-label"></span></div></div>'
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
                + getJSMessage('hovercard.actions.menu.permalink') + '</a></li>'
                + '</ul></li></ul></div><span class="cn-clear"></span></div>';
        contentElem.set('html', body);
        contentElem.getElement('.hover-card-follow a').addEvent('click', this.followUnfollowUser);
        return contentElem;
    },

    followUnfollowUser: function(event) {
        var elem, id, isFollow;
        if (window.followUtils) {
            elem = document.id(this);
            id = elem.getProperty('data-cnt-follow-id');
            isFollow = elem.getProperty('data-cnt-follow-state');
            function successCallback(message, params, isFollow) {
                // check if hovercard is still showing the same content
                if (elem.getProperty('data-cnt-follow-id') == params.userId) {
                    elem.setProperty('data-cnt-follow-state', isFollow);
                    if(isFollow) {
                        elem.addClass('active');
                    } else {
                        elem.removeClass('active');
                    }
                }
            }
            if (isFollow == 'true') {
                followUtils.unfollowUser(id, successCallback);
            } else {
                followUtils.followUser(id, successCallback);
            }
        }
        return false;
    },

    /**
     * @override
     */
    createTagUrl: function(tag) {
        return userUtils.createTagPermalink(tag);
    },

    fillPermalink: function(elem, userData) {
        var permalink = buildRequestUrl('/portal/users/' + userData.alias);
        elem.setProperty('href', permalink);
        if (this.options.openLinksInNewWindow) {
            elem.setProperty('target', '_blank');
        }
    },

    fillUserImage: function(elem, userData) {
        var url = buildRequestUrl('/image/user.do?id=' + userData.userId
                + '&size=MEDIUM&lastModified=' + userData.profileImageVersion);
        elem.getFirst('img').setProperty('src', url);
        this.fillPermalink(elem, userData);
    },

    fillSignature: function(elem, userData) {
        var signature = userUtils.buildFullUserSignature(userData.salutation, userData.firstName,
                userData.lastName, userData.alias);
        // must set href before content because IE8 and IE7 sometimes override content with href value
        this.fillPermalink(elem, userData);
        elem.set('text', signature);
    },

    fillPhoneNumber: function(elem, userData) {
        var number = userUtils.buildPhoneNumber(userData.phoneCountryCode, userData.phoneAreaCode,
                userData.phoneNumber);
        elem.set('text', number);
    },

    prepareFollowButton: function(elem, userData) {
        var followClass = '';
        if (userData.isFollow === true) {
            followClass = 'active';
        }
        elem.className = 'cn-button-look cn-follow ' + followClass;
        elem.setProperty('data-cnt-follow-id', userData.userId);
        elem.setProperty('data-cnt-follow-state', String(!!userData.isFollow));
    }

});