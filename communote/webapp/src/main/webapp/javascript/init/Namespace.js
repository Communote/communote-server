/**
 * Expose methods for adding classes to namespace and also global scope for migrating too namespaced
 * environment. Should be included directly after the Mootools scripts.
 */
(function(window) {
    var namespace = window.communote;
    if (!namespace.addConstructor) {
        namespace.addConstructor = function(name, constructorFunction) {
            namespace.classes[name] = constructorFunction;
            // TODO remove this backwards compatibility code after cleaning global scope
            window[name] = constructorFunction;
        }
    }
    if (!namespace.getConstructor) {
        namespace.getConstructor = function(name, searchGlobal) {
            var constructor = namespace.classes[name];
            // TODO change default of searchGlobal to false when everything is namespaced
            if (!constructor && searchGlobal !== false) {
                constructor = window[name];
            }
            return constructor;
        }
    }
    // export namespace
    runtimeNamespace = namespace;
})(this);