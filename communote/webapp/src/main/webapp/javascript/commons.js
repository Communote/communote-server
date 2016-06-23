function swapShownDivs(divIdToShow, divIdToHide) {
    $(divIdToShow).style.display = 'block';
    $(divIdToHide).style.display = 'none';
}

(function() {
    // predefined button types
    var DIALOG_BUTTON = {
        "ok": "javascript.dialog.button.label.ok",
        "cancel": "javascript.dialog.button.label.cancel",
        "back": "javascript.dialog.button.label.back",
        "next": "javascript.dialog.button.label.next",
        "continue": "javascript.dialog.button.label.continue",
        "abort": "javascript.dialog.button.label.abort",
        "yes": "javascript.dialog.button.label.yes",
        "no": "javascript.dialog.button.label.no"
    };
    
    var runningInIframe = undefined;

    function buildDialogContainer(content, buttons, options) {
        var dialogContainer, contentContainer, contentType, buttonsContainer, i, mainAction;
        var cssClasses = options && options.cssClasses;
        if (cssClasses) {
            cssClasses = 'dialog-container ' + cssClasses;
        } else {
            cssClasses = 'dialog-container';
        }
        dialogContainer = new Element('div', {
            'id': 'dialog-modal',
            'class': cssClasses
        });
        // inject the content element
        contentContainer = new Element('div', {
            'class': 'content-container'
        });
        dialogContainer.grab(contentContainer);
        contentType = typeOf(content);
        // replace single message with paragraph element
        if (contentType === 'string') {
            contentContainer.grab(new Element('p', {
                'text': content,
                'class': 'dialog-paragraph'
            }));
        } else if (contentType == 'array') {
            for (i = 0; i < content.length; i++) {
                contentContainer.grab(new Element('p', {
                    'text': content[i],
                    'class': 'dialog-paragraph'
                }));
            }
        } else {
            // expect content to be an element
            contentContainer.grab(content);
        }
        // append a clearing element
        contentContainer.grab(new Element('span', {
            'class': 'clear',
            'html': '<!-- ie -->'
        }));
        buttonsContainer = new Element('div', {
            'class': 'button-container'
        });
        dialogContainer.grab(buttonsContainer);
        if (buttons) {
            mainAction = parseButtons(buttons, dialogContainer, buttonsContainer);
        }
        return {
            container: dialogContainer,
            mainAction: mainAction
        };
    }

    /**
     * Wrapper that passes the dialog container element to the provided onCloseCallback
     */
    function onCloseCallback(dialogContainer, options) {
        if (options && options.onCloseCallback) {
            options.onCloseCallback.call(null, dialogContainer);
        }
    }
    function onCloseWithWidgetCallback(dialogContainer, widgetId, options) {
        communote.widgetController.removeWidgetById(widgetId);
        onCloseCallback(dialogContainer, options);
    }
    /**
     * Wrapper that passes the dialog container element to the provided onPositionCompleteCallback.
     * Additionally focuses an input if there is one that should be focused.
     */
    function onPositionCompleteCallback(dialogContainer, options, tbWindow) {
        // check for an input element to be focused
        var inputToFocus = dialogContainer.getElement('input[data-cnt-dialog-focus=true]');
        if (inputToFocus) {
            inputToFocus.focus();
        }
        if (options && options.onPositionCompleteCallback) {
            options.onPositionCompleteCallback.call(null, dialogContainer, tbWindow);
        }
    }

    /**
     * Wrapper that passes the dialog container element to the provided onShowCallback
     */
    function onShowCallback(dialogContainer, options, tbWindow) {
        if (options && options.onShowCallback) {
            options.onShowCallback.call(null, dialogContainer, tbWindow);
        }
    }

    function onShowWithWidgetCallback(dialogContainer, widgetContainerSelector, widgetType,
            widgetId, widgetSettings, options, tbWindow) {
        var widgetContainer = dialogContainer.getElement(widgetContainerSelector);
        communote.widgetController.addWidget(widgetContainer, widgetType, widgetId, widgetSettings);
        onShowCallback(dialogContainer, options, tbWindow);
    }

    function parseButtons(buttons, dialogContainer, buttonsContainer) {
        var i, button, buttonType, existingType, label, mainButton, buttonClasses, buttonElem, action, mainAction;
        for (i = 0; i < buttons.length; i++) {
            button = buttons[i];
            buttonType = button.type;
            existingType = DIALOG_BUTTON[buttonType];
            if (existingType) {
                label = getJSMessage(existingType);
            } else {
                // check for label in button definition, if not there create from type
                if (button.label) {
                    label = button.label;
                } else {
                    label = getJSMessage('javascript.dialog.button.common.label.' + buttonType);
                }
            }
            mainButton = button.type == 'main'
                    || (existingType && buttonType == 'ok' || buttonType == 'continue' || buttonType == 'yes');
            buttonClasses = mainButton ? 'cn-inputBtn cn-button main' : 'cn-inputBtn cn-button';
            if (button.cssClass) {
                buttonClasses += ' ' + button.cssClass;
            }
            buttonElem = new Element('input', {
                'type': 'button',
                'name': 'button',
                'class': buttonClasses,
                'value': label
            });
            buttonsContainer.grab(buttonElem, mainButton ? 'top' : 'bottom');
            if (button.action) {
                action = function(button) {
                    if (button.action.call(null, dialogContainer) !== false) {
                        TB_remove(!!button.invokeOnCloseCallback);
                    }
                }.bind(null, button);
            } else {
                action = TB_remove.pass(true);
            }
            buttonElem.addEvent('click', action);
            if (mainButton && !mainAction) {
                mainAction = action;
            }
        }
        return mainAction;
    }

    function widgetRefreshCompleteEventHandler(eventName, details) {
        if (this.widgetId == details.widgetId) {
            // only observing first refresh, so remove listener
            communote.widgetController.unregisterWidgetEventListener('onWidgetRefreshComplete',
                    this);
            if (this.markWidgetLoading) {
                this.dialogContainer.removeClass('widget-loading');
            }
            if (this.onWidgetRefreshCompleteCallback) {
                // augment details with dialogContainer and delegate to callback
                details.dialogContainer = this.dialogContainer;
                this.onWidgetRefreshCompleteCallback.call(null, details);
            }
            TB_position();
        }
    }

    /**
     * Close a previously opened dialog if there is one.
     * 
     * @param {boolean} invokeOnCloseCallback Whether to call the onCloseCallback that was provided
     *            while creating the dialog
     */
    closeDialog = function(invokeOnCloseCallback) {
        TB_remove(invokeOnCloseCallback);
    };

    /**
     * Create a modal dialog that has a title, some contents and a set of buttons.
     * 
     * @param {String} title The title of the dialog
     * @param {String|String[]|Element} content The content of the dialog. Can either be a string
     *            which will be added to a P tag, an array of strings which will be added to
     *            individual P tags or an element that contains the content.
     * @param {Object[]} buttons An array of button definitions. A button definition can contain a
     *            'type', a 'label', an 'action', the 'invokeOnCloseCallback' flag and a 'cssClass'.
     *            The later can contain a CSS class name that should be applied to the button
     *            element. The type can be one of the predefined types 'ok', 'cancel', 'next',
     *            'back', 'continue', 'abort','yes' and 'no'. If any of these types is used the
     *            label attribute will be ignored because there is already a label associated with
     *            each type. If none of these types is set the value of the label attribute will be
     *            used as button label. Additionally there is a special type named 'main' which can
     *            be used to mark a button defined by a label as a main button. A main button
     *            automatically receives the CSS class 'main'. The predefined types 'ok', 'continue'
     *            and 'yes' are also main buttons. The action attribute defines a function to be
     *            called when the button is clicked. If this function does not return false the
     *            popup will be closed automatically after executing the function. If no function is
     *            defined the popup will be closed and the onCloseCallback (see options) will be
     *            invoked if defined. When the 'invokeOnCloseCallback' flag is set to true the
     *            onCloseCallback will also be called if the action function did not return false.
     *            The order of the button definitions will be respected when rendering the buttons.
     * @param {Object} [options] Optional object with options to be passed to the TB_show_popup
     *            function like 'width', 'height', 'onShowCallback' and 'onCloseCallback'.
     *            Additionally the option 'cssClasses' is supported. If set the specified CSS class
     *            names will be applied to the container element that wraps the content and the
     *            buttons. Moreover the option 'triggeringEvent' is supported. This option is
     *            expected to be an Event. If Communote is running in an IFrame and this option is
     *            set to a mouse related event the y-coordinate of the event will be used to
     *            position the popup.
     */
    showDialog = function(title, content, buttons, options) {
        var triggeringEvent;
        var result = buildDialogContainer(content, buttons, options);
        var dialogContainer = result.container;
        popupOptions = Object.merge({
            viewportHorizontalMargin: 20
        }, options);
        popupOptions.title = title;
        popupOptions.content = dialogContainer;
        popupOptions.onShowCallback = onShowCallback.bind(null, dialogContainer, options);
        popupOptions.onCloseCallback = onCloseCallback.bind(null, dialogContainer, options);
        popupOptions.onPositionCompleteCallback = onPositionCompleteCallback.bind(null,
                dialogContainer, options);
        if (runningInIframe == undefined) {
            runningInIframe = window.parent != window;
        }
        if (runningInIframe && popupOptions.triggeringEvent && popupOptions.top == undefined) {
            triggeringEvent = new DOMEvent(popupOptions.triggeringEvent); 
            if (triggeringEvent.page) {
                popupOptions.centerY = triggeringEvent.page.y;
            } 
        }
        if (!popupOptions.onEnterKeyCallback && result.mainAction && popupOptions.mainActionOnEnter) {
            popupOptions.onEnterKeyCallback = result.mainAction;
        }
        TB_create_layer();
        TB_show_popup(popupOptions);
    };
    /**
     * Helper which shows a yes/no confirmation dialog, that runs the acceptAction when the yes
     * button is clicked.
     * 
     * @param {String} title The title of the dialog
     * @param {String|String[]|Element} message The confirm message to show.
     * @param {Function} acceptAction The function to call when the YES button is clicked.
     * @param {Object} [options] Additional settings to be passed to the dialog creation. See
     *            showDialog for details.
     */
    showConfirmDialog = function(title, message, acceptAction, options) {
        var buttons = [];
        buttons.push({
            type: 'yes',
            action: acceptAction
        });
        buttons.push({
            type: 'no'
        });
        showDialog(title, message, buttons, options);
    };

    /**
     * Show a popup dialog that contains a widget
     * 
     * @param {String} title The title of the dialog
     * @param {String} widgetType The type of the widget to create inside the popup
     * @param {Object} [widgetSettings] The settings (staticParams) to configure the widget instance
     * @param {Object} [options] Optional object with options to control the popup creation. The
     *            object can contain the same options as supported by the showDialog function.
     *            Additionally the members 'buttons', 'widgetId', 'widgetWrapperContent',
     *            'widgetContainerSelector', 'markWidgetLoading' and
     *            'onWidgetRefreshCompleteCallback' can be included. The 'buttons' member needs to
     *            be an array as described in the same-named parameter of the showDialog function.
     *            The 'widgetId' allows to specify the ID of the widget, if missing the widgetType
     *            will be used. The 'widgetWrapperContent' allows providing a custom Element
     *            instance to wrap the widget. If this member is set, the string member
     *            'widgetContainerSelector' must also be included and contain a selector to find the
     *            container within the wrapping element into which the widget will be injected. If
     *            the flag 'markWidgetLoading' is true the CSS class 'widget-loading' will be added
     *            to the dialog container until the first refresh of the widget completed. Finally
     *            the function 'onWidgetRefreshCompleteCallback' can be added to provide a callback
     *            that should be called when the first widget refresh completed. The function will
     *            be passed an object containing the dialogContainer element, the widgetId and the
     *            widgetType.
     */
    showDialogWithWidget = function(title, widgetType, widgetSettings, options) {
        var widgetId, content, containerSelector, result, dialogContainer, popupOptions, widgetEventHandler;
        if (!options) {
            options = {};
        }
        widgetId = options.widgetId || widgetType;
        if (options.markWidgetLoading) {
            if (options.cssClasses) {
                options.cssClasses += ' widget-loading'
            } else {
                options.cssClasses = 'widget-loading'
            }
        }
        // only an element is supported as wrapper content and a selector to inject the widget
        // is required
        if (typeOf(options.widgetWrapperContent) == 'element' && options.widgetContainerSelector) {
            content = options.widgetWrapperContent;
            containerSelector = options.widgetContainerSelector;
        } else {
            content = new Element('div', {
                'class': 'dialog-widget-wrapper'
            });
            containerSelector = '.dialog-widget-wrapper';
        }
        result = buildDialogContainer(content, options.buttons, options);
        dialogContainer = result.container;
        // add refresh complete handler to reposition popup, remove loading CSS class and/or call callback
        widgetEventHandler = {};
        widgetEventHandler.markWidgetLoading = options.markWidgetLoading;
        widgetEventHandler.onWidgetRefreshCompleteCallback = options.onWidgetRefreshCompleteCallback;
        widgetEventHandler.widgetId = widgetId;
        widgetEventHandler.dialogContainer = dialogContainer;
        widgetEventHandler.handleEvent = widgetRefreshCompleteEventHandler;
        communote.widgetController.registerWidgetEventListener('onWidgetRefreshComplete',
                widgetEventHandler);
        popupOptions = Object.merge({}, options);
        popupOptions.title = title;
        popupOptions.content = dialogContainer;
        popupOptions.onShowCallback = onShowWithWidgetCallback.bind(null, dialogContainer,
                containerSelector, widgetType, widgetId, widgetSettings, options);
        popupOptions.onCloseCallback = onCloseWithWidgetCallback.bind(null, dialogContainer,
                widgetId, options);
        if (!popupOptions.onEnterKeyCallback && result.mainAction && popupOptions.mainActionOnEnter) {
            popupOptions.onEnterKeyCallback = result.mainAction;
        }
        TB_create_layer();
        TB_show_popup(popupOptions);
    }
})();
// the notification object
var roar;

NOTIFICATION_BOX_TYPES = {
    help: 'roar notify-help',
    info: 'roar notify-info',
    success: 'roar notify-success',
    warning: 'roar notify-warning',
    error: 'roar notify-error',
    failure: 'roar notify-failure'
};

/**
 * Shows a notification popin box to give user feedback about this actions.
 * 
 * @param type of the notification - could be info, warning or error
 * @param title (string | null) the title of the popin notification
 * @param message (stringt) the content of the notification
 * @param options (object) some additional options for the notification
 */
function showNotification(type, title, message, options) {
    type = type || NOTIFICATION_BOX_TYPES.info;
    title = title || "";
    options = options || {};

    if (!roar) {
        init_roar();
    }

    roar.setOptions({
        duration: 6000
    });
    roar.setOptions(options);

    if (title == "") {
        type += ' hide-title';
    }
    roar.setOptions({
        className: type
    });

    roar.alert(title, message);
    // alert() returns a roar object containing all available notifications
    // object -> object.items[0], object.items[1], ...
    // to remove a single item -> roar.remove(object.items[0]);
}
/**
 * Hides a notification popin box.
 * 
 */
function hideNotification() {
    if (roar) {
        roar.empty();
    }
}

/**
 * Initializes the notification object.
 */
function init_roar() {
    roar = new Roar({
        position: 'lowerRight'
    });
}

function searchAndShowRoarNotification(startElement) {
    if (!startElement) {
        startElement = document;
    }
    var roarContainers = $(startElement).getElements('div.roar-notification-container');
    if (roarContainers.length > 0) {
        for (var i = 0; i < roarContainers.length; i++) {
            var roarContainer = roarContainers[i];
            var typeClassName = roarContainer.className.replace('roar-notification-container', '')
                    .trim();
            var oldLength = typeClassName.length;
            typeClassName = typeClassName.replace('infinite-duration', '').trim();
            var infiniteDuration = oldLength != typeClassName.length;
            var type = NOTIFICATION_BOX_TYPES[typeClassName];
            if (type == NOTIFICATION_BOX_TYPES.error || infiniteDuration
                    || type == NOTIFICATION_BOX_TYPES.warning) {
                showNotification(type, '', roarContainer.get('html'), {
                    duration: ''
                });
            } else {
                showNotification(type, '', roarContainer.get('html'), null);
            }
        }
        return true;
    } else {
        return false;
    }
}

(function(window) {
    var handledUnauthorized = false;
    /**
     * Callback to handle an AJAX request that resulted in status code 401. This callback will
     * reload the page with the login page. The current URL is provided as targetUrl so that it will
     * be reloaded after a successful login.
     * 
     * @param {Request} request The request that failed
     * @return true if to continue in the event propagation, false to stop
     */
    window.communote.requestFailureUnauthorizedHandler = function(request) {
        var currentUrl, appUrl;
        if (!handledUnauthorized) {
            handledUnauthorized = true;
            appUrl = communote.server.applicationUrl;
            currentUrl = window.location.href.replace(/;jsessionid=[a-zA-Z0-9]*/, '');
            if (currentUrl.indexOf(appUrl) == 0) {
                currentUrl = currentUrl.substring(appUrl.length);
            }
            window.location.href = appUrl
                    + '/portal/authenticate?sessionTimeout=true&targetUrl='
                    + encodeURIComponent(currentUrl);
        }
        return false;
    }
})(this);
/**
 * Returns true if the mouseover event was really from the element. Only useful when called from a
 * onmouseover handler function.
 * 
 * @param xThis the element that received the event
 * @return {boolean} true if the event was fired for the element
 */
function reallyMouseOverEvent(xThis) {
    // avoid flickering
    // (http://www.conandalton.net/2007/10/horrible-onmouseout-flicker-in-ie.html)
    if (!window.event)
        return true;
    var event = window.event;
    var from = event.fromElement;
    var to = event.toElement;
    return (to == xThis || xThis.contains(to)) && !xThis.contains(from) && xThis != from;
}
/**
 * Returns true if the mouseout event was really from the element. Only useful when called from a
 * onmouseout handler function.
 * 
 * @param xThis the element that received the event
 * @return {boolean} true if the event was fired for the element
 */
function reallyMouseOutEvent(xThis) {
    // avoid flickering
    // (http://www.conandalton.net/2007/10/horrible-onmouseout-flicker-in-ie.html)
    if (!window.event)
        return true;
    var event = window.event;
    var from = event.fromElement;
    var to = event.toElement;
    return (xThis == from || xThis.contains(from)) && !xThis.contains(to) && xThis != to;
}

function mOverAddHoverClass(xThis, cssClass) {
    if (Browser.name === 'ie' && Browser.version < 7) {
        if (reallyMouseOverEvent(xThis)) {
            if (cssClass == null) {
                cssClass = 'hover';
            }
            $(xThis).addClass(cssClass);
        }
    }
}

function mOutRemoveHoverClass(xThis, cssClass) {
    if (Browser.name === 'ie' && Browser.version < 7) {
        if (reallyMouseOutEvent(xThis)) {
            if (cssClass == null) {
                cssClass = 'hover';
            }
            $(xThis).removeClass(cssClass);
        }
    }
}

function mOverShowToolbox(xThis, toolboxSelector) {
    if (!reallyMouseOverEvent(xThis))
        return;
    var curElem = $(xThis);
    if (curElem) {
        var toolboxElem = curElem.getElement(toolboxSelector);
        if (toolboxElem) {
            // check whether to show left or right
            toolboxElem.setStyle('visibility', 'hidden');
            toolboxElem.addClass('flyout-left');
            var viewWide = window.getSize().x;
            var curElemRight = curElem.getCoordinates().right;
            if (curElemRight + toolboxElem.getSize().x <= viewWide) {
                toolboxElem.removeClass('flyout-left');
                toolboxElem.addClass('flyout-right');
            }
            toolboxElem.setStyle('visibility', '');
        }
    }
}
function mOutHideToolbox(xThis, toolboxSelector) {
    if (!reallyMouseOutEvent(xThis))
        return;
    var curElem = $(xThis);
    if (curElem) {
        var toolboxElem = curElem.getElement(toolboxSelector);
        if (toolboxElem) {
            toolboxElem.removeClass('flyout-left');
            toolboxElem.removeClass('flyout-right');
        }
    }
}

/**
 * Function will submit the form if the event is based on a press of return
 * 
 * Note: do not use this method in forms with text INPUTs because a it would also be triggered when
 * selecting an autosuggest created by the browser. For this use-case just let the submit INPUT that
 * should be called by enter be the first submit element in the form. Use CSS for positioning or (a
 * bit uglier) add a cloned element of the input and make it 'invisible' by an absolute negative
 * position.
 * 
 * @return false if the form has been submitted
 */
function submitFormWithEnter(form, hiddenFieldId, value, e) {
    var keycode;
    if (window.event) {
        keycode = window.event.keyCode;
    } else if (e) {
        keycode = e.which;
    } else {
        return true;
    }
    if (keycode == 13) {
        var el = $(hiddenFieldId);
        el.setProperty('name', value);
        form.submit();
        return false;
    } else {
        return true;
    }
}

/**
 * Escape reserved XML characters with XML entities.
 * 
 * @param {String} txt the string to escape
 * @return {String} txt where all occurences of &, < and > are replaced with there XML entities
 */
function escapeXML(txt) {
    txt = txt.replace(/\&/g, '&amp;');
    txt = txt.replace(/</g, '&lt;');
    return txt.replace(/>/g, '&gt;');
}
/**
 * Counterpart to escapeXML which unescapes the content.
 * 
 * @param {String} txt the string to escape
 * @return {String} txt where all occurences of &lt; &gt; and &amp; are replaced by <, > and &
 */
function unescapeXML(txt) {
    txt = txt.replace(/\&gt;/g, '>').replace(/\&lt;/g, '<');
    return txt.replace(/\&amp;/g, '&');
}

// TODO add URL building functions to utils namespace and separate file
/**
 * Insert the 'jsessionId' in the url. Takes care of the query string
 * 
 * @param {String} url the url to use for inserting
 * @return {String} the url with jsessionId included or the unmodified input if no session ID is
 *         required
 */
function insertSessionId(url) {
    var sessionIdString, hashIdx, queryIdx, idx, hostStartIdx, hostEndIdx, host;
    var sessionId = communote.server.applicationSessionId;
    // do nothing if there is no session ID, i.e. user is using cookies, or already contained 
    // note: not checking for JSESSIONID cookie because it's most likely disabled if customer
    // followed our recommendation ;)
    if (!sessionId || url.contains(';jsessionid=')) {
        return url;
    }
    // don't insert session ID into an absolute URL of another domain
    hostStartIdx = url.indexOf('://');
    if (hostStartIdx > -1) {
        hostStartIdx += 3;
        hostEndIdx = url.indexOf('/', hostStartIdx);
        if (hostEndIdx > -1) {
            host = url.substring(hostStartIdx, hostEndIdx);
        } else {
            host = url.substring(hostStartIdx);
        }
        if (location.hostname != host.split(':')[0]) {
            return url;
        }
    }
    sessionIdString = ';jsessionid=' + sessionId;
    hashIdx = url.indexOf('#');
    queryIdx = url.indexOf('?');
    if (hashIdx > -1 || queryIdx > -1) {
        if (hashIdx == -1) {
            idx = queryIdx;
        } else if (queryIdx == -1) {
            idx = hashIdx;
        } else {
            idx = Math.min(hashIdx, queryIdx);
        }
        url = url.substring(0, idx) + sessionIdString + url.substring(idx);
    } else {
        url += sessionIdString;
    }
    return url;
}

/**
 * Get the complete request URL including the host and the session id if necessary
 */
function buildRequestUrl(url) {
    return communote.server.applicationUrl + insertSessionId(url);
}

/**
 * Get the complete resource URL including the host and the session id if necessary
 */
function buildResourceUrl(url) {
    var resourceUrlParam = communote.server.resourceUrlParam;
    if (resourceUrlParam) {
        if (url.contains('?')) {
            url += '&' + resourceUrlParam;
        } else {
            url += '?' + resourceUrlParam;
        }
    }
    return communote.server.resourceUrlBase + insertSessionId(url);
}

/**
 * Get the complete request URL including the host, the session id if necessary and the parameter
 * string. To circumvent the heavy caching of IE a random parameter (random='some rnd number') will
 * be rendered as well.
 * 
 * @param urlPart path part of the URL
 * @param paramString the parameter string for the get request (e.g. 'q=abc&order=down'). Should be
 *            URL escaped if necessary.
 * @return the URL
 */
function buildRequestUrlWithParameters(urlPart, paramString) {
    /** due to IEs brute caching */
    var random = Math.random();
    var urlStr = urlPart;
    if (paramString) {
        urlStr += "?" + paramString + '&random=' + random;
    } else {
        urlStr += "?" + 'random=' + random;
    }
    return buildRequestUrl(urlStr);
}
/**
 * Builds an AJAX GET request. In case the request returns with an X-APPLICATION-RESULT header set
 * to ERROR the errorFunction is called. If this function is not defined the response is interpreted
 * as JSON object containing a key errorMessage which will be shown in a roar popup. In case the
 * header field is not set to ERROR the successFunction is called.
 * 
 * @param urlPart path part of the URL
 * @param paramString (optional) parameter string for the get request (e.g. 'q=abc&order=down')
 * @param successFunction function to be called when the request completed successfully. The
 *            response object will be passed to the function.
 * @param errorFunction (optional) function to be called when the request succeeded but has the
 *            X-APPLICATION-RESULT request header field set to error
 * @return the created request which can be invoked with the send() method
 */
function buildServiceRequest(urlPart, paramString, successFunction, errorFunction) {

    var url = buildRequestUrlWithParameters(urlPart, paramString);
    // we already got the random parameter attached, thus don't use noCache
    return buildJsonRequest(url, false, successFunction, errorFunction);
}

/**
 * Builds an AJAX GET request. In case the request returns with an X-APPLICATION-RESULT header set
 * to ERROR the errorFunction is called. If this function is not defined the response is interpreted
 * as JSON object containing a key errorMessage which will be shown in a roar popup. In case the
 * header field is not set to ERROR the successFunction is called.
 * 
 * @param url the request URL
 * @param successFunction function to be called when the request completed successfully. The
 *            response object will be passed to the function.
 * @param errorFunction (optional) function to be called when the request succeeded but has the
 *            X-APPLICATION-RESULT request header field set to error
 * @return the created request which can be invoked with the send() method
 */
function buildJsonRequest(url, noCache, successFunction, errorFunction) {
    var r = new Request.JSON({
        url: url,
        method: 'get',
        noCache: noCache
    });

    r.addEvent('complete', function(jsonResponse) {

        if (jsonResponse && jsonResponse.status == 'OK') {
            if (typeof successFunction == 'function') {
                successFunction(jsonResponse);
            } else {
                // show success message
                hideNotification();
                showNotification(NOTIFICATION_BOX_TYPES.success, '', jsonResponse.message);
            }
        } else {
            if (typeof errorFunction == 'function') {
                errorFunction(jsonResponse);
            } else {
                // show error message
                hideNotification();
                showNotification(NOTIFICATION_BOX_TYPES.error, '',
                        jsonResponse ? jsonResponse.message : 'Server Error', {
                            duration: ''
                        });
            }
        }
    });

    return r;
}

/**
 * Scrolls the Document container to the x and y coordinate (top/left) of the of the given DOM
 * element. The element must not have the style 'display:none'.
 * 
 * @param {String|Element} elem The element to scroll into view. Can be an element ID or an element.
 *            Can also be null to scroll to the position defined by the offset arguments.
 * @param {Number} offsetX An offset to use for the x coordinate.
 * @param {Number} offsetY An offset to use for the y coordinate.
 * @param {Boolean} force Whether to also scroll if the element is already in the view.
 * @param {Boolean} smooth Whether to scroll smoothly or fast.
 */
function scrollWindowTo(elem, offsetX, offsetY, force, smooth) {
    var pos, scroll, size, viewportLeft, viewportTop, viewportRight, viewportBottom;
    var container, scrollableContainer, scroller, noScroll;
    if (typeOf(offsetX) != 'number') {
        offsetX = 0;
    }
    if (typeOf(offsetY) != 'number') {
        offsetY = 0;
    }
    if (elem == null) {
        pos = {
            x: 0,
            y: 0
        };
    } else {
        container = document.id(elem);
        // cannot scroll to an element that does not exist or is not displayed
        if (container == null || container.getStyle('display') == 'none') {
            return;
        }
        pos = container.getPosition();
    }
    pos.x += offsetX;
    pos.y += offsetY;
    scrollableContainer = document;
    // if scrolling is not forced check if the position is already in view
    if (!force) {
        scroll = scrollableContainer.getScroll();
        size = scrollableContainer.getSize();
        viewportLeft = scroll.x;
        viewportTop = scroll.y;
        viewportRight = viewportLeft + size.x;
        viewportBottom = viewportTop + size.y;
        noScroll = true;
        // only scroll axis that is out of view
        if (viewportLeft > pos.x || viewportRight < pos.x) {
            noScroll = false;
        } else {
            pos.x = viewportLeft;
        }
        if (viewportTop > pos.y || viewportBottom < pos.y) {
            noScroll = false;
        } else {
            pos.y = viewportTop;
        }
        if (noScroll) {
            return;
        }
    }
    // reuse scroller
    scroller = window.smoothScroller;
    if (!scroller) {
        scroller = new Fx.Scroll(scrollableContainer, {
            wheelstops: true,
            duration: 500
        });
        smoothScroller = scroller;
    }
    if (smooth) {
        scroller.start(pos.x, pos.y);
    } else {
        scroller.set(pos.x, pos.y);
    }
}

/**
 * This method searches for tabs an initializes them. activatedTabs must have the form: {
 * 'tabGroup1' : 'activatedTab', 'tabGroupX' : 'anotherActivatedTab' }
 */
init_tabs = function(activatedTabs, domNode) {
    if (!window.tabGroups) {
        tabGroups = {};
    }
    var panels;
    if (domNode) {
        panels = domNode.getElements('.mootabs');
    } else {
        panels = $$('.mootabs');
    }
    if (panels != null && panels.length > 0) {
        for (var i = 0; i < panels.length; i++) {
            var options = {};
            if (activatedTabs != null && activatedTabs[panels[i].get('id')] != null) {
                options['activateOnLoad'] = activatedTabs[panels[i].get('id')];
            } else {
                var activeElement = panels[i].getElement('.active');
                if (activeElement != null) {
                    options['activateOnLoad'] = activeElement.get('id');
                }
            }
            var tab = new mootabs(panels[i].get('id'), options);
            tabGroups[panels[i].get('id')] = tab;
            var closeableTabs = panels[i].getElements('li.mootabs_closeable');
            for (var e = 0; e < closeableTabs.length; e++) {
                var closeSpan = new Element("span");
                closeSpan.addClass("tab-icon-close");
                closeSpan.set('html', '&nbsp');
                closeSpan.set('id', closeableTabs[e].get('id') + 'Close');
                closeSpan.setStyle('display', closeableTabs[e].getStyle('display'));
                closeSpan.addEvent('click', function(e) {
                    e.target.getParent().setStyle('display', 'none');
                    var tabGroup = tabGroups[this];
                    tabGroup.first();
                    /* stopping the event propagation */
                    return false;
                }.bind(panels[i].get('id')));
                closeSpan.inject(closeableTabs[e], 'bottom');
            }
        }
    }
};

/**
 * Initializes tips within the actual document.
 */
init_tips = function(parentContainer) {
    var tipElements;
    var namespace = window.communote;
    if (!namespace) {
        return;
    }
    if (!namespace.toolTips) {
        namespace.toolTips = new namespace.classes.AdvancedTips({
            hideDelay: null
        });
    }
    namespace.toolTips.scan('a.tooltip', parentContainer);
};

/**
 * solve the hover problem for ie6 (KENMEI-3409)
 */
function fix_ie7_styles() {
    if (Browser.name == 'ie' && Browser.version < 7) {
        $$('#cono-view .cn-list-blogs-blogs-entry, #cn-list-users .cn-list-people-persons-entry')
                .each(function(link) {
                    link.addEvents({
                        'mouseenter': function(e) {
                            this.addClass("hover");
                        }
                    });
                    link.addEvents({
                        'mouseleave': function(e) {
                            this.removeClass("hover");
                        }
                    });
                });
    }
}

// http://stackoverflow.com/questions/5723154/truncate-a-string-in-the-middle-with-javascript
var truncateMiddle = function(fullStr, strLen, separator) {
    if (fullStr.length <= strLen) {
        return fullStr;
    }
    separator = separator || '...';
    var charsToShow = strLen - separator.length;
    var frontChars = Math.ceil(charsToShow / 2);
    var backChars = Math.floor(charsToShow / 2);
    return fullStr.substr(0, frontChars) + separator + fullStr.substr(fullStr.length - backChars);
};
