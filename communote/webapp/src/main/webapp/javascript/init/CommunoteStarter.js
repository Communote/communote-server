(function(window) {
    var viewDefs, widgetDefs, i, subViews, subViewName, communote;

    function getResponsiveUtils(config, viewManager) {
        var disabled = config && config.responsiveWebdesignDisabled;
        if (!disabled && communote.classes.ResponsiveUtils) {
            return new communote.classes.ResponsiveUtils(viewManager);
        } else {
            // return dummy object that implements the (descriptive) interface of the responsive utils
            return {
                responsiveDesignEnabled: function() {
                    return false;
                },
                testResponsiveAttribute: function(condition) {
                    return false;
                }
            };
        }
    }
    function initHelpers(config) {
        // not all tools are required on all views, so only init available
        var apiAccessor, contextFilterGroup, hoverCardOptions, hoverCardManager;
        var utilsNamespace = communote.utils;
        if (window.RestApiAccessor) {
            apiAccessor = new RestApiAccessor();
        }
        if (window['FollowUtils']) {
            followUtils = new FollowUtils('/web/v1.1/follow.json');
        }
        if (communote.classes.HoverCardManager) {
            hoverCardManager = communote.hoverCardManager = new communote.classes.HoverCardManager();
        }
        if (config.openLinksInNewWindow) {
            hoverCardOptions = {
                openLinksInNewWindow: true
            }
        }
        if (window['TopicUtils']) {
            if (window.filterWidgetGroupRepo) {
                contextFilterGroup = filterWidgetGroupRepo['mainPageContextManagement'];
            }
            // TODO remove legacy global var after migrating to namespace
            blogUtils = utilsNamespace.topicUtils = new TopicUtils(apiAccessor, contextFilterGroup ? contextFilterGroup
                    .getParameterStore()
                    : undefined, window.defaultBlog);
            if (hoverCardManager && window['TopicHoverCard'] && apiAccessor) {
                hoverCardManager.register('topic', new TopicHoverCard(hoverCardOptions));
            }
        }
        if (Browser.name == 'ie' && Browser.version <= 8) {
            window.defaultBlog = undefined;
        } else {
            delete window.defaultBlog;
        }
        if (window['AutocompleterFactory']) {
            autocompleterFactory = new AutocompleterFactory({
                summaryAtTop: false
            }, null);
        }
        if (apiAccessor) {
            if (window['NoteUtils']) {
                // TODO remove legacy global var after migrating to namespace
                noteUtils = utilsNamespace.noteUtils = new NoteUtils(apiAccessor);
            }
            if (window['UserUtils']) {
                // TODO remove legacy global var after migrating to namespace
                userUtils = utilsNamespace.userUtils = new UserUtils(apiAccessor);
                if (hoverCardManager && window['UserHoverCard']) {
                    hoverCardManager.register('user', new UserHoverCard(hoverCardOptions));
                }
            }
            if (window['NoteTagUtils']) {
                // TODO remove legacy global var after migrating to namespace
                noteTagUtils = utilsNamespace.noteTagUtils = new NoteTagUtils(apiAccessor);
                if (hoverCardManager && window['TagHoverCard']) {
                    hoverCardManager.register('tag', new TagHoverCard(hoverCardOptions));
                }
            }
        }
    }

    function prepareLocale() {
        switch (communote.currentUser.language) {
        case 'de':
            Locale.use('de-DE');
            break;
        default:
            Locale.use('en-US');
        }
        // TODO provide month names with JSMessages 
        Locale.define('de-DE', 'Date', {
            months: [ 'Januar', 'Februar', 'M\u00E4rz', 'April', 'Mai', 'Juni', 'Juli', 'August',
                    'September', 'Oktober', 'November', 'Dezember' ],
            months_abbr: [ 'Jan', 'Feb', 'M\u00E4r', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep',
                    'Okt', 'Nov', 'Dez' ]
        });
    }

    function stripDoSuffixFromPath(path) {
        if (path.slice(-3) == '.do') {
            return path.slice(0, -3);
        } else {
            return path;
        }
    }

    function registerContextLinkHandlers(contexts) {
        var constructor = communote.getConstructor('ActivateContextLinkHandler');
        var linkHandler = communote.linkHandler;
        if (!linkHandler || !constructor) {
            return;
        }
        if (contexts.notesOverview) {
            linkHandler
                    .registerApplicationLinkHandler(new constructor('/portal/home', 'notesOverview', {
                        filterParameterMapping: {
                            noteSearchString: 'postTextSearchString'
                        },
                        multiValueFilterParameters: [ 'userId', 'tagIds' ]
                    }));
        }
        if (contexts.topicsOverview) {
            linkHandler
                    .registerApplicationLinkHandler(new constructor('/portal/topics', 'topicsOverview', {
                        contextDescriptorPostProcessor: function(contextDescr) {
                            // force 'all' as viewId if none is set and tagIds are given
                            if (!contextDescr.viewId && contextDescr.options.filterParams.tagIds) {
                                contextDescr.viewId = 'all';
                            }
                        },
                        filterParameterMapping: {
                            topicSearchString: 'pattern',
                            tagId: 'tagIds'
                        },
                        multiValueFilterParameters: [ 'tagIds' ],
                        processDataHandler: 'processTagFilterData'
                    }));
        }
        if (contexts.usersOverview) {
            linkHandler
                    .registerApplicationLinkHandler(new constructor('/portal/users', 'usersOverview', {
                        filterParameterMapping: {
                            userSearchString: 'searchString',
                            tagId: 'tagIds'
                        },
                        multiValueFilterParameters: [ 'tagIds' ],
                        processDataHandler: 'processTagFilterData'
                    }));
        }
        if (contexts.tagSelected) {
            linkHandler
                    .registerApplicationLinkHandler(new constructor('/portal/tags/*', 'tagSelected', {
                        filterParameterMapping: {
                            noteSearchString: 'postTextSearchString',
                            tagId: 'tagIds'
                        },
                        multiValueFilterParameters: [ 'userId' ],
                        processDataHandler: 'processTagData'
                    }));
        }
        if (contexts.userSelected) {
            linkHandler
                    .registerApplicationLinkHandler(new constructor('/portal/users/*', 'userSelected', {
                        filterParameterMapping: {
                            noteSearchString: 'postTextSearchString',
                            tagId: 'tagIds'
                        },
                        multiValueFilterParameters: [ 'tagIds' ],
                        processDataHandler: 'processUserData'
                    }));
        }
        if (contexts.topicSelected) {
            linkHandler
                    .registerApplicationLinkHandler(new constructor('/portal/topics/*', 'topicSelected', {
                        filterParameterMapping: {
                            noteSearchString: 'postTextSearchString',
                            tagId: 'tagIds'
                        },
                        multiValueFilterParameters: [ 'tagIds', 'userId' ],
                        processDataHandler: 'processTopicData'
                    }));
        }
        if (contexts.topicEdit) {
            linkHandler
                    .registerApplicationLinkHandler(new constructor('/portal/topic-edit', 'topicEdit', {
                        processDataHandler: 'processTopicData'
                    }));
        }
        if (contexts.topicCreate) {
            linkHandler
                    .registerApplicationLinkHandler(new constructor('/portal/topic-create', 'topicCreate', {}));
        }
        if (contexts.userEdit) {
            linkHandler
                    .registerApplicationLinkHandler(new constructor('/portal/user-edit', 'userEdit', {
                        processDataHandler: 'processUserData'
                    }));
        }
    }

    // namespace
    communote = window.communote;

    if (!communote.configuration.init) {
        communote.configuration.init = {};
    }
    // init widget framework by default
    if (communote.configuration.init.widgetFramework === undefined) {
        communote.configuration.init.widgetFramework = true;
    }

    // TODO clearer distinction between different views (home, people, blogs). Still required in 3.0??
    communote.initializer.addBeforeInitCallback(function() {
        var constructor, linkHandlerOptions;
        if (window.CommunoteLocalStorage) {
            // TODO use communote namespace
            communoteLocalStorage = new CommunoteLocalStorage();
        }
        constructor = communote.getConstructor('LinkHandler');
        if (constructor) {
            linkHandlerOptions = {
                    preprocessPathCallback: stripDoSuffixFromPath
            };
            if (communote.configuration && communote.configuration.openLinksInNewWindow) {
                linkHandlerOptions.openInNewWindow = true;
            }
            communote.linkHandler = new constructor(communote.server.applicationUrl, linkHandlerOptions);
        }
    });

    communote.initializer
            .addWidgetFrameworkInitializedCallback(function() {
                var constructor, viewManager, linkHandler, contextManager;
                var config = communote.configuration;
                if (!communote.utils) {
                    communote.utils = {};
                }
                // create ViewManager that handles the widgets of the main page
                if (config && config.mainPageViewManagerConfig) {
                    constructor = communote.getConstructor('ViewManager');
                    viewManager = new constructor(communote.widgetController, '#cn-communote', config.mainPageViewManagerConfig);
                    constructor = communote.getConstructor('MainPageContextManager');
                    contextManager = new constructor(null, config.mainPageContexts, viewManager, config.mainPageContextManagerOptions);
                    filterWidgetGroupRepo['mainPageContextManagement'] = new FilterGroup('mainPageContextManagement', null, contextManager, null, communote.widgetController
                            .getFilterEventProcessor());
                    // TODO this is kind of ugly
                    if (config.publishContextManager) {
                        communote.contextManager = contextManager;
                    }
                    // add link handler for the different views
                    if (!config.disableDefaultActivateContextLinkHandlers) {
                        registerContextLinkHandlers(config.mainPageContexts);
                    }
                }
                // create ResponsiveUtils
                communote.utils.responsiveUtils = getResponsiveUtils(config, viewManager);
            });

    communote.initializer.addAfterInitCallback(function() {
        var config = communote.configuration;
        initHelpers(config);
        prepareLocale();
        // TODO don't like the global init_tabs, but have no better idea
        if (config.init.scanTabs) {
            init_tabs();
        }
    });

    // always add the default appReady callback. The configuration which disables the default
    // behavior might be set later by another script
    communote.initializer.addApplicationReadyCallback(function() {
        // process the location by default, but allow it to be disabled
        if (communote.configuration.init.processLocation !== false) {
            // process initial filter params and do URL parsing to activate correct context
            if (!communote.linkHandler.processLocation()
                    && communote.configuration.init.activateDefaultContext !== false) {
                // if there is no handler send an activateContext event without a contextId
                // to let the contextManager activate the default context if defined
                E2G('activateContext', 'mainPageContextManagement', {});
            }
        } else if (communote.configuration.init.activateDefaultContext !== false) {
            // if the location should not be processed activate the default context, if not disabled
            E2G('activateContext', 'mainPageContextManagement', {});
        }
    });

})(this);
