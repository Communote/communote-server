(function(namespace) {
    var ConditionManager = function() {
        this.conditions = null;
    };

    /**
     * Add a condition. This can be a string or a function. In case a string is passed the so called
     * named condition has to be fulfilled manually by a call to fullfillCondition. In case a
     * function is provided it will be invoked when conditionsFulfilled is called. This function
     * evaluates a condition to decide whether it is fulfilled or not and should return true or
     * false respectively. All added condition callbacks will be invoked in registration order.
     * 
     * @param {Function|String} condition The named condition or condition callback to add.
     */
    ConditionManager.prototype.addCondition = function(condition) {
        var type = typeOf(condition);
        if (!this.conditions) {
            this.conditions = {
                    callbacks: [],
                    named: []
                };
        }
        if (type === 'string') {
            this.conditions.named.push(condition);
        } else if (type === 'function') {
            this.conditions.callbacks.push(condition);
        }
    };

    /**
     * Test whether all added conditions are fulfilled. This will first check the named conditions
     * and after this the callbacks. The latter are tested in registration order. If the first
     * unfulfilled condition is encountered the remaining won't be checked. If one of the
     * callbacks returns that it is fulfilled it will not be called again in later checks for
     * fulfillment.
     * 
     * @return {boolean} True if all conditions are fulfilled, false otherwise
     */
    ConditionManager.prototype.conditionsFulfilled = function() {
        var i, callbacks;
        if (this.conditions) {
            if (this.conditions.named.length > 0) {
                return false;
            }
            callbacks = this.conditions.callbacks;
            // evaluate callbacks in order
            for (i = 0; i < callbacks.length; i++) {
                if (callbacks[i].call(null)) {
                    callbacks.splice(i, 1);
                    i--;
                } else {
                    break;
                }
            }
            return (callbacks.length == 0);
        }
        return true;
    };

    /**
     * Fulfill a named condition that was added with addCondition and a string argument. After calling this method
     * conditionsFulfilled should be invoked to check whether all conditions are now fulfilled.
     * 
     * @param {String} conditionName Name of a condition to mark as fulfilled
     */
    ConditionManager.prototype.fulfillCondition = function(conditionName) {
        var idx;
        if (this.conditions && typeOf(conditionName) === 'string') {
            idx = this.conditions.named.indexOf(conditionName);
            if (idx >= 0) {
                this.conditions.named.splice(idx, 1);
            }
        }
    };
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('ConditionManager', ConditionManager);
    } else {
        window.ConditionManager = ConditionManager;
    }
})(this.runtimeNamespace);