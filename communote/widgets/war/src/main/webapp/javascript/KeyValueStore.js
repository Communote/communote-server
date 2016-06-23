/**
 * @class Helper to store data. The data is expected to be an object with a type and a key attribute.
 * To retrieve the data type and key must be provided.
 */
var C_KeyValueStore = new Class( /** @lends C_KeyValueStore.prototype */ {
   
    storedDate: {},
    
    /**
     * Put some data in the store.
     * @param {Object} data An object containing the data to be stored. The object must have an
     *  attribute named 'type' which defines the content type (e.g. 'user') and an attribute named
     *  'key' which uniquely identifies the stored data within the content type. The remaining 
     *  attributes will be stored under the key and are later retrievable via {@link #get}. A full example
     *  looks like: {type: 'user', key: '65', shortName: 'test user', longName: 'test user (test)'}
     */
    put: function(data) {
        var type = data.type;
        var key = data.key;
        if (type && key) {
            var val = {};
            for (var i in data) {
                if (i != 'type' &&  i != 'key') val[i] = data[i];
            }
            this.storedDate[type + key] = val;
        }
    },
    
    /**
     * Return stored data for a provided content type and key. The result for get('user', 65) 
     * after a call to {@link #put} (with the parameters listed in the documentation of the put
     * method) would be {shortName: 'test user', longName: 'test user (test)'}
     *
     * @param {String} type The content type.
     * @param {String} key The key of the data.
     * @return {Object} An object with the stored data or null if not found.
     */
    get: function(type, key) {
        var d = this.storedDate[type + key];
        if (!d) {
            d = this.load(type, key);
            if (d) this.storedDate[type + key] = d;
        }
        return d;
    },
    
    /**
     * This method will be called by {@link #get} if the requested item is not in the store. Subclasses can
     * override this method to for instance retrieve data from a server. The base implementation
     * returns null.
     * @param {String} type The content type.
     * @param {String} key The key of the data.
     * @return {Object} An object with the sought data or null if not found.
     */
    load: function(type, key) {
        return null;
    },
    
    /**
     * Clear the store.
     */
    clear: function() {
        this.storedDate = {};
    }
});