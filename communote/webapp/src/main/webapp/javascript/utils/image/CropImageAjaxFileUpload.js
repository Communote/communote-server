(function() {
    // TODO check that either Blob array constructor or canvas.toBlob exist?
    var offlineCroppingSupported = undefined;
    /**
     * Check whether offline cropping is supported by looking for the required HTML5 features.
     * 
     * @return true if cropping is supported, false otherwise
     */
    function isOfflineCroppingSupported() {
        var support, canvas, arrayBuffer, byteArray;
        if (offlineCroppingSupported === undefined) {
            // cropping isn't working on iPad (browser crashes, cropper not usable, upload not always working, wrong area uploaded,...)
            support = Browser.platform !== 'ios'
                    && !!(window.File && window.FileReader && window.FileList && window.Blob && Request.prototype.postFormData);
            if (support) {
                // older browsers do not support Blob constructor or canvas.toBlob
                canvas = document.createElement('canvas');
                if (!canvas) {
                    support = false;
                } else if (!canvas.toBlob) {
                    try {
                        arrayBuffer = new ArrayBuffer(2);
                        byteArray = new Uint8Array(arrayBuffer);
                        byteArray[0] = 32;
                        byteArray[1] = 64;
                        new Blob([ byteArray.buffer ], {
                            type: 'application/octet-binary'
                        });
                    } catch (e) {
                        support = false;
                    }
                }
            }
            offlineCroppingSupported = support;
        }
        return offlineCroppingSupported;
    }

    var ImageUpload = new Class({
        Extends: AjaxFileUpload,

        options: {
            // allows to define an alternative Cropper constructor function to use another cropper
            // than the default one (Cropper class). The constructor must accept the image element
            // and an option object. The cropper instance must have a getCoordinates method that
            // returns an object containing the top, left, width and right coordinates of the
            // cropper mask within the picture.
            cropperConstructor: null,
            // function to be called to prepare the options to be passed to cropper constructor.
            // This is usually only necessary when using a customConstructor
            cropperOptionsPreparator: null,
            // whether the cropper should maintain the aspect ratio when resizing the mask
            keepRatio: false,
            // the min size of the cropper mask. Can be an object with 'width' and 'height'
            // attributes holding the pixel values.
            minSize: {
                width: 50,
                height: 50
            },
            // if true the min size will be reduced to compensate the image down scaling caused by
            // scalePopupFitViewport option
            minSizeRespectScaling: false,
            // the target size of the image in pixels as an object with width and height attribute.
            // If defined and the cropped image is smaller, the final image will be surrounded by
            // the backgroundFillColor. If the cropped image is larger than the target size it will
            // be scaled to match that size. If not defined the cropped image can have any size. 
            targetSize: null,
            // whether to show a preview popup of the image if cropping is not necessary or upload
            // directly. Cropping is not necessary if the selected image is smaller than or equal
            // to the minSize and no targetSize is defined. In case a targetSize is defined and the
            // image smaller than tpreviewIfCroppingNotNeeded the minSize, the cropper will be
            // considered necessary as the image will be expanded to the targetSize.
            previewIfCroppingNotNeeded: true,
            // whether to reset the form parent element of the file input when the cropping/preview
            // is canceled. This will clear the file input, but also other fields if the form 
            // contains more fields. Note: resetting the form is the only way to clear the input
            // that works in all browsers.
            resetFormOnCancel: true,
            // If true the image will be scaled down so that the popup showing it and the crop mask
            // will fit into the viewport. This has the benefit that the user does not need to
            // scroll while cropping. If the viewport is large enough this option has no effect.
            // In case the user resizes the browser window after the popup was rendered, the popup
            // will not be resized.
            scalePopupFitViewport: true,
            // the color to use for filling the background when the selected portion of the
            // image is smaller than the targetSize. Has to be hexadecimal color definition
            // starting with a hash.
            backgroundFillColor: '#fff',
            // defines how to place the image into the background. If less than 0 it is placed at
            // the left edge, if 0 it is centered and if greater than 0 it is positioned at the right
            // edge.
            horizontalAlignment: 0,
            // defines how to place the image into the background. If less than 0 it is placed at
            // the top edge, if 0 it is centered and if greater than 0 it is positioned at the bottom.
            verticalAlignment: 0,
            // the title of the popup when it shows a preview of the image without cropping mask
            popupTitlePreviewMode: 'Preview',
            // the title of the popup when it shows a preview of the image with cropping mask
            popupTitleCropMode: 'Select area of image',
            // the title of the accept button in the popup
            popupAcceptButtonLabel: null
        },
        cropperOptions: null,

        imgElementId: null,
        imgOrgSize: {},
        imgSize: {},
        imgIsScaled: false,
        imgFileName: '',
        targetSize: {},
        cropper: null,
        providedCropperOptions: null,
        cropperAvailable: false,
        targetAndMinSizeMatch: false,
        targetSizeDefined: false,

        initialize: function(fileInput, uploadOptions, cropperOptions) {
            uploadOptions = uploadOptions || {};
            fileInput = document.id(fileInput);
            // disable upload of multiple files
            if (fileInput.multiple == 'true') {
                fileInput.multiple = 'false';
            }
            uploadOptions.multiple = false;
            this.parent(fileInput, uploadOptions);
            if (this.options.targetSize) {
                this.targetSize.width = this.options.targetSize.width;
                this.targetSize.height = this.options.targetSize.height;
                if (this.targetSize.width > 0 && this.targetSize.height > 0) {
                    this.targetSizeDefined = true;
                    this.targetAndMinSizeMatch = this.targetSize.width == this.options.minSize.width
                            && this.targetSize.height == this.options.minSize.height;
                }
            }
            // sanitize the constructor function option
            if (this.options.cropperConstructor
                    && typeof this.options.cropperConstructor != 'function') {
                this.options.cropperConstructor = null;
            }
            this.cropperAvailable = !!(this.options.cropperConstructor || window['Cropper']);
            this.providedCropperOptions = cropperOptions;
            this.prepareCropperOptions();
            this.imgElementId = 'crop_image_' + (new Date()).getTime();
        },

        /**
         * Prepare the options to be passed to the cropper
         */
        prepareCropperOptions: function() {
            if (typeof this.options.cropperOptionsPreparator == 'function') {
                this.cropperOptions = this.options.cropperOptionsPreparator.call(this.options,
                        this.providedCropperOptions);
            } else {
                this.cropperOptions = this
                        .prepareDefaultCropperOptions(this.providedCropperOptions);
            }
        },

        /**
         * Prepare the options to be passed to the default cropper (Cropper class instance)
         * 
         * @param {Object} [providedCropperOptions] Options passed to the constructor of this class
         *            that should be merged with the default settings
         * @return {Object} options to be passed to the cropper constructor
         */
        prepareDefaultCropperOptions: function(providedCropperOptions) {
            var cropperOptions = {};
            var options = this.options;
            if (options.minSize) {
                cropperOptions.mini = {
                    x: options.minSize.width,
                    y: options.minSize.height
                };
            }
            cropperOptions.startSize = {
                x: this.targetSize.width,
                y: this.targetSize.height
            };

            cropperOptions.keepRatio = !!this.options.keepRatio;
            if (cropperOptions.keepRatio) {
                // if keeping aspect only create handles for edges
                cropperOptions.handles = [ [ 'top', 'left' ], [ 'top', 'right' ],
                        [ 'bottom', 'left' ], [ 'bottom', 'right' ] ];
            }
            cropperOptions.tooltipResizeHandle = getJSMessage('javascript.cropper.tooltip.resize');
            cropperOptions.tooltipResizeHandleKeepRatio = cropperOptions.tooltipResizeHandle;
            cropperOptions.tooltipResizer = getJSMessage('javascript.cropper.tooltip.move');
            cropperOptions.tooltipResizerDoubleClick = getJSMessage('javascript.cropper.tooltip.move.doubleclick');
            return Object.merge(cropperOptions, providedCropperOptions);
        },

        /**
         * @override Overridden to show a cropping dialog before uploading an image. If the file
         *           list is empty, does not contain an image or the browser does not support the
         *           client side upload, the parent method will be called
         */
        onFileInputChange: function(event) {
            var file;
            if (!isOfflineCroppingSupported() || !this.cropperAvailable) {
                return this.parent(event);
            }
            file = this.getImageFile(event.target);
            if (!file) {
                return this.parent(event);
            }
            this.imgFileName = file.name;
            this.loadImageFileToImg(file);
        },

        /**
         * @override Overridden to show a cropping dialog before uploading an image. If the file
         *           list is empty, does not contain an image or the browser does not support the
         *           client side upload, the parent method will be called
         */
        onFormSubmit: function(event) {
            var file;
            if (!isOfflineCroppingSupported() || !this.cropperAvailable) {
                return this.parent(event);
            }
            file = this.getImageFile(null);
            if (!file) {
                return this.parent(event);
            }
            event.stop();
            this.imgFileName = file.name;
            this.loadImageFileToImg(file);
        },

        /**
         * Get the file object from the provide input of type file.
         * 
         * @param {Element} fileInput The file input element
         * @return {File} the first file of the file list if it is an image, otherwise null
         */
        getImageFile: function(fileInput) {
            var file;
            if (!fileInput) {
                fileInput = this.getFileInput();
            }
            file = fileInput.files[0];
            // TODO define a regex of supported files
            if (file && file.type.match('image.*')) {
                return file;
            }
            return null;
        },

        /**
         * Load the image file as Data-URL into an image by using a FileReader.
         * 
         * @param {File} file The image file to load
         */
        loadImageFileToImg: function(file) {
            var reader = new FileReader();
            reader.onload = function(event) {
                this.previewImageFromDataUrl(event.target.result);
            }.bind(this);
            reader.readAsDataURL(file);
        },

        /**
         * Create an img element from the provided Data-URL and add the show it in a popup which
         * will also provide crop mask to select a portion of the image.
         * 
         * @param {String} dataUrl The Data-URL holding the image data
         */
        // TODO maybe use an Object-URL instead of a Data URL? Are there any benefits?
        previewImageFromDataUrl: function(dataUrl) {
            var imgElem = new Element('img');
            imgElem.set('id', this.imgElementId);
            imgElem.set('src', dataUrl);
            // continue after image has loaded otherwise it is not always working correctly
            imgElem.addEvent('load', function(loadEvent) {
                this.buildPreviewPopup(loadEvent.target);
            }.bind(this));
        },

        /**
         * Called when the user commits the preview popup. In case the cropper was attached the
         * selected portion will be sent to the server. If a target size was defined and the
         * selected area is smaller than that size the selection will be centered in an image of
         * target size which is filled with the color defined in backgroundFillColor option.
         */
        croppingDone: function() {
            var ratio, canvas, cropCoordinates;
            var imgElem = document.id(this.imgElementId);
            if (this.cropper) {
                cropCoordinates = this.cropper.getCoordinates();
                // since the image might be scaled to fit into the viewport, calculate the actual crop-mask
                if (this.imgIsScaled) {
                    ratio = this.imgOrgSize.width / imgElem.width;
                    cropCoordinates.left = (cropCoordinates.left * ratio).toInt();
                    cropCoordinates.width = (cropCoordinates.width * ratio).toInt();
                    ratio = this.imgOrgSize.height / imgElem.height;
                    cropCoordinates.top = (cropCoordinates.top * ratio).toInt();
                    cropCoordinates.height = (cropCoordinates.height * ratio).toInt();
                }

                canvas = this.resizeImageToCanvas(imgElem, cropCoordinates, this.targetSize.width,
                        this.targetSize.height);
                this.cropper = null;
            } else {
                // the element is already a canvas if no cropper was set and the image was smaller
                // than the target size
                if (imgElem.match('canvas')) {
                    canvas = imgElem;
                }
            }
            if (!canvas) {
                // upload original file
                this.uploadFiles(this.getFileInput().files);
            } else {
                this.uploadImageOfCanvas(canvas);
            }
        },

        croppingCanceled: function() {
            var formElem;
            // clear input
            if (this.options.resetFormOnCancel) {
                formElem = this.getFileInput().getParent('form');
                if (formElem) {
                    formElem.reset();
                }
            }
        },

        /**
         * Take the image rendered in the provided canvas and send it to the server under the name
         * of the currently selected file.
         * 
         * @param {HTMLCanvasElement} canvas The canvas whose content should be uploaded as image to
         *            the server
         */
        uploadImageOfCanvas: function(canvas) {
            // use canvas to blob if available (currently only gecko)
            if (canvas.toBlob) {
                canvas.toBlob(this.uploadImageAsBlob.bind(this));
            } else {
                this.uploadImageAsDataURL(canvas.toDataURL());
            }
        },

        /**
         * Send the image that is encoded in a Data-URL to the server.
         * 
         * @param {String} dataUrl A legal Data-URL holding the image data
         */
        uploadImageAsDataURL: function(dataUrl) {
            var splittedDataUrl, byteString, mimeType, arrayBuffer, byteArray, i;
            // parse dataURL: before comma there is protocol and optional meta-data like mime-type
            // and base64 encoding flag, after comma is the actual data
            splittedDataUrl = dataUrl.split(',');
            if (splittedDataUrl[0].indexOf('base64') != -1) {
                // decode base64
                byteString = atob(splittedDataUrl[1]);
            } else {
                // it is URL encoded
                byteString = decodeURI(splittedDataUrl[1]);
            }
            // get mime-type: starts after 'data:' and is separated with ';' from encoding
            mimeType = splittedDataUrl[0].substring(5).split(';')[0];
            // convert to blob by using an unsinged 8 bit ArrayBufferView 
            arrayBuffer = new ArrayBuffer(byteString.length);
            byteArray = new Uint8Array(arrayBuffer);
            for (i = 0; i < byteString.length; i++) {
                byteArray[i] = byteString.charCodeAt(i);
            }
            // according to spec blob can be constructed from ArrayBufferView or ArrayBuffer, since the first is not
            // working in latest Safari (6.0.5) on Mac, use the ArrayBuffer which also works in Chrome and FF
            this.uploadImageAsBlob(new Blob([ byteArray.buffer ], {
                type: mimeType
            }));
        },

        /**
         * Send the image contained in the blob to the server.
         * 
         * @param {Blob} blob The image as blob
         */
        uploadImageAsBlob: function(blob) {
            this.uploadBlob(blob, this.imgFileName);
        },

        /**
         * Create the popup showing a preview of the image and the cropping mask if necessary. The
         * image element will be hidden at first, but shown as soon as the popup has been created.
         * 
         * @param {Element} imgElem The image element.
         */
        buildPreviewPopup: function(imgElem) {
            var wrapperDiv, canvas, attachCropper, popupTitle, buttons;
            this.imgOrgSize.width = imgElem.width;
            this.imgOrgSize.height = imgElem.height;
            attachCropper = true;
            if (this.targetSizeDefined) {
                if (imgElem.width < this.targetSize.width
                        || imgElem.height < this.targetSize.height) {
                    canvas = this.resizeImageToCanvas(imgElem, null, this.targetSize.width,
                            this.targetSize.height);
                    if (this.targetAndMinSizeMatch) {
                        // directly inject canvas and avoid overhead of conversion to image and back again
                        canvas.id = this.imgElementId;
                        imgElem = canvas;
                        attachCropper = false;
                    } else {
                        // create an image from canvas and leave since the called method will invoke this method again
                        this.previewImageFromDataUrl(canvas.toDataURL());
                        return;
                    }
                } else {
                    attachCropper = imgElem.width > this.targetSize.width
                            || imgElem.height > this.targetSize.height
                            || !this.targetAndMinSizeMatch;
                }
            } else {
                attachCropper = imgElem.width > this.options.minSize.width
                        || imgElem.height > this.options.minSize.height;
            }
            if (!attachCropper && !this.options.previewIfCroppingNotNeeded) {
                // no preview required, just upload and quit
                if (canvas) {
                    this.uploadImageOfCanvas(canvas);
                } else {
                    this.uploadFiles(this.getFileInput().files);
                }
                return;
            }
            wrapperDiv = new Element('div', {
                'class': 'image-wrapper'
            });
            wrapperDiv.grab(imgElem);
            // hide image so we can calculate the correct size before showing it
            imgElem.setStyle('display', 'none');
            popupTitle = attachCropper ? this.options.popupTitleCropMode
                    : this.options.popupTitlePreviewMode;
            buttons = [];
            buttons.push({
                action: this.croppingDone.bind(this)
            });
            if (this.options.popupAcceptButtonLabel) {
                buttons[0].type = 'main';
                buttons[0].label = this.options.popupAcceptButtonLabel
            } else {
                buttons[0].type = 'ok';
            }
            buttons.push({
                type: 'cancel'
            });
            showDialog(popupTitle, wrapperDiv, buttons, {
                onShowCallback: this.imagePreviewPopupShown.bind(this, attachCropper),
                onCloseCallback: this.croppingCanceled.bind(this)
            });
        },

        /**
         * Called after the popup was shown. This callback will resize the image if necessary and
         * show it. Finally it attaches the cropper if requested.
         * 
         * @param {Boolean} attachCropper Whether to attach a cropper or only show a preview
         * @param {Element} popupDialogElement The element representing the popup dialog container
         *            inside the popup window
         * @param {Element} popupWindowElement The element representing the popup window
         */
        imagePreviewPopupShown: function(attachCropper, popupDialogElement, popupWindowElement) {
            var popupSize, viewportSize, maxHeight, maxWidth, cropperConstructor;
            var ratio, unscaledMinSize, scaledMinSize;
            var imgWrapperElem = popupDialogElement.getElement('.image-wrapper');
            var imgElem = imgWrapperElem.getElementById(this.imgElementId);
            popupSize = popupWindowElement.getSize();
            viewportSize = window.getSize();
            // maxHeight is viewport height reduced by height that the popup consumes alone
            // without content (= only title, buttons and padding/margins of content wrapper)
            maxHeight = viewportSize.y - popupSize.y;
            // maxWidth is width of viewport reduced by the width of the empty popup which is
            // defined by margins, borders and paddings of all elements wrapping the content.
            // To calculate this width we assume the image-wrapper is in normal flow (not 
            // floated, absolute or something) and thus consumes the available space. The
            // difference between the popup width and the wrapper width are the offsets resulting
            // from padding, border and margin (this also assumes there is no padding for the image-wrapper!).
            maxWidth = popupWindowElement.getStyle('max-width');
            if (maxWidth) {
                maxWidth = parseInt(maxWidth);
            } else {
                maxWidth = viewportSize.x;
            }
            maxWidth = maxWidth - popupSize.x + imgWrapperElem.clientWidth;
            if (this.options.scalePopupFitViewport) {
                this.imgSize = this.scaleDownKeepAspect(this.imgOrgSize.width,
                        this.imgOrgSize.height, maxWidth, maxHeight);
                this.imgIsScaled = this.imgSize.width != this.imgOrgSize.width
                        || this.imgSize.height != this.imgOrgSize.height;
                // reduce minSize if image was scaled
                if (this.imgIsScaled && attachCropper && this.options.minSizeRespectScaling) {
                    unscaledMinSize = this.options.minSize;
                    scaledMinSize = {};
                    ratio = this.imgSize.width / this.imgOrgSize.width;
                    scaledMinSize.width = (this.options.minSize.width * ratio).toInt();
                    ratio = this.imgSize.height / this.imgOrgSize.height;
                    scaledMinSize.height = (this.options.minSize.height * ratio).toInt();
                    this.options.minSize = scaledMinSize;
                    // rebuild the cropper options
                    this.prepareCropperOptions();
                }
            } else {
                this.imgSize.width = this.imgOrgSize.width;
                this.imgSize.height = this.imgOrgSize.height;
            }
            if (this.imgIsScaled) {
                imgElem.setStyles({
                    'width': this.imgSize.width,
                    'height': this.imgSize.height,
                    'display': ''
                });
            } else {
                imgElem.setStyle('display', '');
            }
            if (attachCropper) {
                cropperConstructor = this.options.cropperConstructor || window['Cropper'];
                this.cropper = new cropperConstructor(imgElem, this.cropperOptions);
                // restore unscaled min size
                if (unscaledMinSize) {
                    this.options.minSize = unscaledMinSize;
                    this.prepareCropperOptions();
                }
            }
        },

        resizeImageToCanvas: function(imgElem, cropCoordinates, outputWidth, outputHeight) {
            var sourceTop, sourceLeft, sourceHeight, sourceWidth, maxHeight, maxWidth, outputTop, outputLeft, size, align;
            var finalMaxHeight = outputHeight;
            var finalMaxWidth = outputWidth;
            var canvas, renderContext;
            if (!imgElem) {
                return false;
            }
            sourceTop = sourceLeft = 0;
            maxWidth = this.imgOrgSize.width;
            maxHeight = this.imgOrgSize.height;
            if (cropCoordinates) {
                if (cropCoordinates.left >= 0 && cropCoordinates.left < maxWidth) {
                    sourceLeft = cropCoordinates.left;
                }
                if (cropCoordinates.top >= 0 && cropCoordinates.top < maxHeight) {
                    sourceTop = cropCoordinates.top;
                }
                if (cropCoordinates.width > 0 && cropCoordinates.width + sourceLeft <= maxWidth) {
                    sourceWidth = cropCoordinates.width;
                } else {
                    sourceWidth = maxWidth - sourceLeft;
                }
                if (cropCoordinates.height > 0 && cropCoordinates.height + sourceTop <= maxHeight) {
                    sourceHeight = cropCoordinates.height;
                } else {
                    sourceHeight = maxHeight - sourceTop;
                }
            } else {
                sourceWidth = maxWidth;
                sourceHeight = maxHeight;
            }
            if (!outputWidth) {
                outputWidth = sourceWidth;
            }
            if (!outputHeight) {
                outputHeight = sourceHeight;
            }
            // do nothing if there is no need to resize
            if (sourceLeft == 0 && sourceTop == 0 && sourceWidth == outputWidth
                    && sourceHeight == outputHeight) {
                return false;
            }
            // create a canvas to draw the image
            canvas = new Element('canvas');
            canvas.width = outputWidth;
            canvas.height = outputHeight;

            renderContext = canvas.getContext('2d');
            outputTop = outputLeft = 0;
            // fill background if smaller
            if (outputHeight > sourceHeight || outputWidth > sourceWidth) {
                renderContext.fillStyle = this.options.backgroundFillColor;
                renderContext.fillRect(0, 0, finalMaxHeight, finalMaxHeight);
                if (this.options.keepRatio) {
                    size = this.scaleDownKeepAspect(sourceWidth, sourceHeight, outputWidth,
                            outputHeight);
                    outputHeight = size.height;
                    outputWidth = size.width;
                } else {
                    outputHeight = sourceHeight;
                    outputWidth = sourceWidth;
                }
                if (outputHeight < finalMaxHeight) {
                    align = this.options.verticalAlignment;
                    if (align < 0) {
                        outputTop = 0;
                    } else if (align === 0) {
                        outputTop = Math.floor((finalMaxHeight - outputHeight) / 2);
                    } else {
                        outputTop = finalMaxHeight - outputHeight;
                    }
                }
                if (outputWidth < finalMaxWidth) {
                    align = this.options.horizontalAlignment;
                    if (align < 0) {
                        outputLeft = 0;
                    } else if (align === 0) {
                        outputLeft = Math.floor((finalMaxWidth - outputWidth) / 2);
                    } else {
                        outputLeft = finalMaxWidth - outputWidth;
                    }
                }
            }
            renderContext.drawImage(imgElem, sourceLeft, sourceTop, sourceWidth, sourceHeight,
                    outputLeft, outputTop, outputWidth, outputHeight);
            return canvas;
        },

        /**
         * Calculate new width and height values that do not exceed maxWidth and maxHeight. The new
         * values will have the same aspect ratio as the provided values.
         * 
         * @param {Number} width The width to check against maxWidth
         * @param {Number} height The height to check against maxHeight
         * @param {Number} maxWidth The maximum width that the returned value will not exceed
         * @param {Number} maxHeight The maximum height that the returned value will not exceed
         * @return {Object} object with width and height attributes that hold the calculated values.
         *         If the input values are both smaller than the maximum values they will be
         *         returned.
         */
        scaleDownKeepAspect: function(width, height, maxWidth, maxHeight) {
            var targetAspect, originalAspect, size;
            size = {};
            if (width > maxWidth || height > maxHeight) {
                targetAspect = maxWidth / maxHeight;
                originalAspect = width / height;
                if (originalAspect > targetAspect) {
                    size.width = maxWidth;
                    size.height = Math.round(maxWidth / originalAspect);
                } else {
                    size.height = maxHeight;
                    size.width = Math.round(maxHeight * originalAspect);
                }
            } else {
                size.height = height;
                size.width = width;
            }
            return size;
        }

    });
    this.CropImageAjaxFileUpload = ImageUpload;
})();