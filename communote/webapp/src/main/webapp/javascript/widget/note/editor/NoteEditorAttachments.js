(function(window) {
    const communote = window.communote;
    const i18n = communote.i18n;
    const DROP_OVERLAY_CSS_CLASS = 'cn-note-editor-file-dropzone';

    function addAttachment(handler, attachmentData) {
        var idx = handler.attachmentIds.indexOf(attachmentData.id);
        if (idx < 0) {
            handler.attachmentIds.push(attachmentData.id);
            handler.attachmentData.push(attachmentData);
            handler.attachmentAdded(attachmentData);
        }
    }
    
    /**
     * Click event handler which triggers a click on the file input element. Function needs to be bound to the
     * NoteEditorAttachments instance.
     * @param {Event} event - the click event
     */
    function clickFileInputElement(event) {
        var inputElem = this.widget.domNode.querySelector('input[type=file]');
        if (inputElem) {
            inputElem.click();
        }
        event.preventDefault();
    }
    
    function createDropOverlay(parentElement) {
        var descrElement;
        var overlayElement = parentElement.querySelector('.' + DROP_OVERLAY_CSS_CLASS);
        if (!overlayElement) {
            overlayElement = parentElement.ownerDocument.createElement('div');
            overlayElement.classList.add(DROP_OVERLAY_CSS_CLASS);
            overlayElement.innerHTML = '<div class="' + DROP_OVERLAY_CSS_CLASS + '-description-wrapper">'
                + '<div class="' + DROP_OVERLAY_CSS_CLASS + '-description"></div></div>';
            descrElement = overlayElement.querySelector('.' + DROP_OVERLAY_CSS_CLASS + '-description');
            descrElement.textContent = i18n.getMessage('widget.createNote.attachments.dropzone.description');
            parentElement.appendChild(overlayElement);
        }
        return overlayElement;
    }
    
    /**
     * FileDropzone callback function for drop events. Needs to be bound the NoteEditorAttachments instance.
     *  
     * @param {Object} dropResult - the drop result as defined by FileDropzone
     */
    function fileDropzoneDropCallback(dropResult) {
        var msgKey;
        fileDropzoneLeftCallback.call(this);
        if (dropResult.success) {
            this.attachmentUploader.uploadFiles(dropResult.files);
        } else {
            if (dropResult.error === communote.classes.FileDropzone.ERROR_DIRECTORY) {
                msgKey = 'widget.createNote.attachments.dropzone.droperror.directory';
            } else {
                // expecting no other errors because multiple is allowed and mimeType is not restricted
                if (window.console && typeof console.log === 'function') {
                    console.log('Unexpected error while dropping file: ' + dropResult.error);
                }
                msgKey = 'common.error.unspecified';
            }
            showError(i18n.getMessage(msgKey));
        }
    }
    
    /**
     * FileDropzone callback function which is invoked when files are dragged into the dropzone.
     * Needs to be bound the NoteEditorAttachments instance.
     */
    function fileDropzoneEnteredCallback() {
        var overlayElement = this.widget.domNode.querySelector('.' + DROP_OVERLAY_CSS_CLASS);
        overlayElement.style.display = 'block';
    }
    /**
     * FileDropzone callback function which is invoked when files are dragged out of the dropzone or DnD is canceled.
     * Needs to be bound the NoteEditorAttachments instance.
     */
    function fileDropzoneLeftCallback() {
        var overlayElement = this.widget.domNode.querySelector('.' + DROP_OVERLAY_CSS_CLASS);
        overlayElement.style.display = 'none';
    }

    /**
     * BinaryDataPasteListener callback function which is invoked when data is pasted.
     * Needs to be bound the NoteEditorAttachments instance.
     */
    function pasteHandlerCallback(blobDescriptors) {
        if (this.attachmentUploader) {
            this.attachmentUploader.uploadBlobs(blobDescriptors);
        }
    };
    
    function showError(errorMessage) {
        hideNotification();
        showNotification(NOTIFICATION_BOX_TYPES.error, '', errorMessage, {
            duration: ''
        });
    }

    /**
     * Create a NoteEditorComponent which handles attachment uploads.
     * 
     * @param noteEditorWidget The note editor widget
     * @param {String} action The action/mode of the widget
     * @param {String} initialRenderStyle The initial render style the widget was created with.
     * @param {Object} options The settings (staticParameters) the widget was created and
     *            initialized with.
     * 
     * @class
     */
    function AttachmentHandler(noteEditorWidget, action, initialRenderStyle, options) {
        this.widget = noteEditorWidget;
        this.widgetId = noteEditorWidget.widgetId;
        this.modified = false;
        this.dirty = false;
        this.attachmentSelectionShown = false;
        this.canShowAttachmentSelection = initialRenderStyle === 'full';
        this.attachmentsContainerElem = null;
        this.attachmentUploader = null;
        this.fileDropzone = null;
        // lookup for the IDs
        this.attachmentIds = [];
        // attachment data with all details. The ID of each entry can be found in attachmentIds at the same index.
        this.attachmentData = [];
        noteEditorWidget.addEventListener('widgetRefreshed', this.onWidgetRefreshed, this);
        noteEditorWidget.addEventListener('noteDiscarded', this.onNoteDiscarded, this);
        noteEditorWidget.addEventListener('renderStyleChanged', this.onRenderStyleChanged, this);
        noteEditorWidget.addEventListener('editorRefreshed', this.onEditorRefreshed, this);
        noteEditorWidget.addEventListener('widgetRefreshing', this.cleanup, this);
    }

    /**
     * Adds one or more attachments described by attachment data object.
     * 
     * @param {(Object|Object[])} attachmentData A description of the attachment including the
     *            members 'id', 'fileName', 'mime'.
     */
    AttachmentHandler.prototype.addAttachments = function(attachmentData) {
        var i, l;
        if (!attachmentData) {
            return;
        }
        if (Array.isArray(attachmentData)) {
            for (i = 0, l = attachmentData.length; i < l; i++) {
                addAttachment(this, attachmentData[i]);
            }
        } else {
            addAttachment(this, attachmentData);
        }
    };
    
    /**
     * @protected
     */
    AttachmentHandler.prototype.addUploadDescription = function() {
        var msgKey, elem, selectFilesElem;
        var dndSupport = communote.classes.FileDropzone && communote.classes.FileDropzone.isDragAndDropSupported();
        var pasteSupport = communote.classes.BinaryDataPasteListener
            && communote.classes.BinaryDataPasteListener.isPasteSupported();
        if (dndSupport && pasteSupport) {
            msgKey = 'widget.createNote.attachments.description.select_dnd_paste';
        } else if (dndSupport) {
            msgKey = 'widget.createNote.attachments.description.select_dnd';
        } else if (pasteSupport) {
            msgKey = 'widget.createNote.attachments.description.select_paste';
        } else {
            msgKey = 'widget.createNote.attachments.description.select';
        }
        elem = this.widget.domNode.querySelector('#' + this.widgetId + '-attachments-description');
        elem.innerHTML = i18n.getMessage(msgKey);
        selectFilesElem = elem.querySelector('a');
        if (selectFilesElem) {
            selectFilesElem.role = 'button';
            selectFilesElem.addEventListener('click', clickFileInputElement.bind(this));
        }
    };

    /**
     * Implementation of the NoteEditorComponent method which appends the data of this component.
     * 
     * @param {Object} noteData Object for adding the data to
     * @param {boolean} resetDirtyState Whether to set the internal dirty state to not-dirty.
     */
    AttachmentHandler.prototype.appendNoteData = function(noteData, resetDirtyState) {
        noteData.attachments = this.attachmentData;
        if (resetDirtyState) {
            this.dirty = false;
        }
    };

    /**
     * Implementation of the NoteEditorComponent method which appends the data of this component in
     * the format which is understood by the REST API.
     * 
     * @param {Object} noteData Object for adding the data to
     * @param {boolean} resetDirtyState Whether to set the internal dirty state to not-dirty.
     */
    AttachmentHandler.prototype.appendNoteDataForRestRequest = function(noteData, publish,
            resetDirtyState) {
        noteData.attachmentIds = this.attachmentIds;
        if (resetDirtyState) {
            this.dirty = false;
        }
    };

    /**
     * Called after an attachment has been added. Updates the view.
     * 
     * @param {Object} attachmentData JSON object describing the attached attachment
     * 
     * @protected
     */
    AttachmentHandler.prototype.attachmentAdded = function(attachmentData) {
        var elem, newAttachmentElem, html, uploadElem, wrapper;
        this.dirty = true;
        this.modified = true;
        wrapper = this.attachmentsContainerElem.querySelector('#' + this.widgetId
                + '-attachments-summary');
        wrapper.classList.remove('cn-hidden');
        if (attachmentData.uploadId) {
            uploadElem = wrapper.querySelector('#' + this.widgetId + '-upload-'
                    + attachmentData.uploadId);
        }
        newAttachmentElem = document.createElement('div');
        newAttachmentElem.id = this.widgetId + '-attachment-' + attachmentData.id
        html = '<span class="cn-attachment-filename"><span></span><a href="" target="_blank"></a></span>'
                + '<span class="cn-attachment-filesize"></span>'
                + '<span class="cn-attachment-remove"><a href="javascript:;" class="cn-icon" title="'
                + i18n.getMessage('blogpost.create.attachments.remove.tooltip')
                + '">&nbsp;</a></span>';
        newAttachmentElem.innerHTML = html;
        elem = newAttachmentElem.querySelector('.cn-attachment-filename span');
        elem.textContent = attachmentData.fileName;
        elem.title = attachmentData.fileName;
        elem = newAttachmentElem.querySelector('a');
        elem.textContent = attachmentData.fileName;
        elem.title = attachmentData.fileName;
        elem.href = buildRequestUrl('/portal/files/' + attachmentData.id + '/'
                + attachmentData.fileName);
        elem = newAttachmentElem.querySelector('.cn-attachment-filesize');
        elem.textContent = attachmentData.size;
        elem = newAttachmentElem.querySelector('.cn-attachment-remove a');
        elem.addEventListener('click', this.removeAttachment.bind(this, attachmentData.id));
        if (uploadElem) {
            uploadElem.parentNode.replaceChild(newAttachmentElem, uploadElem);
        } else {
            wrapper.appendChild(newAttachmentElem);
        }

    };
    /**
     * Called after an attachment has been removed. Updates the view.
     * 
     * @param {String} id Identifier of the removed attached. Will be null if all attachments have
     *            been removed.
     * 
     * @protected
     */
    AttachmentHandler.prototype.attachmentRemoved = function(id) {
        this.dirty = true;
        this.modified = true;
        this.removeAttachmentOrUploadElement(id ? '-attachment-' + id : null);
    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the note can be
     * published.
     * 
     * @return {boolean} false if uploads are still in progress, true otherwise
     */
    AttachmentHandler.prototype.canPublishNote = function() {
        if (this.attachmentUploader) {
            // TODO show a error message or hint if uploads are still running?
            return !this.attachmentUploader.hasRunningUploads();
        }
        return true;
    };

    /**
     * @protected
     */
    AttachmentHandler.prototype.cleanup = function() {
        // remove DnD handling while refreshing to avoid strange side effects
        if (this.fileDropzone) {
            this.fileDropzone.destroy();
            this.fileDropzone = null;
        }
    };
    
    /**
     * @protected
     */
    AttachmentHandler.prototype.getToggleElement = function() {
        return this.widget.domNode.querySelector('#' + this.widgetId + '-accessory-attachment');
    };
    
    AttachmentHandler.prototype.getUnconfirmedInputWarning = function() {
    	return null;
    };

    AttachmentHandler.prototype.hideAttachmentSelection = function() {
        var toggleElem;
        if (this.attachmentSelectionShown) {
            toggleElem = this.getToggleElement();
            if (this.attachmentsContainerElem && toggleElem) {
                toggleElem.style.display = '';
                this.attachmentsContainerElem.style.display = 'none';
                this.attachmentSelectionShown = false;
            }
        }
    };

    /**
     * Implementation of the NoteEditorComponent method which initializes the data managed by this
     * component.
     * 
     * @param {?Object} noteData Object with details about the note to initialize with.
     */
    AttachmentHandler.prototype.initContent = function(noteData) {
        this.removeAttachment(null);
        if (noteData) {
            this.addAttachments(noteData.attachments);
        }
        this.showHideAttachmentSelection();
        this.dirty = false;
        this.modified = false;

    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the data managed by this
     * component is dirty.
     * 
     * @return {boolean} true if attachments were removed or added after the dirty state had been
     *         reset
     */
    AttachmentHandler.prototype.isDirty = function() {
        return this.dirty;
    };

    /**
     * Implementation of the NoteEditorComponent method which tests whether the data managed by this
     * component has been modified.
     * 
     * @return {boolean} true if attachments were removed or added after the content had been
     *         initialized
     */
    AttachmentHandler.prototype.isModified = function() {
        return this.modified;
    };

    /**
     * Event handler which is triggered after the upload by the AjaxFileUpload completed.
     * 
     * @protected
     */
    AttachmentHandler.prototype.onAttachmentUploadDone = function(uploadId, jsonResponse) {
        var attachmentData;
        // check for error message
        if (jsonResponse.status == 'ERROR') {
            this.onAttachmentUploadFailed(uploadId, jsonResponse.message);
        } else {
            attachmentData = jsonResponse.result;
            attachmentData.uploadId = uploadId;
            this.addAttachments(attachmentData);
            // empty input field (setting value of input to '' won't work in IE)
            this.attachmentsContainerElem.querySelector('form').reset();
        }
    };

    /**
     * Event handler which is triggered after the upload by the AjaxFileUpload failed.
     * 
     * @protected
     */
    AttachmentHandler.prototype.onAttachmentUploadFailed = function(uploadId, errorMessage) {
        if (errorMessage) {
            showError(errorMessage);
        }
        this.removeAttachmentOrUploadElement('-upload-' + uploadId);
    };

    /**
     * Event handler which is triggered after the upload by the AjaxFileUpload started.
     * 
     * @protected
     */
    AttachmentHandler.prototype.onAttachmentUploadStarted = function(uploadDescriptor) {
        var feedbackElem, elem, fileName, wrapper;
        if (!this.canShowAttachmentSelection) {
            // force 'full' render style on widget, if this is successful the attachment selection can be shown
            this.widget.setRenderStyle('full');
        }
        // show attachment area if still hidden
        if (this.canShowAttachmentSelection) {
            this.showAttachmentSelection();
        }
        // add an element which shows that the upload is in progress
        fileName = uploadDescriptor.fileName;
        wrapper = this.attachmentsContainerElem.querySelector('#' + this.widgetId
                + '-attachments-summary');
        wrapper.classList.remove('cn-hidden');

        feedbackElem = document.createElement('div');
        feedbackElem.id = this.widgetId + '-upload-' + uploadDescriptor.uploadId;
        feedbackElem.classList.add('cn-upload-process');
        feedbackElem.innerHTML = '<span class="cn-attachment-filename"></span>';
        elem = feedbackElem.querySelector('.cn-attachment-filename');
        elem.textContent = i18n.getMessage('blogpost.create.attachments.uploading', [ fileName ]);
        elem.title = fileName;
        wrapper.appendChild(feedbackElem);
    };

    AttachmentHandler.prototype.onEditorRefreshed = function(editor) {
        if (communote.classes.BinaryDataPasteListener) {
            new communote.classes.BinaryDataPasteListener(editor.getInputElement(),
                    pasteHandlerCallback.bind(this), {
                fileNamePrefix: 'image-',
                typeRegex: /image\//
            });
        }
        if (communote.classes.FileDropzone && communote.classes.FileDropzone.isDragAndDropSupported()) {
            createDropOverlay(this.widget.domNode);
            if (this.fileDropzone) {
                this.fileDropzone.destroy();
            }
            this.fileDropzone = new communote.classes.FileDropzone(this.widget.domNode, 
                    fileDropzoneDropCallback.bind(this), {
                attachToIframes: true,
                dropzoneEnteredCallback: fileDropzoneEnteredCallback.bind(this),
                dropzoneLeftCallback: fileDropzoneLeftCallback.bind(this)
            });
        }
    };

    AttachmentHandler.prototype.onNoteDiscarded = function(onlineAutosave) {
        var i, l, noteUtils;
        // if there was no online autosave we have to delete the attachments from server.
        // Even when editing because we are working with a copy of the note.
        if (!onlineAutosave) {
            noteUtils = communote.utils.noteUtils;
            for (i = 0, l = this.attachmentIds.length; i < l; i++) {
                // not really interested in the server response as the job
                // will clean-up the attachments that could not be deleted
                noteUtils.deleteAttachment(this.attachmentIds[i]);
            }
        }
        // note: not resetting the local store as we expect to receive an initContent call if the widget is not removed
    };

    AttachmentHandler.prototype.onRenderStyleChanged = function(changeDescriptor) {
        this.canShowAttachmentSelection = changeDescriptor.newStyle === 'full';
        this.showHideAttachmentSelection();
    };

    AttachmentHandler.prototype.onWidgetRefreshed = function() {
        var field, elem;
        this.attachmentsContainerElem = this.widget.domNode
                .querySelector('.cn-write-note-accessory-attachment');
        field = this.widget.domNode.querySelector('input[type=file]');
        if (field) {
            this.attachmentUploader = new AjaxFileUpload(field, {
                uploadOnChange: true,
                multiple: true
            });
            this.attachmentUploader.addEvent('uploadStarting', this.onAttachmentUploadStarted
                    .bind(this));
            this.attachmentUploader.addEvent('uploadFileNotFound', this.onAttachmentUploadFailed
                    .bind(this));
            this.attachmentUploader.addEvent('uploadFailed', this.onAttachmentUploadFailed
                    .bind(this));
            this.attachmentUploader.addEvent('uploadDone', this.onAttachmentUploadDone.bind(this));
            this.addUploadDescription();
        }
        elem = this.getToggleElement();
        if (elem) {
            elem.addEventListener('click', this.showAttachmentSelection.bind(this));
        }
        this.attachmentSelectionShown = false;
    };

    AttachmentHandler.prototype.removeAttachment = function(id) {
        var idx;
        if (id == null) {
            if (this.attachmentIds.length) {
                this.attachmentIds = [];
                this.attachmentData = [];
                this.attachmentRemoved(null);
            }
        } else {
            idx = this.attachmentIds.indexOf(id);
            if (idx !== -1) {
                this.attachmentIds.splice(idx, 1);
                this.attachmentData.splice(idx, 1);
                this.attachmentRemoved(id);
            }
        }
    };
    /**
     * @protected
     */
    AttachmentHandler.prototype.removeAttachmentOrUploadElement = function(elemIdSuffix) {
        var elem, checkHide;
        var wrapper = this.attachmentsContainerElem.querySelector('#' + this.widgetId
                + '-attachments-summary');
        if (elemIdSuffix) {
            elem = wrapper.querySelector('#' + this.widgetId + elemIdSuffix);
            if (elem) {
                elem.destroy();
                checkHide = true;
            }
        } else {
            // destroy all
            wrapper.getChildren().destroy();
            checkHide = true;
        }
        if (checkHide && wrapper.children.length === 0) {
            wrapper.addClass('cn-hidden');
        }
    };

    AttachmentHandler.prototype.showAttachmentSelection = function() {
        var toggleElem;
        if (!this.attachmentSelectionShown) {
            toggleElem = this.getToggleElement();
            if (this.attachmentsContainerElem && toggleElem) {
                toggleElem.style.display = 'none';
                this.attachmentsContainerElem.style.display = '';
                this.attachmentSelectionShown = true;
            }
        }
    };

    AttachmentHandler.prototype.showHideAttachmentSelection = function() {
        if (this.canShowAttachmentSelection && this.attachmentIds.length) {
            this.showAttachmentSelection();
        } else {
            this.hideAttachmentSelection();
        }
    };

    // publish class to allow subclassing or modification by add-ons
    communote.classes.NoteEditorAttachments = AttachmentHandler;
    // register for all modes
    communote.NoteEditorComponentFactory.register('*', AttachmentHandler);
})(this);