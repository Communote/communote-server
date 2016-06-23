(function(namespace) {

    /**
     * FilterParameterChangedHandler for the viewId which sets the CSS class active to the
     * navigation-item that belongs to the new view.
     * 
     * @param {String} [oldValue] The previous viewId, can be null
     * @param {String} newValue The new viewId
     */
    function viewIdChanged(oldValue, newValue) {
        var navigationItemElem;
        if (oldValue) {
            navigationItemElem = document.getElement('.navigation-item.' + oldValue);
            if (navigationItemElem) {
                navigationItemElem.removeClass('active');
            }
        }
        navigationItemElem = document.getElement('.navigation-item.' + newValue);
        if (navigationItemElem) {
            navigationItemElem.addClass('active');
        }
    }

    /**
     * Click event handler for navigation items added to the vertical navigation by calls to
     * addVerticalNavigationItem;
     * 
     * @param {Event} event The click event
     */
    function verticalNavigationItemClicked(event) {
        var linkElem = document.id(this);
        var linkHandlerData = linkElem.getProperty('data-cnt-linkHandler-data');
        if (linkHandlerData) {
            linkHandlerData = JSON.decode(linkHandlerData);
        }
        namespace.linkHandler.open(linkElem.getProperty('href'), linkHandlerData);
        event.preventDefault();
    }

    if (!namespace) {
        namespace = window;
    }
    if (!namespace.utils) {
        namespace.utils = {};
    }
    namespace.utils.navigationUtils = {

        /**
         * Add a observer which highlights the current active vertical navigation item by applying
         * the CSS class 'active'.
         * 
         * @param {String} filterGroupId The ID of the filter parameter group which should be
         *            observed for viewId changes
         */
        addHighlightActiveVerticalNavigationItemObserver: function(filterGroupId) {
            var constructor = namespace.getConstructor('FilterParameterChangedHandler');
            new constructor(filterGroupId, 'viewId', viewIdChanged);
        },
        /**
         * Add a new navigation item to a page with a vertical navigation that provides a template
         * element with a certain structure and works on switching views which are identified by
         * viewIds. The template element needs to have the ID vertivalNavigationItemTemplate and a
         * child element with CSS class control-navigation-item-title that will receive the title.
         * If the template contains an A tag this element will receive the click handler which calls
         * the linkHandler and passes the href value as URL. The href attribute can contain the
         * string VIEW_ID that will be replaced with the provided viewId. This element can also
         * contain a data-Attribute named data-cnt-linkHandler-data which can contain a serialized
         * JSON object to be passed to the link handler. The template and the other navigation items
         * must have the navigation-item CSS class on the outermost element.
         * 
         * @param {String} title The title of the new navigation item
         * @param {String} viewIdToOpen The ID of the view to open when the navigation item is
         *            clicked
         * @param {String} [where] String describing where to add the navigation item, can be one of
         *            'top', 'bottom', 'before viewId' and 'after viewId'. The first two refer to
         *            the top and the bottom of the navigation. The last two allow to add the new
         *            item before or after an existing item which is identified by its viewId. If
         *            the item does not exist the new item is added to the bottom. If this parameter
         *            is omitted 'bottom' is used.
         */
        addVerticalNavigationItem: function(title, viewIdToOpen, where) {
            var itemElem, viewIdRel, itemLinkElem, href, relElem;
            var template = document.getElementById('vertivalNavigationItemTemplate');
            if (!template) {
                // ignore
                return;
            }
            // IE 8 compatibility
            template = document.id(template);
            // normalize where
            if (where) {
                if (typeof (where) == 'string') {
                    where = where.split(' ');
                }
                viewIdRel = where[1];
                where = where[0];
                if (where != 'before' && where != 'after' && where != 'top' && where != 'bottom') {
                    where = 'bottom';
                }
            } else {
                where = 'bottom';
            }
            itemElem = template.clone(true, false);
            itemElem.addClass(viewIdToOpen)
            itemElem.getElement('.control-navigation-item-title').set('text', title);
            itemLinkElem = itemElem.getElement('a');
            href = itemLinkElem.getProperty('href');
            if (href) {
                href = href.replace(new RegExp('VIEW_ID', 'g'), viewIdToOpen);
                itemLinkElem.setProperty('href', href);
                itemLinkElem.addEvent('click', verticalNavigationItemClicked);
            }
            if ((where == 'before' || where == 'after') && viewIdRel) {
                relElem = document.getElement('.navigation-item.' + viewIdRel);
                if (!relElem) {
                    where = 'bottom';
                }
            }
            if (!relElem) {
                relElem = document.getElement('.navigation-item').getParent();
            }
            itemElem.inject(relElem, where);
            itemElem.setStyle('display', '');
        }

    };

})(window.runtimeNamespace);