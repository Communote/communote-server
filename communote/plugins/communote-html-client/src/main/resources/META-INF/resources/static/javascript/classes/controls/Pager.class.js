/**
 * @class
 * @augments communote.widget.classes.controls.Control
 */
communote.widget.classes.controls.Pager = communote.widget.classes.controls.Control.extend(
/** 
 * @lends communote.widget.classes.controls.Pager.prototype 
 */
{
            name: 'pager',           
            noContainer: false,            
            css: [ 'pagingCss' ],
            template: 'pager',
            parent: undefined,
            offset: 0,
            moreResultsAvailable : undefined,
            totalNumberOfElements : undefined,
            numberOfElements : undefined,
            maxCount: 0,
                
            
            insertValues: function(data) {                
                this.base(data);  
                this.maxCount = this.configData.maxCount;
                this.parent = this.config.parent;
                this.offset = this.configData.offset;
                this.moreResultsAvailable = this.configData.moreResultsAvailable;
                this.totalNumberOfElements = this.configData.totalNumberOfElements;
                this.numberOfElements = this.configData.numberOfElements;
                
                if ((this.numberOfElements == 0) || (this.totalNumberOfElements <= this.maxCount)) {
                    this.hidePagingElements();
                } else {
                    this.handlePageStates(this.offset, this.moreResultsAvailable);
                }                
            },            
            
            handlePageStates: function(offset, moreResultsAvailable) {

                if (offset == 0) {
                    this.switchToInactiveState('cntwPrePage');
                } else {
                    this.switchToActiveState('cntwPrePage');
                }
                if (moreResultsAvailable) {
                    this.switchToActiveState('cntwNextPage');
                } else {
                    this.switchToInactiveState('cntwNextPage');
                }
            },
            
            bindLeftArrowEvents: function() {
                var self = this;          
                communote.jQuery('.cntwPrePage', this.getDomNode()).click(function() {
                    // self.parent.offset = self.offset == 0 ? 0 : self.offset - 10;
                    self.parent.offset = self.offset == 0 ? 0 : self.offset - self.maxCount;
                    self.parent.reRender();
                    return;
                });
            },
            
            bindRightArrowEvents: function() {
                var self = this;
                communote.jQuery('.cntwNextPage', this.getDomNode()).click(function() {
                    // self.parent.offset = self.offset + 10;
                    self.parent.offset = self.offset + self.maxCount;
                    self.parent.reRender();
                    return;
                });
            },
            
            unbindLeftArrowEvents: function() {
                communote.jQuery('.cntwPrePage', this.getDomNode()).unbind();
            },

            unbindRightArrowEvents: function() {
                communote.jQuery('.cntwNextPage', this.getDomNode()).unbind();
            },
            
            hidePagingElements: function() {
                var pagingElements = communote.jQuery('ul.cntwPaging', this.getDomNode());
                pagingElements.hide();
            },

            switchToInactiveState: function(className) {
                var $ = communote.jQuery;
                var domNode = this.getDomNode();
                $('.' + className, domNode).addClass(className + "Inactive");
                $('.' + className, domNode).removeClass(className);
                if (className == 'cntwPrePage'){this.unbindLeftArrowEvents();} else {this.unbindRightArrowEvents();}
            },

            switchToActiveState: function(className) {
                var $ = communote.jQuery;
                var domNode = this.getDomNode();
                $('.' + className, domNode).addClass(className);
                $('.' + className, domNode).removeClass(className + 'cntwPrePageInactive');
                if (className == 'cntwPrePage'){this.bindLeftArrowEvents();} else {this.bindRightArrowEvents();}
            }
       });