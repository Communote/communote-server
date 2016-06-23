var AutoRefresher = new Class({

    Implements: Options,

    options: {
        bypassSession: true,
        functionToCall: null,
        timerIncreasingFactor: 1.2,
        timerInMillis: 10000,
        // do not increase anymore if this value is reached or exceeded
        timerUpperLimit: 120000,
        timerUpperLimitReachedCallback: null,
        url: null
    },

    additionalParams: null,
    additionalParamsAsCSV: null,
    checkUpdatesRequest: null,
    filterParams: null,
    scheduleStartTime: null,
    timerId: null,
    timerIncrementCount: 0,
    timerInMillis: null,
    timerLimitReachedCallbackInvoked: false,

    initialize: function(options) {
        this.setOptions(options);
        this.timerInMillis = this.options.timerInMillis;
        this.additionalParams = {
            bypassSession: !!this.options.bypassSession
        };
        this.additionalParamsAsCSV = [];
    },

    check: function(schedule) {
        if (this.options.functionToCall && this.options.url) {
            if (this.checkUpdatesRequest == null) {
                this.checkUpdatesRequest = this.createRequest();
            }
            this.checkUpdatesRequest.get();
            if (schedule) {
                this.scheduleCheck();
            }
        }
    },

    createRequest: function() {
        var i, paramName;
        var parameters = Object.merge({}, this.filterParams, this.additionalParams);
        for (i = 0; i < this.additionalParamsAsCSV.length; i++) {
            paramName = this.additionalParamsAsCSV[i];
            if (parameters[paramName]) {
                parameters[paramName] = parameters[paramName].join(',');
            }
        }
        return new Request.JSON({
            url: buildRequestUrlWithParameters(this.options.url, Object.toQueryString(parameters)),
            onSuccess: this.options.functionToCall,
            link: 'ignore',
            noCache: true
        });
    },

    /**
     * Get the value of a set additional parameter
     * 
     * @param {String} key The name of the parameter to get.
     * @return {String|Number} the value of the parameter or undefine if not set
     */
    getAdditionalParameter: function(key) {
        return this.additionalParams[key];
    },

    getStatistics: function() {
        var stats = {
            timer: this.timerInMillis,
            timerIncrementCount: this.timerIncrementCount,
            timerLimitReached: this.isTimerLimitReached()
        };
        if (this.scheduleStartTime) {
            stats.nextCheck = this.options.timerInMillis
                    * Math.pow(this.options.timerIncreasingFactor, this.timerIncrementCount - 1)
                    - ((new Date().getTime()) - this.scheduleStartTime);
        }
        return stats;
    },

    isTimerLimitReached: function() {
        return this.timerInMillis > this.options.timerUpperLimit;
    },

    /**
     * @return {Boolean} whether the AutoRefresher is polling for updates or was stopped
     */
    isStarted: function() {
        return this.timerId != null;
    },

    /**
     * Mark the auto refresher as dirty so that it will take the current filter and additional
     * parameters into account when doing the next request. In contrast to the restart method this
     * method allows changes to the parameters without resetting the timer.
     */
    markDirty: function() {
        this.checkUpdatesRequest = null;
    },

    /**
     * Convenience function which stops and starts the AutoRefresher. The timer will be reset to the
     * initial value.
     */
    restart: function() {
        this.stop();
        this.start();
    },

    scheduleCheck: function() {
        if (this.timerId != null) {
            clearTimeout(this.timerId);
        }
        this.scheduleStartTime = (new Date()).getTime();
        this.timerId = this.check.delay(this.timerInMillis, this, true);
        // TODO maybe better update timer when request completed?
        this.updateTimer();
    },

    /**
     * Set an additional parameter that should be included when checking for updates. Any parameter
     * set here will overwrite a same-named parameter that was contained in the filterParameters
     * passed to #setFilterParameters. The refresher needs to be restarted or marked dirty for the
     * new parameter to take effect.
     * 
     * @param {String} key The name of the parameter to set.
     * @param {String| Number} value The value to set. If null the parameter will be removed.
     */
    setAdditionalParameter: function(key, value) {
        if (value == null) {
            delete this.additionalParams[key];
            this.additionalParamsAsCSV.erase(key);
        } else {
            this.additionalParams[key] = value;
            if (typeOf(value) == 'array') {
                this.additionalParamsAsCSV.push(key);
            } else {
                this.additionalParamsAsCSV.erase(key);
            }
        }
    },

    /**
     * Set the filter parameters to be used when checking for updates. These are typically the
     * filter parameters of the widget using the auto refresher. The parameters won't take effect
     * until the refresher is restarted or marked dirty.
     * 
     * @param {Object} filterParameters The parameters to be sent when checking for updates
     */
    setFilterParameters: function(filterParameters) {
        this.filterParams = filterParameters;
    },

    /**
     * Start the AutoRefresher if it is not already running.
     * 
     * @param {Boolean} doImmediateCheck Whether to do an immediate check. If the AutoRefresher is
     *            running this argument is ignored.
     * @param {Boolean} resetTimer Whether to reset the timer or continue with the current
     *            incremented value. If the AutoRefresher is running this argument is ignored.
     */
    start: function(doImmediateCheck, resetTimer) {
        if (this.timerId != null) {
            return;
        }
        if (resetTimer) {
            this.scheduleStartTime = null;
            this.timerIncrementCount = 0;
            this.timerInMillis = this.options.timerInMillis;
            this.timerLimitReachedCallbackInvoked = false;
        }
        if (doImmediateCheck) {
            this.check(true);
        } else {
            this.scheduleCheck();
        }
    },

    /**
     * Stop the AutoRefresher if it is running.
     */
    stop: function() {
        if (this.timerId != null) {
            clearTimeout(this.timerId);
            this.timerId = null;
        }
        if (this.checkUpdatesRequest != null) {
            this.checkUpdatesRequest.cancel();
            this.checkUpdatesRequest = null;
        }
    },

    updateTimer: function() {
        if (this.isTimerLimitReached()) {
            if (!this.timerLimitReachedCallbackInvoked
                    && this.options.timerUpperLimitReachedCallback) {
                this.options.timerUpperLimitReachedCallback.call(null, this);
                this.timerLimitReachedCallbackInvoked = true;
            }
            return;
        }
        this.timerIncrementCount++;
        this.timerInMillis = this.timerInMillis * this.options.timerIncreasingFactor;
    }.protect()
});
