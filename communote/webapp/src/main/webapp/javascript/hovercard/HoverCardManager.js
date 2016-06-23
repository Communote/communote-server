(function(namespace, window) {
    var classesNamespace;

    function useType(type, typesToInclude, typesToExclude) {
        var use = false;
        if (typesToInclude) {
            if (typesToInclude.indexOf(type) != -1) {
                use = true;
            }
        } else if (typesToExclude) {
            if (!typesToExclude.indexOf(type) != -1) {
                use = true;
            }
        } else {
            use = true;
        }
        return use;
    }

    classesNamespace = ((namespace && namespace.classes) || window);
    /**
     * Helper to manage all HoverCard instances on a page.
     */
    classesNamespace.HoverCardManager = function() {
        this.hoverCards = {};
        this.disabled = !window.Modernizr || Modernizr.touch;
    };

    /**
     * Attach all registered HoverCards that have a matching type.
     * 
     * @param {Element} [domNode] Element whose child nodes should be searched for HoverCard
     *            candidates. If not provided the whole DOM will be searched.
     * @param {String|String[]} [typesToInclude] Type name or array of type names of registered
     *            hover cards which should be attached. If not provided all types will be included
     *            if they are not contained in the typesToExclude.
     * @param {String|String[]} [typesToExclude] Type name or array of type names of registered
     *            hover cards which should not be attached. If not provided no types will be
     *            excluded. If typesToInclude and typesToExclude are both defined, only the first
     *            one will be considered.
     */
    classesNamespace.HoverCardManager.prototype.attachHoverCards = function(domNode,
            typesToInclude, typesToExclude) {
        var type, hoverCards;
        if (typesToInclude) {
            typesToInclude = Array.from(typesToInclude);
        }
        if (typesToExclude) {
            typesToExclude = Array.from(typesToExclude);
        }
        hoverCards = this.hoverCards;
        for (type in hoverCards) {
            if (hoverCards.hasOwnProperty(type) && useType(type, typesToInclude, typesToExclude)) {
                hoverCards[type].attach(domNode);
            }
        }
    };

    /**
     * Detach all registered HoverCards that have a matching type.
     * 
     * @param {Element} [domNode] Element whose child nodes should be searched for HoverCard
     *            candidates. If not provided the whole DOM will be searched.
     * @param {String|String[]} [typesToInclude] Type name or array of type names of registered
     *            hover cards which should be detached. If not provided all types will be included
     *            if they are not contained in the typesToExclude.
     * @param {String|String[]} [typesToExclude] Type name or array of type names of registered
     *            hover cards which should not be detached. If not provided no types will be
     *            excluded. If typesToInclude and typesToExclude are both defined, only the first
     *            one will be considered.
     */
    classesNamespace.HoverCardManager.prototype.detachHoverCards = function(domNode,
            typesToInclude, typesToExclude) {
        var type, hoverCards;
        if (typesToInclude) {
            typesToInclude = Array.from(typesToInclude);
        }
        if (typesToExclude) {
            typesToExclude = Array.from(typesToExclude);
        }
        hoverCards = this.hoverCards;
        for (type in hoverCards) {
            if (hoverCards.hasOwnProperty(type) && useType(type, typesToInclude, typesToExclude)) {
                hoverCards[type].detach(domNode);
            }
        }
    };
    classesNamespace.HoverCardManager.prototype.register = function(type, hoverCard) {
        if (type && !this.disabled) {
            this.hoverCards[type] = hoverCard;
        }
    };

    classesNamespace.HoverCardManager.prototype.unregister = function(type) {
        var hoverCard = this.hoverCards[type];
        if (hoverCard) {
            hoverCard.detach(document);
            delete this.hoverCards[type];
        }
    };
})(this.runtimeNamespace, this);
