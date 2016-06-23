/**
 * @class
 * @augments communote.widget.classes.controls.ManagerControl
 */
communote.widget.classes.controls.NoteManager = communote.widget.classes.controls.ManagerControl
        .extend(
/** 
 * @lends communote.widget.classes.controls.NoteManager.prototype 
 */         	
{

            registerListeners: function() {
                this.listenTo('createNote', this.widget.channel);
                this.listenTo('answerNote', this.widget.channel);
                this.listenTo('editNote', this.widget.channel);
                this.listenTo('deleteNote', this.widget.channel);
                this.listenTo("followTopic", this.widget.channel ) ;
                this.base();
            },

            createNote: function(data) {
                /*
                 * if(!(data.topicId && data.value)) { data.error = 'createError';
                 * data.fromControl.fireEvent('error', undefined, data); return; }
                 */
                this.controller.ApiController.createNote(data.topicId, data.value, data.tags,
                        data.fromControl, data.attachmentUploadSessionId, data.attachmentIds);
            },

            answerNote: function(data) {
                /*
                 * if(!(data.topicId && data.value && data.parentNoteId)) { data.error =
                 * 'answerError'; data.fromControl.fireEvent('error', undefined, data); return; }
                 */
                this.controller.ApiController.answerNote(data.topicId, data.parentNoteId,
                        data.value, data.tags, data.isDirectMessage, data.fromControl,
                        data.attachmentUploadSessionId, data.attachmentIds);
            },

            editNote: function(data) {
                /*
                 * if(!(data.topicId && data.value && data.noteId)) { data.error = 'editError';
                 * data.fromControl.fireEvent('error', undefined, data); return; }
                 */
                this.controller.ApiController.editNote(data.topicId, data.noteId, data.value,
                        data.tags, data.fromControl,
                        data.attachmentUploadSessionId, data.attachmentIds);
            },

            deleteNote: function(data) {
                /*
                 * if(!(data.noteId)) { data.error = 'deleteError';
                 * data.fromControl.fireEvent('error', undefined, data); return; }
                 */
                this.controller.ApiController.deleteNote(data.noteId, data.fromControl);
            },

            /**
             * @method followTopic
             * the method will be call by event 'followTopic' and set the active user to following the topic
             * @param {object} data
             */
            followTopic: function(data) {
                this.controller.ApiController.followTopic(data);
            },

            sendNote: function(data) {
                var message = data.value;
                var ppId = data.parentNoteId;
                var bId = data.topicId;
                var noteId = data.noteId;
                this.controller.ApiController.doNote(message, bId, data.from, ppId, noteId);
            }
        });
