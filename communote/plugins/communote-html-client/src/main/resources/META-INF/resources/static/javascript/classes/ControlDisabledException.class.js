(function(namespace) {
    /**
     * Exception to be thrown by a control constructor if the control is disabled by configuration
     * settings.
     * 
     * @param {String} controlName The name of the control
     * @param {String} [message] An additional message
     * @exception
     */
    namespace.ControlDisabledException = function(controlName, message) {
        this.name = 'ControlDisabledException';
        this.controlName = controlName || 'unknown';
        this.message = message || 'Control ' + this.controlName + ' is disabled by configuration';
    };
    namespace.ControlDisabledException.prototype = new Error();
    namespace.ControlDisabledException.prototype.constructor = namespace.ControlDisabledException;
})(communote.widget.classes);