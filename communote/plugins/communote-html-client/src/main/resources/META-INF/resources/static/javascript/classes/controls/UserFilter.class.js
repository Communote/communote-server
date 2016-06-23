/**
 * @class
 * @augments communote.widget.classes.controls.Filter
 */
communote.widget.classes.controls.UserFilter = communote.widget.classes.controls.Filter.extend(
/** 
 * @lends communote.widget.classes.controls.UserFilter.prototype
 */		
{
    name: 'UserFilter',
    resource: 'users',
    titleKey: 'htmlclient.userfilter.users',
    // this attribute is not in the response but will be added in parseData
    labelAttribute: 'fullName',
    filterParameterValueAttribute: 'userId',
    filterParameterName: 'userIds',
    offset: 0,
    maxCount: 1,
    renderParameters: null,

    /**
     * @param id
     * @param directive
     * @param widget
     */
    
    constructor: function(id, directive, widget) {
        var preselected;
        this.base(id, directive, widget);
        // define the parameters to ignore which are the userIds and userAliases
        this.filterParametersToIgnore = ['userIds', 'userAliases'];
        this.renderParameters = {};
        // in case there are predefined userAliases and userIds always add them
        // to the render parameters
        // !!! a single id is a numeric type, comma separated id's are string type
        preselected = widget.configuration.fiPreselectedAuthorIds.toString();
        if (preselected && (preselected.length > 0)) {
            this.renderParameters['userIds'] = preselected;
        }
        preselected = widget.configuration.fiPreselectedAuthors;
        if (preselected && (preselected.length > 0)) {
            this.renderParameters['userAliases'] = preselected;
        }
        this.maxCount = widget.configuration.fiAuthorPageSize;
    },

    /**
     * overwritten
     */
    getRenderParameters: function() {
        return this.renderParameters;
    },

    createElementIdFromUserId: function(userId) {
        return this.filterElementIdPrefix + userId;
    },

    /**
     * overwritten
     */
    createFilterElementId: function(item) {
        // use userId instead of index
        return this.createElementIdFromUserId(item.userId);
    },

    /**
     * overwritten
     */
    filterChanged: function(data) {
        var selector, elem;
        var $ = communote.jQuery;
        // the user filter should not be filtered when a user is selected, just highlight
        if (!$.isArray(data) && data.paramName === this.filterParameterName) {
            selector = '#' + this.createElementIdFromUserId(data.value);
            // find element and toggle css class
            elem = $(selector, this.getDomNode());
            if (data.added) {
                elem.addClass('cntwActive');
            } else {
                elem.removeClass('cntwActive');
            }
        } else {
            this.offset = 0;
            this.base();
        }
    },

    /**
     * overwritten
     */
    insertValues: function(data, skipRenderingDone) {
        this.base(data, skipRenderingDone);
        data = {
                offset: this.offset,
                moreResultsAvailable : data.origResult.metaData.moreElementsAvailable,
                totalNumberOfElements : data.origResult.metaData.numberOfElements,
                numberOfElements : data.users.length,
                maxCount : this.maxCount
        };
        if (data.totalNumberOfElements > this.maxCount) {
            this.setContainerHeight(data.numberOfElements);
            this.controls = [{
                    type: 'Pager',
                    slot: '.cntwPager',
                    data: data
            }];
            this.loadControls();
            this.renderSubControls();
        }
    },

    /**
     * @method setContainerHeight
     * @param (integer) numberOfLines - lines
     */
    setContainerHeight: function(numberOfLines){
        var $ = communote.jQuery;
        var domNode = this.getDomNode();

        var elem = $('.cntwFilterList', domNode);

        var pic = $("IMG", elem);
        var height = parseInt(pic.css("height") || pic.css("max-height"));

        height = this.getFullHeight(elem, numberOfLines, height);
        this.lastHeight = height = this.lastHeight > height ? this.lastHeight : height;
        elem.attr("style", "min-height:" + height + "px;");

        var contHeight = height;
        contHeight += this.getFullHeight($('.cntwFilterHeader', domNode), 1);
        contHeight += this.getFullHeight($('.cntwPager', domNode), 1);
        $(domNode).attr("style", "min-height:" + contHeight + "px;");
    },

    /**
     * overwritten
     */
    parseDataProcessItem: function(data, item, selectedValues) {
        // augment item before calling base, because base needs labelAttribute
        var userName;
        if (this.widget.configuration.useSharePointProfilePictures) {
            // SP SMALL Thumb is smaller than 40px
            item.imgSrc = communote.widget.ApiController.getUserImageUrl(item);
        }
        else{
            item.imgSrc = communote.widget.ApiController.getUserImageUrl(item, 'SMALL');
        }
        userName = communote.utils.getUserFullName(item, true);
        item[this.labelAttribute] = userName;
        this.base(data, item, selectedValues);
    },

    createPureDirective: function() {
        var itemSelector = '.cntwFilterList .cntwItem';
        var directive = {
                '.cntwFilterTitle': 'filterTitle'
        };
        directive[itemSelector] = {};
        directive[itemSelector]['entry<-' + this.resource] = {
                '.@id': 'entry.id',
                '.@class+': 'entry.highlightClass',
                'img@src': 'entry.imgSrc',
                'img@title': 'entry.' + this.labelAttribute
       };
       return directive;
    }

});