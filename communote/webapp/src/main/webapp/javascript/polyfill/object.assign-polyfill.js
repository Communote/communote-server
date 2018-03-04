// Object.assign polyfill for older browsers (e.g. IE11)
// simplified version (without support for symbols) of https://github.com/ljharb/object.assign/blob/master/implementation.js
if (typeof Object.assign !== 'function') {
    (function() {
        var canBeObject = function (obj) {
            return typeof obj !== 'undefined' && obj !== null;
        };
        var toObject = Object;
        Object.assign = function(target, source1) {
            var objTarget, s, source, i, props, value, key;
            if (!canBeObject(target)) { 
                throw new TypeError('target must be an object');
            }
            objTarget = toObject(target);
            for (s = 1; s < arguments.length; ++s) {
                source = toObject(arguments[s]);
                props = Object.keys(source);
                for (i = 0; i < props.length; ++i) {
                    key = props[i];
                    value = source[key];
                    if (source.propertyIsEnumerable(key)) {
                        objTarget[key] = value;
                    }
                }
            }
            return objTarget;
        };
    })();
}