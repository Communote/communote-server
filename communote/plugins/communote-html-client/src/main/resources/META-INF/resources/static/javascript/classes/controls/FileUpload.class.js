/**
 * @class
 * @augments communote.widget.classes.controls.InputField
 */
// TODO CodeReview: why extending InputField? This class is not applicable for the methods defined there!
 communote.widget.classes.controls.FileUpload = communote.widget.classes.controls.InputField.extend(
/** 
 * @lends communote.widget.classes.controls.FileUpload.prototype 
 */
{
    name: "FileUpload",
    template: "FileUpload",
    noContainer: true,
    infoTextLabel: "htmlclient.writecontainer.fileUpload.placeholder",
    confirmOnEnter: true,
    alwaysClear: false,
    keepValue: false,
    autoExpand: false,
    inputFile: null,
    fileIdBase: "cntwAttachmentFile",
    attachmentUploadSessionId: 0,
    // TODO CodeReview: this does not work because the object is shared between instances of this control. 
    // Always initialize objects within the constructor! 
    attachmentIds: [],
    formUpload: null,

    /**
     * @method appendEntry
     * append an file entry to the file list
     * @param {jq object} list
     * @param {object} data - data object with fileName and contentLength
     * @param {string} id
     */
    // TODO CodeReview: no need for id parameter because it is member of attachment data object
    appendEntry: function(list, data, id) {
        var titleText;
        var self = this;
        var $ = communote.jQuery;
        var entry = $("<li></li>", this.domNode.context);
        this.registerAttachments(id);
        entry.addClass("cntwItem");
        titleText = this.controller.I18nController.getText("htmlclient.fileupload.entry.delete");
        // TODO CodeReview: better encode filename because it can contain characters like >
        // TODO CodeReview: reuse utils.formatFileSize! 
        entry.html(  "<a href=\"javascript:''\""
                   + " id=\"" + this.fileIdBase + (id ? "-" + id : "") + "\""
                   + " class=\"" + this.fileIdBase + "\""
                   + " title=\"" + titleText + "\">"
                   + data.fileName + " "
                   + "(" + (Math.round(data.contentLength * 10 / 1024) / 10) + " KB)"
                   + "</a>");
        list.append(entry);
        entry.click(function() {
            self.removeEntry(this);
            return false;
        });
    },

    /**
     * @method bindEvents
     * overwritten
     */
    bindEvents: function() {
        this.base();
        var iframeUpload; 
        var self = this;
        var $ = communote.jQuery;
        var target = "cntwFileUploadIframe" + "_" + this.domId;
        var containerFrame = $(".cntwFileUpload .cntwFileUpload-frameContainer", this.getDomNode());
        // insert the iframe
        var classIFrame = "cntwFileUpload-iframe";
        var htmlString = "<iframe class=\"" + classIFrame + "\""
                + " name=\"" + target + "\""
                + " src=\"javascript:''\"></iframe>";
        containerFrame.html(htmlString);
        iframeUpload = $("." + classIFrame, containerFrame);
        // define the load event
        iframeUpload.load(function(){
            var iframeContext = this.contentWindow;
            if (iframeContext !== undefined) {
                if (iframeContext.response) {
                    if (iframeContext.response.result) {
                        self.displayFileList(iframeContext.response.result);
                    } else {
                        self.fireEvent('error', self.channel, {
                            message: iframeContext.response.message,
                            type: 'error',
                            error: ''
                        });
                        self.inputFile[0].value = "";
                    }
                }
            }
        });
        // TODO CodeReview: please check the template because it contains an unnecessary hidden input
        this.formUpload = $(".cntwFileUpload .cntwFileUpload-form", this.getDomNode());
        this.formUpload.attr("target", target);
        this.attachmentUploadSessionId = Math.random().toString().substr(2);
        var url = communote.widget.ApiController.getFileUploadUrl(this.attachmentUploadSessionId);
        this.formUpload.attr("action", url);
        this.formUpload.submit(function(){
            return true;
        });
        this.inputFile = $(".cntwFileUpload-inputFile", this.formUpload);
        this.inputFile.change(function(){
            if (this.value.length != 0) {
                self.formUpload.attr("target", target);
                self.formUpload.submit();
            }
        });
    },

    /**
     * @method displayFileList
     * manage the file list for new uploading files
     * @param {array} result List of JSON objects describing the uploaded attachments
     */
    // TODO CodeReview: maybe better name like showUploadSummary or something? Also method description should be improved. 
    displayFileList: function(result) {
        if (result && (result.length > 0)) {
            var id, container, list, $;
            
            $ = communote.jQuery;            
            id = result[0].attachmentId;
            container = $(".cntwAttachmentListContainer", this.domNode.parent());
            list = $("ul", container);
            if (list.length === 0) {
                list = this.generateList(container);
            }
            this.appendEntry(list, result[0], id);
            this.inputFile[0].value = "";
        
        }
    },

    /**
     * @method generateList
     * return a file list width the leading label entry
     * @param {jq object} container
     */
    generateList: function(container) {
        var $ = communote.jQuery;
        var context = container.context;
        var entry;
        list = $("<ul></ul>", context);
        list.addClass("cntwAttachmentList");
        container.append(list);
        entry = $("<li></li>", context);
        entry.addClass("cntwListLabel");
        entry.html(this.controller.I18nController
                .getText("htmlclient.fileupload.attachments") +  ":");
        list.append(entry);
        return list;
    },

    /**
     * @method getAttachmentIds
     * return an array with the file id's for attachment upload
     */
    getAttachmentIds: function() {
        return this.attachmentIds[this.attachmentUploadSessionId];
    },

    /**
     * @method getAttachmentUploadSessionId
     * return the unique session id for attachment upload
     */
    getAttachmentUploadSessionId: function() {
        return this.attachmentUploadSessionId;
    },

    /**
     * @method registerAttachments
     * register the id in attachment list
     * @param {string} id
     */
    // TODO CodeReview: why so complicated? One FileUpload is associated with one write control and needs only one uploadSessionId!
    // you actually only need to push the ID of the attachment into the array
    registerAttachments: function(id) {
        var ids, i;
        // this.attachment = this.attachment || [];
        this.attachmentIds = this.attachmentIds || [];
        // TODO CodeReview: despite of what is mentioned above, a general hint: never ever use Arrays like Objects!!!!!
        this.attachmentIds[this.attachmentUploadSessionId] =
                this.attachmentIds[this.attachmentUploadSessionId] || [];
        ids = this.attachmentIds[this.attachmentUploadSessionId];
        for (i = 0; i < ids.length; i++) {
            if (id == ids[i]) {
		return;
	    }
        }
        this.attachmentIds[this.attachmentUploadSessionId].push(id);
    },

    registerListeners: function() {
        this.base();
        this.listenTo("success", "global", "successSaveNote");
    },

    /**
     * @method removeEntry
     * the entry is clicked and will remove from view and datastore (not from server)
     * @param {dom object} elem
     */
    removeEntry: function(elem) {
        var $ = communote.jQuery;
        var jqElem = $(elem);
        // e.g. cntwAttachmentFile-1131
        var id = $("a", jqElem).attr("id").substr(this.fileIdBase.length + 1);
        ids = this.attachmentIds[this.attachmentUploadSessionId];
        for (var i = 0; i < ids.length; i++) {
            if (ids[i] == id) {
                this.attachmentIds[this.attachmentUploadSessionId].splice(i, 1);
                break;
            }
        }
        if (!this.attachmentIds[this.attachmentUploadSessionId].length){
            jqElem.parent().remove();
        } else {
            jqElem.remove();
        }
    },

    /**
     * @method searchFrameByName
     * return the frame by a given name (iteration)
     * @param {dom object} domPart
     * @param {string} name
     */
    searchFrameByName: function(list, name) {
        var result = null;
        list = list.frames;
        if (list) {
            result = list[name];
            if (!result) {
                for (var i = 0; i < list.length; i++) {
                    result = this.searchFrameByName(list[i], name);
                    if (result) {
			break;
		    }
                }
            }
        }
        return result;
    },

    /**
     * @method successSaveNote
     * after the note is saved, must set a new attachmentUploadSessionId on the form
     */
    // TODO CodeReview: it is not necessary to change the uploadSessionId, but you should clear the attachmentIds
    successSaveNote: function() {
        if (this.formUpload){
            this.attachmentUploadSessionId = Math.random().toString().substr(2);
            var url = communote.widget.ApiController.getFileUploadUrl(this.attachmentUploadSessionId);
            this.formUpload.attr("action", url);
        }
    }
});