/**
 * ControlFactory
 * this class still contains a lot of function that are unnecessary right now in the past models of
 * controls have to be registered .. right now this is not the case the only requirement is, that
 * the 'type' in the config should lead to a valid class
 * @class
 * @augments communote.widget.classes.controllers.Controller
 */
communote.widget.classes.controllers.ControlFactory = communote.widget.classes.controllers.Controller.extend(
/** 
 * @lends communote.widget.classes.controllers.ControlFactory.prototype
 */
{
    name: "ControlFactory",

    constructor: function() {
        this.id = 0;
    },

    /**
     * 
     * @param
     * @param
     * @return
     */
    getControl: function(config, widget) {
        var id, control;
        var impl = communote.widget.classes.controls[config.type];
        if (impl !== undefined) {
            id = this.createId();
            control = undefined;
            try {
                control = new impl(id, config, widget);
            } catch (e) {
                if (!e instanceof communote.widget.classes.ControlDisabledException) {
                    throw e;
                }
            }
            return control;
        }
        return undefined;
    },

    createId: function() {
        var id = this.id++;
        return id;
    }
});
