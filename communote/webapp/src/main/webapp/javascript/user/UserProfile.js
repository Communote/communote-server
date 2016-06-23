var
    UserProfileWidget = new Class({

    	Extends: TSWidget,

		widgetGroup: "user",


        setup: function() {
        
        	this.setFilterParameter('filter', '');
        },
        
        getListeningEvents: function() {

            return ['onUserClick'];
        },

        onUserClick: function(userId) {
        	this.setFilterParameter('userId', userId);
        	this.refresh();
        }
});

