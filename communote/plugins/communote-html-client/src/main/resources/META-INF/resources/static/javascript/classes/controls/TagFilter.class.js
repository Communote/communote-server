/**
 * @class
 * @augments communote.widget.classes.controls.Filter
 */
communote.widget.classes.controls.TagFilter = communote.widget.classes.controls.Filter.extend(
/** 
 * @lends communote.widget.classes.controls.TagFilter.prototype
 */ 	
{
    name: 'TagFilter',
    resource: 'tags',
    titleKey: 'htmlclient.tagfilter.tags',
    labelAttribute: 'name',
    filterParameterValueAttribute: 'tagId',
    filterParameterName: 'tagIds'
});
