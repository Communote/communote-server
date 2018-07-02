var AjaxFileUpload = new Class({

    Implements: [ Options, Events ],

    options: {
        // allow selection of multiple files if the browser supports it. If undefined, the
        // 'multiple' attribute of the input field is evaluated. If true, the selection of
        // multiple files is enabled. If false, the selection of multiple files is disabled.
        multiple: undefined,
        // the URL to upload the file to, overrides the action attribute of the form if set
        uploadUrl: null,
        // set to true if the file should be uploaded when the content of the input changed.
        // when false the form must be submitted.
        uploadOnChange: false,
        // if true a response that does not contain a response JSON object is considered a failure
        // and the uploadFailed event will be fired. Otherwise the uploadDone event will be fired 
        // and the response object will be passed as argument if available.
        requireResponseObject: true,
        // defines a parameter to be added to the form to inform the server that the response
        // should be wrapped in an HTML page with a script tag that defines a global variable
        // named response that holds the response. If this parameter is not set the response
        // is expected to be JSON.
        htmlResponseParameter: 'htmlResponse',
        defaultErrorMessage: 'Upload failed.'
    },

    uploadForm: null,
    targetIframeId: null,
    boundIframeOnloadEvent: null,
    formDataUpload: false,
    fileInputName: null,
    uploadIdBase: '',
    uploadIdCount: 0,
    runningUploads: null,

    initialize: function(fileInput, options) {
        var form;
        var elem = document.id(fileInput);
        if (elem.type != 'file') {
            throw new Error('This class can only be applied to inputs of type file');
        }
        this.setOptions(options);
        form = elem.getParent('form');
        // use FormData if available and file API is supported otherwise use iframe method
        if (Request.prototype.postFormData && typeof elem.files === 'object') {
            this.initFormDataUpload(elem, form);
        } else {
            this.initIframeUpload(elem, form);
        }
        if (this.options.uploadOnChange) {
            elem.onchange = this.onFileInputChange.bind(this);
            // avoid keydowns
            elem.onkeydown = function() {
                return false;
            };
        } else {
            if (elem.onchange) {
                delete elem.onchange;
            }
            form.addEvent('submit', this.onFormSubmit.bind(this));
        }
        this.uploadForm = form;
        this.uploadIdBase = String.uniqueID();
        this.runningUploads = {};
    },
    collectDataOfForm: function() {
        var i, l, elem;
        var data = {};
        var inputElems = this.uploadForm.querySelectorAll('input');
        var responseParam = this.options.htmlResponseParameter;
        for (i = 0, l = inputElems.length; i < l; i++) {
            elem = inputElems[i];
            // skip file input
            if (elem.type !== 'file' && elem.name !== responseParam) {
                data[elem.name] = elem.value;
            }
        }
        return data;
    },
    initFormDataUpload: function(elem, form) {
        this.formDataUpload = true;
        this.fileInputName = elem.getProperty('name');
        if (!this.options.uploadUrl) {
            this.options.uploadUrl = form.getProperty('action');
        }
        if (this.options.multiple == undefined) {
            this.options.multiple = !!elem.multiple;
        } else if (elem.multiple !== undefined) {
            elem.multiple = this.options.multiple;
        }
    },
    initIframeUpload: function(elem, form) {
        var responseParam, responseParamElem;
        if (!form.getProperty('method')) {
            form.setProperty('method', 'post');
        }
        // non-IFrame upload only supports FormData, so force the same encoding
        if (form.getProperty('enctype') != 'multipart/form-data') {
            form.setProperty('enctype', 'multipart/form-data');
            // IE < 8 does not support enctype, thus set encoding. Browsers do not care if both are set.
            form.setProperty('encoding', 'multipart/form-data');
        }
        if (this.options.uploadUrl) {
            form.setProperty('action', this.options.uploadUrl);
        }
        responseParam = this.options.htmlResponseParameter;
        if (responseParam) {
            responseParamElem = form.getElement('input[name=' + responseParam + ']');
            if (!responseParamElem) {
                responseParamElem = new Element('input', {
                    type: 'hidden',
                    name: responseParam
                });
                form.grab(responseParamElem);
            }
            responseParamElem.value = 'true';
        }
        // setup iframe if it is not there
        this.initTargetIframe(form, elem);
    },

    initTargetIframe: function(form, fileInput) {
        var uploadTargetId = form.getProperty('target');
        if (!uploadTargetId) {
            if (fileInput.id) {
                uploadTargetId = fileInput.id + '_upload_iframe';
            } else {
                uploadTargetId = 'ajax_file_upload_' + new Date().getTime();
            }
            form.setProperty('target', uploadTargetId);
        }
        var iframe = document.id(uploadTargetId);
        if (!iframe) {
            iframe = new Element('iframe', {
                id: uploadTargetId,
                name: uploadTargetId
            });
            // set src to javascript:'' and not about:blank to avoid IE6 'non-secure items' warning
            iframe.setProperty('src', 'javascript:\'\'');
            iframe.setStyle('display', 'none');
            iframe.inject(form, 'after');
            iframe.onload = null;
        }
        this.targetIframeId = uploadTargetId;
    },

    attachIFrameOnLoadHandler: function() {
        var iframe = document.id(this.targetIframeId);
        if (!this.boundIframeOnloadEvent) {
            // keep a reference to allow removal of the event
            this.boundIframeOnloadEvent = this.iframeOnLoad.bind(this);
            // use mootools event attachment to get it working in IE6
            iframe.addEvent('load', this.boundIframeOnloadEvent);
        }
    },

    createUploadDescriptor: function(fileName, cancelable) {
        var id = this.uploadIdBase + (++this.uploadIdCount);
        return {
            cancelable: cancelable,
            fileName: fileName,
            uploadId: id
        };
    },

    onFileInputChange: function(event) {
        var fileInput, descriptor;
        fileInput = event ? event.target : this.getFileInput();
        if (this.formDataUpload) {
            this.uploadFiles(fileInput.files);
        } else {
            fileInput = document.id(fileInput);
            descriptor = this.prepareIFrameUpload(fileInput);
            if (descriptor) {
                try {
                    this.uploadForm.submit();
                } catch (e) {
                    delete this.runningUploads[descriptor.uploadId];
                    // IE complains when string is not a parseable fully qualified file name
                    // TODO maybe show message like "file not found"
                    this.fireEvent('uploadFileNotFound', [descriptor.uploadId, descriptor.fileName]);
                }
            }
        }
    },

    onFormSubmit: function(event) {
        var fileInput = this.getFileInput();
        if (this.formDataUpload) {
            event.stop();
            this.uploadFiles(fileInput.files);
        } else {
            if (!this.prepareIFrameUpload(fileInput)) {
                event.stop();
            }
        }
    },
    
    prepareIFrameUpload: function(fileInput) {
        var descriptor;
        var fileName = fileInput.value;
        if (fileName.length != 0 && !this.hasRunningUploads()) {
            // attach upload done handler if not done yet
            this.attachIFrameOnLoadHandler();
            descriptor = this.createUploadDescriptor(fileName, false);
            this.runningUploads[descriptor.uploadId] = {
                    fileName: fileName,
                    request: false
            };
            this.fireEvent('uploadStarting', descriptor);
            return true;
        }
        return false;
    },

    getUploadForm: function() {
        return this.uploadForm;
    },

    getFileInput: function() {
        return this.uploadForm.getElement('input[type=file]');
    },

    /**
     * Upload the given files. If multiple option is false, only the first file is uploaded.
     * 
     * @param {(File[]|FileList)} fileList - the files to upload
     */
    uploadFiles: function(fileList) {
        var formData, i, l;
        var additionalData = this.collectDataOfForm();
        // TODO add support for toggling sending with no files selected from allowed to not allowed
        if (!this.options.multiple) {
            formData = this.buildFormData(fileList[0], additionalData);
            this.postFormData(formData, fileList[0].name);
        } else {
            for (i = 0, l = fileList.length; i < l; i++) {
                formData = this.buildFormData(fileList[i], additionalData);
                this.postFormData(formData, fileList[i].name);
            }
        }
    },

    uploadBlob: function(blob, fileName) {
        var formData = this.buildFormData(null, this.collectDataOfForm());
        formData.append(this.fileInputName, blob, fileName);
        this.postFormData(formData, fileName);
    },
    

    /**
     * Upload some blobs. The multiple option is ignored.
     * 
     * @param {Object[]} descriptors - an array of objects where each entry needs to have a blob
     *            and a fileName member which hold the Blob instance and the name respectively
     */
    uploadBlobs: function(descriptors) {
        var formData, additionalData, i, descriptor;
        var length = descriptors.length;
        if (length > 0) {
            additionalData = this.collectDataOfForm();
            for (i = 0; i < length; i++) {
                descriptor = descriptors[i];
                formData = this.buildFormData(null, this.collectDataOfForm());
                formData.append(this.fileInputName, descriptor.blob, descriptor.fileName);
                this.postFormData(formData, descriptor.fileName);
            } 
        }
    },

    postFormData: function(formData, fileName) {
        var request = new Request.JSON({
            url: this.options.uploadUrl,
            noCache: true
        });
        var descriptor = this.createUploadDescriptor(fileName, true);
        request.addEvent('success', function(response) {
            this.uploadDone(descriptor.uploadId, response);
        }.bind(this));
        request.addEvent('failure', function(xhr) {
            var message;
            if (xhr && xhr.responseText) {
                try {
                    message = JSON.parse(xhr.responseText).message;
                } catch (e) {
                    // use fallback
                }
            }
            this.uploadFailed(descriptor.uploadId, message);
        }.bind(this));
        this.runningUploads[descriptor.uploadId] = {
             fileName: fileName,
             request: request
        };
        this.fireEvent('uploadStarting', descriptor);
        request.postFormData(formData);
    },

    buildFormData: function(file, additionalData) {
        var keys, i, l, key;
        var formData = new FormData();
        if (additionalData) {
            keys = Object.keys(additionalData);
            for (i = 0, l = keys.length; i < l; i++) {
                key = keys[i];
                formData.append(key, additionalData[key]);
            }
        }
        if (file) {
            formData.append(this.fileInputName, file);
        }
        return formData;
    },
    
    hasRunningUploads: function() {
        return Object.getLength(this.runningUploads) > 0;
    },

    uploadDone: function(uploadId, responseObj) {
        if (!this.runningUploads[uploadId]) {
            return;
        }
        delete this.runningUploads[uploadId];
        if (this.options.requireResponseObject) {
            if (!responseObj) {
                this.fireEvent('uploadFailed', [uploadId, this.options.defaultErrorMessage]);
                return;
            }
        }
        this.fireEvent('uploadDone', [uploadId, responseObj]);
    },
    
    uploadFailed: function(uploadId, message) {
        delete this.runningUploads[uploadId];
        this.fireEvent('uploadFailed', [uploadId, message || this.options.defaultErrorMessage]);
    },

    iframeOnLoad: function() {
        var iframe = document.id(this.targetIframeId);
        // there is only one upload when using iframe method
        var uploadId = Object.keys(this.runningUploads)[0];
        // TODO handle error, but how? The no-response case is already handled by uploadDone.
        this.uploadDone(uploadId, iframe.contentWindow.response);
        iframe.contentWindow.response = null;
    }
});

(function() {
    var progressSupport = ('onprogress' in new Browser.Request);

    if (!!window.FormData) {
        Request
                .implement({

                    postFormData: function(formData) {
                        var url, trimPosition, xhr;
                        if (!this.check(formData)) {
                            return this;
                        }
                        if (typeOf(formData) != 'object' || formData.constructor != FormData) {
                            // do a normal post
                            return this.post(formData);
                        }
                        this.options.isSuccess = this.options.isSuccess || this.isSuccess;
                        this.running = true;

                        url = this.options.url || document.location.pathname;
                        trimPosition = url.lastIndexOf('/');
                        if (trimPosition > -1 && (trimPosition = url.indexOf('#')) > -1)
                            url = url.substr(0, trimPosition);

                        if (this.options.noCache) {
                            url += (url.contains('?') ? '&' : '?') + String.uniqueID();
                        }
                        xhr = this.xhr;
                        if (progressSupport) {
                            xhr.onloadstart = this.loadstart.bind(this);
                            xhr.onprogress = this.progress.bind(this);
                        }

                        xhr.open('POST', url, this.options.async, this.options.user,
                                this.options.password);
                        if (this.options.user && 'withCredentials' in xhr) {
                            xhr.withCredentials = true;
                        }

                        xhr.onreadystatechange = this.onStateChange.bind(this);

                        // TODO block content-type?
                        Object.each(this.headers, function(value, key) {
                            try {
                                xhr.setRequestHeader(key, value);
                            } catch (e) {
                                this.fireEvent('exception', [ key, value ]);
                            }
                        }, this);

                        this.fireEvent('request');
                        xhr.send(formData);
                        if (!this.options.async) {
                            this.onStateChange();
                        } else if (this.options.timeout) {
                            this.timer = this.timeout.delay(this.options.timeout, this);
                        }
                        return this;
                    }
                });
    }
})();