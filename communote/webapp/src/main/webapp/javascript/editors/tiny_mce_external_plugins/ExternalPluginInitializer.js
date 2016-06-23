(function() {
    tinymceExternalPluginInitializer = {
        delayedPlugins: null,
        delayedRegistrationTimeout: null,

        add: function(name, shortName, plugin) {
            var self;
            if (window.tinymce) {
                this.createAndRegister(name, shortName, plugin);
            } else {
                if (!this.delayedPlugins) {
                    this.delayedPlugins = [];
                    self = this;
                    this.delayedRegistrationTimeout = setInterval(function(){
                        self.createAndRegisterDelayed();
                    }, 100);
                }
                this.delayedPlugins.push({
                    name: name,
                    shortName: shortName,
                    plugin: plugin
                });
            }
        },

        createAndRegisterDelayed: function() {
            var i, pluginDescr;
            if (window.tinymce && tinymce.PluginManager) {
                clearInterval(this.delayedRegistrationTimeout);
                for (i = 0; i < this.delayedPlugins.length; i++) {
                    pluginDescr = this.delayedPlugins[i];
                    this.createAndRegister(pluginDescr.name, pluginDescr.shortName, pluginDescr.plugin);
                }
                delete this.delayedPlugins;
            }
        },

        createAndRegister: function(name, shortName, plugin) {
            var scopedObject, i, splittedName;
            tinymce.create(name, plugin);
            splittedName = name.split('.');
            scopedObject = window;
            for (i = 0; i < splittedName.length; i++) {
                scopedObject = scopedObject[splittedName[i]];
            }
            tinymce.PluginManager.add(shortName, scopedObject);
        }
    };
})();