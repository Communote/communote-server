var TopicHoverCard = new Class({

    Extends: HoverCard,

    options: {
        selector: '*[data-cnt-topic-id]',
        hoverCardCssClass: 'topic-hover-card',
        lazyTipsIdExtractor: 'data-cnt-topic-id'
    },

    /**
     * @override
     */
    loadDataCallback: function(id, successCallback, errorCallback) {
        return blogUtils.getTopic(id, successCallback, errorCallback);
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
                + '<a href="" data-cnt-hovercard-accessor="fillTopicImage"><img src="" /></a></div>'
                + '<div class="hovercard-details">'
                + '<div class="hovercard-heading"><a href="" data-cnt-hovercard-accessor="fillTitle"></a></div>'
                + '<div id="topicHoverCardDescription" data-cnt-hovercard-accessor="fillDescription"></div>'
                + '</div><span class="cn-clear"></span>'
                + '<div class="hovercard-tags" data-cnt-hovercard-accessor="fillTags"><span class="cn-icon"></span><span class="cn-icon-label"></span></div></div>'
                + '<div class="hovercard-actions"><div class="hover-card-follow">'
                + '<a href="javascript:;" class="cn-button-look" data-cnt-hovercard-accessor="prepareFollowButton">'
                + '<span class="cn-icon"></span>'
                + '<span class="cn-icon-label">'
                + getJSMessage('follow.link.follow')
                + '</span></a></div><div class="hover-card-more">'
                + '<ul class="cn-menu"><li class="cn-more-actions"><a class="cn-button-look" href="javascript:;"><span class="cn-icon-label">'
                + moreAction + ' </span><span class="cn-icon cn-arrow"><!-- &#9660; --></span></a></li>'
                + '<li><ul class="cn-menu-list">'
                + '<li class="cn-email"><a class="cn-link cn-edit" data-cnt-hovercard-accessor="fillEmail" href="" title="'
                + getJSMessage('hovercard.actions.topic.email.tip')
                + '">'
                + getJSMessage('hovercard.actions.topic.email.label')
                + '</a></li>'
                + '<li><a href="" data-cnt-hovercard-accessor="fillPermalink" class="cn-link cn-permalink">'+getJSMessage('hovercard.actions.menu.permalink')+'</a></li>'
                + '</ul></li></ul></div><span class="cn-clear"></span></div>';
        contentElem.set('html', body);
        contentElem.getElement('.hover-card-follow a').addEvent('click', this.followUnfollowTopic);
        return contentElem;
    },

    followUnfollowTopic: function(event) {
        var elem, id, isFollow;
        if (window.followUtils) {
            elem = document.id(this);
            id = elem.getProperty('data-cnt-follow-id');
            isFollow = elem.getProperty('data-cnt-follow-state');
            function successCallback(message, params, isFollow) {
                // check if hovercard is still showing the same content
                if (elem.getProperty('data-cnt-follow-id') == params.blogId) {
                    elem.setProperty('data-cnt-follow-state', isFollow);
                    if(isFollow) {
                        elem.addClass('active');
                    } else {
                        elem.removeClass('active');
                    }
                }
            }
            if (isFollow == 'true') {
                followUtils.unfollowBlog(id, successCallback);
            } else {
                followUtils.followBlog(id, successCallback);
            }
        }
        return false;
    },

    isSingleMailAddressMode: function(email, alias) {
        var idx;
        if (this.singleMailMode == undefined) {
            idx = email.indexOf(alias);
            if (idx != 0) {
                this.singleMailMode = true;
            } else {
                // assume ist's multi-address mode, but only if a previous email address for
                // another topic is not the same
                if (this.previousEmail) {
                    if (this.previousEmail.alias != alias) {
                        this.singleMailMode = this.previousEmail.email == email;
                        return this.singleMailMode;
                    }
                } else {
                    this.previousEmail = {
                        alias: alias,
                        email: email
                    };
                }
                return false;
            }
        }
        return this.singleMailMode;
    },

    /**
     * @override
     */
    createTagUrl: function(tag) {
        return blogUtils.createTagPermalink(tag);
    },
    
    fillTopicImage: function(elem, topicData) {
        elem.getFirst('img').setProperty('src', blogUtils.getTopicProfileImageUrl(topicData));
        this.fillPermalink(elem, topicData);
    },

    fillPermalink: function(elem, topicData) {
        var permalink = buildRequestUrl('/portal/topics/' + topicData.alias);
        elem.setProperty('href', permalink);
        if (this.options.openLinksInNewWindow) {
            elem.setProperty('target', '_blank');
        }
    },

    fillTitle: function(elem, topicData) {
        this.fillPermalink(elem, topicData);
        elem.set('text', topicData.title);
    },

    fillDescription: function(elem, topicData) {
        var description = topicData.description || '';
        if (description.length > 65) {
            description = description.substring(0, 65) + ' ...';
            elem.setProperty('title', topicData.description);
        } else {
            elem.setProperty('title', '');
        }
        elem.set('text', description);
    },

    fillEmail: function(elem, topicData) {
        var email = topicData.topicEmail;
        if (email) {
            if (this.isSingleMailAddressMode(email, topicData.alias)) {
                email += '?subject=[' + topicData.alias + ']';
            }
            elem.setProperty('href', 'mailto:' + email);
        } else {
            elem.getParent('li.cn-email').setStyle('display', 'none');
        }
    },

    prepareFollowButton: function(elem, topicData) {
        var followClass = '';
        if (topicData.isFollow === true) {
            followClass = 'active';
        }
        elem.className = 'cn-button-look cn-follow ' + followClass;
        elem.setProperty('data-cnt-follow-id', topicData.topicId);
        elem.setProperty('data-cnt-follow-state', String(!!topicData.isFollow));
    }

});