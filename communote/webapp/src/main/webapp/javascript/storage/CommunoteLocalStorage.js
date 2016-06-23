var CommunoteLocalStorage = new Class({

    localStorage: undefined,

    /**
     * Initializer, which checks if localStorage is available and creates a dummy storage if not.
     */
    initialize: function() {
        if (this.isLocalStorageAvailable()) {
            this.localStorage = localStorage;
        } else {
            // Pseudo LocalStorage for ancient browsers and private browsing mode to avoid errors.
            // Not fully functional.
            this.localStorage = {
                internalStore: new Hash(),
                length: 0,
                setItem: function(key, value) {
                    this.internalStore.set(key, value);
                    this.length = this.internalStore.getLength();
                },
                getItem: function(key) {
                    return this.internalStore.get(key);
                },
                removeItem: function(key) {
                    this.internalStore.erase(key);
                    this.length = this.internalStore.getLength();
                },
                clear: function() {
                    this.internalStore.empty();
                    this.length = 0;
                },
                key: function(index) {
                    return this.internalStore.getValues()[index];
                }
            };
        }
    },
    /**
     * Method to check if a native localStorage is available and accessible.
     * 
     * @returns {Boolean} True, if it is possible to store objects in a native localStorage.
     */
    isLocalStorageAvailable: function() {
        return typeof (Storage) !== 'undefined' && function() {
            try {
                localStorage.setItem('hello-store-test', 'communote');
                localStorage.removeItem('hello-store-test');
            } catch (err) {
                return false;
            }
            return true;
        }();
    },

    /**
     * Set the value for the given key.
     * 
     * @param key The key to set.
     * @param value The value for the key.
     */
    setItem: function(key, value) {
        this.localStorage.setItem(key, value);
    },

    /**
     * Returns the value for the given key.
     * 
     * @param key The key.
     * @returns The value or null/undefined if none.
     */
    getItem: function(key) {
        return this.localStorage.getItem(key);
    },

    /**
     * Removes the item for the given key from the store.
     * 
     * @param key The key.
     */
    removeItem: function(key) {
        this.localStorage.removeItem(key);
    },
    /**
     * Removes all items from the store.
     */
    clear: function() {
        this.localStorage.clear();
    },
    length: function() {
        return this.localStorage.length();
    }
});
