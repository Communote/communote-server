var AboutUserProfileWidget = new Class( {

    Extends: C_FilterWidget,

    widgetGroup: 'user/profile',
    // selector to access the element containing the text with the follow status
    followTextSelector: '',
    // describes the action when the follow status changed. If true the text with the follow status will
    // be hidden when the user is unfollowed and shown when followed. If false the text will change.
    followTextHideOnUnfollow: false,
    followButtonSelector: '',
    unfollowButtonSelector: '',

    init: function() {
        var hideOnUnfollow, userId, userIdParam;
        this.parent();
        if (this.filterWidgetGroup) {
            // register for userId changes
            this.observedFilterParams.push('userId');
        } else {
            // user ID can also be provided as a setting
            this.copyStaticParameter('userId');
        }
        // TODO is this still valid??
        if (this.getStaticParameter('followTextSelector')) {
            this.followTextSelector = this.getStaticParameter('followTextSelector');
        }
        if (this.getStaticParameter('followButtonSelector')) {
            this.followButtonSelector = this.getStaticParameter('followButtonSelector');
        }
        if (this.getStaticParameter('unfollowButtonSelector')) {
            this.unfollowButtonSelector = this.getStaticParameter('unfollowButtonSelector');
        }
        hideOnUnfollow = this.getStaticParameter('followTextHideOnUnfollow');
        if (hideOnUnfollow && hideOnUnfollow.toLowerCase() === 'true') {
            this.followTextHideOnUnfollow = true;
        }
        this.copyStaticParameter('showFollowAction', true);
    },
    
    getListeningEvents: function() {
        return this.parent().combine(
                ['onItemFollowed', 'onItemUnfollowed']);
    },
    
    onItemFollowed: function(params) {
        this.followStatusChanged(true, params);
    },
    
    onItemUnfollowed: function(params) {
        this.followStatusChanged(false, params);
    },
    getCurrentUserId: function() {
        if (this.filterWidgetGroup) {
            return this.filterParamStore.getFilterParameter('userId');
        } else {
            return this.getFilterParameter('userId');
        }
    },
    
    followStatusChanged: function(follow, params) {
        var textElem, text, btnElem;
        
        // only handle the currently shown user
        if (params.userId != this.getCurrentUserId()) {
            return;
        }
        // modify the follow status text
        if (this.followTextSelector) {
            textElem = this.domNode.getElement(this.followTextSelector);
            if (textElem) {
                if (this.followTextHideOnUnfollow) {
                    // show or hide element
                    textElem.setStyle('display', follow ? '' : 'none');
                } else {
                    // change text
                    text = getJSMessage('user.profile.follow.text.' + (follow ? 'follow' : 'unfollow'));
                    textElem.set('text', text);
                }
            }
        }
        // swap the follow buttons if available
        if (this.followButtonSelector) {
            btnElem = this.domNode.getElement(this.followButtonSelector);
            if (btnElem) {
                // for buttons or inputs with value attribute set the value otherwise the text child
                btnElem.setStyle('display', follow ? 'none' : '');
            }
        }
        if (this.unfollowButtonSelector) {
            btnElem = this.domNode.getElement(this.unfollowButtonSelector);
            if (btnElem) {
                // for buttons or inputs with value attribute set the value otherwise the text child
                btnElem.setStyle('display', follow ? '' : 'none');
            }
        }
    }

});