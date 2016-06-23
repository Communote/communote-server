/**
 * Controller Class it basically does nothing more than provieds the basic init cycle for every
 * control write itself to the communote namespace and 
 * @class
 * @name communote.widget.classes.controllers.Controller
 */
communote.widget.classes.controllers.Controller = communote.Base.extend(
/**
 * @lends communote.widget.classes.controllers.Controller.prototype
 */	
{
    init: function(initializer) {
        // TODO do we really need this lazy initialization?
        communote.widget[this.name] = this;
        initializer.controllerReady(this);
    }
});
