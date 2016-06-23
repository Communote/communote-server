/**
 * @class
 * @augments communote.widget.classes.controls.WriteContainer
 */
communote.widget.classes.controls.EditContainer = communote.widget.classes.controls.WriteContainer
        .extend(
/** 
 * @lends communote.widget.classes.controls.EditContainer.prototype
 */
{
            name: 'EditContainer',
            confirmAction: 'edit',
            bindEvents: function() {
                this.base();
                var existTags, container, i, name, item;
                var field = this.getSubControl('type', 'WriteField');
                var tags = this.getSubControl("nodeSelector", ".cntwTagSelector");
                field.setText(this.configData.text);
                if (this.configData.tags && (this.configData.tags != '')) {
                    existTags = this.configData.tags;
                    container = tags.parent.getDomNode(); 
                    for ( i = 0; i < existTags.length; i++) {
                        name = existTags[i].name == undefined ? existTags[i].defaultName : existTags[i].name;
                        item = {};
                        item["label"] = name;
                        item["apiTag"] = existTags[i];
                        this.addTag(item, container);
                    }
                }
                // add attachment to the attachmentlist for editing
                if (this.configData.attachments && this.configData.attachments.length) {
                    var container = communote.jQuery(".cntwAttachmentListContainer", this.domNode);
                    var fu = this.getSubControl("type", "FileUpload");
                    var attachments = this.configData.attachments;
                    var list = fu.generateList(container);
                    for (var i = 0; i < attachments.length; i++) {
                        fu.appendEntry(
                                list,
                                attachments[i],
                                attachments[i].attachmentId);
                    }
                }
            },
            
            /**
             * This method overwrites parents method to do nothing.
             */
            
            addDefaultTags: function() {
            },

            /**
             * define the directive for send control (overwrite)
             */
            defineSendControls: function() {
                this.controls.push({
                    type: "SendControls",
                    slot: ".cntwButtonList",
                    data: {
                        pipeList: true,
                        items: [ {
                            label: "htmlclient.common.cancel",
                            css: "cntwCancelNote"
                        }, {
                            label: "htmlclient.common.store",
                            css: "cntwReplyNote"
                        } ]
                    }
                });
            },

            sendNote: function(data) {
                data.noteId = this.configData.noteId;
                this.base(data);
            },

            optionSelected: function(item) {
                this.base(item);
                if (item.hasClass('cntwCancelNote')){this.fireEvent('cancelEdit', this.parent.channel);}
            }
        });
