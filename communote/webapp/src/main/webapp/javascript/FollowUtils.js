(function() {
    var followUtils = function(localUrl) {
        this.url = buildRequestUrl(localUrl);
    };
    
    function invalidateCache(type, id) {
        var utils;
        if (type == 'userId') {
            utils = window.userUtils;
        } else if (type == 'blogId') {
            utils = window.blogUtils;
        } else if (type == 'tag') {
            utils = window.noteTagUtils;
        }
        if (utils) {
            utils.entityChanged(id);
        }
    };
    
    function followButtonClickSuccess(message, params, isFollow) {
        if(isFollow) {
            this.addClass('active');
        } else {
            this.removeClass('active');
        }
    };
    
    function followUnfollowComplete(resultObj, paramName, paramValue, isFollow, successCallback, errorCallback) {
        var message = resultObj.message;
        var defaultAction = true;
        var params = {};
        params[paramName] = paramValue;
        if (resultObj.status == 'OK') {
            if (successCallback) {
                defaultAction = successCallback.call(null, message, params, isFollow) !== false;
            }
            if (defaultAction) {
                // TODO utils should handle the event as soon as they are more generic 
                invalidateCache(paramName, paramValue);
                E(isFollow ? 'onItemFollowed' : 'onItemUnfollowed', params);
            }
        } else {
            if (errorCallback) {
                defaultAction = errorCallback.call(null, message, params, isFollow) !== false;
            }
            if (defaultAction && message) {
                showNotification(NOTIFICATION_BOX_TYPES.error, '', message, {
                    duration : ''
                });
            }
        }
    };
    
    followUtils.prototype.doRequest = function(paramName, paramValue, isFollow, successCallback, errorCallback) {
        var request, data, params = {};
        params[paramName] = paramValue;
        params.action = isFollow ? 'follow' : 'unfollow';
        data = Hash.toQueryString(params);
        request = new Request.JSON( {
            url: this.url,
            method: 'post',
            data: data,
            noCache: true
        });
        request.addEvent('complete', function(resultObj) {
            followUnfollowComplete(resultObj, paramName, paramValue, isFollow, successCallback);
        });
        request.send();
    };

    followUtils.prototype.followUser = function(userId, successCallback, errorCallback) {
        this.doRequest('userId', userId, true, successCallback, errorCallback);
        return false;
    };
    followUtils.prototype.followBlog = function(blogId, successCallback, errorCallback) {
        this.doRequest('blogId', blogId, true, successCallback, errorCallback);
        return false;
    };
    followUtils.prototype.followDiscussion = function(discussionId, successCallback, errorCallback) {
        this.doRequest('discussionId', discussionId, true, successCallback, errorCallback);
        return false;
    };
    followUtils.prototype.followTag = function(tag, successCallback, errorCallback) {
        this.doRequest('tag', tag, true, successCallback, errorCallback);
        return false;
    };

    followUtils.prototype.unfollowUser = function(userId, successCallback, errorCallback) {
        this.doRequest('userId', userId, false, successCallback, errorCallback);
        return false;
    };
    followUtils.prototype.unfollowBlog = function(blogId, successCallback, errorCallback) {
        this.doRequest('blogId', blogId, false, successCallback, errorCallback);
        return false;
    };
    followUtils.prototype.unfollowDiscussion = function(discussionId, successCallback, errorCallback) {
        this.doRequest('discussionId', discussionId, false, successCallback, errorCallback);
        return false;
    };
    followUtils.prototype.unfollowTag = function(tag, successCallback, errorCallback) {
        this.doRequest('tag', tag, false, successCallback, errorCallback);
        return false;
    };

    followUtils.prototype.followAction = function(action, id, successCallback, errorCallback) {
        if (typeof this[action] == 'function') {
            return this[action](id, successCallback, errorCallback);
        } else {
            throw new Error('Unsupported follow action ' + action);
        }
    };
    
    followUtils.prototype.followButtonClicked = function(element, type, id) {
        element = document.id(element);
        if(element.hasClass("active")) {
            if(type == 'Blog') {
                this.unfollowBlog(id, followButtonClickSuccess.bind(element));
            } else if (type == 'Tag') {
                this.unfollowTag(id, followButtonClickSuccess.bind(element));
            } else if (type == 'User') {
                this.unfollowUser(id, followButtonClickSuccess.bind(element));
            }
        } else {
            if(type == 'Blog') {
                this.followBlog(id, followButtonClickSuccess.bind(element));
            } else if (type == 'Tag') {
                this.followTag(id, followButtonClickSuccess.bind(element));
            } else if (type == 'User') {
                this.followUser(id, followButtonClickSuccess.bind(element));
            }
        }
    };
    
    FollowUtils = followUtils;
})();