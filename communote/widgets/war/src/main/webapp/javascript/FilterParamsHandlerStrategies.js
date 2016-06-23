/**
 * @class
 * <p>Strategy mixin for handling changed filter parameters in a {@link C_FilterParameterListener}.
 * This strategy tests the implementing class for a method named '<param-name>Changed' and calls
 * it if it exists.</p>
 * <p>
 * When implementing this class you need to provide a method named <b>handleChangedParamsDefaultAction</b>
 * which takes the name of the changed parameter (type: String) and is called when there is no
 * method in the implementing class to handle the parameter. Moreover a boolean property named
 * breakAfterDefaultAction can be defined and if true the evaluation of changed parameters stops
 * after the first run of the default action. This is quite useful if the default action is a 
 * refresh.</p>
 */
var FilterParamsHandlerStrategyByName = new Class( /** @lends FilterParamsHandlerStrategyByName.prototype */ {
    
    /**
     * Implementation of {@link C_FilterParameterListener#filterParametersChanged} which calls
     * {@link #handleChangedParamsByName}
     * @param {String[]} changedParams A collection of the names of the changed parameters 
     */
    filterParametersChanged : function(changedParams) {
        this.handleChangedParamsByName(changedParams);
    },
    
    /**
     * Handling strategy for changed parameters which tests for a method named '<param-name>Changed'
     * and calls it if it exists. In case such a method is not defined, the method 
     * {@link #handleChangedParamsDefaultAction} is called.
     * @param {String[]} changedParams A collection of the names of the changed parameters
     */
    handleChangedParamsByName : function(changedParams) {
        var pLen = changedParams.length;
        if (this.breakAfterDefaultAction && pLen > 0) {
            // eval functions first
            var handlerFunctions = [];
            for (var i = 0; i < pLen; i++) {
                var p = changedParams[i];
                var handlerFn = this[p + 'Changed'];
                if (handlerFn && typeOf(handlerFn) == 'function') {
                    handlerFunctions[i] = handlerFn;
                } else {
                    // execute default action and stop
                    this.handleChangedParamsDefaultAction(p);
                    this.allChangedParamsHandlerRun();
                    return;
                }
            }
            // run handler
            for (var i = 0; i < pLen; i++) {
                handlerFunctions[i].call(this);
            }
        } else {
            // eval and call right away
            for (var i = 0; i < pLen; i++) {
                var p = changedParams[i];
                var handlerFn = this[p + 'Changed']; 
                if (handlerFn && typeOf(handlerFn) == 'function') {
                    handlerFn.call(this);
                } else {
                    this.handleChangedParamsDefaultAction(p);
                }
            }
        }
        this.allChangedParamsHandlerRun();
    },
    
    /**
     * Called after all handler functions have been run. Default implementation does nothing.
     */
    allChangedParamsHandlerRun: function() {
    }
    
});
/**
 * @class
 * <p>
 * Strategy mixin for handling changed filter parameters in a FilterWidget. This strategy uses mapping 
 * from parameter name to function names for resolving the handler method to be called. The mapping
 * must be provided within the static parameters in the following format:
 * paramToFunctionMapping=paramName1:functionName1,paramName2:functionName2</p>
 * <p>
 * When implementing this class you need to provide a method named handleChangedParamsDefaultAction
 * which takes the name of the changed parameter (type: String) and is called when there is no
 * method in the implementing class to handle the parameter or no mapping exists for the parameter.
 * Moreover the handling methods defined in the mapping must be implemented.</p>
 * @borrows C_FilterParameterListener#initChangedParamsHandler as #initChangedParamsHandler
 */
var FilterParamsHandlerStrategyByMapping = new Class( /** @lends FilterParamsHandlerStrategyByMapping.prototype */{
    /**
     * Implementation of {@link C_FilterParameterListener#filterParametersChanged} which calls 
     * {@link #handleChangedParamsByMapping}
     *
     * @param {String[]} changedParams A collection of the names of the changed parameters 
     * @override
     */
    filterParametersChanged: function(changedParams) {
        this.handleChangedParamsByMapping(changedParams);
    },
    
    
    /**
     * Implementation of {@link C_FilterParameterListener#initChangedParamsHandler}.
     *
     * @param {String|Object} args The mapping as an object mapping parameter name to function or
     *  as a string in the following format: 'paramName1:functionName1,paramName2:functionName2'
     * @override
     */
    initChangedParamsHandler: function(args) {
        var m = {};
        //var mappingString = this.staticParams.paramToFunctionMapping;
        if (typeOf(args) == 'string') {
            var parts = args.split(',');
            for (var i = 0; i < parts.length; i++) {
                var pf = parts[i].split(':');
                if (pf.length == 2) {
                    pf[0] = pf[0].trim();
                    pf[1] = pf[1].trim();
                    if (pf[0].length > 0 && pf[1].length > 0) {
                        m[pf[0]] = pf[1];
                    }
                }
            }
            
        } else m = args;
        this.param2FunctionMapping = m;
    },
    
    /**
     * Handling strategy for changed parameters which tests for a method defined in a mapping from
     * parameter name to handling function and calls it if it exists. In case such a method is not
     * defined, the method #handleChangedParamsDefaultAction is called.
     * @param {String[]} changedParams 
     *              A collection of the names of the changed parameters
     */
    handleChangedParamsByMapping : function(changedParams) {
        var i, p, handlerFnName, handlerFn;
        for (i = 0; i < changedParams.length; i++) {
            p = changedParams[i];
            handlerFnName = this.param2FunctionMapping[p];
            handlerFn = this[handlerFnName];
            if (handlerFn && typeOf(handlerFn) == 'function') {
                handlerFn.call(this);
            } else {
                this.handleChangedParamsDefaultAction(p);
            }
        }
    }
});