var TopBlogsWidget = new Class( {
    Extends: C_Widget,

    widgetGroup: 'blog',
    
    getListeningEvents: function() {
        return ['onNotesChanged', 'onBlogUpdate'];
    },

    init: function() {
        this.parent();
        this.copyStaticParameter('numberOfMaxResults');
    },
    
    onNotesChanged: function() {
        this.refresh();
    },
    
    onBlogUpdate: function(blogData) {
        if (blogData && (blogData.newTitle || blogData.newAlias)) {
            this.refresh();
        }
    }
});