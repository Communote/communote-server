(function() {
    // TODO error handling; visualize state (all note containers!); update note metadata; be aware that there can be multiple CPL widgets -> update all; Icon; touch action; comment changes watch state on root if not yet set (depending on schedule!)
    function watchUnwatchDiscussion(actionData) {
        var noteId = (typeof actionData.noteId === 'string') ? actionData.noteId.toInt() : actionData.noteId;
        var metaData = actionData.widget.getNoteMetaData(actionData.noteId);
        var request = new XMLHttpRequest();   // new HttpRequest instance 
        request.open('POST', buildRequestUrl('/communote_discussion_notification_plugin/watch'));
        request.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
        request.setRequestHeader('Accept', 'application/json');
        request.addEventListener('load', function(e){console.log(e.target.responseText)});
        request.send(JSON.stringify({
            noteId: noteId,
            watch: !metaData['communote_discussion_notification_plugin.currentUserWatches'] 
        }));
        
    }
    
    communote.initializer.addBeforeWidgetScanCallback(function() {
        var widgetController = communote.widgetController;
        var actionHandlerProvider = {
                /**
                 * Implementation of a method defined by the widget event listener interface.
                 */
                getWidgetListenerGroupId: function() {
                    return undefined;
                },
                /**
                 * Implementation of a method defined by the widget event listener interface.
                 */
                handleEvent: function(eventName, params) {
                    var actionHandlerManager;
                    if (eventName === 'onWidgetInitComplete' && params.widgetType === 'ChronologicalPostListWidget') {
                        actionHandlerManager = widgetController.getWidget(params.widgetId).noteActionHandler;
                        actionHandlerManager.addActionHandler('communote_discussion_notification_plugin-watch', watchUnwatchDiscussion);
                    }
                }
        };
        widgetController.registerWidgetEventListener('onWidgetInitComplete', actionHandlerProvider);
    });
})();