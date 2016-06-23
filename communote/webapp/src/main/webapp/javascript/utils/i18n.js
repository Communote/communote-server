(function(window) {
    var i18n = window.communote.i18n;
    if (!i18n) {
        i18n = window.communote.i18n = {};
    }
    // localizedMessages contain the key to localized message mapping in the language of the current user
    if (!i18n.localizedMessages) {
        // TODO probably we should load the messages for the current category via a JSON request, maybe on first access
        i18n.localizedMessages = {};
    }
    /**
     * Return the localized message for the given key in the locale of the current user.
     * 
     * @param {String} key The key of the message to return
     * @param {Array} [args] An optional array of arguments to replace {i} placeholders in the
     *            message string, where i is a positive number representing the index of the item in
     *            the args array
     * @param {String} [fallback] The fallback to return if there is no message for the text. If
     *            null or undefined a fallback will be generated from key: '{{{' + key + '}}}'
     */
    i18n.getMessage = function(key, args, fallback) {
        var text, i;
        var messages = i18n.localizedMessages;
        if (!(key in messages)) {
            text = (fallback == null) ? '{{{' + key + '}}}' : '' + fallback;
        } else {
            text = messages[key];
            if (args) {
                for (i = 0; i < args.length; i++) {
                    text = text.replace("{" + i + "}", args[i]);
                }
            }
        }
        return text;
    };
    /**
     * Return whether there is a message for the given key in the locale of the current user.
     * 
     * @param {String} key The key to test
     * @return {Boolean} true if there is a message, false otherwise
     */
    i18n.hasMessage = function(key) {
        return key in i18n.localizedMessages;
    };
    // TODO remove legacy code
    // expose the old global function
    getJSMessage = i18n.getMessage; 
})(this);
