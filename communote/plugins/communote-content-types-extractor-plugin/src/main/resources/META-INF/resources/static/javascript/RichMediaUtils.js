// TODO should just be a utils class that provides a method to search and render links, currently there is no benefit
// in creating a RichMedia object -> only one instance needed
// one consideration: if there is a way to stop a playing video via JavaScript we could return for each added video
// a wrapper object with methods to stop the playback. A widget could use this to stop the video before refreshing.
// TODO add to communote namespace
// TODO make it more generic: extract type and call a method render<ExtractedType>Media if exists -> other plugins
// could add new media types
var RichMedia = new Class({
    
    Implements: [Options],

    options: {
    
        context: null,
        /**
         * Elements to check
         * (single element, elements collection or a string selector)
         * @var {Element|Elements|String}
         */
        elements: 'span.richmedia-source'
    },
    
    elements: [],
    
    initialize: function(options) {
        // Setting options
        this.setOptions(options);

        var context = (this.options.context) ? this.options.context : document;

        switch (typeOf(this.options.elements)) {
            case 'string':
                this.elements = context.getElements(this.options.elements);
                break;
            case 'element':
                this.elements = [this.options.elements];
                break;
            default:
                this.elements = this.options.elements;
        }
    },
    
    /** 
     * Renders a single element.
     */
    renderElement: function(element) {
        if(element.hasClass('YOUTUBE')){
            this.renderYoutube(element);
        } else if(element.hasClass('VIMEO')){
            this.renderVimeo(element);
        }
    },

    /** 
     * Renders all elements.
     */
    renderElements: function() {
        for( var i=0; i < this.elements.length; i++ ) {
            if(this.elements[i].hasClass('YOUTUBE')){
                this.renderYoutube(this.elements[i]);
            } else if(this.elements[i].hasClass('VIMEO')){
                this.renderVimeo(this.elements[i]);
            }
        }
    },
    
    /** 
     * Renders YouTube videos.
     */
    renderYoutube: function(element) {
        var mediaUrl = '//www.youtube.com/embed/';
        var mediaQueryString = '?wmode=transparent&rel=0';
        var params = this.getParams(element);
        
        // Safari may not update iframe content with a static ID
        var mediaId = "media-id-" + new Date().getTime(); 
        
        var content = new Element('iframe', {
            'id': mediaId,
            'src': params.protocol + mediaUrl + params.mediaId + mediaQueryString,
            'class': params.cssClass,
            'title': params.frameTitle,
            'type': 'text/html'
        });
        
        content.replaces(element);
    },

    /** 
     * Renders vimeo videos.
     */
    renderVimeo: function(element) {
        var mediaUrl = '//player.vimeo.com/video/';
        var params = this.getParams(element);
        
        // Safari may not update iframe content with a static ID
        var elementId = "media-id-" + new Date().getTime(); 
        
        var content = new Element('iframe', {
            'id': elementId,
            'src': params.protocol + mediaUrl + params.mediaId,
            'class': params.cssClass,
            'title': params.title,
            'type': 'text/html'
        });
        
        content.replaces(element);
    },
    
    /** 
     * @return The video parameters.
     */
    getParams: function(element) {
        // TODO refactor to use data-* Attributes instead of CSS classes
        var params = { 'mediaId': '', 'cssClass': '', 'title': '', 'protocol': ''};
        // TODO better name force https
        // use protocol of current page to avoid injecting insecure content when using HTTPS
        params.protocol = element.hasClass('https') ? 'https:' : window.location.protocol;
        
        // remove not relevant css classes
        element.removeClass('richmedia-source');
        element.removeClass('YOUTUBE');
        element.removeClass('VIMEO');
        element.removeClass('https');

        params.cssClass = element.get('class');
        params.title = element.get('title');
        params.mediaId = element.get('html');
        return params;
    }
});