/**
 * FloatingToolbar.js
 *
 * https://www.communote.com
 */

(function(tinymce) {
// Shorten class names
var dom = tinymce.DOM;
/**
 * This class is used to create toolbars a toolbar is a container for other controls like buttons etc.
 *
 * @class tinymce.ui.FloatingToolbar
 * @extends tinymce.ui.Container
 */
tinymce.create('tinymce.ui.FloatingToolbar:tinymce.ui.Container', {
    /**
     * Renders the toolbar as a HTML string. This method is much faster than using the DOM and when
     * creating a whole toolbar with buttons it does make a lot of difference.
     *
     * @method renderHTML
     * @return {String} HTML for the toolbar control.
     */
    renderHTML : function() {
        var html = '', controls, curControl, cssClasses, settings = this.settings, i, l, separatorHTML;

        controls = this.controls;
        for (i = 0, l = controls.length; i < l; i++) {
            cssClasses = 'mceToolbarItem';
            
            // mark first
            if (i === 0) {
                cssClasses += ' mceFirst';
            } else if (i === l - 1) {
                cssClasses += ' mceLast';
            }
            
            curControl = controls[i];
            if (curControl.Separator) {
                // HTML of separator is not compatible with IE8 (closing tag is missing)
                if (!separatorHTML) {
                    separatorHTML = curControl.renderHTML().slice(0, -2) + '></span>';
                }
                html += '<div class="' + cssClasses + '">' +  separatorHTML + '</div>';
            } else {
                // Render control HTML
                html += '<div class="' + cssClasses + '">' +  controls[i].renderHTML() + '</div>';
            }
        }

        cssClasses = 'mceFloatingToolbar';
        if (settings['class']) {
            cssClasses += ' ' + settings['class'];
        }

        html = '<div id="' + this.id + '" class="' + cssClasses + '" role="presentation" tabindex="-1">' + html + '</div>' 
        return html;
    }
});
})(tinymce);