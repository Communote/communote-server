/**
 * @class
 * @augments communote.widget.classes.controllers.Controller
 */
communote.widget.classes.controllers.I18nController = communote.widget.classes.controllers.Controller.extend(
/** 
 * @lends communote.widget.classes.controllers.I18nController.prototype
 */		
{
    name: "I18nController",

    /**
     * @param {Configuration} configuration the Configuration to use
     */
    constructor: function(configuration, apiAccessor) {
        this.configuration = configuration;
        this.dateFormatter = communote.utils.DateFormatter;
        this.apiAccessor = apiAccessor;
        // only supporting one language, which is current user locale or the overridden one of configuration
        this.messages = undefined; 
    },


    /**
     * retrieve the translation
     * 
     * @param key string - keyvalue for the translation table
     * @result success the load operation: true | false
     */
    getText: function(key) {
        var text;
        if (!this.messages) {
            this.load();
        }
        text = this.messages[key];
        if (text === undefined) {
            text = "[" + key + "," + lang + "]";
        }
        return text;
    },
    
    /**
     * returns the text via getText and does some simple {key} -> value with the result (poor man's formatting).
     * Example:
     * 
     * key:   Message Key
     * data:   {0: 'awesome'}
     * result: This is some awesome text
     * 
     */
    getTextFormatted: function(key, data) {

        // do basic text lookup
        var text = this.getText(key);
        
        // replace placeholders
        for (key in data) {
            text = text.replace('{' + key + '}', data[key]);
        }
        
        return text;
    },

    /**
     * load the lang file and cache it
     * 
     * @result bool - success (true) or failed (false)
     */
    load: function() {
        var messages = {};
        var url = this.apiAccessor.buildMessageUrl(this.configuration.lang);
        var successFlag = false;
        communote.jQuery.ajax({
            async: false,
            type: "get",
            url: url,
            cache: true,
            data: {},
            dataType: "json",
            success: function(data, textStatus) {
                successFlag = true;
                messages = data;
            },
            error: function(data, textStatus, thrownError) {
                successFlag = false;
            }
        });
        this.messages = messages;
        if (successFlag) {
            this.dateFormatter.setOptions({
                days: messages["htmlclient.common.dateformat.days"].split("|"),
                months: messages["htmlclient.common.dateformat.months"].split("|"),
                offset: messages["htmlclient.common.dateformat.weekdayOffset"],
                timeSuffixes: messages["htmlclient.common.dateformat.timeSuffixes"].split("|"),
                pattern: messages["htmlclient.common.dateformat.pattern"],
                patternDate: messages["htmlclient.common.dateformat.pattern.date"],
                patternTime: messages["htmlclient.common.dateformat.pattern.time"]
            });
        }
        return successFlag;
    },
    
    /**
     * @method getDate
     * return only the date
     * @param {numeric} datetime - unix timestamp
     * @param {string} format - optional
     * @return {string}
     */
    getDate: function(datetime, format) {
        return this.dateFormatter.getDate(datetime, format);
    },

    /**
     * @method getTime
     * return only the time
     * @param {numeric} datetime - unix timestamp
     * @param {string} format - optional
     * @return {string}
     */
    getTime: function(datetime, format) {
        return this.dateFormatter.getTime(datetime, format);
    },

    /**
     * @method getDateTime
     * return the full date and time
     * @param {numeric} datetime - unix timestamp
     * @param {string} format - optional
     * @return {string}
     */
    getDateTime: function(datetime, format) {
        return this.dateFormatter.getDateTime(datetime, format);
    }
});
