(function(namespace) {
    var localNamespace, debouncedResizeHandlers, resizeDebounceTimeoutId;

    /**
     * Perform a set like compare of two arrays (i.e. position is ignored) and returns whether they
     * have the same content. The operation is case and type sensitive and is only suitable for
     * arrays containing the simple types Number, String and Boolean.
     * 
     * @param {Array} array1 One of the arrays to compare.
     * @param {Array} array2 One of the arrays to compare.
     * @return {Boolean} whether the content is the same
     */
    function compareArraysIgnorePosition(array1, array2) {
        var i;
        var equal = array1.length === array2.length;
        for (i = 0; equal && i < array1.length; i++) {
            if (!array2.contains(array1[i])) {
                equal = false;
            }
        }
        return equal;
    }

    function compareParameterValues(value1, value2) {
        var typeOfValue1 = typeOf(value1);
        var typeOfValue2 = typeOf(value2);
        if (isEmpty(value1, typeOfValue1)) {
            return isEmpty(value2, typeOfValue2);
        } else if (isEmpty(value2, typeOfValue2)) {
            return false;
        }
        if (typeOfValue1 == 'array') {
            if (typeOfValue2 == 'array') {
                return compareArraysIgnorePosition(value1, value2);
            }
            // treat array with one value equal to that value, because a join(',') would lead to
            // the same parameter string
            if (value1.length > 1) {
                return false;
            }
            value1 = value1[0];
            typeOfValue1 = typeOf(value1);
        } else if (typeOfValue2 == 'array') {
            if (value2.length > 1) {
                return false;
            }
            value2 = value2[0];
            typeOfValue2 = typeOf(value2);
        }
        // if one value is string, compare as string
        if (typeOfValue1 == 'string' && typeOfValue2 != 'string') {
            return value1 === value2.toString();
        } else if (typeOfValue2 == 'string' && typeOfValue1 != 'string') {
            return value1.toString() === value2;
        }
        return value1 === value2;
    }

    function invokeResizeHandlers() {
        var i, l;
        for (i = 0, l = debouncedResizeHandlers.length; i < l; i++) {
            debouncedResizeHandlers[i].call();
        }
    }

    function invokeResizeHandlersDebounced() {
        if (resizeDebounceTimeoutId) {
            clearTimeout(resizeDebounceTimeoutId);
        }
        resizeDebounceTimeoutId = setTimeout(invokeResizeHandlers,
                localNamespace.resizeDebounceTimeout);
    }

    /**
     * Test whether a value is empty. A value is considered empty if it is undefined/null, an empty
     * array or the empty string.
     * 
     * @param {String|Number|Boolean|Array} value The value to test
     * @param {String} [typeOfValue] The type of the value as returned by mootools typeOf function.
     * @return {Boolean} true if the value is empty, false otherwise
     */
    function isEmpty(value, typeOfValue) {
        if (value == null) {
            return true;
        }
        if (!typeOfValue) {
            typeOfValue = typeOf(value);
        }
        return (typeOfValue == 'array' || typeOfValue == 'string') && value.length === 0;
    }

    if (!namespace) {
        namespace = window;
    }
    if (!namespace.utils) {
        namespace.utils = {};
    }

    localNamespace = namespace.utils;
    localNamespace.resizeDebounceTimeout = 100;

    /**
     * Add a function to be called after the browser window got resized. Since the resize event is
     * triggered with a high frequency during resizing, the calling of the handler is
     * debounced/throttled. Debouncing is achieved by delaying the execution of the handler by some
     * milliseconds (communote.utils.resizeDebounceTimeout). If in that time another resize event
     * occurs the previous one will be ignored and the handling of that new one is delayed again.
     * 
     * To remove a handler later on call removeDebouncedResizeEventHandler.
     * 
     * @param {Function} handler The handler to add
     */
    localNamespace.addDebouncedResizeEventHandler = function(handler) {
        if (!debouncedResizeHandlers) {
            debouncedResizeHandlers = [];
            window.addEvent('resize', invokeResizeHandlersDebounced);
            debouncedResizeHandlers.push(handler);
        } else {
            if (debouncedResizeHandlers.indexOf(handler) === -1) {
                debouncedResizeHandlers.push(handler);
            }
        }
    };

    /**
     * Compare two objects containing filter parameters and return whether they are equal. This
     * operation is case and type sensitive with the exception of comparing non-array values which
     * will be compared as strings if one of the values is a string. Additionally empty arrays,
     * empty strings and unset values will be considered equal. For performance reasons the objects
     * are expected to not contain object values. If only one of the objects is undefined the return
     * value will be false.
     * 
     * @param {Object} params1 An object containing filter parameters. The values are expected to be
     *            strings, numbers, booleans or arrays of these types.
     * @param {Object} params2 An object containing filter parameters. The values are expected to be
     *            strings, numbers, booleans or arrays of these types.
     * @return {Boolean} true if the objects are equal, false otherwise
     */
    localNamespace.compareFilterParameters = function(params1, params2) {
        var paramName;
        if (params1 == params2) {
            return true;
        }
        if (!params1 || !params2) {
            return false;
        }
        for (paramName in params1) {
            if (!params1.hasOwnProperty(paramName)) {
                continue;
            }
            if (!compareParameterValues(params1[paramName], params2[paramName])) {
                return false
            }
        }
        // check all parameters of params2 not in params1
        for (paramName in params2) {
            if (!params2.hasOwnProperty(paramName) || params1[paramName] != undefined) {
                continue;
            }
            if (!compareParameterValues(params2[paramName], undefined)) {
                return false
            }
        }
        return true;
    },

    /**
     * Create a diff from 2 arrays of primitive elements. The method is type and case sensitive.
     * 
     * @param {Number[]|String[]} arr1 The first array to compare with the other array
     * @param {Number[]|String[]} arr2 The first array to compare with the other array
     * @return {Object} an object with the members 'both' that holds all items that are in both
     *         arrays, 'added' that holds all items which are only in arr2 and 'removed' holds all
     *         items which are only in arr1. These members are all arrays and can be empty.
     */
    localNamespace.createDiff = function(arr1, arr2) {
        var i, added, both, removed, idx;
        var result = {
            added: [],
            both: [],
            removed: []
        };
        // shortcuts
        if (!arr1 || arr1.length == 0) {
            if (arr2) {
                result.added.append(arr2);
            }
            return result;
        }
        if (!arr2 || arr2.length == 0) {
            result.removed.append(arr1);
            return result;
        }
        // add all from arr2 to added and remove the common elements in the next step
        added = result.added = arr2.clone();
        both = result.both;
        removed = result.removed;
        // copy common elements, remove from added and save missing in removed
        for (i = 0; i < arr1.length; i++) {
            idx = added.indexOf(arr1[i]);
            if (idx > -1) {
                both.push(arr1[i]);
                added.splice(idx, 1);
            } else {
                removed.push(arr1[i]);
            }
        }
        return result;
    };

    localNamespace.eventPreventDefault = function(event) {
        if (!event.$extended) {
            event = new DOMEvent(event);
        }
        event.preventDefault();
    },

    localNamespace.eventStop = function(event) {
        if (!event.$extended) {
            event = new DOMEvent(event);
        }
        event.stop();
        event.propagationStopped = true;
    },

    localNamespace.eventStopPropagation = function(event) {
        if (!event.$extended) {
            event = new DOMEvent(event);
        }
        event.stopPropagation();
        event.propagationStopped = true;
    },

    /**
     * Extract the parameter string (query part of the URL) from the current location. If the
     * parameter string contains a targetUrl parameter, this URL will be used to extract the
     * parameter string.
     * 
     * @return {String} the parameter string without leading ? character and not decoded
     */
    localNamespace.extractParameterString = function() {
        var paramString, idx, match;
        paramString = window.location.search;
        match = /(\?|&)targetUrl=([^&]+)(&|$)/.exec(paramString);
        if (match) {
            // page like login: actual param string is in the targetUrl parameter
            paramString = decodeURIComponent(match[2]);
            idx = paramString.indexOf('?');
            if (idx != -1) {
                paramString = paramString.substring(idx + 1);
            } else {
                paramString = '';
            }
        } else {
            if (paramString.length > 0) {
                paramString = paramString.substring(1);
            }
        }
        return paramString;
    },
    /**
     * Get the anchor element that was the target of a click event. This method is especially
     * helpful if the A tag contains other markup like a SPAN since the event target will be that
     * SPAN instead.
     * 
     * @param {Event|DOMEvent} event The click event. It is not checked whether the event is a click
     *            event!
     * @param {Boolean} checkAllParents If true all parents will be checked if on is an A tag,
     *            otherwise only the direct parent is checked, which can be a lot faster in deep
     *            HTML structures and is usually enough.
     * @return {Element} the target of the click or null if the target was not an anchor
     */
    localNamespace.getClickedLinkElement = function(event, checkAllParents) {
        var parent;
        var target = event.target;
        if (target.tagName === 'A') {
            // make mootools element if not a mootools event
            return event.event ? target : document.id(target);
        } else {
            if (checkAllParents) {
                if (!event.event) {
                    // no mootools event
                    target = document.id(target);
                }
                // use our method which is faster than mootools getParent because it stops as soon as a note was found
                parent = localNamespace.getMatchingParentElement(target, 'A');
            } else {
                parent = target.parentNode;
                if (!parent || parent.tagName !== 'A') {
                    parent = null;
                } else {
                    parent = document.id(parent);
                }
            }
        }
        return parent;
    };
    localNamespace.getCommunoteEntityDetailsFromElement = function(element) {
        var entityId;
        var entityDetails = element.getProperty('data-cnt-entity-details');
        if (entityDetails) {
            entityDetails = JSON.decode(entityDetails);
            // add additional details for the different entities
            if (entityDetails.tagName) {
                if (entityDetails.tagId == undefined) {
                    entityId = element.getProperty('data-cnt-tag-id');
                    entityDetails.tagId = parseInt(entityId);
                }
            } else if (entityDetails.alias && entityDetails.title) {
                // it's a topic, add ID if missing
                if (entityDetails.topicId == undefined) {
                    entityId = element.getProperty('data-cnt-topic-id');
                    entityDetails.topicId = parseInt(entityId);
                }
            } else if (entityDetails.alias) {
                // it's a user entity
                if (entityDetails.userId == undefined) {
                    entityId = element.getProperty('data-cnt-user-id');
                    entityDetails.userId = parseInt(entityId);
                }
                // add legacy shortName and longName
                entityDetails.shortName = userUtils.buildShortUserSignature(
                        entityDetails.firstName, entityDetails.lastName, entityDetails.alias);
                entityDetails.longName = userUtils.buildFullUserSignature(null,
                        entityDetails.firstName, entityDetails.lastName, entityDetails.alias);
            } else if (entityDetails.noteTitle) {
                // it's a note entity
                if (!entityDetails.noteId) {
                    entityId = element.getProperty('data-cnt-note-id');
                    entityDetails.noteId = parseInt(entityId);
                }
            }
        }
        return entityDetails;
    };
    /**
     * Return the provided node if it matches the given selector or a the first parent of the node
     * matching the given selector.
     * 
     * @param {Element} node The node which should itself or whose parents should be tested with the
     *            selector
     * @param {String} selector The CSS3 selector to test with. For performance reasons this should
     *            be a simple selector (like class, id or name of the element)
     * @param {Element} [topMostNode] If provided this element is the last parent node to test, if
     *            missing the method walks up to the document
     * @return {Element} the found element or null
     */
    localNamespace.getMatchingParentElement = function(node, selector, topMostNode) {
        var documentElement = node.ownerDocument;
        var toMooElem = document.id;
        while (node) {
            // stop on document
            if (node === documentElement) {
                break;
            }
            if (node.match(selector)) {
                return node;
            }
            if (node === topMostNode) {
                break;
            }
            node = toMooElem(node.parentNode);
        }
        return null;
    };

    /**
     * Convert a query string into an object containing the parameters as key value pairs. The keys
     * can contain the [] like in 'key[]=1&key[]=2' or 'key[1]=1&key[0]=2' or 'key[a]=1&key[b]=2'.
     * In these cases the resulting object will contain array or object as value for the key. For
     * the examples it would be {key:['1','2']}, {key:['2','1']} and {key: {a:'1', b:'2'}}. The
     * values in the resulting object are always strings (or an array of strings or an object with
     * string values).
     * 
     * @param {String} paramString The parameter string which can be URL encoded and must not
     *            contain the leading '?'
     * @param {String} [multikeyHandling] Defines how to handle re-occurring keys, since this is not
     *            standardized. Can have the values 'array' to create an array from all values,
     *            'last' to override any previous value or 'first' to keep the first found value.
     *            The latter is the default.
     * @return {Object} key value mapping for the parameters
     */
    localNamespace.getObjectFromQueryString = function(paramString, multikeyHandling) {
        var keyValues, keyValueSplitted, i, l, paramName, vaule, arrayKeyRegex, regexResult, idxStr, idx;
        var result = {};
        if (paramString) {
            // match keys like abc[] or abc[0]
            arrayKeyRegex = /(.+)(\[[^\]]*\])/;
            // + is not handled by decodeURIComponent
            paramString = paramString.replace(/\+/g, ' ');
            keyValues = paramString.split('&');
            for (i = 0, l = keyValues.length; i < l; i++) {
                paramName = '';
                keyValueSplitted = keyValues[i].split('=');
                if (keyValueSplitted.length == 2) {
                    paramName = decodeURIComponent(keyValueSplitted[0]);
                    value = decodeURIComponent(keyValueSplitted[1]);
                } else if (keyValueSplitted.length == 1) {
                    paramName = decodeURIComponent(keyValueSplitted[0]);
                    value = '';
                } else if (keyValueSplitted.length == 0) {
                    paramName = decodeURIComponent(keyValues[i]);
                    value = '';
                }
                if (paramName) {
                    regexResult = arrayKeyRegex.exec(paramName);
                    if (regexResult) {
                        // first group is param name, second the []-syntax string
                        paramName = regexResult[1];
                        if (regexResult[2] === '[]') {
                            // create new array or add to exisiting
                            result[paramName] = Array.from(result[paramName]);
                            result[paramName].push(value);
                        } else {
                            idxStr = regexResult[2].slice(1, -1);
                            idx = Number(idxStr);
                            if (isNaN(idx)) {
                                // object identifier like in parameter name abc[member1]
                                if (!result[paramName]) {
                                    result[paramName] = {};
                                }
                                result[paramName][idxStr] = value;
                            } else {
                                // array index
                                if (!result[paramName]) {
                                    result[paramName] = [];
                                }
                                result[paramName][idx] = value;
                            }
                        }
                    } else {
                        // parameter without []
                        if (result[paramName]) {
                            if (multikeyHandling === 'array') {
                                result[paramName] = Array.from(result[paramName]);
                                result[paramName].push(value);
                            } else if (multikeyHandling === 'last') {
                                result[paramName] = value;
                            }
                            // default behavior is to keep the first found value
                        } else {
                            result[paramName] = value;
                        }
                    }
                }
            }
        }
        return result;
    };

    /**
     * Get the text currently selected in the given document.
     * 
     * @param {Document} document The document whose selected text is to be retrieve
     * @return {String} the selected text, will be an empty string if nothing is selected
     */
    localNamespace.getSelectedText = function(document) {
        var curWindow, selText;
        if (!document) {
            document = window.document;
            curWindow = window;
        } else {
            curWindow = document.window;
        }
        if (curWindow.getSelection) {
            selText = getSelection().toString();
        } else if (document.getSelection) {
            selText = document.getSelection().toString();
        } else if (document.selection) {
            selText = document.selection.createRange().text;
        } else {
            selText = '';
        }
        return selText;
    };

    localNamespace.invokeLinkHandlerOnClickEvent = function(event, linkElement) {
        var linkHandler, linkHandlerData;
        // check if the default was prevented, if not call the link handler if the href contains a protocol
        linkHandler = communote.linkHandler;
        if (linkHandler && !localNamespace.isEventDefaultPrevented(event)) {
            if (!linkElement) {
                linkElement = localNamespace.getClickedLinkElement(event, false);
                if (!linkElement) {
                    return;
                }
            }
            if (linkElement.href && linkElement.href.indexOf('://')) {
                linkHandlerData = linkElement.getProperty('data-cnt-linkHandler-data');
                try {
                    if (!linkHandlerData) {
                        // check for data describing a Communote entity like a tag or topic
                        linkHandlerData = localNamespace
                                .getCommunoteEntityDetailsFromElement(linkElement);
                    } else {
                        linkHandlerData = JSON.decode(linkHandlerData);
                    }
                } catch (e) {
                    linkHandlerData = undefined;
                }
                linkHandler.open(linkElement.href, linkHandlerData);
                localNamespace.eventPreventDefault(event);
            }
        }
    };
    /**
     * Return if eventPreventDefault was called on an event.
     * 
     * @param {DOMEvent} event The event to test
     * @return true if preventDefault was called
     */
    localNamespace.isEventDefaultPrevented = function(event) {
        return event.event.defaultPrevented != undefined ? event.event.defaultPrevented
                : event.event.returnValue === false;
    };
    /**
     * Return if eventStopPropagation was called on the event.
     * 
     * @param {DOMEvent} event The event to test
     * @return true if eventStopPropagation was called
     */
    localNamespace.isEventPropagationStopped = function(event) {
        return event.propagationStopped;
    };

    /**
     * Load a JavaScript file by adding a SCRIPT element to the HEAD.
     * 
     * @param {String} url The URL to the script
     * @param {Function} [callback] Function to be called after the script was loaded
     * @return {Object} object with a cancel method to cancel the loading
     */
    localNamespace.loadJavaScriptFile = function(url, successCallback) {
        var handle = {};
        var head = document.head || document.getElementsByTagName('head')[0]
                || document.documentElement;
        var script = document.createElement('script');
        script.async = 'async';
        script.src = url;
        script.type = 'text/javascript';
        if (successCallback) {
            // new browser have onload. IE (at least 9) has both, but onload fires before script is parsed. So prefer onreadystate change.
            if ('onreadystatechange' in script) {
                script.onreadystatechange = function() {
                    if (this.readyState === 'loaded' || this.readyState === 'complete') {
                        this.onreadystatechange = null;
                        successCallback.call();
                    }
                };
            } else {
                script.onload = function(event) {
                    successCallback.call();
                    event.target.onload = null;
                };
            }
            // cannot really cancel, cancel will just avoid the execution of the callback
            handle.cancel = function() {
                if ('onreadystatechange' in this) {
                    this.onreadystatechange = null;
                } else {
                    this.onload = null;
                }
            }.bind(script);
        } else {
            handle.cancel = function() {
            };
        }
        head.appendChild(script);
        // avoid memory leaks
        script = head = undefined;
        return handle;
    };
    /**
     * Remove all items from array 1 which are also in array 2. The method is type and case
     * sensitive and expects that the items in the arrays are primitives.
     * 
     * @param {Number[]|String[]} arr1 The array to remove from
     * @param {Number[]|String[]} arr2 The array with the elements to remove
     * @return {Number[]|String[]} arr1
     */
    localNamespace.removeAllFromArray = function(arr1, arr2) {
        var i, l;
        // shortcut
        if (!arr1 || arr1.length == 0 || !arr2 || arr2.length == 0) {
            return arr1;
        }
        i = 0;
        l = arr1.length;
        while (i < l) {
            if (arr2.indexOf(arr1[i]) > -1) {
                arr1.splice(i, 1);
                l--;
            } else {
                i++;
            }
        }
        return arr1;
    };
    /**
     * Removes a debounced resize event handler that was previously added via a call to
     * addDebouncedResizeEventHandler.
     * 
     * @param {Function} handler The handler to remove. This must be the same function instance as
     *            passed to addDebouncedResizeEventHandler.
     */
    localNamespace.removeDebouncedResizeEventHandler = function(handler) {
        var i, l;
        for (i = 0, l = debouncedResizeHandlers.length; i < l; i++) {
            if (debouncedResizeHandlers[i] === handler) {
                debouncedResizeHandlers.splice(i, 1);
                break;
            }
        }
        if (!debouncedResizeHandlers.length) {
            debouncedResizeHandlers = null;
            window.removeEvent('resize', invokeResizeHandlersDebounced);
        }
    };

    /**
     * Selects the text of the element with the given id. The text can then be copied with Ctrl+C by
     * the user.
     * 
     * @param {String} containerId ID of the element to select the text for
     */
    localNamespace.selectTextOfElement = function(containerid) {
        var range, selection;
        var doc = document;
        var elem = doc.getElementById(containerid);
        if (elem) {
            if (doc.selection) {
                range = doc.body.createTextRange();
                range.moveToElementText(elem);
                range.select();
            } else if (window.getSelection) {
                range = doc.createRange();
                range.selectNode(elem);
                selection = getSelection();
                selection.removeAllRanges();
                selection.addRange(range);
            }
        }
    };

})(window.runtimeNamespace);
