// TODO this is kind of strange! First it is inconsistent (2 methods need self, the other working on this) and 2nd the
// methods aren't really reusable, since they programatically bound to the autocomplete widget. Why not using inheritance instead?
// The renderItem function passed to the renderMenu method could be converted into a method that is overriden in the subclasses. 
communote.jqueryui = {
        
        resizeMenu: function(self){
            var width;
            var posOpts = self.options.position;
            // use positioning target for width calculation if defined
            if (posOpts && posOpts.of) {
                width = self.options.position.of.outerWidth();
            } else {
                width = self.element.outerWidth();
            }
            // TODO let container overflow horizontally if content is wider?  
            self.menu.element.outerWidth(width);
        },
        
        renderMenu: function(self, ul, items, renderItem, metadata){
            var currentCategory = "";
            var term;
            var metadataCount = 0;            
            var toDelete = [];
            // TODO referencing communote.jQuery wouldn't be necessary if inheritance were used
            var $ = communote.jQuery; 
            
            // set max height if given and activate scrolling
            if (self.options.maxHeight !== undefined) {
                ul.css('max-height', self.options.maxHeight);
                ul.addClass('ui-autocomplete-scrolling');
            }
            
            // add class if given
            if (self.options.cssClass !== undefined) {
                ul.addClass(self.options.cssClass);
            }
            
            

            // removes the leading encapsulated tag inputs and whitespaces from the term to prevent missing highlighting            
            term = self.term;             
            term = term.substring(term.lastIndexOf(',')+1);
            term = term.replace(/^\s+|\s+$/g,'');
            

            // render items
            $.each( items, function( index, item ) {
                var idx, re, reResult, utils, length, label, displayLabel;
                
                if ( (item.category != currentCategory) && item.category !== undefined ) {                    
                    if((metadata.length != 0) && (currentCategory != "")){
                        if(metadata[metadataCount]){
                            ul.append('<li class="metadata">' + metadata[metadataCount] + '</li>');
                        }
                        metadataCount = metadataCount + 1;
                    }
                    ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
                    currentCategory = item.category;
                }
                // build value to display: highlight match in response and encode it
                utils = communote.utils;
                                
                re = new RegExp($.ui.autocomplete.escapeRegex(term), 'i');
                length = term.length;
                label = item.label;
                if(label != ""){
                    
                    displayLabel = '';
                    idx = 0;
                    // only interested in first match
                    reResult=re.exec(label);
                    if (reResult) {
                        displayLabel += utils.encodeXml(label.substring(idx, reResult.index));
                        displayLabel += '<strong>' + utils.encodeXml(reResult[0]) + '</strong>';
                        idx = reResult.index + length;
                    }
                    displayLabel += utils.encodeXml(label.substring(idx));
                
                
                    var renderedItem = renderItem(item);
                    
                    if (item.context !== undefined) {
                        renderedItem.append("<a>" + displayLabel + "&nbsp;["+item.context+"]"+"</a>");
                    } else {
                        renderedItem.append("<a>" + displayLabel + "</a>");
                    }
                    
                    renderedItem.appendTo(ul);
                } else {
                    toDelete[toDelete.length] = index;
                }
                
            });
            
            for(var i = toDelete.length-1; i >= 0; i--){
                items.splice(toDelete[i],1);
            }
            
            if(metadata[metadataCount]){
                ul.append('<li class="metadata">' + metadata[metadataCount] + '</li>');
            } 
        },
        
        activateMenu: function( event, item ) {
            this.deactivate();
            if((item!=null) && (item.offset() != null)){
                if (this.hasScroll()) {
                    var offset = item.offset().top - this.element.offset().top,
                        scroll = this.element.scrollTop(),
                        elementHeight = this.element.height();
                    if (offset < 0) {
                        this.element.scrollTop( scroll + offset);
                    } else if (offset >= elementHeight) {
                        this.element.scrollTop( scroll + offset - elementHeight + item.height());
                    }
                }
                this.active = item.eq(0)
                    .children("a")
                        .addClass("ui-state-hover")
                        .attr("id", "ui-active-menuitem")
                    .end();
                this._trigger("focus", event, { item: item });
            }
        }
};
    

/**
 * our Catcomplete extension for jQuery UI
 */
(function($){
    $.widget( "custom.catcomplete", $.ui.autocomplete, {
        metadata: [],
        _resizeMenu: function() {
            communote.jqueryui.resizeMenu(this);
        },
        _renderMenu: function( ul, items ) {
            this.menu.activate =  communote.jqueryui.activateMenu;
            var self = this;
            communote.jqueryui.renderMenu(self, ul, items, function(item){
                var doc = self.element[0].ownerDocument;
                return $("<li></li>", doc).data("item.autocomplete", item);
            },self.metadata);
        }
        
    });
})(communote.jQuery);

/**
 * our Imagecomplete extension for jQuery UI
 */
(function($){
    $.widget( "custom.imagecomplete", $.ui.autocomplete, {
        metadata: [],
        _resizeMenu: function() {
            communote.jqueryui.resizeMenu(this);
        },
        _renderMenu: function( ul, items ) {
            var self = this;
            this.menu.activate =  communote.jqueryui.activateMenu;
            communote.jqueryui.renderMenu(self, ul, items, function(item){

                var renderedItem, imgUrl, imageContainer, user, doc;

                doc = self.element[0].ownerDocument;
                user = item.user;
                imgUrl = communote.widget.ApiController.getUserImageUrl(user,'SMALL');
                
                renderedItem = $("<li></li>", doc).data("item.autocomplete", item).addClass('cntwImageItem');
                imageContainer = $('<div></div>', doc).addClass('cntwAuthorImage');
                imageContainer.append($('<img />', doc).attr({src:imgUrl, alt:item.label, title:item.label}));
                renderedItem.append(imageContainer);
                
                return renderedItem;
            }, self.metadata);
        }
    });
})(communote.jQuery);
/**
 * simple menu rendered as overlay and positioned to the element
 */
(function($){
    $.widget( 'custom.overlaymenu', {
        options: {
            menuCssClass: 'cntwMenu',
            itemSetTitleCssClass: 'cntwMenu',
            itemSetEntryCssClass: 'cntwItem',
            itemIdPrefix: null,
            itemLabelAttribute: 'label',
            itemSetTitleAttribute: 'title',
            itemSetEntriesAttribute: 'entries',
            positionDefinition: {
                my: 'left top',
                at: 'left bottom',
                collision: 'none'
            },
            openCallback: null,
            closeCallback: null,
            selectionCallback: null,
            loadData: null,
            loadDataBind: null,
            loadDataCssClass: null,
            growHorizontally: false
        },
        positionDefinition: null,
        items: null,
        itemIdPrefix: null,
        isOpen: false,
        isDirty: true,
        
        _create: function() {
            var appendTo, menuElem, doc;
            doc = this.element[0].ownerDocument;
            menuElem = $('<ul></ul>', doc);
            menuElem.addClass(this.options.menuCssClass);
            menuElem.css({ 
                'top': 0, 
                'left': 0, 
                'position': 'absolute',
                'overflow-x': this.options.growHorizontally ? 'hidden' : 'auto'
            });
            menuElem.zIndex(this.element.zIndex() + 1);
            menuElem.hide();
            appendTo = $(this.options.appendTo || 'body', doc);
            menuElem.appendTo(appendTo[0]);
            this.menu = this._createMenuUI(menuElem);
            this.positionDefinition = $.extend({
                of: this.element
            }, this.options.positionDefinition);
            if (this.options.itemIdPrefix) {
                this.itemIdPrefix = this.options.itemIdPrefix;
            } else {
                this.itemIdPrefix = (new Date()).getTime() + '_' + Math.floor(Math.random()*1000);
            }
            this.items = [];
        },
        _createMenuUI: function(menuElem) {
            var self = this;
            return menuElem.menu({
                selected: function(event, data) {
                    var id = self.menu.active.attr('id');
                    var idx = parseInt(id.substring(self.itemIdPrefix.length), 10);
                    self.selected(self.items[idx], this.active);
                    return true;
                }
            }).data('menu');
        },
        destroy: function() {
            this.menu.destroy();
            this.menu.element.remove();
            $.Widget.prototype.destroy.call(this);
        },
        /**
         * Mark menu as dirty. The next call to open will invoke the loadData function if
         * one is defined in the options. If the menu is currently opened loadData will 
         * be triggered immediately.
         */
        markDirty: function() {
            this.isDirty = true;
            if (this.isOpen) {
                this._loadData();
            }
        },
        toggle: function(eventData) {
            if (this.isOpen) {
                this.close(eventData);
            } else {
                this.open(eventData);
            }
        },
        open: function(eventData) {
            var elem, callback;
            if (!this.isOpen && ((this.items.length > 0) || this.isDirty)) {
                // if dirty check if there is load function and call it
                if (this.isDirty && !this._loadData()) {
                    return;
                }
                this.isOpen = true;
                elem = this.menu.element;
                elem.show();
                elem.position(this.positionDefinition);
                this._resizeMenu();
                callback = this.options.openCallback;
                if ($.isFunction(callback)) {
                    callback.call(this, eventData);
                }
            }
        },
        close: function(eventData) {
            var callback;
            if (this.isOpen) {
                this.isOpen = false;
                this.menu.element.hide();
                this.menu.deactivate();
                callback = this.options.closeCallback;
                if ($.isFunction(callback)) {
                    callback.call(this, eventData);
                }
            }
        },
        _resizeMenu: function() {
            var width = this.element.outerWidth();
            if (this.options.growHorizontally) {
                width = Math.max(width, this.menu.element.width('').outerWidth());
            }
            this.menu.element.outerWidth(width);
        },
        
        _loadData: function() {
            var bind, cssClass;
            if ($.isFunction(this.options.loadData)) {
                cssClass = this.options.loadDataCssClass;
                if (cssClass) {
                    this.menu.element.addClass(cssClass);
                }
                bind = this.options.loadDataBind || this; 
                this.options.loadData.call(bind);
                return true;
            }
            return false;
        },
        dataLoaded: function(data) {
            var cssClass;
            this.isDirty = false;
            this.insertValues(data, true);
            cssClass = this.options.loadDataCssClass;
            if (cssClass) {
                this.menu.element.removeClass(cssClass);
            }
        },
        insertValues: function(data, replace) {
            var elem, title;
            elem = this.menu.element;
            if (replace) {
                elem.empty();
                this.items = [];
            }
            if ($.isArray(data)) {
                this._appendMenuElements(elem, data);
            } else {
                // if object append a title element and add entries afterwards
                title = data[this.options.itemSetTitleAttribute];
                if (title) {
                    this._appendTitleElement(elem, title);
                }
                this._appendMenuElements(elem, data[this.options.itemSetEntriesAttribute]);
            }
            this.menu.refresh();
            
        },
        _appendTitleElement: function(container, title) {
            var titleCssClass = this.options.itemSetTitleCssClass;
            var li = $('<li></li>', container[0].ownerDocument);
            li.text(title);
            if (titleCssClass.length > 0) {
                li.addClass(titleCssClass);
            }
            container.append(li);
        },
        _appendMenuElements: function(container, data) {
            var i, li, labelAttribute, item, itemCssClass, doc;
            if (data) {
                doc = container[0].ownerDocument;
                itemCssClass = this.options.itemSetEntryCssClass;
                labelAttribute = this.options.itemLabelAttribute;
                for (i = 0; i < data.length; i++) {
                    item = data[i];
                    li = $('<li></li>', doc);
                    li.addClass(itemCssClass);
                    li.attr('id', this.itemIdPrefix + this.items.length);
                    li.append($('<a></a>', doc).text(item[labelAttribute]));
                    container.append(li);
                    this.items.push(item);
                }
            }
        },
        /**
         * Called when a menu element is clicked.
         */
        selected: function(item, elem) {
            var callback;
            this.close(null);
            callback = this.options.selectionCallback;
            if ($.isFunction(callback)) {
                callback.call(this, item);
            }
        }
    });
})(communote.jQuery);
            