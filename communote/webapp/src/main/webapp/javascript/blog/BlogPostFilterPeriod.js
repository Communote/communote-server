var BlogPostFilterPeriodWidget = new Class({
    Extends: C_FilterWidget,
    Implements: FilterParamsHandlerStrategyByName,

    widgetGroup: "blog",

    startCalendarId: false,
    endCalendarId: false,
    startCalendar: null,
    endCalendar: null,

    /**
     * whether the end date should work as an inclusive filter, that is also notes created on the
     * end date should be retrieved. It's true by default. If another behavior is required define an
     * appropriate static parameter.
     */
    isEndDateInclusive: true,

    dateFormat: 'm/d/y',

    /**
     * {integer} holds the selected start date as timestamp
     */
    startDate: null,
    /**
     * {integer} holds the selected end date as timestamp
     */
    endDate: null,

    observedFilterParams: [ 'startDate', 'endDate' ],

    init: function() {
        this.parent();
        this.startCalendarId = this.widgetId + '_period_filter_start';
        this.endCalendarId = this.widgetId + '_period_filter_end'; 
    },
    
    /**
     * @override
     */
    beforeRemove: function() {
        this.cleanup();
        this.parent();
    },
    cleanup: function() {
        if (this.startCalendar) {
            this.startCalendar.destroy();
        }
        if (this.endCalendar) {
            this.endCalendar.destroy();
        }
    },
    /**
     * Implementation of the default action required by the FilterParamsHandlerStrategyByName.
     */
    handleChangedParamsDefaultAction: function(changedParam) {
        this.refresh();
    },

    startDateChanged: function(data) {
        var newStartDate;
        var newStartDateAsTimestamp = this.filterParamStore.getFilterParameter('startDate');
        var oldStartDateAsTimestamp = this.startDate && this.startDate.getTime();
        if (newStartDateAsTimestamp != oldStartDateAsTimestamp && this.startCalendar) {
            newStartDate = newStartDateAsTimestamp != null ? new Date(newStartDateAsTimestamp)
                    .increment('ms', communote.currentUser.timeZoneOffset) : null;
            this.startDate = newStartDate;
            if (this.endCalendar) {
                this.endCalendar.options.minDate = newStartDate;
            }
            if (newStartDate != null) {
                this.domNode.getElementById(this.startCalendarId).set('value',
                        newStartDate.format(this.startCalendar.options.format)).store(
                        'datepicker:value', newStartDate.strftime());
            } else {
                $(this.startCalendarId).erase('value');
            }
            this.switchRemoveIcon('start', this.startDate != null);
        }
    },

    endDateChanged: function(data) {
        var oldEndDateAsTimestamp, newEndDate;
        var newEndDateAsTimestamp = this.filterParamStore.getFilterParameter('endDate');
        if (newEndDateAsTimestamp && this.isEndDateInclusive) {
            // decrement by one day because the date is to be interpreted as inclusive filter
            newEndDateAsTimestamp -= 86400000;
        }
        oldEndDateAsTimestamp = this.endDate && this.endDate.getTime();
        if (newEndDateAsTimestamp != oldEndDateAsTimestamp && this.endCalendar) {
            newEndDate = newEndDateAsTimestamp != null ? new Date(newEndDateAsTimestamp).increment(
                    'ms', communote.currentUser.timeZoneOffset) : null;
            this.endDate = newEndDate;
            if (this.startCalendar) {
                this.startCalendar.options.maxDate = newEndDate
                        || new Date().increment('ms', communote.currentUser.timeZoneOffset);
            }
            if (newEndDate != null) {
                this.domNode.getElementById(this.endCalendarId).set('value',
                        newEndDate.format(this.endCalendar.options.format)).store(
                        'datepicker:value', newEndDate.strftime());
            } else {
                $(this.endCalendarId).erase('value');
            }
            this.switchRemoveIcon('end', this.endDate != null);
        }
    },

    switchRemoveIcon: function(name, enable) {
        var icon = this.domNode.getElement('.' + name + ' .remove-icon');
        if (icon != null) {
            if (enable) {
                icon.setStyle('display', '');
            } else {
                icon.setStyle('display', 'none');
            }
        }
    },

    refreshComplete: function(responseMetadata) {
        var currentUser = communote.currentUser;
        var maxDate = new Date().increment('ms', currentUser.timeZoneOffset);
        this.dateFormat = getJSMessage('blog.filter.period.dateformat.picker');

        this.startCalendar = new Picker.Date(this.startCalendarId, {
            positionOffset: {
                y: 5
            },
            useFadeInOut: (Browser.name !== 'ie'),
            days_abbr: localizedDateFormatter.options.days,
            months_abbr: localizedDateFormatter.options.months,
            onSelect: this.changeStartDate.bind(this),
            maxDate: maxDate,
            timezoneOffsetInMillis: currentUser.timeZoneOffset,
            format: this.dateFormat,
            weeknumbers: true
        });
        // hide to avoid IE8 JavaScript error when destroying picker before showing it
        this.startCalendar.hide();

        this.endCalendar = new Picker.Date(this.endCalendarId, {
            positionOffset: {
                y: 5
            },
            days_abbr: localizedDateFormatter.options.days,
            months_abbr: localizedDateFormatter.options.months,
            useFadeInOut: (Browser.name !== 'ie'),
            onSelect: this.changeEndDate.bind(this),
            maxDate: maxDate,
            timezoneOffsetInMillis: currentUser.timeZoneOffset,
            format: this.dateFormat,
            weeknumbers: true
        });
        this.endCalendar.hide();

        if (this.startDate != null) {
            var dateObj = this.startDate != null ? new Date(this.startDate) : null;
            this.switchRemoveIcon('start', dateObj != null);
            $('startCalendarId').set('value', dateObj.toString());
        }
        if (this.endDate != null) {
            var dateObj = this.endDate != null ? new Date(this.endDate) : null;
            this.switchRemoveIcon('end', dateObj);
            $('endCalendarId').set('value', dateObj.toString());
        }

    },
    
    /**
     * @override
     */
    refreshStart: function() {
        this.cleanup();
    },

    filterLastNDays: function(nDays) {
        var endDate = new Date().decrement('ms', communote.currentUser.timeZoneOffset);
        var startDate = endDate.clone().decrement('day', nDays);
        this.updateFilter(startDate, endDate);
    },

    filterLastNMonths: function(nMonths) {
        var endDate = new Date().decrement('ms', communote.currentUser.timeZoneOffset);
        var startDate = endDate.clone().decrement('month', nMonths);
        this.updateFilter(startDate, endDate);
    },

    changeStartDate: function(startDate) {
        startDate = startDate != null ? startDate.clone()
                .decrement('ms', communote.currentUser.timeZoneOffset) : null;
        this.domNode.getElementById(this.startCalendarId).blur();
        this.updateFilter(startDate, this.endDate);
    },

    changeEndDate: function(endDate) {
        endDate = endDate != null ? endDate.clone().decrement('ms', communote.currentUser.timeZoneOffset)
                : null;
        this.domNode.getElementById(this.endCalendarId).blur();
        this.updateFilter(this.startDate, endDate);
    },

    /**
     * Fire a filter event to update the date filter.
     * 
     * @param {Date} startDate The start date to set. Should be the current startDate if this value
     *            did not change
     * @param {Date} endDate The end date to set. Should be the current endDate if this value did
     *            not change
     */
    updateFilter: function(startDate, endDate) {
        var eventData = {
            start: startDate && startDate.getTime()
        };
        if (endDate != null) {
            // increment end date by one day for inclusive filtering
            eventData.end = this.isEndDateInclusive ? endDate.getTime() + 86400000 : endDate
                    .getTime();
        } else {
            eventData.end = null;
        }
        this.sendFilterGroupEvent("onSetPeriodFilter", eventData);
    }
});
