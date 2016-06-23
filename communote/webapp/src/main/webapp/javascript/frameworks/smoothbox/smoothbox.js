/*
 * Smoothbox v20080623 by Boris Popoff (http://gueschla.com) To be used with mootools 1.2
 * 
 * Based on Cody Lindley's Thickbox, MIT License
 * 
 * Licensed under the MIT License: http://www.opensource.org/licenses/mit-license.php
 */
// amo: Made compatible with Mootools 1.4.5 (setOpacity(x) => setStyle('opacity',x))
// TODO refactor:
// - get rid of all these global variables and functions
// - Define some generic Popup Mootools class
// - avoid searching DOM for same element over and over within same function
// - get rid of eval
(function() {
    var TB_doneOnce = 0;
    //RWI multilayer popup support
    var TB_zIndex_incr = 2; // used to calculate z-index of current popup window
    // z-index of overlay is this value minus 1
    var TB_layer_zIndex = 102;
    var TB_layer = 0; // current layer
    var TB_layer_data = []; // stack for info of current layer
    TB_layer_data.push({
        width: 0,
        height: 0,
        centerX: -1,
        centerY: -1,
        left: -1,
        top: -1,
        onRemove: null,
        windowPositionMorph: null
    });
    var resizeHandlerSet = false;

    function keydownEventHandler(event) {
        var callback;
        var event = new DOMEvent(event);
        if (event.code == 27) {
            // close and call close function if defined
            TB_remove(true);
            // no propagation
            event.stopPropagation();
        } else if(event.code == 13 && (callback = TB_layer_data[TB_layer].onEnterKeyCallback)) {
            callback.call();
            event.stop();
        }
    }

    function resizeEventHandler() {
        TB_position();
        TB_load_position();
        TB_overlaySize();
    }

    function TB_bind(event) {
        var event = new DOMEvent(event);
        // stop default behaviour
        event.preventDefault();
        // remove click border
        this.blur();
        // get caption: either title or name attribute
        var caption = this.title || this.name || "";
        // get rel attribute for image groups
        var group = this.rel || false;
        // display the box for the elements href
        TB_show(caption, this.href, group);
        this.onclick = TB_bind;
        return false;
    }

    function TB_beforeShowWindow(name, parameters) {
        if (name) {
            if (parameters) {
                eval(name + "('TB_ajaxContent" + TB_layer + "', '" + parameters + "')");
            } else {
                eval(name + "('TB_ajaxContent" + TB_layer + "')");
            }
        }
    }
    //helper functions below

    function TB_showWindow() {
        var tbLoad;
        var tbWindow = $('TB_window' + TB_layer);
        if (TB_doneOnce == 0) {
            TB_doneOnce = 1;
            tbWindow.set('tween', {
                duration: 250,
                onComplete: function() {
                    var tbLoad = $('TB_load');
                    if (tbLoad) {
                        tbLoad.dispose();
                    }
                }
            });
            tbWindow.tween('opacity', 0, 1);
        } else {
            tbWindow.setStyle('opacity', 1);
            tbLoad = $('TB_load');
            if (tbLoad) {
                tbLoad.dispose();
            }
        }
    }

    function TB_overlaySize() {
        var newWidth, newHeight;
        var tbOverlay = $("TB_overlay");
        var tbHideSelect = $("TB_HideSelect");

        // we have to set this to 0px before so we can reduce the size / width of the overflow onresize 
        tbOverlay.setStyles({
            'height': '0px',
            'width': '0px'
        });
        tbHideSelect.setStyles({
            'height': '0px',
            'width': '0px'
        });
        newHeight = window.getScrollHeight() + 'px';
        newWidth = window.getScrollWidth() + 'px';
        tbOverlay.setStyles({
            'height': newHeight,
            'width': newWidth
        });
        tbHideSelect.setStyles({
            'height': newHeight,
            'width': newWidth
        });
    }

    function TB_load_position() {
        if ($("TB_load")) {
            $("TB_load").setStyles({
                left: (window.getScrollLeft() + (window.getWidth() - 56) / 2) + 'px',
                top: (window.getScrollTop() + ((window.getHeight() - 20) / 2)) + 'px',
                display: "block"
            });
        }
    }

    function TB_parseQuery(query) {
        // return empty object
        if (!query)
            return {};
        var params = {};

        // parse query
        var pairs = query.split(/[;&]/);
        for ( var i = 0; i < pairs.length; i++) {
            var pair = pairs[i].split('=');
            if (!pair || pair.length != 2)
                continue;
            // unescape both key and value, replace "+" with spaces in value
            params[unescape(pair[0])] = unescape(pair[1]).replace(/\+/g, ' ');
        }
        return params;
    }

    function createOverlay(closeOnClick) {
        var hideSelect;
        var tbOverlay = document.id('TB_overlay');
        if (!tbOverlay) {
            // TODO what's this hideselect good for? IE6 crap?
            hideSelect = new Element('iframe').setProperty('id', 'TB_HideSelect').inject(
                    document.body, 'inside');
            hideSelect.setStyle('opacity', 0);
            tbOverlay = new Element('div').setProperty('id', 'TB_overlay').inject(document.body,
                    'inside');
            tbOverlay.setStyle('opacity', 0);
            TB_overlaySize();
            TB_load_position();

            tbOverlay.set('tween', {
                duration: 400
            });
            tbOverlay.tween('opacity', 0, 0.6);

        } else {
            // set new z-index of overlay
            tbOverlay.setStyle('z-index', TB_layer_zIndex - 1);
        }
        if (closeOnClick) {
            // close function should be run if defined
            tbOverlay.onclick = TB_remove.pass(true);
        }
        if (!resizeHandlerSet) {
            window.addEvent('resize', resizeEventHandler);
            resizeHandlerSet = true;
        }
    }

    function createLoadFeedback() {
        var tbLoad = $("TB_load");
        if (!tbLoad) {
            tbLoad = new Element('div').setProperty('id', 'TB_load')
                    .inject(document.body, 'inside');
            tbLoad.innerHTML = "<img src='" + TB_loadingImgUrl + "' />";
            TB_load_position();
        }
    }

    function parseSizeValue(size) {
        if (size != null && size !== 'auto') {
            size = String.toInt(size);
            if (isNaN(size) || size < 1) {
                size = null;
            } else {
                size = size + 'px';
            }
        }
        return size;
    }

    function invokeRemoveCallback(onRemoveCallback) {
        if (onRemoveCallback) {
            if (typeof (onRemoveCallback) == 'string') {
                eval(onRemoveCallback);
            } else if (typeof (onRemoveCallback) == 'function') {
                onRemoveCallback.call();
            }
        }
    }

    function createPopupWindow(title, cssClasses, options) {
        var width, height, tbWindowId, closeBtnId, tbWindow, contentElem, closeLabel, html;
        var renderCloseButton;
        if (!options) {
            options = {};
        }
        renderCloseButton = options.renderCloseButton !== false;
        tbWindowId = 'TB_window' + TB_layer;
        closeBtnId = 'TB_closeWindowButton' + TB_layer;
        tbWindow = document.id(tbWindowId);
        if (!tbWindow) {
            tbWindow = new Element('div').setProperty('id', tbWindowId).inject(document.body,
                    'inside');
            tbWindow.setStyle('opacity', 0);
            tbWindow.setStyle('z-index', TB_layer_zIndex - 1);
        }
        if (options.viewportHorizontalMargin > 0) {
            tbWindow.setStyle('max-width', window.getSize().x - options.viewportHorizontalMargin + 'px');
        }
        cssClasses = cssClasses ? 'TB_window ' + cssClasses : 'TB_window';
        tbWindow.className = cssClasses;
        html = '';
        if (title || options.renderEmptyTitle) {
            title = escapeXML(title);
            html = '<div id="TB_title">';
            if (renderCloseButton) {
                closeLabel = getJSMessage('javascript.dialog.button.close');
                html += '<div id="TB_closeAjaxWindow"><a href="javascript:;" id="' + closeBtnId
                    + '" class="TB_closeWindowButton cn-icon" title="' + closeLabel + '"><span>'
                    + closeLabel + '</span></a></div>';
            }
            html += '<div id="TB_ajaxWindowTitle" title="' + title + '">' + title + '</div>';
            html += '<div id="TB_ajaxWindowTitleBottom"></div></div>';
        } else {
            renderCloseButton = false;
        }
        html += '<div id="TB_ajaxContent' + TB_layer + '" class="TB_ajaxContent"></div>';
        tbWindow.innerHTML = html;

        width = parseSizeValue(TB_layer_data[TB_layer].width);
        height = parseSizeValue(TB_layer_data[TB_layer].height);
        if (width || height) {
            contentElem = tbWindow.getElement('.TB_ajaxContent');
            if (width) {
                contentElem.setStyle('width', width);
            }
            if (height) {
                contentElem.setStyle('height', height);
            }
        }
        // close function should be run if defined
        if (renderCloseButton) {
            tbWindow.getElementById(closeBtnId).onclick = TB_remove.pass(true);
        }
        if (TB_layer == 0) {
            document.addEvent('keydown', keydownEventHandler);
        }
        return tbWindow;
    }

    function popupWindowFadeOutComplete(windowElemId, onRemoveCallback) {
        var windowElem;
        invokeRemoveCallback(onRemoveCallback);
        windowElem = document.id(windowElemId);
        if (windowElem) {
            windowElem.dispose();
        }
    }

    // public functions

    // add smoothbox to href elements that have a class of .smoothbox
    TB_attach = function() {
        $$("a.smoothbox").each(function(el) {
            el.onclick = TB_bind;
        });
    }

    // creates a new layer if necessary
    TB_create_layer = function() {
        if (TB_layer > 0 || $('TB_overlay')) {
            if (TB_layer == 0 && $('TB_overlay')) {
                // cancel running transitions
                $('TB_overlay').get('tween').cancel();
                if ($('TB_window0'))
                    $('TB_window0').get('tween').cancel();
                if (!$('TB_window0')) {
                    // stay on layer
                    return;
                }
            }
            TB_layer_data.push({});
            TB_layer += 1;
            TB_layer_zIndex += TB_zIndex_incr;
        }
    };
    // called when the user clicks on a smoothbox link
    // deprecated: use TB_show_popup instead
    TB_show = function(caption, url, rel) {
        return TB_show2(caption, url, rel, null);
    }

    // deprecated: use TB_show_popup instead
    TB_show2 = function(caption, url, rel, element, closeFunction) {
        var tbWindow, baseURL, queryString, params, layerData;
        // RWI create layer unique elements, but only one overlay which is moved on z-axis on new popup

        // create iframe, overlay and box if non-existent
        createOverlay(true);
        createLoadFeedback();
        // check if a query string is involved
        baseURL = url.match(/(.+)?/)[1] || url;
        //code to show html pages
        queryString = url.match(/\?(.+)/)[1];
        params = TB_parseQuery(queryString);

        layerData = TB_layer_data[TB_layer]; 
        layerData.width = params['width'];
        layerData.height = params['height'];
        layerData.left = -1;
        layerData.top = -1;
        layerData.centerX = -1;
        layerData.centerY = -1;

        var onBeforeShow = params['onBeforeShow'];
        var onBeforeShowParameter = null; //RWI when not assigning something, next assignment is not working in firefox3
        onBeforeShowParameter = params['onBeforeShowParameter'];
        // RWI paramter holding a function (closure) to be invoked when the smoothbox is closed/removed
        layerData.onRemove = closeFunction ? closeFunction : params['onClose'];

        tbWindow = createPopupWindow(caption);

        if (url.indexOf('TB_inline') != -1) {
            if (element == null) {
                if ($(params['inlineId']).getFirst()) {
                    element = $(params['inlineId']).getFirst().clone();
                } else {
                    element = $(params['inlineId']).clone();
                }
            }

            element.inject(tbWindow.getElement('.TB_ajaxContent'));
            TB_position();
            TB_beforeShowWindow(onBeforeShow, onBeforeShowParameter);
            TB_showWindow();
        } else {
            var handlerFunc = function(responseTree, responseElements, responseHTML,
                    responseJavaScript) {
                $("TB_ajaxContent" + TB_layer).set('html', responseHTML);
                TB_position();
                TB_beforeShowWindow(onBeforeShow, onBeforeShowParameter);
                TB_showWindow();
            };
            new Request.HTML({
                'url': url,
                method: 'get',
                onComplete: handlerFunc
            }).send();
        }
    };

    TB_show_popup = function(options) {
        var closeCallback, onShowCallback, onEnterKeyCallback, tbWindow, contentContainer, layerData;
        var title = options.title || '';
        var content = options.content || '';
        closeCallback = options.onCloseCallback;
        if (typeof closeCallback != 'function') {
            closeCallback = null;
        }
        onShowCallback = options.onShowCallback;
        if (typeof onShowCallback != 'function') {
            onShowCallback = null;
        }
        onEnterKeyCallback = options.onEnterKeyCallback;
        if (typeof onEnterKeyCallback != 'function') {
            onEnterKeyCallback = null;
        }
        layerData = TB_layer_data[TB_layer];
        layerData.height = options.height;
        layerData.width = options.width;
        // absolut left position of popup
        if (options.left >= 0) {
            layerData.left = options.left;
        } else {
            layerData.left = -1;
        }
        // absolut top position of popup
        if (options.top >= 0) {
            layerData.top = options.top;
        } else {
            layerData.top = -1;
        }
        // if left is not provided or < 0, defines the X position which will become the center of the popup 
        if (options.centerX > 0) {
            layerData.centerX = options.centerX;
        } else {
            layerData.centerX = -1;
        }
        // if top is not provided or < 0, defines the Y position which will become the center of the popup
        if (options.centerY > 0) {
            layerData.centerY = options.centerY;
        } else {
            layerData.centerY = -1;
        }
        layerData.onRemove = closeCallback;
        layerData.onEnterKeyCallback = onEnterKeyCallback; 
        // close on overlay click by default
        createOverlay(options.closeOnOverlayClick == null ? true : !!options.closeOnOverlayClick);
        createLoadFeedback();
        tbWindow = createPopupWindow(title, options.windowCssClasses, options);
        contentContainer = tbWindow.getElement('.TB_ajaxContent');
        if (typeof content == 'string') {
            contentContainer.set('html', content);
        } else {
            contentContainer.grab(content);
        }
        if (onShowCallback) {
            onShowCallback.call(null, tbWindow);
        }
        if (typeof options.onPositionCompleteCallback == 'function') {
            TB_position(options.onPositionCompleteCallback.bind(null, tbWindow));
        } else {
            TB_position();
        }
        TB_showWindow();
    };

    TB_position = function(completeCallback) {
        var windowElem, layerData, top, left, windowElemSize, scrollOffset, viewportSize, morph;
        windowElem = document.id('TB_window' + TB_layer);
        if (windowElem) {
            layerData = TB_layer_data[TB_layer];
            // reuse morph to handle calls to TB_position while morph still running
            morph = layerData.windowPositionMorph;
            if (!morph) {
                morph = new Fx.Morph(windowElem, {
                    onComplete: completeCallback,
                    duration: 75,
                    link: 'cancel'
                });
                layerData.windowPositionMorph = morph;
            }
            top = layerData.top;
            if (top < 0) {
                // no top position given -> center horizontally
                windowElemSize = windowElem.getSize();
                viewportSize = window.getSize();
                scrollOffset = window.getScroll();
                if (layerData.centerY > 0) {
                    top = layerData.centerY - windowElemSize.y / 2;
                    // avoid uneccessary scrolling
                    if (top + windowElemSize.y > scrollOffset.y + viewportSize.y) {
                        top = scrollOffset.y + viewportSize.y - windowElemSize.y;
                    }
                    if (top < scrollOffset.y) {
                        top = scrollOffset.y;
                    }
                } else {
                    // center in view
                    top = scrollOffset.y + (viewportSize.y - windowElemSize.y) / 2;
                }
                if (top < 0) {
                    top = 0;
                }
            }
            left = layerData.left;
            if (left < 0) {
                if (!windowElemSize) {
                    windowElemSize = windowElem.getSize();
                    viewportSize = window.getSize();
                    scrollOffset = window.getScroll();
                }
                if (layerData.centerX > 0) {
                    left = layerData.centerX -  windowElemSize.x / 2;
                } else {
                    left = scrollOffset.x + (viewportSize.x - windowElemSize.x) / 2;
                }
                if (left < 0) {
                    left = 0;
                }
            }
            morph.start({
                left: left + 'px',
                top: top + 'px'
            });
        }
    };
    TB_remove = function(evalCloseFunction) {
        var layerData;
        var tbWindow, onRemoveCallback, popupFadeFunction;
        var tbOverlay = $('TB_overlay');

        if (!tbOverlay) {
            return;
        }
        // remove events when on layer 0
        if (TB_layer == 0) {
            tbOverlay.onclick = null;
            document.removeEvent('keydown', keydownEventHandler);
        }
        if (evalCloseFunction) {
            onRemoveCallback = TB_layer_data[TB_layer].onRemove;
        }
        if ($('TB_closeWindowButton' + TB_layer))
            $("TB_closeWindowButton" + TB_layer).onclick = null;
        tbWindow = $('TB_window' + TB_layer);
        if (tbWindow) {
            popupFadeFunction = popupWindowFadeOutComplete.bind(null, 'TB_window' + TB_layer,
                    onRemoveCallback);
            tbWindow.set('tween', {
                duration: 250,
                onCancel: popupFadeFunction,
                onComplete: popupFadeFunction
            });
            tbWindow.tween('opacity', 1, 0);
        } else {
            invokeRemoveCallback(onRemoveCallback);
        }

        if (TB_layer == 0) {
            tbOverlay.set('tween', {
                duration: 400,
                onCancel: function() {
                    $('TB_overlay').setStyle('opacity', 0.6);
                },
                onComplete: function() {
                    var overlay = $('TB_overlay');
                    if (overlay != null) {
                        overlay.dispose();
                    }
                    var hideSelect = $('TB_HideSelect');
                    if (hideSelect != null) {
                        hideSelect.dispose();
                    }
                }
            });
            tbOverlay.tween('opacity', 0.6, 0);
            if (resizeHandlerSet) {
                window.removeEvent('resize', resizeEventHandler);
                resizeHandlerSet = false;
            }

        } else {
            // decrease z-index
            TB_layer_zIndex -= TB_zIndex_incr;
            tbOverlay.setStyle('z-index', TB_layer_zIndex - 1);
        }

        TB_doneOnce = 0;

        if (TB_layer > 0) {
            TB_layer_data.pop();
            TB_layer -= 1;
        } else if (TB_layer_data[0].windowPositionMorph) {
            TB_layer_data[0].windowPositionMorph = null;
        }
        //RWI: remove TB_load div in case popup was closed before loading finished 
        var tbLoadElem = $('TB_load');
        if (tbLoadElem) {
            tbLoadElem.dispose();
        }
        return false;
    };
})();