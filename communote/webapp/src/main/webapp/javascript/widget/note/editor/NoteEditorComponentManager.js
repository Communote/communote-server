(function() {

    // TODO use jsdoc to define the NoteEditorComponent interface

    /**
     * Manager NoteEditorComponents which implements all methods of such a component and delegates
     * each call to the managed components. This constructor uses the NoteEditorComponentFactory to
     * create all components for the given action / mode.
     * 
     * @param {Widget} noteEditorWidget The note editor widget for which the components should be
     *            created.
     * @param {String} action The action / mode of the note editor widget for which the components
     *            should be created.
     * @param {String} initialRenderStyle The render style of the widget at the moment this
     *            constructor is called.
     * @param {Object} options The options (staticParameters) the widget was initialized with.
     * 
     * @class
     */
    function ComponentManager(noteEditorWidget, action, initialRenderStyle, options) {
        this.components = communote.NoteEditorComponentFactory.create(noteEditorWidget, action,
                initialRenderStyle, options);
    }

    ComponentManager.prototype.appendNoteData = function(noteData, resetDirtyState) {
        var i, l;
        for (i = 0, l = this.components.length; i < l; i++) {
            this.components[i].appendNoteData(noteData, resetDirtyState);
        }
    };

    ComponentManager.prototype.appendNoteDataForRestRequest = function(noteData, publish,
            resetDirtyState) {
        var i, l;
        for (i = 0, l = this.components.length; i < l; i++) {
            this.components[i].appendNoteDataForRestRequest(noteData, publish, resetDirtyState);
        }
    };

    ComponentManager.prototype.canPublishNote = function() {
        var i, l;
        for (i = 0, l = this.components.length; i < l; i++) {
            if (!this.components[i].canPublishNote()) {
                return false;
            }
        }
        return true;
    };
    // TODO better use event widgetRemoving
    ComponentManager.prototype.destroy = function() {
        var i, l;
        for (i = 0, l = this.components.length; i < l; i++) {
            this.components[i].destroy();
        }
    };

    ComponentManager.prototype.initContent = function(noteData) {
        var i, l;
        for (i = 0, l = this.components.length; i < l; i++) {
            this.components[i].initContent(noteData);
        }
    };

    ComponentManager.prototype.isDirty = function() {
        var i, l;
        for (i = 0, l = this.components.length; i < l; i++) {
            if (this.components[i].isDirty()) {
                return true;
            }
        }
        return false;
    };

    ComponentManager.prototype.isModified = function() {
        var i, l;
        for (i = 0, l = this.components.length; i < l; i++) {
            if (this.components[i].isModified()) {
                return true;
            }
        }
        return false;
    };

    communote.classes.NoteEditorComponentManager = ComponentManager;
})();