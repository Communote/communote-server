var SetAnonymousAccessWidget = new Class({

    Extends: C_Widget,

    widgetGroup: 'blog',

    publicAccessQueryUrlBase: '/web/v1.1.3/blogPublicAccess.json',
    editMode: false,

    init: function() {
        this.parent();
        this.copyStaticParameter('blogId');
        this.copyStaticParameter('showEditModeToggle', true);
        this.copyStaticParameter('editMode', false);
        this.editMode = this.getFilterParameter('editMode');
    },

    onBlogPublicAccess: function() {
        var fields = {};
        fields['publicAccess'] = this.domNode.getElement('input[type=checkbox]').checked;
        fields['blogId'] = this.getFilterParameter('blogId');

        new Request.JSON({
            url: buildRequestUrl(this.publicAccessQueryUrlBase),
            'onComplete': function(response) {

                if (response.status == 'ERROR') {
                    if (response.message != null && response.message.length > 0) {

                        showNotification(NOTIFICATION_BOX_TYPES.failure, null, response.message,
                            null);
                    }
                    this.domNode.getElement('#blog-public-access').reset();
                } else {

                    showNotification(NOTIFICATION_BOX_TYPES.success, null, getJSMessage(
                        'blog.member.public.access.update.success', []), null);
                    this.openViewMode('false');
                }

            }.bind(this)
        }).post(fields);

        // always return false for using as onclick-handler
        return false;
    },

    toggleEditMode: function(edit) {
        if (this.editMode != edit) {
            if (!edit && this.getStaticParameter('cancelUrl')) {
                location.href = this.getStaticParameter('cancelUrl');
                return;
            }
            this.editMode = edit;
            this.setFilterParameter('editMode', edit);
            this.refresh();
        }
    }
});
