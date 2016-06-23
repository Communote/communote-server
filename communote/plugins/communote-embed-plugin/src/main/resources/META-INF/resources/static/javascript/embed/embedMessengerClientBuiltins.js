/**
 * Register some built-in request handlers to the embed messenger
 */
(function(window) {
    var communote = window.communote;
    var messenger = communote.embed && communote.embed.messenger;
    if (!messenger) {
        return;
    }
    
    function getNoteRequestHandler(requestId, data, completeCallback) {
        // data is ID
        communote.utils.noteUtils.getTimelineNote(data, function(result){
                completeCallback.call(null, requestId, true, result.result);
            }, function(result) {
                completeCallback.call(null, requestId, false, result);
            }
        );
    }
    
    function getNoteRequestHandlerFunction() {
        if (communote.utils && communote.utils.noteUtils) {
            return getNoteRequestHandler;
        }
    }
    
    if (!communote.currentUser) {
        messenger.addRequestHandler('getNote', messenger.requestHandlerCallbackRestApiNotAuthorized);
    } else {
        messenger.addHandlerAfterLoad('getNote', 'request', getNoteRequestHandlerFunction);
    }
})(this);