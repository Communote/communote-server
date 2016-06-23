var InviteUserToBlogWidget = new Class({
    Extends: C_Widget,

    widgetGroup: 'blog',

    inviteQueryUrlBase: '/web/v1.1.3/blogInviteUser.json',

    init: function() {
        this.copyStaticParameter('blogId');
        this.parent();
    },

    onBlogMemberInviteClick: function() {
        this.startLoadingFeedback();
        var fields = {};
        var fieldCollector = function(element) {
            fields[element.name] = element.value;
        };

        this.domNode.getElements('input').each(fieldCollector);
        this.domNode.getElements('select').each(fieldCollector);

        // add user role field
        fields['role'] = this.domNode.getElement('#blogaccess_invite_user').value;
        fields['blogId'] = this.getStaticParameter('blogId');

        new Request.JSON({
            url: buildRequestUrl(this.inviteQueryUrlBase),
            'onComplete': function(response) {
                if (response.status != 'ERROR') {
                    showNotification(NOTIFICATION_BOX_TYPES.success, null, getJSMessage(
                            'blog.member.management.inviteuser.success', []), null);
                    this.refresh();
                    return;
                }
                if (response.result != null) {

                    // remove existing messages
                    this.domNode.getElements('div.cn-error-marker').removeClass('cn-error');
                    this.domNode.getElements('label.cn-error').each(function(element) {
                        element.set('html', '');
                        element.getParent().removeClass('cn-error');
                    });

                    // iterate error fields
                    for ( var i = 0; i < response.result.length; i++) {

                        var name = response.result[i].name;
                        var message = response.result[i].message;

                        var formElement = this.domNode.getElement('input[name=' + name + ']');
                        if (formElement == null) {

                            formElement = this.domNode.getElement('select[name=' + name + ']');
                        }

                        this.addInviteErrorMessage(formElement.get('id'), message);
                    }
                }

                // show notification if existing
                if (response.message != null && response.message.length > 0) {

                    showNotification(NOTIFICATION_BOX_TYPES.failure, null, response.message, null);
                }
                this.stopLoadingFeedback();
            }.bind(this)
        }).post(fields);
    },

    addInviteErrorMessage: function(elementId, message) {
        this.domNode.getElements('div.cn-error-marker.cn-for-' + elementId).addClass('cn-error');
        var labelContainer = this.domNode.getElement('label.cn-error[for=' + elementId + ']');
        labelContainer.getParent().addClass('cn-error');
        var errorMessage = new Element('span', {
            'class': 'cn-error-message',
            'id': elementId + '.errors',
            'html': message
        });

        errorMessage.inject(labelContainer, 'top');
    }
});
