(function(namespace) {
    var GlobalIdUploadImageWidget = new Class({
        Extends: C_FilterWidget,

        widgetGroup: "image",
        uploadOptions: {
            keepRatio: true,
            minSize: {},
            minSizeRespectScaling: true,
            targetSize: {},
            previewIfCroppingNotNeeded: false,
            uploadOnChange: true,
            uploadUrl: undefined
        },
        formActionUrl: null,
        useTargetSizeAsMinSize: true,

        init: function() {
            var staticParam, intValue;
            this.parent();
            staticParam = this.getStaticParameter('useTargetSizeAsMinSize');
            if (staticParam === false || staticParam === 'false') {
                this.useTargetSizeAsMinSize = false;
            }
            if (!this.useTargetSizeAsMinSize) {
                intValue = parseInt(this.getStaticParameter('minHeight'));
                if (!isNaN(intValue)) {
                    this.uploadOptions.minSize.height = intValue;
                }
                intValue = parseInt(this.getStaticParameter('minWidth'));
                if (!isNaN(intValue)) {
                    this.uploadOptions.minSize.width = intValue;
                }
            }
            
            this.uploadOptions.popupTitleCropMode = getJSMessage('widget.globalid-upload-image.js.crop.dialog.title');
            this.uploadOptions.popupAcceptButtonLabel = getJSMessage('widget.globalid-upload-image.js.crop.dialog.accept');
            this.uploadOptions.uploadUrl = namespace.server.applicationUrl + '/widgets/image/GlobalIdUploadImageUploader.json';

            this.copyStaticParameter('imageType'); // "banner" or "profile"
            this.setEntityId();
        },

        setEntityId: function() {
            var entityId = 'default';
            if (this.getStaticParameter('entityType') == 'topic') {
                if (this.filterWidgetGroup) {
                    entityId = 'topic.' + this.filterParamStore.getFilterParameter('blogId');
                } else if (window.blogUtils) {
                    entityId = 'topic.' + window.blogUtils.getCurrentBlogId();
                }
            } else {
                entityId = this.getStaticParameter('entityId');
            }
            this.setStaticParameter('entityId', entityId);
            this.copyStaticParameter('entityId');
        },

        refreshComplete: function(responseMetadata) {
            var formElem;
            var fileInput = this.domNode.getElement('input[type=file]');
            if (fileInput) {
                this.uploadOptions.targetSize.height = responseMetadata.targetHeight;
                this.uploadOptions.targetSize.width = responseMetadata.targetWidth;
                if (this.useTargetSizeAsMinSize) {
                    this.uploadOptions.minSize.height = this.uploadOptions.targetSize.height;
                    this.uploadOptions.minSize.width = this.uploadOptions.targetSize.width;
                }
                this.ajaxUploader = new CropImageAjaxFileUpload(fileInput, this.uploadOptions);
                this.ajaxUploader.addEvent('uploadDone', this.imageUploadDone.bind(this));
                this.ajaxUploader.addEvent('uploadFailed', this.imageUploadFailed.bind(this));
            }
            this.parent(responseMetadata);
            searchAndShowRoarNotification(this.domNode);
        },

        /**
         * Do a JSON POST request to the URL of the form and send the provided actionValue as value of
         * the action parameter.
         *
         * @param {String} actionValue The action to send
         */
        sendImageAction: function(actionValue) {
            var request = new Request.JSON({
                url: this.uploadOptions.uploadUrl,
                method: 'post',
                data: {
                    action: actionValue
                }
            });
            request.addEvent('onComplete', function(response) {
                this.imageUploadDone(null, response, actionValue == 'resetuserimageajax');
            }.bind(this));
            request.send();
        },

        imageUploadDone: function(uploadId, jsonResponse, setDefault) {
            var defaultBtn;
            if (jsonResponse.status == 'ERROR') {
                this.imageUploadFailed(uploadId, jsonResponse.message);
            } else {
                hideNotification();
                if (jsonResponse.message) {
                    showNotification(NOTIFICATION_BOX_TYPES.success, '', jsonResponse.message, null);
                }
                // exchange image path with returned value
                this.domNode.getElement('img.cn-profile-image').src = jsonResponse.result.imageUrl;
                E2("onGlobalIdImageChanged", this.widgetId, {
                    entityId: this.getStaticParameter('entityId'),
                    imageType: this.getStaticParameter('imageType'),
                    imageUrl: jsonResponse.result.imageUrl
                });
                // activate/disable setDefault button
                defaultBtn = this.domNode.getElement('input[type=button].control-set-default');
                if (defaultBtn) {
                    if (setDefault) {
                        defaultBtn.setProperty('disabled', 'disabled');
                    } else {
                        defaultBtn.removeProperty('disabled');
                    }
                }
                this.domNode.getElement('form').reset();
            }

        },
        imageUploadFailed: function(uploadId, message) {
            // also reset to give user the chance to upload the same file again, since using onChange trigger
            hideNotification();
            showNotification(NOTIFICATION_BOX_TYPES.error, '', message, {
                duration: ''
            });
            this.domNode.getElement('form').reset();
            
        },
        setDefault: function() {
            var request = new Request.JSON({
                url: this.uploadOptions.uploadUrl,
                method: 'post',
                data: {
                    widgetAction: 'reset_default',
                    entityId: this.getStaticParameter("entityId"),
                    imageType: this.getStaticParameter("imageType")
                }
            });
            request.addEvent('onComplete', function(response) {
                this.imageUploadDone(null, response, true);
            }.bind(this));
            request.send();
        }
    });
    if (namespace && namespace.addConstructor) {
        namespace.addConstructor('GlobalIdUploadImageWidget', GlobalIdUploadImageWidget);
    } else {
        window.GlobalIdUploadImageWidget = GlobalIdUploadImageWidget;
    }
})(window.runtimeNamespace);