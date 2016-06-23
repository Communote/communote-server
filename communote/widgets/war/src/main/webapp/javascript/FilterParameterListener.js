/**
 * @class
 * A listener for changed filter parameters. An instance of this class must be attached to a
 * {@link C_FilterParameterStore} to be informed about parameter changes. This class can be
 * used as a mixin.
 */
var C_FilterParameterListener = new Class( /** @lends C_FilterParameterListener.prototype */{

    /**
     * Hook for initializing a strategy to handle a change of a parameter.
     *
     * @param {Object} args Some arguments to be passed to the handler.
     */
    initChangedParamsHandler: function(args) {
    },
    
    /**
     * Called by the associated FilterParameterStore of this listener when some of the observed
     * parameters have changed. This implementation does nothing.
     *
     * @param {String[]} changedParams A collection of the names of the changed parameters 
     */
    filterParametersChanged: function(changedParams) {
    },
    
    /**
     * Return a collection of filter parameter keys/names that should be observed. If one of 
     * these parameters changes in the associated filter parameter store the listener will be
     * informed via a call to {@link #filterParametersChanged}.
     *
     * @return {String[]} The parameter names.
     */
    getObservedFilterParameters: function() {
        return [];
    }
});