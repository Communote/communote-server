(function() {
    'use strict';

    const defaultOptions = {
        fileNamePrefix: undefined,
        fileNameDefaultExtension: '',
        preventDefault: true,
        stopPropagation: true,
        typeRegex: undefined
    };

    const binaryPasteSupported = !!(window.ClipboardEvent && window.DataTransferItemList);

    function createBlobDescriptor(file, fileNamePrefix, fileNameSuffix, fileNameDefaultExtension) {
        var name, idx, extension;
        var descriptor = {};
        // File implements the Blob interface
        descriptor.blob = file;
        if (!fileNamePrefix) {
            descriptor.fileName = file.name;
        } else {
            name = fileNamePrefix + fileNameSuffix;
            idx = file.name ? file.name.lastIndexOf('.') : -1;
            if (idx !== -1) {
                extension = file.name.substring(idx + 1);
            } else {
                extension = fileNameDefaultExtension;
            }
            if (extension) {
                name += '.' + extension;
            }
            descriptor.fileName = name;
        }
        return descriptor;
    }
    function getUniqueSuffix(listener) {
        var timestamp, suffix;
        // no prefix, no need to create suffix
        if (!listener.options.fileNamePrefix) {
            return null;
        }
        timestamp = new Date().getTime();
        suffix = '' + timestamp;
        if (listener.lastPasteTimestamp === timestamp) {
            suffix += '_' + listener.lastPasteTimestampCount;
            listener.lastPasteTimestampCount++;
        } else {
            listener.lastPasteTimestamp = timestamp;
            listener.lastPasteTimestampCount = 1;
        }
        return suffix;
    }
    function onPaste(e) {
        var handled = false;
        var items = e.clipboardData && e.clipboardData.items;
        if (items) {
            handled = processFiles.call(this, items);
        }
        if (handled) {
            if (this.options.stopPropagation) {
                e.stopPropagation();
            }
            if (this.options.preventDefault) {
                e.preventDefault();
            }
        }
    }
    /**
     * Check all items in the DataTransfer for being files and having the correct mime type. All
     * found matching files will be passed to the callback function provided to the constructor of
     * the paste listener.
     * 
     * @param {DataTransferItemList} clipboardDataItems - Items extracted from the DataTransfer of
     *            the ClipboardEvent
     * @returns true if at least one item was a file with a matching mime type
     */
    function processFiles(clipboardDataItems) {
        var i, l, item, file;
        var handled = false;
        var files = [];
        for (i = 0, l = clipboardDataItems.length; i < l; i++) {
            item = clipboardDataItems[i];
            if (item.kind === 'file' && isMatchingMimeType(this.options.typeRegex, item.type)) {
                file = item.getAsFile();
                // sometimes (FF esr on linux) the file is null, no idea how to reproduce,
                // ignore but treat as handled
                if (file) {
                    files.push(createBlobDescriptor(file, this.options.fileNamePrefix,
                            getUniqueSuffix(this), this.options.fileNameDefaultExtension));
                }
                handled = true;
            }
        }
        if (files.length) {
            this.callback.call(null, files);
        }
        return handled;
    }

    function isMatchingMimeType(typeRegex, mimeType) {
        if (typeRegex) {
            if (!typeRegex.test(mimeType)) {
                return false;
            }
            typeRegex.lastIndex = 0;
        }
        return true;
    }

    /**
     * Create a listener for the 'paste' event and attach it to the given element. Whenever the
     * paste event is fired and the pasted data is binary the given callback is invoked with the
     * pasted data. If the browser doesn't support processing of pasted binary data, no listener
     * will be attached to the element. Can for instance be used to handle paste of clipboard image
     * data.
     * 
     * @param {(String|Element)} elem - CSS selector of the element or the element to attach to. If
     *            the element is undefined or the selector does not return an element an error is
     *            thrown.
     * @param {Function} callback - the function to call for a paste event with binary data. The
     *            function will be passed an array of objects where each object represents a pasted
     *            binary blob and has the members blob and fileName. The blob holds the data and is
     *            an instance of Blob. The fileName is a string with the name of the blob as
     *            provided by the browser or constructed from the fileNamePrefix option if defined.
     * @param {?Object} options - optional options for customizing the listener
     * @param {?String} options.fileNamePrefix - prefix to use for creating a unique filename for
     *            each pasted binary data. If provided this name will be passed to the callback
     *            instead of the one given by the browser.
     * @param {?String} options.fileNameDefaultExtension - file extension to append to the unique
     *            filename if the name provided by the browser has no extension. Will be ignored if
     *            fileNamePrefix option is not set.
     * @param {?boolean} options.preventDefault - prevent default handling after processing the
     *            paste event. Defaults to true.
     * @param {?boolean} options.stopPropagation - stop propagation after processing the paste
     *            event. Defaults to true.
     * @param {?RegExp} options.typeRegex - regular expression to apply on the mime type of the
     *            pasted data to only handle binary data with a matching mime type. If not given,
     *            all binary data is handled. If a captured paste event has no binary data matching
     *            the regex, the propagation or default handling of the event won't be stopped or
     *            prevented.
     * 
     * @class
     */
    var Listener = function(elem, callback, options) {
        if (typeof elem === 'string') {
            elem = document.querySelector(elem);
        }
        if (!elem || !elem.addEventListener) {
            throw new Error('No element provided for adding the paste event listener');
        }
        // ignore if callback is missing or not supported
        if (callback && binaryPasteSupported) {
            this.lastPasteTimestamp = null;
            this.lastPasteTimestampCount = 0;
            this.callback = callback;
            this.options = Object.assign({}, defaultOptions, options);
            this.boundHandler = onPaste.bind(this);
            this.elem = elem;
            elem.addEventListener('paste', this.boundHandler);
        }
    };

    /**
     * @return {boolean} whether the browser supports handling of paste events with binary data.
     */
    Listener.isPasteSupported = function() {
        return binaryPasteSupported;
    };

    /**
     * Remove the attached paste event listener from the element passed to the constructor.
     */
    Listener.prototype.detach = function() {
        if (this.boundHandler) {
            this.elem.removeEventListener('paste', this.boundHandler);
        }
    };

    communote.classes.BinaryDataPasteListener = Listener;
})();