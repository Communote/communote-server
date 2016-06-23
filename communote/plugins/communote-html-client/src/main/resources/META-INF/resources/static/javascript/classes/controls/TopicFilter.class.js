/**
 * @class
 * @augments communote.widget.classes.controls.Filter
 */
communote.widget.classes.controls.TopicFilter = communote.widget.classes.controls.Filter.extend(
/** 
 * @lends communote.widget.classes.controls.TopicFilter.prototype
 */ 	
{
    name: 'TopicFilter',
    resource: 'topics',
    titleKey: 'htmlclient.topicfilter.topics',

    labelAttribute: 'title',
    filterParameterValueAttribute: 'topicId',
    filterParameterName: 'topicIds',
    offset: 0,
    maxCount: 1,
    renderParameters: null,
    lastHeight: 0,
    header: undefined,

    /**
     * @param id
     * @param config
     * @param widget
     */
    constructor: function(id, config, widget) {
        var preselected, paramStore;
        this.base(id, config, widget);
        // define the parameters to ignore which are the topicIds and topicAliases
        this.filterParametersToIgnore = ['topicIds', 'topicAliases'];
        this.renderParameters = {};
        // in case there are predefined topicAliases and topicIds always add them
        // to the render parameters
        paramStore = widget.getFilterParameterStore();
        preselected = paramStore.getPredefinedFilterParameter('topicIds');
        if (preselected) {
            this.renderParameters['topicIds'] = preselected;
        }
        preselected = paramStore.getPredefinedFilterParameter('topicAliases');
        if (preselected) {
            this.renderParameters['topicAliases'] = preselected;
        }
        this.maxCount = widget.configuration.fiTopicPageSize;
    },

    /**
     * overwritten
     */
    getRenderParameters: function() {
        return this.renderParameters;
    },

    createElementIdFromTopicId: function(topicId) {
        return this.filterElementIdPrefix + topicId;
    },

    /**
     * overwritten
     */
    createFilterElementId: function(item) {
        // use topicId instead of index
        return this.createElementIdFromTopicId(item.topicId);
    },

    /**
     * overwritten
     */
    filterChanged: function(data) {
        var selector, elem;
        var $ = communote.jQuery;
        // the topic filter should not be filtered when a topic is selected, just highlight
        if (!$.isArray(data) && data.paramName === this.filterParameterName) {
            selector = '#' + this.createElementIdFromTopicId(data.value);
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
                moreResultsAvailable: data.origResult.metaData.moreElementsAvailable,
                totalNumberOfElements: data.origResult.metaData.numberOfElements,
                numberOfElements: data.topics.length,
                maxCount: this.maxCount
        };
        if(data.totalNumberOfElements > this.maxCount) {
            this.setContainerHeight(data.numberOfElements);
            this.controls  = [{
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
        var height = this.getFullHeight(elem, numberOfLines);
        this.lastHeight = height = this.lastHeight > height ? this.lastHeight : height;
        elem.attr("style", "min-height:" + height + "px;");

        var contHeight = height;
        contHeight += this.getFullHeight($('.cntwFilterHeader', domNode), 1);
        contHeight += this.getFullHeight($('.cntwPager', domNode), 1);

        $(domNode).attr("style", "min-height:" + contHeight + "px;");
    }

});
