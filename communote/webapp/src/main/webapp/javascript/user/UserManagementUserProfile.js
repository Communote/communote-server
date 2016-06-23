var UserManagementUserProfileWidget = new Class( {

    Extends : C_FormWidget,

    widgetGroup : "management/user",

    extractName : function() {
        var profile = {};
        if (this.domNode.getElementById('userProfile.firstName') != null) {
            profile.firstName = this.domNode.getElementById('userProfile.firstName').value;
        };
        if (this.domNode.getElementById('userProfile.lastName') != null) {
            profile.lastName = this.domNode.getElementById('userProfile.lastName').value;
        };
        if (this.domNode.getElement('input[name=alias]') != null) {
            profile.alias = this.domNode.getElement('input[name=alias]').value;
        };
        profile.userId = this.getFilterParameter('userId');
        return profile;
    },

    refreshStart : function() {
        this.copyStaticParameter('userId');
        this.parent();
    },

    refreshComplete: function(responseMetadata) {
        if (!this.refreshCausedBySubmit) {
            this.oldUserName = this.extractName();
        }
        this.parent(responseMetadata);
    },

    onSubmitSuccess : function() {
        searchAndShowRoarNotification(this.domNode);
        var newName = this.extractName();
        if (newName.firstName != this.oldUserName.firstName
                || newName.lastName != this.oldUserName.lastName) {
            this.oldUserName = newName;
            E2('onUserProfileChanged', null, newName);
        }
    },

    onSubmitFailure : function() {
        searchAndShowRoarNotification(this.domNode);
    }
});