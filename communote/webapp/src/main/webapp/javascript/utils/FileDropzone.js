// implementation notes: DnD API is crap. When attaching the DnD event listeners to an element, the
// events will also fire for child elements when they are hovered. Especially confusing is that the
// current element receives a dragleave if a child is entered. This makes it difficult to find out
// whether the dropzone was left or just a child got hovered. If an element has no own hoverable
// space (due to padding, text content, height CSS style, ...) it won't be the target of DnD events,
// instead the children will be. Typical event sequence when dragging something into an element
// without children: elem-dragenter, elem-dragover (fired when mouse is moved, and continuously
// when mouse isn't moved, but only in IE). If there is a child sequence is: elem-dragenter, 
// elem.parent-dragleave, elem-dragover. Esc key and entering an embedded iFrame lead to a single
// dragleave.
// Solution: delay handling of dragleave and cancel this delay if a new dragenter or dragover is
// encountered. If there are embedded iFrames attach DnD event listeners to their documents (if not
// same domain, caller could show an overlay if dropzone is entered but won't help if dragged
// directly into the iFrame).
(function() {
    'use strict';
    
    // defaults for the options passed to the constructor, for documentation look there.
    const defaultOptions = {
        attachToIframes: false,
        dropzoneEnteredCallback: undefined,
        dropzoneLeftCallback: undefined,
        multiple: true,
        stopPropagation: true,
        typeRegex: undefined
    };
    
    const fileSystemApiSupport = !!(window.DataTransferItem && DataTransferItem.prototype.webkitGetAsEntry);
    const fileDndSupport = !!(window.DragEvent && window.DataTransfer && (fileSystemApiSupport || window.FileReader && window.Blob));
    
    function addEventListener(elem, eventName, handlerFunction, bindTarget) {
        var boundHandler = handlerFunction.bind(bindTarget);
        elem.addEventListener(eventName, boundHandler);
        return boundHandler;
    }
    
    function addEventListenersToIframes(elem, boundOnDragenter, boundOnDragover, boundOnDragleave) {
        var i, l, frameDocument;
        var iframes = elem.querySelectorAll('iframe');
        for (i = 0, l = iframes.length; i < l; i++) {
            try {
                frameDocument = iframes[i].contentDocument.documentElement;
                frameDocument.addEventListener('dragenter', boundOnDragenter);
                frameDocument.addEventListener('dragover', boundOnDragover);
                frameDocument.addEventListener('dragleave', boundOnDragleave);
            } catch (e) {
                // no access to iframe, can't add listener
            }
        }
    }
    
    function clearDropzoneLeftTimeout(dropzone) {
        if (dropzone.dropzoneLeftTimeout) {
            clearTimeout(dropzone.dropzoneLeftTimeout);
            dropzone.dropzoneLeftTimeout = undefined;
        }
    }
    
    function containsDirectory(dataTransferItems) {
        var i, l, item;
        for (i = 0, l = dataTransferItems.length; i < l; i++) {
            item = dataTransferItems[i];
            if (item.kind === 'file' && item.webkitGetAsEntry().isDirectory) {
                return true;
            }
        }
        return false;
    }
    function containsUnsupportedMimeType(files, mimeTypeRegex) {
        var i, l, file, mimeType;
        if (mimeTypeRegex) {
            for (i = 0, l = files.length; i < l; i++) {
                file = files[i];
                if (file.type == undefined) {
                    mimeType = '';
                } else {
                    mimeType = file.type;
                }
                if (!mimeTypeRegex.test(mimeType)) {
                    return true;
                }
                mimeTypeRegex.lastIndex = 0;
            }
        }
        return false;
    }
    
    function handleDropzoneEntered(dropzone, event) {
        var i, l, types;
        clearDropzoneLeftTimeout(dropzone);
        // since dragenter is fired for all child nodes, ensure we only check validity of data once
        // and don't invoke callback for each child
        if (!dropzone.dropzoneEntered) {
            dropzone.dropzoneEntered = true;
            types = event.dataTransfer.types;
            for (i = 0, l = types.length; i < l; i++) {
                if (types[i] === 'Files') {
                    dropzone.validData = true;
                    if (dropzone.options.dropzoneEnteredCallback) {
                        dropzone.options.dropzoneEnteredCallback.call(null);
                    }
                    break;
                }
            }
        }
    }
    
    function handleDropzoneLeft() {
        this.dropzoneLeftTimeout = undefined;
        if (this.validData && this.options.dropzoneLeftCallback) {
            this.options.dropzoneLeftCallback.call(null);
        }
        this.dropzoneEntered = false;
        this.validData = false;
    }
    
    function handleFileReaderDirectoryCheckComplete(asyncDirCheckContext, success, event) {
        asyncDirCheckContext.completeCount++;
        if (!success) {
            asyncDirCheckContext.directoryContained = true;
        }
        // when all are completed continue
        if (asyncDirCheckContext.files.length === asyncDirCheckContext.completeCount) {
            if (asyncDirCheckContext.directoryContained) {
                notifyDropError(asyncDirCheckContext.dropzone.dropCallback, Dropzone.ERROR_DIRECTORY);
            } else {
                handleFilesDropped(asyncDirCheckContext.dropzone, asyncDirCheckContext.files);
            }
        }
    }
    
    function handleFilesAndDirectoriesDropped(dropzone, files) {
        var i, l, file, blob, end, fileReader;
        var asyncDirCheckContext = {
            directoryContained: false,
            completeCount: 0,
            dropzone: dropzone,
            files: files
        };
        var errorCallback = handleFileReaderDirectoryCheckComplete.bind(null, asyncDirCheckContext, false);
        var successCallback = handleFileReaderDirectoryCheckComplete.bind(null, asyncDirCheckContext, true);
        for (i = 0, l = files.length; i < l; i++) {
            file = files[i];
            end = file.size > 64 ? 64 : file.size;
            // extract a subset and pass to a FileReader, if it is a directory it should fail
            blob = file.webkitSlice ? file.webkitSlice(0, end) : file.slice(0, end);
            fileReader = new FileReader();
            fileReader.onerror = errorCallback;
            fileReader.onload = successCallback;
            fileReader.readAsDataURL(blob);
        }
    }
    
    function handleFilesDropped(dropzone, files) {
        if (containsUnsupportedMimeType(files, dropzone.options.typeRegex)) {
            notifyDropError(dropzone.dropCallback, Dropzone.ERROR_TYPE);
        } else {
            notifyDropSuccess(dropzone.dropCallback, files);
        }
    }
    
    function onDragenter(event) {
        handleDropzoneEntered(this, event);
        // IE requires to prevent all dragenter events otherwise not all dragleave events would be
        // triggered and we can't reset the 'entered' state
        event.preventDefault();
        if (this.validData) {
            this.stopPropagation(event);
            event.dataTransfer.dropEffect = 'copy';
        } else {
            // since we canceled the event which means dropping is allowed, give feedback that
            // dropping is not allowed. However, doesn't work in IE (cursor will flicker).
            event.dataTransfer.dropEffect = 'none';
        }
    }
    
    function onDragleave(event) {
        // avoid double firing of leave if mouse is moved fast over the child nodes
        clearDropzoneLeftTimeout(this);
        // pretty high timeout, only for IE because the dragover isn't always fired fast enough. Chrome
        // and FF work with 0ms flawlessly.
        this.dropzoneLeftTimeout = setTimeout(this.boundHandleDropzoneLeft, 150);
        // no need to preventDefault because event is not cancelable
        if (this.validData) {
            this.stopPropagation(event);
        }
    }
    
    function onDragover(event) {
        // in case dragover fires too slow the dropzoneLeftTimeout could have been reached. For this
        // case we treat dragover like dragenter. This could cause flicker in browser if entered/left
        // callbacks do CSS modifications, but dropping still works.
        handleDropzoneEntered(this, event);
        if (this.validData) {
            event.dataTransfer.dropEffect = 'copy';
            event.preventDefault();
            this.stopPropagation(event);
        }
    }
    
    function onDrop(event) {
        var files;
        if (this.validData) {
            event.preventDefault();
            this.stopPropagation(event);
            if (this.dropCallback) {
                files = event.dataTransfer.files;
                if (!files.length) {
                    // IE only: at least one directory was part of the dropped files
                    notifyDropError(this.dropCallback, Dropzone.ERROR_DIRECTORY);
                } else if (files.length > 1 && !this.options.multiple) {
                    notifyDropError(this.dropCallback, Dropzone.ERROR_MULTIPLE);
                } else {
                    if (fileSystemApiSupport) {
                        if (containsDirectory(event.dataTransfer.items)) {
                            notifyDropError(this.dropCallback, Dropzone.ERROR_DIRECTORY);
                        } else {
                            handleFilesDropped(this, files);
                        }
                    } else {
                        // need another way of checking for directories, but not IE because already
                        // handled (see above). IE doesn't implement Filereader.readAsBinaryString
                        if (!window.FileReader.prototype.readAsBinaryString) {
                            handleFilesDropped(this, files);
                        } else {
                            handleFilesAndDirectoriesDropped(this, files);
                        }
                    }
                }
            }
            this.validData = false;
        }
        this.dropzoneEntered = false;
        clearDropzoneLeftTimeout(this);
    }
    
    function removeEventListenersFromIframes(elem, boundOnDragenter, boundOnDragover, boundOnDragleave) {
        var i, l, frameDocument;
        var iframes = elem.querySelectorAll('iframe');
        for (i = 0, l = iframes.length; i < l; i++) {
            try {
                frameDocument = iframes[i].contentDocument.documentElement;
                frameDocument.removeEventListener('dragenter', boundOnDragenter);
                frameDocument.removeEventListener('dragover', boundOnDragover);
                frameDocument.removeEventListener('dragleave', boundOnDragleave);
            } catch (e) {
                // no access to iframe, no need to remove listener
            }
        }
    }
    
    function noop(){
    }
    
    function notifyDropError(dropCallback, error) {
        dropCallback.call(null, {success: false, error: error});
    }
    
    function notifyDropSuccess(dropCallback, files) {
        dropCallback.call(null, {success: true, files: files});
    }
    
    function stopPropagation(event) {
        event.stopPropagation();
    }
    
    /**
     * Turn an element into a dropzone for files. If the browser doesn't supports drag and drop
     * (DnD) operations for files the constructor won't do anything.
     * 
     * @param {[String|Element]} elem - CSS selector of an element or an element which should be
     *            turned into a dropzone.
     * @param {Function} dropCallback - function to be invoked when a file is dropped in the
     *            dropzone. The function will be passed an object with a boolean 'success' attribute.
     *            If success is true, the object will contain a 'files' attribute holding an array
     *            of File objects of the dropped files. In case success is false, the drop caused an
     *            error and the object's 'error' member will be set to one of the ERROR_* constants.
     * @param {?Object} options - optional options for customizing the dropzone
     * @param {?boolean} options.attachToIframes - scan for child iFrames and try to observe the
     *            document of the frames for DnD events. iFrames of other domains will be ignored.
     *            Should be true if there are embedded iFrames and they should be treated as part
     *            of the dropzone. If false, a drag into an iFrame would lead to an invocation of
     *            the dropzoneLeftCallback and drops will be ignored. Default is false.
     * @param {?Function} options.dropzoneEnteredCallback - function to be invoked when a file is
     *            dragged into the dropzone. Can be used to give visual feedback that files can be
     *            dropped.
     * @param {?Function} options.dropzoneLeftCallback - function to be invoked when a file is
     *            dragged out of the dropzone or the DnD operation was canceled (ESC key)
     * @param {?boolean} options.multiple - whether multiple files can be dropped. If false, a drop
     *            of multiple files will lead to error ERROR_MULTIPLE. Default is true.
     * @param {?boolean} options.stopPropagation - whether to stop propagation of the DnD events if
     *            a file is dragged into the dropzone. Default is true.
     * @param {?RegExp} options.typeRegex - regular expression to test the mime type of the dropped
     *            files. If any file has a mime type which doesn't match, error ERROR_TYPE is
     *            passed to the drop callback. If falsy, the mime type isn't checked. Note: this
     *            relies on what the browser provides as mime type which can be empty for not so
     *            common files and is often derived from the file extension.
     * 
     * @class
     */
    var Dropzone = function(elem, dropCallback, options) {
        if (typeof elem === 'string') {
            elem = document.querySelector(elem);
        }
        if (!elem || !elem.addEventListener) {
            throw new Error('No element provided for adding the drag and drop event listener');
        }
        if (fileDndSupport) {
            this.elem = elem;
            this.dropCallback = dropCallback;
            this.options = Object.assign({}, defaultOptions, options);
            this.dropzoneEntered = false;
            this.dropzoneLeftTimeout = undefined;
            this.validData = false;
            this.boundOnDragenter = addEventListener(elem, 'dragenter', onDragenter, this);
            this.boundOnDragover = addEventListener(elem, 'dragover', onDragover, this);
            this.boundOnDragleave = addEventListener(elem, 'dragleave', onDragleave, this);
            this.boundOnDrop = addEventListener(elem, 'drop', onDrop, this);
            this.boundHandleDropzoneLeft = handleDropzoneLeft.bind(this);
            if (this.options.attachToIframes) {
                addEventListenersToIframes(elem, this.boundOnDragenter, this.boundOnDragover,
                        this.boundOnDragleave);
            }
            if (this.options.stopPropagation) {
                this.stopPropagation = stopPropagation;
            } else {
                this.stopPropagation = noop;
            }
        }
    };
    // drop error denoting that a directory was dropped
    Dropzone.ERROR_DIRECTORY = 'directory';
    // drop error denoting that a multiple files were dropped although options.multiple is false
    Dropzone.ERROR_MULTIPLE = 'multiple';
    // drop error denoting that option.typeRegex was given and one of the dropped files has a not
    // matching mime type
    Dropzone.ERROR_TYPE = 'mimeType';
    
    /**
     * @return {boolean} whether dragging and dropping files is supported by the browser. If false
     * the constructor won't create a dropzone.
     */
    Dropzone.isDragAndDropSupported = function() {
        return fileDndSupport;
    };
    /**
     * Destroy the dropzone created for the element passed to the constructor so it doesn't handle
     * any DnD events anymore.
     */
    Dropzone.prototype.destroy = function() {
        if (this.boundOnDragenter) {
            this.elem.removeEventListener('dragenter', this.boundOnDragenter);
            this.elem.removeEventListener('dragleave', this.boundOnDragleave);
            this.elem.removeEventListener('dragover', this.boundOnDragover);
            this.elem.removeEventListener('drop', this.boundOnDrop);
            if (this.options.attachToIframes) {
                removeEventListenersFromIframes(this.elem, this.boundOnDragenter,
                        this.boundOnDragover, this.boundOnDragleave);
            }
            this.boundOnDragenter = undefined;
            this.boundOnDragover = undefined;
            this.boundOnDragleave = undefined;
            this.boundOnDrop = undefined;
        }
        clearDropzoneLeftTimeout(this);
        this.validData = false;
    };
    // TODO remove
    Dropzone.createTestElement = function() {
        var div = document.body.appendChild(document.createElement('div'));
        div.innerHTML = '<div id="wrapper-el" style="margin: 20px;border: 1px solid;"><div id="outer" style="background-color: greenyellow;"><div id="first-child" style="padding: 5px;background-color: aliceblue;"><div id="second-child" style="background-color: beige;padding: 5px;"><div id="inner" style="height: 80px;background-color: burlywood;">blah blubbber</div></div></div></div></div>';
    };
    communote.classes.FileDropzone = Dropzone;
})();