var mootabs = new Class({

    initialize: function(element, options) {
        this.options = Object.append({
            height: 'auto',
            changeTransition: Fx.Transitions.Bounce.easeOut,
            duration: 1000,
            mouseOverClass: '',
            activateOnLoad: 'first',
            useAjax: false,
            ajaxUrl: '',
            ajaxOptions: {
                method: 'get'
            },
            ajaxLoadingText: 'Loading...'
        }, options);

        this.el = $(element);
        this.elid = element;

        this.el.setStyles({
            height: this.options.height
        });

        this.titles = $$('#' + this.elid + ' .mootabs_title')[0].getChildren();
        this.panels = $$('#' + this.elid + ' .mootabs_panel');
        this.panelHeight = 'auto';

        this.panels.setStyle('height', this.panelHeight);

        this.titles.each(function(item) {
            if(item.hasClass('mootabs_disabled')){
                return;
            }
            item.addEvent('click', function() {
                item.removeClass(this.options.mouseOverClass);
                this.activate(item);
            }.bind(this));

            item.addEvent('mouseover', function() {
                if (item != this.activeTitle) {
                    item.addClass(this.options.mouseOverClass);
                }
            }.bind(this));

            item.addEvent('mouseout', function() {
                if (item != this.activeTitle) {
                    item.removeClass(this.options.mouseOverClass);
                }
            }.bind(this));
        }.bind(this));

        if (this.options.activateOnLoad != 'none') {
            if (this.options.activateOnLoad == 'first') {
                this.activate(this.titles[0], true);
            } else {
                this.activate(this.options.activateOnLoad, true);
            }
        }
    },

    activate: function(tab, skipAnim) {
        var myTab;
        if (!skipAnim != null) {
            skipAnim = true;
        }
        if (typeOf(tab) == 'string') {
            myTab = $$('#' + this.elid + ' .mootabs_title')[0].getChildren().filter('[id=' + tab + ']')[0];
            tab = myTab;
        }

        if (typeOf(tab) != 'element') {
            return;
        }

        if (tab.getPrevious() == null) {
            this.panels.getParent().addClass('mootabs_firsttab_active');
        } else {
            this.panels.getParent().removeClass('mootabs_firsttab_active');
        }

        var newTab = tab.getProperty('id') + 'Panel';
        this.panels.removeClass('active');

        this.activePanel = this.panels.filter("#" + newTab);

        this.activePanel.addClass('active');

        if (this.options.changeTransition != 'none' && skipAnim == false) {
            this.panels.filter("#" + newTab).setStyle('height', 0);
            var changeEffect = new Fx.Elements(this.panels.filter("#" + newTab), {
                duration: this.options.duration,
                transition: this.options.changeTransition
            });
            changeEffect.start({
                '0': {
                    'height': [ 0, this.panelHeight ]
                }
            });
        }

        this.titles.removeClass('active');

        tab.addClass('active');

        this.activeTitle = tab;

        if (this.options.useAjax) {
            this._getContent();
        }
    },

    _getContent: function() {
        this.activePanel.setHTML(this.options.ajaxLoadingText);
        var newOptions = {
            update: this.activePanel.getProperty('id')
        };
        this.options.ajaxOptions = Object.extend(this.options.ajaxOptions, newOptions || {});
        var tabRequest = new Ajax(this.options.ajaxUrl + '?tab='
            + this.activeTitle.getProperty('id'), this.options.ajaxOptions);
        tabRequest.request();
    },

    addTab: function(id, label, content) {
        // the new title
        var newTitle = new Element('li', {
            'id': id
        });
        newTitle.appendText(label);
        this.titles.include(newTitle);
        $$('#' + this.elid + ' ul').adopt(newTitle);
        newTitle.addEvent('click', function() {
            this.activate(newTitle);
        }.bind(this));

        newTitle.addEvent('mouseover', function() {
            if (newTitle != this.activeTitle) {
                newTitle.addClass(this.options.mouseOverClass);
            }
        }.bind(this));
        newTitle.addEvent('mouseout', function() {
            if (newTitle != this.activeTitle) {
                newTitle.removeClass(this.options.mouseOverClass);
            }
        }.bind(this));
        // the new panel
        var newPanel = new Element('div', {
            'style': {
                'height': this.options.panelHeight
            },
            'id': id + 'Panel',
            'class': 'mootabs_panel'
        });
        if (!this.options.useAjax) {
            newPanel.setHTML(content);
        }
        this.panels.include(newPanel);
        this.el.adopt(newPanel);
    },

    removeTab: function(title) {
        if (this.activeTitle.title == title) {
            this.activate(this.titles[0]);
        }
        $$('#' + this.elid + ' .mootabs_title')[0].getChildren().filter('[id=' + tab + ']')[0].remove();

        $$('#' + this.elid + ' .mootabs_panel').filter("#" + title + 'Panel')[0].remove();
    },

    next: function() {
        var nextTab = this.activeTitle.getNext();
        if (!nextTab) {
            nextTab = this.titles[0];
        }
        this.activate(nextTab);
    },

    first: function() {
        this.activate(this.titles[0]);
    },

    previous: function() {
        var previousTab = this.activeTitle.getPrevious();
        if (!previousTab) {
            previousTab = this.titles[this.titles.length - 1];
        }
        this.activate(previousTab);
    }
});
