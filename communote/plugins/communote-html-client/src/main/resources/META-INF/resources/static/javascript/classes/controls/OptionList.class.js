/**
 * @class
 * @name communote.widget.classes.controls.OptionList
 * @augments communote.widget.classes.controls.ContainerControl
 */
communote.widget.classes.controls.OptionList = communote.widget.classes.controls.ContainerControl
        .extend(
/** 
 * @lends communote.widget.classes.controls.OptionList.prototype
 */
{
            name: "OptionList",
            noContainer: false,
            containerTag: 'ul',
            css: [ 'cntwOptionList' ],

            registerListeners: function(listeners) {
                this.listenTo('optionSelected', this.parent.channel);

                this.base();
            },

            loadControls: function() {
                var i, item;
                var items = this.configData.items;
                for (i in items) {
                    item = items[i];
                    if (item.items) {
                        subItems = communote.jQuery.merge([ {
                            label: 'htmlclient.optionlist.less',
                            css: 'cntwLessItems'
                        } ], item.items);
                        this.controls.push({
                            type: 'OptionList',
                            name: this.name,
                            slot: '.' + item.css,
                            css: this.config.css,
                            data: {
                                items: subItems
                            }
                        });
                    }
                }
                this.base();
            },

            loadData: function() {
                var items = this.configData.items || [ {
                    label: 'no items'
                } ];
                this.insertValues({
                    options: items
                });
            },

            insertValues: function(data) {
                this.base(data);
                var node;
                var list = this.getDomNode();
                var children = list.children();
                if (this.configData.pipeList) {
                    list.addClass('cntwPipeList');
                }
                if (this.configData.autoWidth) {
                    list.addClass('cntwAutoWidth');
                    children.width((100 / children.length) + '%');
                }
                children.addClass('cntwItem');
                children.first().addClass('cntwFirst');
                children.last().addClass('cntwLast');
                if (this.parent.type == this.type) {
                    node = this.getDomNode();
                    node.addClass('cntwPopupOptionList');
                }
            },

            bindEvents: function() {
                var more;
                var $ = communote.jQuery;
                var self = this;
                var items = $('> .cntwItem', this.getDomNode());
                items.click(function() {
                    self.fireEvent('optionSelected', self.parent.channel, $(this).children());
                    self.fireEvent('optionSelected', self.channel, $(this).children());
                    if (self.controls.length > 0) {
                        return false;// prevent event bubbeling
                    }
                    return true;
                });
                more = $('> .cntwItem > .cntwMoreItems', this.getDomNode());
                //list = more.find('> .cntwOptionList');
                more.click(function() {
                    self.showSubList();
                });
                if (this.configData.showSelection){self.fireEvent('optionSelected', self.parent.channel, items.first().children());}
            },

            showSubList: function() {
                var more = communote.jQuery('> .cntwItem > .cntwMoreItems', this.getDomNode());
                var list = more.find('> .cntwOptionList');
                var container = list.parent();
                var offset = container.offset() || {};
                list.css({
                    position: 'absolute',
                    top: offset.top,
                    left: offset.left
                });
                list.show();
            },

            optionSelected: function(selected) {
                var item = selected.parent();
                var items = item.parent().children();
                if (!selected.hasClass('cntwMoreItems') && (this.parent.type == this.type)) {
                    this.getDomNode().hide();
                }
                if (this.configData.showSelection) {
                    items.removeClass('cntwSelected');
                    item.addClass('cntwSelected');
                }
            },

            parseData: function(data) {
                this.base(data);
                var self = this;
                communote.jQuery.each(data.options, function(index, option) {
                    // if label == false label is disabeled (this will make debugging
                    // easier, if you just forgot to set the label)
                    if ((option.label && option.label === false) 
                            || (option.text && option.text === false)) {
                        option.optionText = "";
                        option.hide = true;
                    } else {
                        if (option.text === undefined) {
                            option.optionText = self.getLabel(option.label);
                        } else {
                            option.optionText = option.text;
                        }
                    }
                    option.itemClass = 'cntwItem';
                    if (option.hide) {
                        option.itemClass = ' cntwHide';
                    }
                    if (option.selected) {
                        option.itemClass += ' cntwSelected';
                    }
                });
            },

            getDirectives: function() {
                var dir = {
                    '.cntwItem': {
                        'option<-options': {
                            '.cntwOption': 'option.optionText',
                            '.cntwOption@class': 'option.css',
                            '.@class': 'option.itemClass'
                        }
                    }
                };
                return dir;
            }
        });
