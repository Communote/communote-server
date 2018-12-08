// TODO is there a generic way to stop a playing video via JavaScript?
(function() {
    'use strict';

    const communote = window.communote;
    const i18n = communote.i18n;

    const urlMappings = {
        'YOUTUBE': 'https://www.youtube.com/embed/$MEDIA_ID$?wmode=transparent&rel=0',
        'VIMEO': 'https://player.vimeo.com/video/$MEDIA_ID$'
    };
    const idSeed = 'a' + Math.random().toString(16).substring(2);
    var idCount = 0;
    var resizeObservingMediaUtils = [];
    
    function adaptIFramesToAvailableWidth(mediaUtilsInstance, mediaListWrapperElem) {
        var i;
        var size = mediaUtilsInstance.determineSize(mediaListWrapperElem);
        var elems = mediaListWrapperElem.querySelectorAll('iframe');
        for (i = 0; i < elems.length; i++) {
            elems[i].width = size.width;
            elems[i].height = size.height;
        }
    }
    function adaptToAvailableWidth(mediaUtilsInstance) {
        var curWidth, i, mediaListWrapperElem;
        if (!mediaUtilsInstance.widget.widgetShown) {
            mediaUtilsInstance.missedResize = true;
        } else {
            curWidth = mediaUtilsInstance.widget.domNode.clientWidth;
            // if width of common parent did not change, children should still have enough space
            if (curWidth !== mediaUtilsInstance.lastWidth) {
                for (i = mediaUtilsInstance.mediaListIds.length - 1; i >= 0; i--) {
                    mediaListWrapperElem = document.getElementById(mediaUtilsInstance.mediaListIds[i]);
                    if (mediaListWrapperElem) {
                        adaptIFramesToAvailableWidth(mediaUtilsInstance, mediaListWrapperElem);
                    } else {
                        // cleanup if wrapper doesn't exist anymore (e.g. note deleted)
                        mediaUtilsInstance.mediaListIds.splice(i, 1);
                    }
                }
                mediaUtilsInstance.lastWidth = curWidth;
                if (!mediaUtilsInstance.mediaListIds.length) {
                    stopObservingResize(mediaUtilsInstance);
                }
            }
        }
    }
    
    function createUniqueId() {
        return idSeed + idCount++;
    }
    
    function handleResize() {
        resizeObservingMediaUtils.forEach(adaptToAvailableWidth);
    }

    function renderIFrame(mediaDefinition, size, parentElem) {
        var iframeElem = document.createElement('iframe');
        iframeElem.id = createUniqueId();
        iframeElem.title = i18n.getMessage('plugins.contenttypesextractor.embedmedia.iframe.title.' + mediaDefinition.type);
        iframeElem.setAttribute('allowfullscreen', 'allowfullscreen');
        iframeElem.classList.add('cnt-embedmedia');
        iframeElem.classList.add('cnt-embedmedia-' + mediaDefinition.type);
        iframeElem.width = size.width;
        iframeElem.height = size.height;
        iframeElem.src = urlMappings[mediaDefinition.type].replace('$MEDIA_ID$', mediaDefinition.id);
        parentElem.appendChild(iframeElem);
        return iframeElem.id;
    }

    function renderMediaListEntry(mediaDefinition, size, listElem) {
        var liElem;
        if (urlMappings[mediaDefinition.type] && mediaDefinition.id) {
            liElem = document.createElement('li');
            renderIFrame(mediaDefinition, size, liElem);
            listElem.insertBefore(liElem, listElem.lastChild);
        }
    }

    function startObservingResize(mediaUtilsInstance) {
        if (resizeObservingMediaUtils.indexOf(mediaUtilsInstance) === -1) {
            resizeObservingMediaUtils.push(mediaUtilsInstance);
            if (resizeObservingMediaUtils.length === 1) {
                // TODO use debounce with higher delay?
                communote.utils.addDebouncedResizeEventHandler(handleResize);
            }
        }
    }
    function stopObservingResize(mediaUtilsInstance) {
        var idx = resizeObservingMediaUtils.indexOf(mediaUtilsInstance);
        if (idx > -1) {
            resizeObservingMediaUtils.splice(idx, 1);
            if (resizeObservingMediaUtils.length === 0) {
                communote.utils.removeDebouncedResizeEventHandler(handleResize);
            }
        }
    }
    

    /**
     * Check all media definitions whether they have a type for which a mapping exists and have
     * an ID.
     * 
     * @param {Object[]} mediaDefinitions - Media definitions to validate
     * @return {Object} a validation result whose count member represents the number of valid
     *         definitions and whose startIndex member holds the index of the first valid
     *         definition
     */
    function validateMediaDefinitions(mediaDefinitions) {
        var i, l, mediaDefinition;
        var count = 0;
        var startIndex = 0;
        if (mediaDefinitions) {
            for (i = 0, l = mediaDefinitions.length; i < l; i++) {
                mediaDefinition = mediaDefinitions[i];
                if (urlMappings[mediaDefinition.type] && mediaDefinition.id) {
                    count++;
                } else if (i === startIndex) {
                    startIndex++;
                }
            }
        }
        return {
            count: count,
            startIndex: startIndex
        };
    }

    var EmbedMediaUtils = function(widget, options) {
        this.widget = widget;
        this.preferredWidth = options.preferredWidth;
        this.preferredHeight = options.preferredHeight;
        this.adaptToWidth = options.adaptToWidth;
        this.mediaListIds = [];
        widget.addEventListener('noteContainerPrepared', this.onNoteContainerPrepared, this);
        widget.addEventListener('widgetShown', this.onWidgetShown, this);
        widget.addEventListener('widgetRefreshing', this.reset, this);
    };
    /**
     * Determine width and height of the IFrame by taking the available width of the passed element
     * and the aspect defined by the preferred width and height passed as option to the constructor
     * into account.
     * 
     * @param {Element} widthProvidingElement - DOM element whose width will be used to calculate an
     *            appropriate height if it is smaller than the configured preferred width.
     * @return {Object} an object with width and height members
     */
    EmbedMediaUtils.prototype.determineSize = function(widthProvidingElement) {
        var width, height;
        if (widthProvidingElement) {
            // TODO clientWidth includes padding - check for padding and subtract it?
            width = widthProvidingElement.clientWidth;
        }
        if (!width || width >= this.preferredWidth) {
            width = this.preferredWidth;
            height = this.preferredHeight;
        } else {
            height = Math.round(width * this.preferredHeight / this.preferredWidth);
        }
        return {
            width: width,
            height: height
        };
    };
    EmbedMediaUtils.prototype.createMediaList = function(noteId, mediaCount, showMoreStartIndex) {
        var mediaListWrapperElem, mediaListId, listElem, actionElem;
        mediaListWrapperElem = document.createElement('div');
        mediaListWrapperElem.classList.add('cn-media-list');
        mediaListWrapperElem.classList.add('cn-media-video');
        mediaListId = createUniqueId();
        mediaListWrapperElem.id = mediaListId;
        listElem = mediaListWrapperElem.appendChild(document.createElement('ul'));
        if (mediaCount > 1) {
            listElem.appendChild(document.createElement('li')).classList.add('cn-embedmedia-action');
            actionElem = listElem.firstChild.appendChild(document.createElement('a'));
            actionElem.role = 'button';
            if (mediaCount > 2) {
                actionElem.innerHTML = i18n.getMessage('plugins.contenttypesextractor.embedmedia.action.more.pl', [mediaCount - 1]);
            } else {
                actionElem.innerHTML = i18n.getMessage('plugins.contenttypesextractor.embedmedia.action.more.sg', [1]);
            }
            actionElem.addEventListener('click', this.onShowMoreLessClick.bind(this, {
                showMore: true,
                noteId: noteId,
                label: i18n.getMessage('plugins.contenttypesextractor.embedmedia.action.less'),
                rendered: false,
                startIndex: showMoreStartIndex,
                mediaListId: mediaListId
            }));
        }
        return {
            id: mediaListId,
            listElem: listElem,
            wrapperElem: mediaListWrapperElem
        }
    };
    EmbedMediaUtils.prototype.onNoteContainerPrepared = function(noteData) {
        var mediaWrapperElem, mediaList;
        var metaData = this.widget.getNoteMetaData(noteData.id);
        var validationResult = validateMediaDefinitions(metaData.embedMedia);
        if (validationResult.count) {
            mediaWrapperElem = noteData.container.querySelector('.cn-note-media');
            if (mediaWrapperElem) {
                mediaList = this.createMediaList(noteData.id, validationResult.count,
                        validationResult.startIndex + 1);
                this.mediaListIds.push(mediaList.id);
                // can't measure size because not in DOM, do it afterwards
                renderMediaListEntry(metaData.embedMedia[validationResult.startIndex],
                        {width: this.preferredWidth, height: this.preferredHeight}, mediaList.listElem);
                mediaWrapperElem.insertBefore(mediaList.wrapperElem, mediaWrapperElem.firstChild);
                adaptIFramesToAvailableWidth(this, mediaList.wrapperElem);
                if (this.adaptToWidth) {
                    startObservingResize(this);
                    this.lastWidth = this.lastWidth || this.widget.domNode.clientWidth;
                }
            }
        }
    };
    EmbedMediaUtils.prototype.onShowMoreLessClick = function(context, event) {
        var currentLabel, mediaListWrapperElem, elems, i, l;
        event.preventDefault();
        mediaListWrapperElem = document.getElementById(context.mediaListId);
        if (context.showMore) {
            if (context.rendered) {
                elems = mediaListWrapperElem.querySelectorAll('li');
                // hide all, except first and last (last one is show-more element)
                for (i = 1, l = elems.length - 1; i < l; i++) {
                    elems[i].style.display = '';
                }
            } else {
                this.renderMediaItems(context.noteId, context.startIndex, mediaListWrapperElem);
                context.rendered = true;
            }
        } else {
            elems = mediaListWrapperElem.querySelectorAll('li');
            for (i = 1, l = elems.length - 1; i < l; i++) {
                elems[i].style.display = 'none';
            }
        }
        currentLabel = event.target.innerHTML;
        event.target.innerHTML = context.label;
        context.label = currentLabel;
        context.showMore = !context.showMore;
    };
    EmbedMediaUtils.prototype.onWidgetShown = function() {
        if (this.missedResize) {
            this.missedResize = false;
            adaptToAvailableWidth(this);
        }
    };
    EmbedMediaUtils.prototype.renderMediaItems = function(noteId, startIndex, mediaListWrapperElem) {
        var size, listElem, i, l;
        var metaData = this.widget.getNoteMetaData(noteId);
        var mediaDefinitions = metaData && metaData.embedMedia
        if (mediaDefinitions && mediaDefinitions.length > startIndex) {
            size = this.determineSize(mediaListWrapperElem);
            listElem = mediaListWrapperElem.querySelector('ul');
            for (i = startIndex, l = mediaDefinitions.length; i < l; i++){
                renderMediaListEntry(mediaDefinitions[i], size, listElem);
            }
        }
    };
    EmbedMediaUtils.prototype.reset = function() {
        this.mediaListIds = [];
        if (this.adaptToWidth) {
            this.lastWidth = 0;
            stopObservingResize(this);
        }
    };

    var widgetLifecycleEventListener = {
        widgetToEmbedMediaUtil: {},
        handleEvent: function(eventName, eventData) {
            var widget, embedMedia;
            if (eventName === 'onWidgetInitComplete') {
                if (eventData.widgetType === 'ChronologicalPostListWidget') {
                    widget = communote.widgetController.getWidget(eventData.widgetId);
                    embedMedia = new EmbedMediaUtils(widget, communote.embedMedia.defaultOptions);
                    this.widgetToEmbedMediaUtil[eventData.widgetId] = embedMedia;
                }
            } else if (eventName === 'onWidgetRemove') {
                if (this.widgetToEmbedMediaUtil[eventData.widgetId]) {
                    this.widgetToEmbedMediaUtil[eventData.widgetId].reset();
                    delete this.widgetToEmbedMediaUtil[eventData.widgetId];
                }
            }
        }
    };

    if (communote.initializer) {
        communote.initializer.addWidgetFrameworkInitializedCallback(function() {
            communote.widgetController.registerWidgetEventListener('onWidgetInitComplete', widgetLifecycleEventListener);
            communote.widgetController.registerWidgetEventListener('onWidgetRemove', widgetLifecycleEventListener);
        });
    }

    communote.embedMedia = communote.embedMedia || {};
    // expose defaultOptions for customization
    if (!communote.embedMedia.defaultOptions) {
        communote.embedMedia.defaultOptions = {
            preferredWidth: 384,
            preferredHeight: 216,
            adaptToWidth:  true
        };
    }
    communote.embedMedia.addUrlMapping = function(mediaType, urlPattern) {
        if (!urlMappings[mediaType]) {
            urlMappings[urlMappings] = urlPattern;
        }
    };
    
})();