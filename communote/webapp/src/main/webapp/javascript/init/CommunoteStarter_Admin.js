(function(window) {
    
    communote.initializer.addWidgetFrameworkInitializedCallback(function(){
        var userManagementGroup, parameterStore, initialFilterParameters;
        if (window.addClientManagementFilterGroups) {
            userManagementGroup = new FilterGroup('userManagementGroup',
                    [], 'UserManagementFilterEventHandler', null, communote.widgetController.getFilterEventProcessor());
            initialFilterParameters = window.communote.initialFilterParameters;
            if (initialFilterParameters) {
                parameterStore = userManagementGroup.getParameterStore();
                parameterStore.setFilterParameter('userId', initialFilterParameters.userId);
                parameterStore.setFilterParameter('searchString',
                        initialFilterParameters.userSearchString);
            }
            filterWidgetGroupRepo.userManagementGroup = userManagementGroup;
        }
    });
    
    communote.initializer.addAfterInitCallback(function() {
        // TODO namespace!
        autocompleterFactory = new AutocompleterFactory({
            summaryAtTop: false
        }, null);
        init_tabs();
        init_tips();
        searchAndShowRoarNotification();
    });
    
    communote.configuration.init = {
            widgetFramework: true
        };
})(this);
