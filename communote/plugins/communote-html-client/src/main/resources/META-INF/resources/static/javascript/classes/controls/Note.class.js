/**
 * @class
 * @augments communote.widget.classes.controls.ContainerControl
 */
communote.widget.classes.controls.Note = communote.widget.classes.controls.ContainerControl
        .extend(
/** 
 * @lends communote.widget.classes.controls.Note.prototype
 */           	
{
            containerTag: 'li',

            getNoteId: function() {
                return this.configData.noteId;
            },

            isDirectMessage: function(){
                return this.configData.isDirectMessage;
            },

            getAuthor: function() {
                return this.configData.author;
            },

            getTopic: function() {
                return this.configData.topic;
            },

            getAttachments: function() {
                return this.configData.attachments;
            },

            getImageAttachments: function() {
                var i;
                var $ = communote.jQuery;
                var imageAttachments = new Array();
                var conf = this.widget.configuration;
                for (i = 0; i < this.configData.attachments.length; i++) {
                    if ($.inArray(this.configData.attachments[i].fileType, conf.imageContentTypes) >= 0) {
                        imageAttachments.push(this.configData.attachments[i]);
                    }
                }
                return imageAttachments;
            },

            getAttachmentsButImages: function() {
                var i;
                var $ = communote.jQuery;
                var attachments = new Array();
                var conf = this.widget.configuration;
                for (i = 0; i < this.configData.attachments.length; i++) {
                    if ($.inArray(this.configData.attachments[i].fileType, conf.imageContentTypes) < 0) {
                        attachments.push(this.configData.attachments[i]);
                    }
                }
                return attachments;
            },

            getUsedTags: function() {
                var result = this.configData.tags;
                if (result.length) {
                    result.unshift( { name: "#" } );
                }
                return result;
            },

            getNotifiedUsers: function() {
                var i;
                var utils = communote.utils;
                var result = this.configData.usersToNotify;
                for (i = 0; i < result.length; i++) {
                    result[i].name = utils.encodeXml(utils.getUserFullName(
                            result[i], false));
                }
                if (result.length) {
                    result.unshift( { name: "@", alias: "", firstName: "", lastName: "" } );
                }
                return result;
            },

            registerListeners: function() {
                this.base();
                this.listenTo('openDiscussion');
                this.listenTo('closeDiscussion');
                this.listenTo('cancelReply');
                this.listenTo('cancelEdit');
            },

            loadControls: function() {
                var $ = communote.jQuery;
                // prepare the configData object for use
                this.configData.canEdit = ($.inArray("CAN_EDIT", this.configData.rights) > -1);
                this.configData.canDelete = ($.inArray("CAN_DELETE", this.configData.rights) > -1);
                this.configData.canReply = ($.inArray("CAN_REPLY", this.configData.rights) > -1);
                this.configData.isFavorite = ($.inArray("FAV", this.configData.userNoteProperties) > -1);
                this.configData.isLike = ($.inArray("LIKE", this.configData.userNoteProperties) > -1);
                this.configData.isNotify = ($.inArray("NOTIFY", this.configData.userNoteProperties) > -1);
                this.configData.textShort = this.configData.shortText != null ? this.configData.shortText
                        + this.widget.configuration.shortTextReadMore
                        : this.configData.text;
                this.controls = [];
                if (this.configData.canReply) {
                    this.addControlReplyContainer();
                }
                this.addControlActivityList();
                this.addControlReactionList();
                this.addControlStatusIcons();
                this.base();
            },

            addControlReplyContainer: function() {
                this.controls.push({
                    type: 'ReplyContainer',
                    slot: '.cntwReplyContainer',
                    noContainer: true,
                    data: {
                        topicId: this.getTopic().topicId,
                        parentNoteId: this.getNoteId(),
                        author: this.getAuthor(),
                        isDirectMessage: this.isDirectMessage()
                    }
                });
            },

            addControlActivityList: function() {
                var items = [];
                var activityList = {
                    type: 'OptionList',
                    name: 'ActivityList',
                    slot: '.cntwSocial',
                    css: [ 'cntwActivityList' ],
                    data: {}
                };
                if (this.widget.configuration.msgShowReply && this.configData.canReply) {
                    items.push({
                        label: "htmlclient.note.reply",
                        css: "cntwReplyNote"
                    });
                }
                if (this.widget.configuration.msgShowEdit && this.configData.canEdit) {
                    items.push({
                        label: "htmlclient.note.edit",
                        css: "cntwEditNote"
                    });
                }
                if (this.widget.configuration.msgShowDelete && this.configData.canDelete) {
                    items.push({
                        label: "htmlclient.note.delete",
                        css: "cntwDeleteNote"
                    });
                }
                // liking is only possible if there is an authenticated user (i.e. not public access)
                if (this.widget.configuration.msgShowLike && communote.currentUser) {
                    items.push({
                        label: "htmlclient.note.like",
                        css: "cntwLikeNote",
                        selected: this.configData.isLike
                    });
                }
                // marking as favorit is only possible if there is an authenticated user
                if (this.widget.configuration.msgShowFavor && communote.currentUser) {
                    items.push({
                        label: "htmlclient.note.favorite",
                        css: "cntwFavoriteNote",
                        selected: this.configData.isFavorite
                    });
                }
                activityList.data.items = items;
                this.controls.push(activityList);
            },

            addControlReactionList: function() {
        	var self = this;  
                var reactionList;
                var numberOfLikes = false;
                // TODO this inner function is useless!
                var numberOfDiscussionNotes = (function() {
                    var result = "";
                    // TODO first part of comparison is not needed because: null < 1, 0 < 1, -1 < 1
                    if ((self.configData.discussion.numberOfDiscussionNotes && (self.configData.discussion.numberOfDiscussionNotes > 1))
                            && !self.parent.discussionId) {
                        result = self.configData.discussion.numberOfDiscussionNotes;
                    }
                    return result;
                })();
                if (this.widget.configuration.msgShowLike) {
                    numberOfLikes = this.configData.numberOfLikes || false;
                }
                reactionList = {
                    type: 'OptionList',
                    slot: '.cntwSocial',
                    css: [ 'cntwReactionList' ],
                    data: {
                        items: [ {
                            text: numberOfLikes,
                            css: numberOfLikes ? "cntwLikes cntwView" : "cntwLikes"
                        }, {
                            text: numberOfDiscussionNotes,
                            css: "cntwShowDiscussion cntwAnswers",
                            hide: numberOfDiscussionNotes == ""
                        }, {
                            label: 'htmlclient.note.hideDiscussion',
                            css: "cntwHideDiscussion",
                            hide: true
                        } ]
                    }
                };
                this.controls.push(reactionList);
            },

            /**
             * add the valid status icons to the note
             */
            addControlStatusIcons: function() {
                var statusList = {
                    type: 'OptionList',
                    slot: '.cntwStatusIcons',
                    css: [ 'cntwStatusIcons' ],
                    data: {
                        items: [
                                {
                                    text: '',
                                    css: this.configData.isNotify
                                            && !this.configData.isDirectMessage ? "cntwNotify cntwView"
                                            : "cntwNotify"
                                },
                                {
                                    text: '',
                                    css: this.configData.isDirectMessage ? "cntwDirectMessage cntwView"
                                            : "cntwDirectMessage"
                                },
                                {
                                    text: '',
                                    css: this.configData.isFavorite ? "cntwFavorite cntwView"
                                            : "cntwFavorite"
                                },
                                {
                                    text: '',
                                    css: this.widget.configuration.msgShowLike && this.configData.isLike ? "cntwLike cntwView" : "cntwLike"
                                } ]
                    }
                };
                this.controls.push(statusList);
            },

            loadData: function() {
                this.insertValues({
                    note: this.configData
                });
            },

            insertValues: function(data) {
                this.base(data);
                var properties = this.configData.properties || [];
                
                
            	for(var i = 0; i < properties.length; i++){
            		if((properties[i].key =='contentTypes.activity') && 
            				(properties[i].keyGroup == 'com.communote.plugins.communote-plugin-activity-base') && 
            				(properties[i].value == 'activity')){
            			this.getDomNode().find('> .cntwNoteContainer').addClass('cntwActivity');
            		}
            	}
                
                
                if (this.configData.parentNote
                        && (this.configData.parentNote.getNoteId() == this.getNoteId())) {
                    this.getDomNode().find('> .cntwNoteContainer').addClass('cntwSelected');
                }
            },

            bindEvents: function() {
                var $ = communote.jQuery;
                var self = this;
                var note = $('> .cntwNoteContainer', this.getDomNode());
                var noteBody = $('.cntwNoteBody', note);
                var replyContainer = $('.cntwReplyContainer', note);
                // var editContainer = $('.cntwEditContainer', note);  
                var activityList = $('.cntwContainerActivityList', note);
                // click an dropdown-menuitem
                $('.cntwReplyNote', noteBody).click(function() {
                    $(this).hide();
                    replyContainer.show();
                    activityList.css('visibility', 'hidden');
                });
                $('.cntwEditNote', noteBody).click(function() { 
                    self.controller.ApiController.plainNote(self.getNoteId(),
                            self.displayEdit, self);
                    activityList.css('visibility', 'hidden');
                });
                $('.cntwDeleteNote', noteBody).click(function() {
                    var obj = {
                        message: self.getLabel('htmlclient.note.deleteNote'),
                        isInline: false,
                        isRemoveable: true,
                        submitClass: 'cntwDeleteButton',
                        submitText: self.getLabel("htmlclient.common.yes"),
                        cancelText: self.getLabel("htmlclient.common.no")
                    };
                    var box = self.showMessage(obj);
                    if (box !== null) {
                        box.find('.cntwDeleteButton').click(function() {
                            box.remove();
                            self.fireEvent('deleteNote', self.widget.channel, {
                                noteId: self.getNoteId()
                            });
                        });
                    }
                });
                // click the like button
                $(".cntwLikeNote", noteBody).click(function() {
                    self.controller.ApiController.likeNote(self.successLike, self, {
                        noteId: self.getNoteId(),
                        isLike: !self.configData.isLike
                    });
                });
                // click the favorite button
                $(".cntwFavoriteNote", noteBody).click(function() {
                    self.controller.ApiController.favoriteNote(self.successFavorite, self, {
                        noteId: self.getNoteId(),
                        isFavorite: !self.configData.isFavorite
                    });
                });
                // click on note entry
                $('.cntwShowDiscussion ', noteBody).click(function() {
                    self.fireEvent('openDiscussion');
                });
                $('.cntwHideDiscussion', noteBody).click(function() {
                    self.fireEvent('closeDiscussion');
                });
                // this prevents open/close on selecting text
                $('.cntwNoteBodyContentContent', noteBody).live("mousedown", function(evt) {                    
                    var $this = $(this).data("mousedown", true);
                    $this.data("selected", false);
                    setTimeout(function() {
                        if ($this.data("mousedown") == true) {
                            $this.data("selected", true);
                        } else {
                            $this.data("selected", false);
                        }
                    }, 200);
                 }).live("mouseup", function(evt) { 
                    if(!$('.cntwEditContainer').is(':visible') && !$('.cntwEditContainer').is(':visible')){
                        if ($(this).data("selected") == false) {
                            if($(".cntwMessage", noteBody).is(':visible')){
                                $(".cntwMessage", noteBody).hide();
                                $(".cntwUsedTags", noteBody).hide();
                                $(".cntwNotifiedUser", noteBody).hide();
                                $(".cntwMessageShort", noteBody).show();
                                self.fireEvent("sizeChanged", self.widget.channel);
                            } else {
                                $(".cntwMessageShort", noteBody).hide();
                                $(".cntwMessage", noteBody).show();
                                $(".cntwUsedTags", noteBody).show();
                                $(".cntwNotifiedUser", noteBody).show();
                                self.fireEvent("sizeChanged", self.widget.channel);
                            }
                        }
                    }
                    $(this).data("mousedown", false);
                }).live("mouseleave", function(evt) {
                    $(this).data("mousedown", false);
                });
                // this prevents open/close on click on images, links, attachments
                $('a', noteBody).live("mouseup",function (event){
                    event.stopPropagation();
                 });
                $('.cntwEditContainer', noteBody).live("mouseup",function (event){
                    event.stopPropagation();
                 });
                $('.cntwReplyContainer', noteBody).live("mouseup",function (event){
                    event.stopPropagation();
                 });                
                $('.cntwShowMoreImages', noteBody).click(function() {
                    $(".cntwImages .cntwItem a.cntwShowMoreImages", noteBody).remove();
                    $(".cntwImages .cntwItem a", noteBody).show();
                });
                $('.cntwLikes', noteBody).click(function() {
                    self.toggleLikingUsers();
                });
                if (this.widget.configuration.msgShowAuthorImg) {
                    $(".cntwAuthorImage", noteBody).show();
                    $(".cntwNoteBodyContent", noteBody).addClass("cntwNoteMarginLeft");
                }
            },

            /**
             * @method successFavorite execute, when favorite request is successed
             */
            successFavorite: function() {
                communote.widget.EventController.fireEvent('setFavoriteStatus', "global", {
                    noteId: this.getNoteId()
                });
            },

            /**
             * @method toggleFavorite toggle the status of icons and buttons in the view for
             *         favorite
             */
            toggleFavorite: function() {
                var $ = communote.jQuery;
                this.configData.isFavorite = !this.configData.isFavorite;
                $("> .cntwNoteContainer > .cntwNote .cntwStatusIcons .cntwFavorite", this.domNode)
                        .toggleClass("cntwView");
                $("> .cntwNoteContainer > .cntwNote .cntwActivityList .cntwFavoriteNote",
                        this.domNode).parent().toggleClass("cntwSelected");
            },

            /**
             * @method successLike execute, when like request is successed
             */
            successLike: function() {
                communote.widget.EventController.fireEvent('setLikeStatus', "global", {
                    noteId: this.getNoteId()
                });
            },

            /**
             * @method toggleLike toggle the status of icons and buttons in the view for like
             */
            toggleLike: function() {
                var likeNumber, likes;
                var $ = communote.jQuery;
                var domNode = this.getDomNode();
                this.configData.isLike = !this.configData.isLike;
                $("> .cntwNoteContainer > .cntwNote .cntwStatusIcons .cntwLike", domNode).toggleClass("cntwView");
                $("> .cntwNoteContainer > .cntwNote .cntwActivityList .cntwLikeNote", domNode).parent().toggleClass("cntwSelected");
                likeNumber = $("> .cntwNoteContainer > .cntwNote .cntwReactionList .cntwLikes", domNode);
                likes = parseInt(likeNumber.html() || 0);
                this.configData.isLike ? likes++ : likes--;
                this.setNumberOfLikes(likes);
                this.refreshLikingUsers(likes);
            },

            /**
             * This method updates the displayed number of likes
             *
             * {number} likesCount the new number of likes
             */

            setNumberOfLikes: function(likesCount) {
                var likeNumber = communote.jQuery(
                        "> .cntwNoteContainer > .cntwNote .cntwReactionList .cntwLikes",
                        this.getDomNode());
                if (likesCount > 0) {
                    likeNumber.html(likesCount);
                    likeNumber.addClass("cntwView");
                } else {
                    likeNumber.html("");
                    likeNumber.removeClass("cntwView");
                }
            },


            /**
             * This method refreshes the images of the note likers.
             *
             */

            toggleLikingUsers: function() {
                var likeList = communote.jQuery(
                        "> .cntwNoteContainer > .cntwNote .cntwLikeList", this.domNode);
                if (likeList.children().length > 0) {
                    likeList.empty();
                } else {
                    this.displayLikingUsers(likeList);
                }
            },

            /**
             * This method refreshes the images of the note likers.
             * {number} likesCount the new number of likes
             */

            refreshLikingUsers: function(likesCount) {
                var likeList = communote.jQuery
                        ("> .cntwNoteContainer > .cntwNote .cntwLikeList", this.domNode);
                if(likesCount > 0) {
                    if(likeList.children().length > 0){
                        // Hack to avoid fluttering effect
                        likeList.css("height", likeList.height() + "px");
                        this.displayLikingUsers(likeList);
                        setTimeout(function(){likeList.css("height", "auto");}, 300);
                    }
                } else {
                    likeList.empty();
                }
            },

            /**
             * This method displays the images of the note likers.
             * {jQ} likeList like the list object
             */

            displayLikingUsers: function(likeList) {
                var ul;
                var $ = communote.jQuery;
                var utils = communote.utils;
                var self = this;
                likeList.empty();
                // use context and not ownerDocument since it is the jQuery instance
                ul = $('<ul></ul>', likeList.context).addClass('cntwItemList').addClass('cntwLikersList');
                ul.appendTo(likeList);

                communote.widget.ApiController.getNoteLikers(function(result, message,channel) {
                    var i, li, user, userImageUrl, userName, img;
                    var ownerDocument = likeList.context;
                    for(i = 0; i < result.notes[0].users.length; i++){
                        user = result.notes[0].users[i];
                        li = $('<li></li>', ownerDocument).addClass('cntwItem');
                        userImageUrl = communote.widget.ApiController.getUserImageUrl(user, 'SMALL');
                        userName = utils.encodeXml(utils.getUserFullName(user, true));
                        img = $('<img/>', ownerDocument).attr('src', userImageUrl).attr('title', userName).addClass("cntwLikersImage");
                        li.append(img);
                        li.appendTo(ul);
                    }
                    self.setNumberOfLikes(i++);
                }, this, this.getNoteId());
                likeList.show();
            },

            /**
             * add the edit control to DOM, called by click event on '.cntwEditNote'.
             *
             * @param {object} data
             * @param {string} message
             * @param {string} channel
             */
            displayEdit: function(data, message, channel) {
                var note, editContainer;
                var $ = communote.jQuery;
                var record = data.notes;
                var directive = {
                    type: 'EditContainer',
                    slot: '.cntwEditContainer',
                    noContainer: true,
                    data: {
                        topicId: record.topicId,
                        noteId: record.noteId,
                        text: record.text,
                        tags: record.tags,
                        attachments: record.attachments
                    }
                };
                var control = this.loadControl(directive);
                control.render();
                $(this).hide();
                note = $('> .cntwNoteContainer', this.getDomNode());
                
                $('.cntwMessage', note).hide();
                $('.cntwMessageShort', note).hide();
                $('.cntwImages', note).hide();
                $('.cntwAttachments', note).hide();
                $('.cntwUsedTags', note).hide();
                $('.cntwNotifiedUser', note).hide();
                
                editContainer = $('.cntwEditContainer', note);
                editContainer.show();
                control = control.getSubControl('type', 'WriteField');
                control.triggerAutoExpand();
            },

            /**
             * returns the shorted Authorname by a given length
             */
            getAuthorName: function() {
                var authorName = "";
                var utils = communote.utils;
                var author = this.getAuthor();
                if (utils.isUserDeleted(author)) {
		    return (this.getLabel('htmlclient.userfilter.user.anonymous'));
		}

                authorName = author.firstName ? author.firstName.substr(0, 1) + ". " : "";
                authorName += author.lastName || author.alias || "undefined!";

                return authorName;
            },

            parseData: function(data) {
                this.base(data);
                var utils = communote.utils;
                var note = data.note;
                // ERROR: id is already used!
                note.cntwId = 'cntw-' + this.getNoteId();
                note.cntwClass = "cntwNoteBody";
                note.cntwClass += this.configData.isNotify ? " cntwIsNotify" : "";
                note.cntwClass += this.configData.isDirectMessage ? " cntwIsDirectMessage" : "";
                note.userImageUrl = communote.widget.ApiController
                        .getUserImageUrl(this.getAuthor());
                note.datetime = this.controller.I18nController.getDateTime(note.creationDate
                        + (this.widget.configuration.utcTimeZoneOffset * 60000));
                note.topicName = utils.encodeXml(this.getTopic().title);
                note.authorName = utils.encodeXml(this.getAuthorName());
                note.authorFullName = utils
                        .encodeXml(utils.getUserFullName(this.getAuthor(), true));
                note.attachments = this.getAttachments();

                // TODO optimization: the next two methods should be merged into one which splits
                // the attachments into images and other attachments
                note.attachmentsButImages = this.getAttachmentsButImages();
                note.images = this.getImageAttachments();
                this.parseThumbLinks(note.images);
                this.parseAttachmentLinks(note.attachments);
                note.usedTags = this.getUsedTags();
                note.notifiedUsers = this.getNotifiedUsers();
            },

            parseAttachmentLinks: function(attachments) {
                var i, attach, name, size, url, displayText;
                var utils = communote.utils;
                var config = this.widget.configuration;
                var urlPrefix = config.baseHost + config.cntPath + '/portal/files/';
                for (i = 0; i < attachments.length; i++) {
                    attach = attachments[i];
                    name = attach.fileName;
                    size = communote.utils.formatFileSize(attach.contentLength);
                    displayText = utils.encodeXml(name) + ' (' + size + ')';
                    url = communote.widget.ApiController.apiAccessor.buildRequestUrl(urlPrefix
                            + attach.attachmentId + "/" + encodeURIComponent(name));
                    attach.link = '<a href="' + url + '" target="_blank" title="' + displayText + '"> ' + displayText + ' </a>';
                }
            },

            parseThumbLinks: function(images) {
                var i, label, image, name, size, thumbUrl, linkUrl;
                var utils = communote.utils;
                var config = this.widget.configuration;
                var maxLength = 3;
                var urlPrefix = config.baseHost + config.cntPath;
                for (i = 0; i < images.length; i++) {
                    image = images[i];
                    name = image.fileName;
                    size = communote.utils.formatFileSize(image.contentLength);
                    thumbUrl = communote.widget.ApiController.apiAccessor.buildRequestUrl(urlPrefix
                            + "/image/attachment.do", {id:image.attachmentId});
                    linkUrl = communote.widget.ApiController.apiAccessor.buildRequestUrl(urlPrefix
                            + '/portal/files/' + image.attachmentId + "/" + encodeURI(name));
                    image.thumbLink = '<a ';
                    if ((images.length > maxLength) && (i > (maxLength - 1))) {
                        image.thumbLink += 'style="display:none" ';
                    }
                    image.thumbLink += 'href="' + linkUrl + '" target="_blank" title="';
                    image.thumbLink += utils.encodeXml(name) + ' (' + size + ')">';
                    image.thumbLink += '<img src="' + thumbUrl + '" alt="' + utils.encodeXml(name)
                            + '"/>';
                    image.thumbLink += '</a>';
                    if ((images.length > maxLength) && (i == (maxLength - 1))) {
                        var i18nData = {
                            0: images.length - maxLength
                        };
                        switch (images.length) {
                        case (maxLength + 1):
                            label = "htmlclient.note.images.showMore.singular";
                            break;
                        default:
                            label = "htmlclient.note.images.showMore.plural";
                        }
                        image.thumbLink += '<a class="cntwShowMoreImages">'
                                + this.getLabel(label, i18nData) + '</a>';
                    }
                }
            },

            getDirectives: function() {
                var directive = {
                    '.cntwNoteBody@id': 'note.cntwId',
                    '.cntwNoteBody@class': 'note.cntwClass',
                    '.cntwAuthorImage img@src': 'note.userImageUrl',
                    '.cntwAuthorImage img@title': 'note.authorFullName',
                    '.cntwTime': 'note.datetime',
                    '.cntwMessage': 'note.text',
                    ".cntwMessageShort": "note.textShort",
                    '.cntwTopicName': 'note.topicName',
                    '.cntwAuthor': 'note.authorName',
                    '.cntwAuthor@title': 'note.authorFullName',
                    '.cntwImages .cntwItem': {
                        'image<-note.images': {
                            '.': 'image.thumbLink'
                        }
                    },
                    '.cntwAttachments .cntwItem': {
                        'attachment<-note.attachmentsButImages': {
                            '.': 'attachment.link'
                        }
                    },
                    '.cntwUsedTags .cntwItem': {
                        'tag<-note.usedTags': {
                            '.': function(arg) {
                                return communote.utils.encodeXml(arg.item.name);
                            }
                        }
                    },
                    '.cntwNotifiedUser .cntwItem': {
                        'user<-note.notifiedUsers': {
                            '.': 'user.name',
                            '.@title': function(arg) {
                                return communote.utils.encodeXml(communote.utils
                                        .getUserFullName(arg.item));
                            }
                        }
                    }
                };
                return directive;
            },

            openDiscussion: function() {
                var $ = communote.jQuery;
                var noteContSelector = '> .cntwNoteContainer';
                var noteCont = $(noteContSelector, this.getDomNode());
                var note = $('> .cntwNote', noteCont);
                var noteBody = $('.cntwNoteBody', noteCont);
                var answerSelector = '> .cntwAnswerContainer';
                var answerContainer = $(answerSelector, noteCont);
                var replies = answerContainer.find('> .cntwNoteList').children();
                $('> .cntwAnswerContainer', noteCont).show();
                $('.cntwHideDiscussion', noteBody).parent().show();
                $('.cntwShowDiscussion', noteBody).parent().hide();
                note.addClass('cntwDiscussionShown');
                // if discussion was open, dont load it again
                if (replies.length > 0) {
                    this.hideNoteListNotes();
                } else {
                    /** create new notelist as 'discussion' */
                    var discussion = this.loadControl({
                        slot: noteContSelector + answerSelector,
                        type: 'NoteList'
                    });
                    this.discussion = discussion;
                    discussion.isDiscussionView = true;
                    discussion.noContainer = true;
                    discussion.parentNoteId = this.configData.noteId;
                    discussion.discussionId = this.configData.discussion.numberOfDiscussionNotes
                            > 1 ? this.configData.discussion.discussionId : -1;
                    discussion.baseList = this.configData.baseList;
                    discussion.render();
                }
            },

            closeDiscussion: function() {
                var $ = communote.jQuery;
                var domNode = this.getDomNode();
                var noteSelector = '> .cntwNoteContainer';
                var noteCont = $(noteSelector, domNode);
                var note = $('> .cntwNote', noteCont);
                var noteBody = $('.cntwNoteBody', noteCont);
                var answerSelector = '> .cntwAnswerContainer';
                var answerContainer = $(answerSelector, noteCont);
                note.removeClass('cntwDiscussionShown');
                answerContainer.hide();
                this.showNoteListNotes();
                $('.cntwHideDiscussion', noteBody).parent().hide();
                $('.cntwShowDiscussion', noteBody).parent().show();
                this.fireEvent('sizeChanged', this.widget.channel);
            },

            showNoteListNotes: function() {
                this.toggleNoteVisibility(true);
            },

            hideNoteListNotes: function() {
                this.toggleNoteVisibility(false);
            },

            /**
             * expand or fold discussion items
             */
            toggleNoteVisibility: function(visible) {
                var baseNotes, discNotes, i, j;
                if (this.discussion) {
                    baseNotes = this.configData.baseList.controlList;
                    discNotes = this.discussion.controlList;
                    for (i = 0; i < discNotes.length; i++) {
                        for (j = 0; j < baseNotes.length; j++) {
                            baseNote = baseNotes[j];
                            if (baseNote.type === "Note") {
                                if ((baseNote.getNoteId() != this.getNoteId())
                                        && (baseNote.getNoteId() == discNotes[i].getNoteId())) {
                                    if (visible) {
                                        baseNote.getDomNode().show();
                                    } else {
                                        baseNote.getDomNode().hide();
                                    }
                                }
                            }
                        }
                    }
                }
                this.checkSeparator();
            },

            /**
             * hide/show the date separator, if has or has not entries
             */
            checkSeparator: function() {
                var elements = this.getDomNode().parent().children();
                var hasOnlyHiddenNotes = true;
                var currentSeparator;
                for (var i = 0; i < elements.length; i++) {
                    switch (elements[i].className) {
                        case "cntwDateSeparator":
                            this.toggleSeparator(currentSeparator, hasOnlyHiddenNotes);
                            hasOnlyHiddenNotes = true;
                            currentSeparator = elements[i];
                            break;
                        case "cntwContainerNote":
                            hasOnlyHiddenNotes &= elements[i].style.display == "none";
                            break;
                    }
                }
                this.toggleSeparator(currentSeparator, hasOnlyHiddenNotes);
            },

            toggleSeparator: function(currentSeparator, hasOnlyHiddenNotes) {
                if (currentSeparator) {
                    if (hasOnlyHiddenNotes) {
                        currentSeparator.style.display = "none";
                    } else {
                        currentSeparator.style.display = "list-item";
                    }
                }
            },

            cancelReply: function() {
                var reply;
                var $ = communote.jQuery;
                var note = $('> .cntwNoteContainer', this.getDomNode());
                var noteBody = $('.cntwNoteBody', note);
                var replyContainer = $('.cntwReplyContainer', note);
                var activityList = $('.cntwContainerActivityList', note);
                activityList.css('visibility', 'visible');
                // removed all messages
                $('.cntwInfoBox div', replyContainer).click();
                reply = $('.cntwReplyNote', noteBody);
                replyContainer.hide();
                reply.show();
            },

            cancelEdit: function() {
                var $ = communote.jQuery;
                var note = $('> .cntwNoteContainer', this.getDomNode());
                var noteBody = $('.cntwNoteBody', note);
                var editContainer = $('.cntwEditContainer', note);
                var activityList = $('.cntwContainerActivityList', note);
                activityList.css('visibility', 'visible');
                var edit = $('.cntwEditNote', noteBody);
                editContainer.hide();
                
                $('.cntwMessageShort', note).show();
                $('.cntwImages', note).show();
                $('.cntwAttachments', note).show();
                $('.cntwUsedTags', note).show();
                $('.cntwNotifiedUser', note).show();
                
                edit.show();
                this.getSubControl('type', 'EditContainer').destroy();
            },

            /**
             * This method renders the date before the notes.
             */

            addTemplatePrefix: function() {
                this.base();
                var date, lastDate, newNode;
                var parentComponent = this.parent || this.widget;
                var parentDomNode = parentComponent.getDomNode();
                // ATTENTION: this can more then one entry return !!!
                var parentContainer = parentDomNode.find(this.nodeSelector).first();

                if (!parentComponent.isDiscussionView) {
                    date = this.controller.I18nController.getDate(this.configData.creationDate
                            + (this.widget.configuration.utcTimeZoneOffset * 60000),
                            this.controller.I18nController
                                    .getText("htmlclient.common.dateformat.pattern.date"));
                    lastDate = parentComponent.lastDate;
                    if (!lastDate || (lastDate != date)) {
                        newNode = communote.jQuery('<li class="cntwDateSeparator"><span>' 
                                + date + '</span></li>',
                                parentContainer[0].ownerDocument);
                        parentContainer.append(newNode);
                        parentComponent.lastDate = date;
                    }
                }
            }

        });
