// TODO implement EvictionStrategy for easy change of algorithm for evicting items when limit is
// reached
Cache = new Class({
    Implements: Options,

    options: {
        // default limit of cached items
        limit: 25,
        // amount of seconds after which a cached item should be considered invalid. If less
        // than 1 the item will be cached forever.
        timeToLive: -1
    },
    items: null,
    keys: null,
    // millisecond timeToLive
    timeToLive: 0,

    initialize: function(options) {
        this.setOptions(options);
        this.initCache();
        if (this.options.timeToLive > 0) {
            this.timeToLive = this.options.timeToLive * 1000;
        }
    },

    initCache: function() {
        this.items = {};
        this.keys = [];
    }.protect(),

    /**
     * Return a cached item or null if it is not cached or exceeded the timeToLive.
     * 
     * @param {String|Number} key The key of the item to retrieve
     * @return {Object} The cached value
     */
    get: function(key) {
        var item, ttl, value;
        item = this.items[key];
        if (item) {
            ttl = this.timeToLive;
            if (ttl > 0 && item.stats.timestamp + ttl < (new Date()).getTime()) {
                this.internalRemoveByKey(key);
                return null;
            }
            value = item.value;
            // TODO call EvictionStrategy to update stats on get
        }

        return value;
    },

    /**
     * Add a value identified by a key to the cache. If there is already a cached item with that key
     * it will be updated.
     * 
     * @param {String} key The key of the value to cache
     * @param {Object} value The value to cache
     */
    put: function(key, value) {
        var oldestId, added, keys;
        var items = this.items;
        var item = items[key];
        if (!item) {
            added = true;
            keys = this.keys;
            if (keys.length == this.options.limit) {
                this.limitReached(keys, items);
            }
            item = {
                stats: {}
            };
            items[key] = item;
            keys.push(key);
        }
        item.value = value;
        item.stats.timestamp = (new Date()).getTime();
        // TODO call EvictionStrategy to update stats on put
    },

    limitReached: function(keys, items) {
        var ttl, i, item, itemRemoved, now;
        ttl = this.timeToLive;
        if (ttl > 0) {
            now = (new Date()).getTime();
            // remove one item that exceeded the time to live
            for (i = 0; i < keys.length; i++) {
                item = items[keys[i]];
                if (item.stats.timestamp + ttl < now) {
                    this.internalRemoveByIndex(i);
                    itemRemoved = true;
                    break;
                }
            }
        }
        if (!itemRemoved) {
            // TODO call EvictionStrategy to return index/key of item to remove
            // fifo eviction strategy: remove first item that was added to the cache
            this.internalRemoveByIndex(0);
        }
    },

    internalRemoveByIndex: function(idx) {
        var key = this.keys.splice(idx, 1)[0];
        delete this.items[key];
    }.protect(),

    internalRemoveByKey: function(key) {
        delete this.items[key];
        this.keys.erase(key);
    }.protect(),

    /**
     * Remove a cached value.
     * 
     * @param {String} key The key of the value to remove from the cache
     * @return {Boolean} whether the value was cached
     */
    remove: function(key) {
        if (this.items[key]) {
            this.internalRemoveByKey(key);
            return true;
        }
        return false;
    },
    /**
     * Empty the cache.
     */
    removeAll: function() {
        this.initCache();
    }
});