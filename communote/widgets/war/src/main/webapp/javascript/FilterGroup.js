var FilterGroup = new Class( /** @lends FilterGroup.prototype */
{
    id: null,
    groupMembers: [],
    filterParameterStore: null,

    /**
     * Create a new FilterGroup with an associated {@link C_FilterParameterStore} and
     * {@link C_FilterEventHandler}.
     * 
     * @param {String} id The identifier of the group.
     * @param {String[]} paramStoreOptions Options to be passed to the filter parameter store.
     * @param {String|Class|FilterEventHandler} eventHandlerClass The name of the associated event
     *            handler class or the class object or the handler instance.
     * @param {Object} [eventHandlerOptions] Options to be passed to the event handler constructor
     * @param {C_FilterEventProcessor} eventProcessor The FilterEventProcessor to which the
     *            created/provided event handler will be registered.
     * @constructs
     * @class A class that provides means for scoping filter events by defining a group with a
     *        unique ID and associating a {@link C_FilterEventHandler} and a
     *        {@link C_FilterParameterStore} to this group. If a filter event is sent to the ID of
     *        the group the handler of this group will be called. The handler will usually update
     *        one or more parameters in the store and inform the members (e.g. widgets) of the group
     *        about the changed parameters. Thus, widgets that share the same filter parameters
     *        should be member of the same group.
     */
    initialize: function(id, paramStoreOptions, eventHandlerClass, eventHandlerArguments,
            eventProcessor) {
        var handler, finalHandlerArgs, dynamicConstructor, typeOfHandlerArg, handlerConstructor;
        var defaultFilteredUnresetableParams;
        this.id = id;
        typeOfHandlerArg = typeOf(eventHandlerClass);
        if (typeOfHandlerArg == 'string') {
            handlerConstructor = window[eventHandlerClass];
            if (!handlerConstructor) {
                throw 'No class found for ' + eventHandlerClass;
            }
            defaultFilteredUnresetableParams = handlerConstructor.defaultFilteredUnresetableParams;
        } else if (typeOfHandlerArg == 'class') {
            handlerConstructor = eventHandlerClass;
            defaultFilteredUnresetableParams = handlerConstructor.defaultFilteredUnresetableParams;
        } else {
            // is already the instance
            handler = eventHandlerClass;
            // a bit ugly using $constructor
            defaultFilteredUnresetableParams = eventHandlerClass.$constructor.defaultFilteredUnresetableParams;
        }
        if (defaultFilteredUnresetableParams) {
            if (paramStoreOptions.filteredUnresetableParams) {
                paramStoreOptions.filteredUnresetableParams.combine(defaultFilteredUnresetableParams);
            } else {
                paramStoreOptions.filteredUnresetableParams = [].append(defaultFilteredUnresetableParams);
            }
        }
        this.filterParameterStore = new C_FilterParameterStore(paramStoreOptions);
        if (handlerConstructor) {
            // null for this in arguments to be passed to bind
            finalHandlerArgs = [ null, this ];
            if (typeOf(eventHandlerArguments) == 'array') {
                finalHandlerArgs.append(eventHandlerArguments);
            } else if (eventHandlerArguments) {
                finalHandlerArgs.push(eventHandlerArguments);
            }
            dynamicConstructor = handlerConstructor.bind
                    .apply(handlerConstructor, finalHandlerArgs);
            handler = new dynamicConstructor();
        } else {
            // just link
            handler.setFilterGroup(this);
        }
        eventProcessor.registerEventHandler(handler);
    },

    /**
     * Add a member to the group and register it as a listener for changes in the filter parameter
     * store of the group.
     * 
     * @param {C_FilterParameterListener} member The member to add.
     */
    addMember: function(member) {
        // member must implement the method that returns the observed parameters
        if (!member.getObservedFilterParameters) {
            return;
        }
        if (this.groupMembers.indexOf(member) == -1) {
            this.groupMembers.push(member);
            this.filterParameterStore.attachListener(member);
        }
    },

    /**
     * Remove a member from the group.
     * 
     * @param {C_FilterParameterListener} member The member to remove.
     */
    removeMember: function(member) {
        var idx = this.groupMembers.indexOf(member);
        if (idx >= 0) {
            this.groupMembers.splice(idx, 1);
            this.filterParameterStore.removeListener(member);
        }
    },

    /**
     * Return the associated filter parameter store.
     * 
     * @return {C_FilterParameterStore} The parameter store.
     */
    getParameterStore: function() {
        return this.filterParameterStore;
    }
});