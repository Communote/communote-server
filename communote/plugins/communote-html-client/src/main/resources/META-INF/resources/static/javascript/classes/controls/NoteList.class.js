/**
 * @class
 * @augments communote.widget.classes.controls.ContainerControl
 */
communote.widget.classes.controls.NoteList = communote.widget.classes.controls.ContainerControl
        .extend(
/** 
 * @lends communote.widget.classes.controls.NoteList.prototype
 */        	
{
            name: 'NoteList',
            noteTemplate: 'Note',
            // TODO fgr: if this the same resource for normal note list and discussions how do you manage that the discussion is not filtered?
            // by fgr: the resource attribute is only for the standard 'loadData()' function to be passed to the api
            //         the note list overrides this basic function and calls for all discussion(has a partenNoteId) a different
            //         api function
            resource: 'timeLineNotes',
            isDiscussionView: false,
            followTopicAlias: '',
            followTopicId: -1,
            isFollowTopic: false,
            lockMore: false,
            countRenderedSubControls: 0,
            lastDate: null,
            renderingFirstSet: false,
            isEmpty: false,

            registerListeners: function() {
                this.base();
                this.listenTo('noteServiceSuccessEvent', this.widget.channel, 'reRender');
                // if not showing a discussion refresh on filter changes
                if (!this.parentNoteId) {
                    this.listenTo('filterChanged', this.widget.channel, 'reRender');
                }
                this.listenTo('optionSelected', this.parent.channel);
                this.listenTo('OnClickLoadNewNotes', "global");
                this.listenTo('setFavoriteStatus', "global");
                this.listenTo('setLikeStatus', "global");
            },

            /**
             * @method OnClickLoadNewNotes
             * reset and load the notelist, if new notes available (event triggered)
             */
            OnClickLoadNewNotes : function() {
                this.reRender();
                this.loadData();
            },

            /**
             * @override
             */
            includeFilterParameters: function() {
                return true;
            },

            loadData: function() {
                if (!this.offset) {
                    this.offset = 0;
                    delete this.lastDate;
                }
                if (this.maxCount === undefined) {
                    this.maxCount = this.widget.configuration.msgMaxCount;
                }
                if (this.parentNoteId) {
                    this.controller.ApiController.answers(this.parentNoteId, this);
                } else {
                    if (this.widget.configuration.msgShowMessages) {
                        this.controller.ApiController.getData(this);
                    } else {
                        var data = {
                            notes: [],
                            origResult: {
                                metaData: {
                                    moreElementsAvailable: false
                                }
                            }
                        };
                        var self = this;
                        setTimeout(function(){self.insertValues(data);}, 0);
                    }
                }
            },

            insertValues: function(data, skipRenderingDone) {
                var preSel, i;
                this.renderingFirstSet = !this.offset && !this.isDiscussionView;
                if (data) {
                    if (!this.isDiscussionView) {
                        if (data.origResult.metaData.numberOfElements == 0) {
                            this.controller.EventController.fireEvent('notNoteElements');
                        }
                        if (data.origResult.metaData.moreElementsAvailable) {
                            this.controller.EventController.fireEvent('moreNoteElementsAvailable');
                        } else {
                            this.controller.EventController
                                    .fireEvent('noMoreNoteElementsAvailable');
                        }
                    }
                    preSel = this.widget.configuration.fiPreselectedTopics.toString() || "";
                    if (preSel !== "" && preSel.split(",").length === 1) {
                        this.followTopicAlias = preSel;
                        this.controller.ApiController.getTopicByAlias(this.followTopicStatus,
                                this, preSel);
                        // moved to end of method followTopicStatus (CNHC-518)
                        // this.controller.EventController.fireEvent('viewFollowTopicButton');
                    }
                }

                if (data.timeLineNotes) {
                    for (i = 0; i < data.timeLineNotes.length; i++) {
                        data.timeLineNotes[i].isDiscussionView = this.isDiscussionView;
                        if (this.offset === 0 && !this.isDiscussionView) {
                            this.widget.firstNoteCreationDate = data.timeLineNotes[i].creationDate;
                        }
                        this.controls.push({
                            type: 'Note',
                            slot: '.cntwNoteList',
                            data: data.timeLineNotes[i]
                        });
                        this.offset++;
                    }
                }
                this.loadControls();
                if (this.renderingFirstSet && this.controlList.length === 0
                        && this.widget.configuration.msgShowMessages) {
                    this.lockMore = true;
                    this.showMessage({
                        message: this.getLabel('htmlclient.notelist.noNotesVisible'),
                        type: "info", // error|success (inserted as css class)
                        messageClass: "cntwMessageNoHover", // additional css class
                        isLabel: false, // will force the message to be used as property key
                        isRemoveable: true, // disappear the message after a time, or removed by click
                        isInline: true, // make the message appear inline or float it centered
                        submitText: "", // text for submit button
                        submitClass: "", // css class for submit button (used as identifier)
                        cancelText: "" // text for cancel button (remove the request by click)
                    });
                    this.isEmpty = true;

                }
                // when rendering notes after a click on load more we must not invoke the Pure
                // rendering, otherwise we would loose all the bound events
                if (this.renderingFirstSet) {
                    this.base(data);
                } else {
                    this.renderSubControls();
                }
                if (this.parentNoteId){this.parent.hideNoteListNotes();}

                this.endWorking();
				this.controller.EventController.fireEvent('IsRenderedNoteList');
				if (!skipRenderingDone){this.renderingDone();}
            },

            /**
             * @method followTopicStatus
             * set the result of request for following topic
             * @param (object) resultData - json object with the api request result
             */
            followTopicStatus: function(resultData) {
                if (resultData.writableTopics && resultData.writableTopics[0]) {
                    this.followTopicId = resultData.writableTopics[0].topicId;
                    this.isFollowTopic = resultData.writableTopics[0].isFollow;
                    this.widget.configuration["topicPreselected"] = resultData.writableTopics[0];
                    this.controller.EventController.fireEvent('viewFollowTopicButton');
                }
            },

            optionSelected: function(item) {
                if (item.jquery) {
                    if (item.hasClass('cntwMoreNotes') && !item.hasClass('cntwGrayed')
                            && !this.lockLoadingMoreData) {
                        this.lockLoadingMoreData = true;
                        this.lockMore = true;
                        communote.jQuery(".cntwFooter",this.domNode.context).fadeOut("fast");
                        this.showWorkingIndicator();
                        this.loadData();
                        this.hideWorkingIndicator();
                    }
                    if (item.hasClass('cntwFollowTopic') && !item.hasClass('cntwGrayed')) {
                        this.controller.ApiController.followTopic(this.followTopicResult, this, {
                            topicId: this.followTopicId,
                            isFollow: this.isFollowTopic
                        });
                    }
                }
            },

            followTopicResult: function(resultData) {
                var elem;
                if (resultData.origResult.status === "OK") {
                    this.isFollowTopic = !this.isFollowTopic;
                }
                elem = communote.jQuery(".cntwFollowTopic", this.widget.rootNode);
                if (this.isFollowTopic) {
                    elem.html(this.controller.I18nController
                            .getText("htmlclient.widget.footer.unfollowTopic"));
                    elem.addClass("cntwStrikeThrough");
                } else {
                    elem.html(this.controller.I18nController
                            .getText("htmlclient.widget.footer.followTopic"));
                    elem.removeClass("cntwStrikeThrough");
                }
            },

            loadControls: function() {
                var i, control;
                if (this.controls !== undefined && this.widget.configuration.msgShowMessages) {
                    // TODO fgr: please add a short comment describing what the baseList is. Preferably, add members of the object before method declarations.
                    // by fgr:   the base list is the only note list, that is no discussion, every notelist that is a discussion
                    //           get a baselist in the lines below
                    //           the baselist is manly for hiding notes, that are visible in a discussion
                    // REFACTOR: extend a Discussion from NoteList, to split that functionality and attribute a disscussion has
                    for (i = 0; i < this.controls.length; i++) {
                        control = this.controls[i];
                        if (!control.data){control.data = {};}
                        if (this.parentNoteId) {
                            control.data.parentNote = this.parent;
                            control.data.baseList = this.baseList;
                        } else {
                            control.data.baseList = this;
                        }
                    }
                    this.base();
                }
            },

            /**
             * overwrite
             * computed, if an note entry is rendered! (not the note list!!!)
             */
            renderingDone: function() {
                var msgMaxCount = this.widget.configuration.msgMaxCount;
                this.countRenderedSubControls++;
                if (this.countRenderedSubControls === msgMaxCount || this.isEmpty ) {
                    this.countRenderedSubControls = 0;
                    this.lockLoadingMoreData = false;
                }
            },

            reRender: function() {
                communote.jQuery(".cntwFooter",this.domNode.context).fadeOut("fast");
                communote.jQuery(".cntwViewFilter",this.domNode.context).fadeOut("fast");
                this.getDomNode().find('.cntwNoNoteVisible').remove();
                this.offset = 0;
                this.controls = [];
                this.base();
            },

            errorValue: function(errorObj) {
                errorObj.inline = true;
                this.base(errorObj);
                communote.jQuery('.cntwNoteContainer', this.getDomNode()).hide();
            },

            SendReply: function(noteId) {
                var $ = communote.jQuery;
                var list = $('> .cntwNoteList', this.getDomNode());
                var note = $('> .cntwNoteContainer[cntwNoteId="' + noteId + '"]', list);
                var textarea = $('> .cntwReplyContainer textarea', note);
                var data = {};
                data.value = textarea.attr('value');
                data.parentNoteId = noteId;
                data.topicId = textarea.attr('cntwTopicId');
                this.fireEvent('sendNote', this.widget.channel, data);
            },

            getTopParentNoteList: function() {
                if ((this.parent == undefined) || (this.parent.type != this.type)) {
		    return this;
		} else {
		    return this.parent.getTopParentNoteList();
		}
            },

            /**
             * hides the working indicator
             */
            hideWorkingIndicator: function() {
                if (this.lockMore === true) {
                    this.lockMore = false;
                    var node = this.getDomNode();
                    var indicator = node.parent().find('.cntwWorkingIndicator');
                    setTimeout(function() {
                        indicator.remove();
                        node.css('height', 'auto');
                    },500);
                } else {
                    this.base();
                }
            },

            /**
             * @method setFavoriteStatus search the object for noteId and call them routine for
             *         setting icons and button
             * @param {object} data - included the noteId
             */
            setFavoriteStatus: function(data) {
                for ( var i = 0; i < this.controlList.length; i++) {
                    var note = this.controlList[i];
                    if (note.type === "Note" && note.configData.noteId === data.noteId) {
                        note.toggleFavorite();
                        break;
                    }
                }
            },

            /**
             * @method setLikeStatus search the object for noteId and call them routine for setting
             *         icons and button
             * @param {object} data - included the noteId
             */
            setLikeStatus: function(data) {
                for ( var i = 0; i < this.controlList.length; i++) {
                    var note = this.controlList[i];
                    if (note.type === "Note" && note.configData.noteId === data.noteId) {
                        note.toggleLike();
                        break;
                    }
                }
            }

        });
