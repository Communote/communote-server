var UserProfileImageUploadWidget = new Class({
    Extends: C_Widget,

    widgetGroup: "user/profile",
    uploadOptions: {
        keepRatio: true,
        minSize: {
            width: 200,
            height: 200
        },
        minSizeRespectScaling: true,
        targetSize: {
            width: 200,
            height: 200
        },
        previewIfCroppingNotNeeded: false,
        uploadOnChange: true
    },
    formActionUrl: null,

    init: function() {
        this.parent();
        this.uploadOptions.popupTitleCropMode = getJSMessage('javascript.dialog.user.image.title.crop');
        this.uploadOptions.popupAcceptButtonLabel = getJSMessage('javascript.dialog.user.image.button.accept');
    },

    refreshComplete: function(responseMetadata) {
        var formElem;
        var fileInput = this.domNode.getElement('input[type=file]');
        if (fileInput) {
            this.ajaxUploader = new CropImageAjaxFileUpload(fileInput, this.uploadOptions);
            this.ajaxUploader.addEvent('uploadDone', this.imageUploadDone.bind(this));
            this.ajaxUploader.addEvent('uploadFailed', this.imageUploadFailed.bind(this));
        }
        formElem = this.domNode.getElement('form');
        this.formActionUrl = formElem.getProperty('action');
        this.parent(responseMetadata);
    },

    /**
     * Do a JSON POST request to the URL of the form and send the provided actionValue as value of
     * the action parameter.
     * 
     * @param {String} actionValue The action to send
     */
    sendImageAction: function(actionValue) {
        var request = new Request.JSON({
            url: this.formActionUrl,
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
            // hide notifications
            hideNotification();
            if (jsonResponse.message) {
                showNotification(NOTIFICATION_BOX_TYPES.success, '', jsonResponse.message, {
                    duration: ''
                });
            }
            // exchange image path with returned value
            this.domNode.getElement('img').src = buildRequestUrl(jsonResponse.result.pathLarge);
            // update user logos on page if any
            E('onUserLogoChanged', buildRequestUrl(jsonResponse.result.pathMedium));
            // activate/disable setDefault button
            defaultBtn = this.domNode.getElement('input[type=button].control-set-default');
            if (defaultBtn) {
                if (setDefault) {
                    defaultBtn.setProperty('disabled', 'disabled');
                } else {
                    defaultBtn.removeProperty('disabled');
                }
            }
            // clear upload field (using reset since setting value to '' does not work in IE8 and newer)
            this.domNode.getElement('form').reset();
        }
    },

    imageUploadFailed: function(uploadId, message) {
        hideNotification();
        showNotification(NOTIFICATION_BOX_TYPES.error, '', message, {
            duration: ''
        });
        // reset form to allow uploading same image again
        this.domNode.getElement('form').reset();
    }
});