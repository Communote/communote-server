/**
 * @class
 * Configuration
 */
// TODO convert into a normal object communote.widget.DefaultConfiguration. The xyz_min and xyz_max should be
// in a separate object with a suitable name. 
communote.widget.classes.Configuration = communote.Base.extend(
/** @lends communote.widget.classes.Configuration.prototype */	
{
    // a flag to check if this is an configuration class
    isConfiguration: true,
    // the language that will be used. If undefined the user's or browser (anonymous user use-case) language will be used.
    lang: undefined,
    // will create a iframe and insert the widget into it, good to protect css styles
    useIframe: true,
    // if useIframe is true this options allows to define a max height that when reached will stop 
    // automatic growth of the iframe and vertical scrollbars will be shown instead
    maxHeight: 0,
    // will force the widget to encode widget parameters
    usingProxy: false,
    // the basic host for any url used
    baseHost: window.location.protocol + "//" + window.location.host,
    // the path to the communote root, to access api, files and the plugin
    cntPath: "/microblog/global",
    // the path to the plugin
    filesPath: "/plugins/${maven-symbolicname}/",
    // an optional host for api calls (only useful, when using jsonp)
    dataHost: undefined,
    // the api path (concatenated to the cntPath)
    // TODO should be changed to /api/ for non-standalone usage as soon as user images and attachments are available via /api/.../ resource
    dataPath: "/web/rest/2.2/",
    // the used css files, if used urls starting with 'http', it will be used absolute
    cssFiles: [ "/resources/styles/htmlClient-styles.css?build=" + pluginBuildTimestamp ],
    // the used template file, if used urls starting with 'http', it will be used absolute
    templateFile: "/resources/htmlClient-templates.tmpl.html?build=" + pluginBuildTimestamp,
    // the interval, if request about new notes (in ms)
    activeRefreshInterval: 60000,
    inactiveRefreshInterval: 300000,
    // send button is unabled after clicked (in ms)
    timeoutForSend: 2000,
    imageContentTypes: ["image/jpeg","image/gif","image/png"],
    hideSuggestionTitleForAliases: ["DefaultNoteTagStore"],
    hideSuggestionForAliases: ["InternalSharePointTagStore"],
    attachmentUploadUrl: "/microblog/global/blog/attachmentUpload.do",
    // whether to allow REST requests with doPublicAccess parameter when there is no user
    allowPublicAccess: true,
    // -------------------------------------------------------------------------
    // the set of basic controls
    controls: [ {
        slot: ".cntwHeader",
        type: "WriteContainer"
    }, {
        slot: ".cntwHeader",
        type: "FilterContainer"
    }, {
        slot: ".cntwContent",
        type: "ViewFilter"
    }, {
        slot: ".cntwContent",
        type: "NoteRefresh"
    }, {
        slot: ".cntwContent",
        type: "NoteList"
    }, {
        slot: ".cntwFooter",
        type: "FooterContainer",
        data: {
            items: [ {
                label: 'htmlclient.widget.footer.followTopic',
                css: 'cntwFollowTopic'
            }, {
                label: 'htmlclient.widget.footer.moreNotes',
                css: 'cntwMoreNotes'
            }, {
                label: 'htmlclient.widget.footer.toCommunote',
                css: 'cntwToCommunote'
            } ],
            autoWidth: true
        }
    } ],

    /** Element after short Text in Note */
    // -------------------------------------------------------------------------
    shortTextReadMore: "<span class=\"cntwReadMore\"> [&hellip;]</span>",

    /** PARAMETER */
    // timezone offset in minutes (parameter)
    // -------------------------------------------------------------------------
    utcTimeZoneOffset: 0,       // CNHC-141 - ok
    utcTimeZoneOffset_min: -720,
    utcTimeZoneOffset_max: 840,
    // whether to to override the utcTimeZoneOffset with the timeZoneOffset provided by Communote's Information resource
    utcTimeZoneOffsetOfCommunote: false, 
    /** for the following configuration please read the functional concept */
    // -------------------------------------------------------------------------
    edShowCreate: true,         // CNHC-55  - ok
    edInitLines: 2,             // CNHC-70  - ok
    edInitLines_min: 1,
    edInitLines_max: 50,
    edShowTopicChooser: true,   // CNHC-214 - ok
    edTopicList: "",            // CNHC-104 - ok
    edPreselectedTopic: "",     // CNHC-74  - ok
    edShowTag: true,            // CNHC-216 - ok
    edAddDefaultTags: "",       // CNHC-103 - ok
    edShowUpload: false,        // CNHC-87  - ok
    edShowTagField: false,        // CNHC-467
    // -------------------------------------------------------------------------
    msgShowMessages: true,      // CNHC-200 - ok
    msgShowEdit: true,          // CNHC-91  - ok
    msgShowReply: true,         // CNHC-175 - ok
    msgShowDelete: true,        // CNHC-181 - ok
    msgShowLike: true,          // CNHC-144 - ok
    msgShowFavor: true,         // CNHC-201 - ok
    msgShowAuthorImg: true,     // CNHC-71  - ok
    msgMaxCount: 10,            // CNHC-113 - ok 
    msgMaxCount_min: 0,
    msgMaxCount_max: 50,
    msgShowViews: "all,following,me,favorites",           // CNHC-178/CNHC-388 - ok
    msgViewSelected: "all",                               // CNHC-142/CNHC-388 - ok
    msgFollowButton: true,      // CNHC-169 - ok
    msgHomeButton: false,       // CNHC-166 - ok
    msgHomeUrl: "",             //          - ok
    msgShowFooter: true,        // CNHC-??? - ok
    // -------------------------------------------------------------------------
    fiShowFilter: true,         // CNHC-219 - ok
    fiShowSearch: true,         // CNHC-89  - ok
    fiShowTopic: true,          // CNHC-168 - ok
    fiShowTagCloud: true,       // CNHC-119 - ok
    fiShowAuthor: true,         // CNHC-197 - ok
    fiPreselectedSearch: "",    // CNHC-157 - ok
    fiPreselectedTopics: "",    // CNHC-231 - ok
    fiPreselectedTopicIds: "",  // CNHC-231 - ok
    fiPreselectedTagIds: "",    // CNHC-213 - ok
    fiPreselectedAuthors: "",   // CNHC-90  - ok
    fiPreselectedAuthorIds: "", // CNHC-90  - ok
    fiPreselectedNoteId: "",    // CNHC-202 - ok
    fiTopicPageSize: 10,        // CNHC-127 - ok
    fiTopicPageSize_min: 1,
    fiTopicPageSize_max: 50,
    fiAuthorPageSize: 14,       // CNHC-100 - ok
    fiAuthorPageSize_min: 1,
    fiAuthorPageSize_max: 50,
    // -------------------------------------------------------------------------
    acShow: true,               // CNHC-135
    acShowFilter: true,         // CNHC-120
    //---------------------------------------------------------------------------
    useSharePointProfilePictures: false // CNHC-380

 });
