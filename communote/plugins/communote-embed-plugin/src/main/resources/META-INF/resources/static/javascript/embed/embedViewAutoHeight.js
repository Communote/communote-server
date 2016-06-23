/**
 * If the page is rendered in an IFrame and the Browser supports postMessage API, setup
 * infrastructure to update the height of the IFrame to the height of the content.
 */
(function(window) {
    var namespace, useBodyScrollHeight, lastHeight, actualHeight, scrollbarsShown, orgBodyPositionStyle, i;

    namespace = window.communote.embed;
    // ensure that the messenger exists
    if (!namespace) {
        return;
    } else if (!namespace.messenger) {
        // because of unpredictable load order other scripts might have added some callbacks to run
        // after autoheight is prepared, clean it up
        delete namespace.autoHeightPreparedCallbacks;
        namespace.autoHeightEnabled = false;
        return;
    }
    // check if autoHeight should be disabled and export this configuration for other components
    if (namespace.requestParameters.autoHeight
            && namespace.requestParameters.autoHeight.toLowerCase() === 'false') {
        namespace.autoHeightEnabled = false;
        return;
    }
    namespace.autoHeightEnabled = true;

    if (namespace.maxHeight == undefined && namespace.requestParameters.maxHeight) {
        namespace.maxHeight = parseInt(namespace.requestParameters.maxHeight);
        if (isNaN(namespace.maxHeight) || namespace.maxHeight <= 0) {
            namespace.maxHeight = undefined;
        }
    }

    if (namespace.minHeight == undefined && namespace.requestParameters.minHeight) {
        namespace.minHeight = parseInt(namespace.requestParameters.minHeight);
        if (isNaN(namespace.minHeight) || namespace.minHeight > namespace.maxHeight) {
            namespace.minHeight = undefined;
        }
    }

    if (!namespace.updateIFrameHeight) {
        /**
         * Update the height of the IFrame by sending the setHeight command with the EmbedMessenger
         * to the parent frame. Will be called if the height should be updated. Could be overridden
         * by a custom implementation.
         * 
         * @param {Number} newHeight The new height
         */
        namespace.updateIFrameHeight = function(newHeight) {
            namespace.messenger.sendCommand('setHeight', newHeight + 'px');
        };
    }
    /**
     * Resize the IFrame if the height has changed. Asserts that minHeight and maxHeight are not
     * exceeded.
     */
    namespace.resizeHeight = function() {
        var height, newHeight, showScrollbars;
        var doc = window.document;
        if (useBodyScrollHeight) {
            // when using scroll height of the body top-margins of children with no previous siblings
            // are not considered but increase the height of document since they shift the body downwards
            // downwards. To compensate this we add the boundingClientRect.top of the body. Since
            // this value is relative to the viewport scrolling has to be considered too.
            height = doc.body.scrollHeight;
            // TODO need to correct rounding issues?
            height += Math.round(doc.body.getBoundingClientRect().top);
            // IE 8 does not support pageYOffset but returns correct value for
            // documentElement.scrollTop since a doctype exists
            height += window.pageYOffset != undefined ? window.pageYOffset
                    : doc.documentElement.scrollTop;
        } else {
            height = doc.documentElement.scrollHeight;
        }
        if (height !== lastHeight) {
            if (height !== actualHeight) {
                newHeight = height;
                if (namespace.minHeight != undefined && newHeight < namespace.minHeight) {
                    newHeight = namespace.minHeight;
                }
                if (namespace.maxHeight != undefined && newHeight > namespace.maxHeight) {
                    newHeight = namespace.maxHeight;
                    showScrollbars = true;
                }
                if (newHeight != actualHeight) {
                    namespace.updateIFrameHeight(newHeight);
                    if (showScrollbars && !scrollbarsShown) {
                        doc.body.style.overflowY = 'scroll';
                        doc.body.style.position = orgBodyPositionStyle;
                        scrollbarsShown = true;
                    } else if (!showScrollbars && scrollbarsShown) {
                        doc.body.style.overflowY = 'hidden';
                        // set relative position (see domready handler below for details)
                        doc.body.style.position = 'relative';
                        scrollbarsShown = false;
                    }
                    actualHeight = newHeight;
                }
            }
            lastHeight = height;
        }
    }

    // in chrome body.scrollHeight is not reduced if content height decreases (e.g. note container
    // collapsed), but documentElement.scrollHeight returns the correct values. In FF and IE it's
    // the opposite. In both browsers body.offsetHeight (as used by tinymce resize plugin) does not
    // respect height of absolute positioned elements like the more menu of a note.
    useBodyScrollHeight = window.Browser.name === 'firefox' || window.Browser.name === 'ie';

    // check for callbacks that need to be called after this script did its preparations
    if (namespace.autoHeightPreparedCallbacks) {
        for (i = 0; i < namespace.autoHeightPreparedCallbacks.length; i++) {
            namespace.autoHeightPreparedCallbacks[i].call(null);
        }
        delete namespace.autoHeightPreparedCallbacks;
    }

    window.addEvent('domready', function() {
        var doc = window.document;
        var bodyStyle = doc.body.style;
        // document element must not have 100% width otherwise the height cannot be reduced
        doc.documentElement.style.height = 'auto';
        doc.documentElement.style.minHeight = '0';
        bodyStyle.height = 'auto';
        bodyStyle.minHeight = '0';
        // hide scrollbars by default
        scrollbarsShown = false;
        bodyStyle.overflowY = 'hidden';
        // when overflowing body must be positioned relative otherwise height of absolute positioned
        // child elements (like smoothbox overlay) is not respected and these elements will be clipped
        orgBodyPositionStyle = bodyStyle.position;
        bodyStyle.position = 'relative';
        // cannot use Firefox's MozScrolledAreaChanged event because it only tracks size changes
        // of the document. But when the content size is reduced the document size is not because
        // it is defined by the size of the IFrame. Feels like a bug...
        // Thus, check periodically instead.
        setInterval(namespace.resizeHeight, namespace.checkResizeTimeout || 200);
    });

})(this);