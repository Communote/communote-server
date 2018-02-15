// NoteEditorComponent 'virtual' interface declaration
/**
 * Interface for components which provide additional features for the note editor widget.
 * Implementations can use the communote.NoteEditorComponentFactory instance to register themselves
 * for certain editor actions/modes. The ComponentManager which is instantiated by the note editor
 * widget uses the NoteEditorComponentFactory to create the components for the current action/mode.
 * 
 * @interface NoteEditorComponent
 */

/**
 * Append the data managed by this component to an object. This method will typically be called
 * during an offline automatic save. When loading the automatic save the filled object will be
 * passed to initContent. Therefore the data should be added with all details required to initialize
 * this component.
 * 
 * @function
 * @name NoteEditorComponent#appendNoteData
 * @param {Object} noteData - Object to append the data to.
 * @param {boolean} resetDirtyState - whether to reset the dirty state after appending the data.
 */
/**
 * Append the data managed by this component to an object. Since this method is called as part of an
 * online automatic save or a submit of the note which are both done via the Rest API, the data
 * should be added in a way understood by the Rest API.
 * 
 * @function
 * @name NoteEditorComponent#appendNoteDataForRestRequest
 * @param {Object} noteData - Object to append the data to.
 * @param {boolean} resetDirtyState - whether to reset the dirty state after appending the data.
 */
/**
 * Test whether the the note can be published. Will be called whenever the user clicks the send
 * button of the widget. If any component returns false, the note won't be submitted. Components can
 * use this hook to delay the submit until a running asynchronous operation is finished. The widget
 * doesn't show any feedback in this case, so components could do this instead.
 * 
 * @function
 * @name NoteEditorComponent#canPublishNote
 * @return {boolean} whether the note can be published
 */
/**
 * Query the component whether there is unconfirmed input which would get lost when publishing the
 * note. This method is called before sending the note and if a component returns a warning, a
 * confirm dialog is shown asking the user to confirm or cancel the submit.
 * 
 * @function
 * @name NoteEditorComponent#getUnconfirmedInputWarning
 * @return {?Object} null or false if there is no unconfirmed input to warn about or an object
 *         consisting of the two string members message and inputName. The former should hold a
 *         warning message which will be shown if only this component has unconfirmed input. The
 *         inputName should contain a label or name identifying the input and will be used when
 *         several components have unconfirmed input to construct a warning listing the fields.
 */
/**
 * Initializes the data managed by this component. After initializing isDirty and isModified should
 * return false.
 * 
 * @function
 * @name NoteEditorComponent#initContent
 * @param {?Object} noteData - Object with details about the note to initialize with.
 */
/**
 * Test whether the data managed by the component should be considered dirty, that is it has been
 * modified after the last invocation of initContent or after the last time the dirty flag was
 * reset. If it is considered dirty and automatic saving is enabled in the widget, an autosave will
 * be triggered.
 * 
 * @function
 * @name NoteEditorComponent#isDirty
 * @return {boolean} whether the data managed by the component should be considered dirty
 */
/**
 * Test whether the data managed by the component has been modified since the last invocation of
 * initContent.
 * 
 * @function
 * @name NoteEditorComponent#isModified
 * @return {boolean} whether the data managed by the component has been modified
 */

(function() {
    const
    i18n = communote.i18n;

    /**
     * Manager of NoteEditorComponents which implements all methods of such a component and
     * delegates each call to the managed components. This constructor uses the
     * NoteEditorComponentFactory to create all components for the given action / mode.
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

    /**
     * Add an initialized component to the managed ones. In most cases there is no need to call this
     * method because the components are retrieved from the communote.NoteEditorComponentFactory.
     * 
     * @param {NoteEditorComponent} component - the component to add
     */
    ComponentManager.prototype.addComponent = function(component) {
        this.components.push(component);
    };

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

    ComponentManager.prototype.getUnconfirmedInputWarning = function() {
        var i, l, warning;
        var warnings = [];
        var inputNames = [];
        for (i = 0, l = this.components.length; i < l; i++) {
            warning = this.components[i].getUnconfirmedInputWarning();
            if (warning) {
                warnings.push(warning);
                if (warning.inputName) {
                    inputNames.push(warning.inputName);
                }
            }
        }
        if (warnings.length === 0) {
            return null;
        }
        if (warnings.length === 1) {
            if (warnings[0].message) {
                return warnings[0].message;
            }
        }
        return i18n.getMessage('widget.createNote.unconfirmed.input.warning', [ inputNames
                .join(', ') ]);
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