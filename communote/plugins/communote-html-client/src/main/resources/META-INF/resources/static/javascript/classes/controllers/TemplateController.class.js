/**
 * @class
 * @augments communote.widget.classes.controllers.Controller 
 */
communote.widget.classes.controllers.TemplateController = communote.widget.classes.controllers.Controller.extend(
/** 
 * @lends communote.widget.classes.controllers.TemplateController.prototype
 */	
{
    name: "TemplateController",

    /**
     * initializing
     * 
     * @param config
     */
    constructor: function(config) {
        this.store = {};
    },

    /**
     * retrieve the template
     * 
     * @param key string
     * @return
     */
    getTemplate: function(key, control) {
        var template;
        key = key.toLowerCase();
        if (control) {
            if (this.store[key] === undefined) {
                this.loadAll(control.widget.configuration);
            }
            if (this.store[key] === undefined) {
                this.load(key, control);
            }       
            template = this.store[key];
        }
        return template;
    },

    /**
     * load all templates and cache them
     * 
     */
    loadAll: function(config) {
        var url, response;
        var protocolPrefix = 'http';  
        var templateNameContentSeparator = "|||";   
        if (config.templateFile.substr(0, protocolPrefix.length) === protocolPrefix){
            url = config.templateFile;
        }
        else{
            url = config.baseHost +  config.cntPath + config.templateFile;
        } 
        communote.jQuery.ajax({
            async: false,
            type: "get",
            url: url,
            cache: true,
            data: "",
            dataType: "text",
            success: function(data, textStatus) {
               response = data;
            }
        }); 
        var lines = response.split("\n");
        for (var i = 0; i < lines.length; i++){ 
            var template = lines[i].split(templateNameContentSeparator);
            this.store[template[0]] = template[1]; 
        } 
              
    },

    /**
     * load the template and cache it
     * 
     * @param key string - the filename of the template without extension (not case-sensitive) the
     *            cache keyname ist case-sensitive
     */
    load: function(key, control) {
        var template = "";
        var config = control.widget.configuration;
        var url = config.baseHost + config.cntPath + config.filesPath + 'templates/' + key.toLowerCase()
                + ".tmpl.html";
        jQuery.ajax({
            async: false,
            type: "get",
            url: url,
            cache: true,
            data: "",
            dataType: "text",
            success: function(data, textStatus) {
                template = data;
            },
            error: function(data, textStatus) {
            }
        });
        if (template === "") {
            template = "Template for key '" + key + "' does not exist!";
        }
        this.store[key] = template;
    }
});
