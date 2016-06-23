/**
 * Embed Communote by creating an IFrame pointing to the embed view and injecting it into an HTML
 * element.
 */
(function(window) {
    var autoresizeSupported, embedCount;

    if (window.communote && window.communote.embedCommunote) {
        return;
    }

    function extractParamsFromOptions(options, optionsToIgnore) {
        var arrayContains, params, i, type, key, value;
        // helper function to decide whether a value is an array
        var isArray = Array.isArray || function(a) {
            return Object.prototype.toString.call(a) === '[object Array]'
        };

        if (Array.prototype.indexOf) {
            arrayContains = function(a, v) {
                return a.indexOf(v) != -1;
            };
        } else {
            arrayContains = function(a, v) {
                var i, l;
                for (i = 0, l = a.length; i < l; i++) {
                    if (a[i] === v) {
                        return true;
                    }
                }
                return false;
            };
        }
        params = [];
        for (i in options) {
            if (options.hasOwnProperty(i) && !arrayContains(optionsToIgnore, i)) {
                value = options[i];
                type = typeof (value);
                if (type === 'function') {
                    continue;
                }
                key = encodeURIComponent(i);
                // ignore parameter names that are not ANSI
                if (key.length != i.length) {
                    continue;
                }
                if (type === 'object') {
                    // check for arrays
                    if (isArray(value)) {
                        value = encodeURIComponent(value.join(','));
                    } else {
                        continue;
                    }
                } else if (type === 'string') {
                    value = encodeURIComponent(value);
                }
                if (value != null) {
                    params.push(key + '=' + value);
                }
            }
        }
        return params;
    }

    function findElement(selector) {
        var jQuery, match, elems, tagName, i;
        var doc = window.document;
        if (doc.querySelector) {
            try {
                return doc.querySelector(selector);
            } catch (e) {
                // occurs if syntax of the selector is not correct or browser (e.g. IE 8) only supports level 2.1 and CSS 3 selector is used
            }
        }
        // use jQuery if exists
        jQuery = window.jQuery || window.$;
        if (jQuery) {
            return jQuery(selector)[0];
        } else if (!doc.querySelector) {
            // IE < 8. Check for simple ID or class selector.
            match = /.*#([^ ]+)$/.exec(selector);
            if (match) {
                return doc.getElementById(match[1]);
            }
            match = /^\.([^ .]+)$/.exec(selector);
            if (match) {
                // return first element with that class
                return doc.getElementsByClassName(match[1])[0];
            }
            match = /^([A-Za-z]+)\.([^ .]+)$/.exec(selector);
            if (match) {
                tagName = match[1].toLowerCase();
                elems = doc.getElementsByClassName(match[2]);
                for (i = 0; i < elems.length; i++) {
                    if (tagName === elems[i].tagName.toLowerCase()) {
                        return elems[i];
                    }
                }
            }
        }
        return null;
    }

    function handleSetHeightCommand(instanceId, iframeElem, height) {
        iframeElem.style.height = height;
    }

    function normalizeCSSLengthValue(value) {
        var asNumber;
        var type = typeof (value);
        if (type === 'number') {
            value = Math.round(value) + 'px';
        } else if (type === 'string') {
            // if it is just a number append 'px', otherwise return as is
            asNumber = Number(value);
            if (!isNaN(asNumber)) {
                value = Math.round(asNumber) + 'px';
            }
        } else {
            value = '';
        }
        return value;
    }

    // if the messenger is not initialized auto-resize is not supported   
    autoresizeSupported = !!window.communote.messenger;
    embedCount = 0;

    // prepare namespace
    if (!window.communote) {
        window.communote = {};
    }

    /**
     * Embed Communote in another page with the help of an IFrame.
     * 
     * @param {String} selector A CSS-Selector of the element to add the iIFrame to. The selector has
     *            to be a CSS level 2.1 (and not level 3) selector if IE 8 should be supported. In
     *            case support for IE 7 is required, the selector should be a simple ID or class
     *            selector. Alternatively you could add jQuery to your page because it will be used
     *            if available and the selector is not supported by the current browser.
     * @param {Object} options Object with the following supported settings
     * @param {String} options.server Base URL to the Communote server up to, but without,
     *            'microblog'. The protocol part is optional, if non is given the one of the current
     *            location is used.
     * @param {String} [options.communoteId] ID of the enterprise account. If omitted 'global' is
     *            used.
     * @param {Number} [options.topicId] ID of a topic to restrict the notes to those created in
     *            that topic.
     * @param {Number} [options.userId] ID of a user to restrict the notes to those created by that
     *            user.
     * @param {Number|String} [options.tagIds] ID or CSV ID string of tags to restrict the notes to
     *            those tagged with that tag.
     * @param {String|Number} [options.height] height for the IFrame. Can be a number, a CSS length
     *            value with unit or 'auto'. If it is a number it will be interpreted as pixel
     *            value. If 'auto' the IFrame will be resized automatically to the height of the
     *            content if the browser supports postMessage API. If not (IE < 8), the width will
     *            be set to '100%'. If omitted the default is 'auto'.
     * @param {String|Number} [options.minHeight] min-height for the IFrame. Will be added as
     *            min-height to the style attribute of the injected IFrame. In case the value is a
     *            number it will be interpreted as pixel value. In case height is 'auto' and
     *            auto-resizing to the content height is supported the minHeight will be the lower
     *            bound for the auto-resize.
     * @param {String|Number} [options.maxHeight] max-height for the IFrame. Will be added as
     *            max-height to the style attribute of the injected IFrame. In case the value is a
     *            number it will be interpreted as pixel value. In case height is 'auto' and
     *            auto-resizing to the content height is supported the maxHeight will be the upper
     *            bound for the auto-resize.
     * @param {String|Number} [options.width] width for the IFrame. Will be added as width to the
     *            style attribute of the injected IFrame. In case the value is a number it will be
     *            interpreted as pixel value. If omitted '100%' will be used. If set to the empty
     *            string the width style will not be set.
     * @param {String|Number} [options.minWidth] min-width for the IFrame. Will be added as
     *            min-width to the style attribute of the injected IFrame. In case the value is a
     *            number it will be interpreted as pixel value.
     */
    window.communote.embedCommunote = function(selector, options) {
        var communoteSrc, embedContainer, instanceId, iframeElem, params, optionsToIgnore;
        var cssLengthValue, height;

        if (!options && !options.server) {
            throw 'Required option \'server\' is missing.'
        }

        embedContainer = findElement(selector);

        instanceId = 'communote-embed-widget_' + (embedCount++);

        // create iframe
        iframeElem = embedContainer.ownerDocument.createElement('IFRAME');
        iframeElem.setAttribute('frameborder', '0');
        iframeElem.name = 'communote-embed-widget';
        iframeElem.id = instanceId;

        // options to ignore when building param string
        optionsToIgnore = [ 'server', 'propertyFilter', 'height', 'minWidth', 'autoHeight' ];

        // parse dimension options
        cssLengthValue = normalizeCSSLengthValue(options.minWidth);
        if (cssLengthValue) {
            iframeElem.style.minWidth = cssLengthValue;
        }
        if (options.width !== '' && options.width !== false) {
            cssLengthValue = normalizeCSSLengthValue(options.width);
            iframeElem.style.width = cssLengthValue || '100%';
        }
        if (options.height !== '' && options.height !== false) {
            height = options.height || 'auto';
            if (height === 'auto' && !autoresizeSupported) {
                iframeElem.style.height = '100%';
                height = false;
            } else if (height !== 'auto') {
                cssLengthValue = normalizeCSSLengthValue(height);
                if (cssLengthValue) {
                    iframeElem.style.height = cssLengthValue;
                }
            }
        }
        // autoresize case: min and max height are passed to embed view and evaluated there, but only if they have pixel values
        cssLengthValue = normalizeCSSLengthValue(options.minHeight);
        if (cssLengthValue && (height !== 'auto' || cssLengthValue.slice(-2) !== 'px')) {
            iframeElem.style.minHeight = cssLengthValue;
            optionsToIgnore.push('minHeight');
        }
        cssLengthValue = normalizeCSSLengthValue(options.maxHeight);
        if (cssLengthValue && (height !== 'auto' || cssLengthValue.slice(-2) !== 'px')) {
            iframeElem.style.maxHeight = cssLengthValue;
            optionsToIgnore.push('maxHeight');
        }
        
        // build source URL of IFrame
        communoteSrc = '';

        if (options.server.indexOf('://') == -1) {
            communoteSrc += window.location.protocol + '//';
        }

        communoteSrc += options.server;

        if (communoteSrc.charAt(communoteSrc.length - 1) != '/') {
            communoteSrc += '/';
        }
        communoteSrc += 'microblog/';

        if (options.communoteId) {
            communoteSrc += options.communoteId;
            if (communoteSrc.charAt(communoteSrc.length - 1) != '/') {
                communoteSrc += '/';
            }
            optionsToIgnore.push('communoteId');
        } else {
            communoteSrc += 'global/';
        }
        communoteSrc += 'embed';

        params = extractParamsFromOptions(options, optionsToIgnore);
        // add autoHeight parameter
        if (height === 'auto') {
            window.communote.messenger.addMessengerClient(instanceId, communoteSrc);
            window.communote.messenger.addCommandHandler('setHeight', handleSetHeightCommand);
            params.push('autoHeight=true');
            params.push('embedInstanceId=' + instanceId);
            params.push('embedHostUrl='
                    + encodeURIComponent(location.protocol + '//' + location.host));
        } else {
            params.push('autoHeight=false');
        }
        communoteSrc += '?' + params.join('&');

        iframeElem.src = communoteSrc;
        embedContainer.appendChild(iframeElem);
    };
})(this);