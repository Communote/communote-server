(function() {
    var DEFAULT_ACTIONS = [ 'create', 'repost', 'edit', 'comment' ];

    function addConstructor(action, constructorFunction, target) {
        var constructors = target[action];
        if (!constructors) {
            constructors = [];
            target[action] = constructors;
        }
        constructors.push(constructorFunction);
    }

    function removeConstructor(constructorFunction, target) {
        var idx;
        if (target) {
            idx = target.indexOf(constructorFunction);
            if (idx > -1) {
                target.splice(idx, 1);
            }
        }
    }

    function ComponentFactory() {
        this.constructors = {};
    }

    /**
     * Create all NoteEditorComponents for the given action by invoking all constructors which were
     * registered for that action.
     * 
     * @param {Widget} noteEditorWidget The note editor widget for which the components should be
     *            created.
     * @param {String} action The action / mode of the note editor widget for which the components
     *            should be created.
     * @param {String} initialRenderStyle The render style of the widget at the moment this method
     *            is called.
     * @param {Object} options The options (staticParameters) the widget was initialized with.
     * 
     * @return the created components
     */
    ComponentFactory.prototype.create = function(noteEditorWidget, action, initialRenderStyle,
            options) {
        var i, l, component, constrFn;
        var components = [];
        var constructors = this.constructors[action];
        if (constructors) {
            for (i = 0, l = constructors.length; i < l; i++) {
                constrFn = constructors[i];
                component = new constrFn(noteEditorWidget, action, initialRenderStyle, options);
                if (component) {
                    components.push(component);
                }
            }
        }
        return components;
    };
    /**
     * Register a constructor of a NoteEditorComponent.
     * 
     * @param {(String|String[])} action The action / mode of the note editor widget for which the
     *            component should be constructed. When '*' is used, the component will be created
     *            when the widget's action is one of the default actions (DEFAULT_ACTIONS).
     * @param {Function} constructorFunction The constructor of the NoteEditorComponent. The
     *            constructor will be invoked with the arguments passed to the create method.
     */
    ComponentFactory.prototype.register = function(action, constructorFunction) {
        var i, l;
        if (action === '*') {
            action = DEFAULT_ACTIONS;
        }
        if (Array.isArray(action)) {
            for (i = 0, l = action.length; i < l; i++) {
                addConstructor(action[i], constructorFunction, this.constructors);
            }
        } else {
            addConstructor(action, constructorFunction, this.constructors);
        }
    };

    /**
     * Unregister a constructor of a NoteEditorComponent.
     * 
     * @param {(?String|String[])} action The actions / modes for which the constructor should be
     *            removed. If omitted, it is removed for all actions.
     * @param {Function} constructorFunction The constructor to remove.
     */
    ComponentFactory.prototype.unregister = function(action, constructorFunction) {
        var i, l, keys;
        if (action == null) {
            action = Object.keys(this.constructors);
        } else if (action === '*') {
            action = DEFAULT_ACTIONS;
        }
        if (Array.isArray(action)) {
            for (i = 0, l = action.length; i < l; i++) {
                removeConstructor(constructorFunction, this.constructors[action[i]]);
            }
        } else {
            removeConstructor(constructorFunction, this.constructors[action]);
        }
    };

    communote.NoteEditorComponentFactory = new ComponentFactory();
})();