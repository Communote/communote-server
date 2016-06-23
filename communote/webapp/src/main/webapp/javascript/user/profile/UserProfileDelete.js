var UserProfileDeleteWidget = new Class( {
    Extends: C_Widget,

    widgetGroup: "user/profile",

    deleteUserAccount : function() {
        var url = buildRequestUrl("/user/profile/deleteAccount.do");

        var mode = this.domNode.getElement('input[name=deleteMode]:checked');
        
        if(mode == null) {
            mode = this.domNode.getElement('input[name=deleteMode]').get('value');
        } else {
            mode = mode.get('value');
        }
        
        var a = new Request.JSON( {
            url: url,
            method: 'post',
            data: 'deleteMode=' + mode
        });
        
        a.addEvent('complete', function(jsonResponse) {

            if (jsonResponse.status == 'ERROR') {
                hideNotification();
                showNotification(NOTIFICATION_BOX_TYPES.error, '', jsonResponse.message, {duration:''});
            } else {
                // reload page
                window.location.href = communote.server.applicationUrl + '/logout';
            }
            
        });
        a.send();
    }
});