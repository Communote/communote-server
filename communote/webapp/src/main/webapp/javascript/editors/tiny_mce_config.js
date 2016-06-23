// some utility funtions for tinyMCE and the init config

// hook to override some tinyMCE events
function handleTinyMCEEvent(e) {
	if (e.type == "keydown") {
		if (e.ctrlKey) { 
			var ctrl = true;
		}
		if (e.keyCode == 86) {
			var v = true;
		}
		if (ctrl && v) {
			return tinymce.dom.Event.cancel(e);
		}
	}
	// continue with default event handling
	return true;
}

var isTinyMCEInitialized = false;
/**
 * initializes the TinyMCE editor.
 * Configuration notes:
 *      - no content CSS because the CSS for the content is part of the skin
 *      - no height definition because we cannot provide a value less than 100px, thus
 *        we set height with a setup hook
 */
function initializeTinyMce() {
    if (!isTinyMCEInitialized && window.tinyMCE != null) {
        isTinyMCEInitialized = true;
        tinyMCE.init({
        	mode: 'none',
        	plugins: 'paste,fullscreen,-suggestions,-changesettings',
            apply_source_formatting: false,
            force_p_newlines: true,
            inline_styles: false, // to avoid spans with style attributes for u tag when sending
            formats: {
        		underline: {inline: 'u'},
        		bold: {inline: 'b'},
        		italic: {inline: 'i'}
        	},
        	
            paste_preprocess: function(pl, o) {
                // remove links with no or javascript href (insensitive for IE)
                o.content = o.content.replace(/<a[^>]*href=['"]javascript:;[^>]*>(.+?)<\/a>/gi, '$1');
                o.content = o.content.replace(/<a[^>]*href=['"]['"][^>]*>(.+?)<\/a>/gi, '$1');
                o.content = o.content.replace(/<a[^>]*class=['"]control-atlr-link[^>]*>(.+?)<\/a>/gi, '$1');
            },
            valid_elements: "+span[data-mce-type|class],@[id],a[!href|target=_blank|title],b/strong,i/em,u,br,-ul,-ol,-li,-div,p,-blockquote",
            //invalid_elements: "a[title]", not working
        	//handle_event_callback : 'handleTinyMCEEvent',
            // using custom communote theme
        	theme: 'communote',
        	// theme does not provide CSS for skins, so add them manually. UI-CSS is part of portal CSS category.
        	editor_css: false,
        	content_css: communote.environment.tinymceContentCssUrl,
        	skin: 'communote',
            // communote theme uses the theme_advanced options
        	theme_advanced_buttons1: 'bold,italic,underline,separator,bullist,numlist,outdent,indent,separator,link,unlink,separator,undo,redo,separator,fullscreen,separator,changesettings',
        	theme_advanced_toolbar_location: 'top',
        	theme_advanced_toolbar_align: 'center',
        	theme_advanced_statusbar_location: 'bottom',
        	theme_advanced_resizing: true,
        	theme_advanced_resize_horizontal: false,
        	theme_advanced_path: false,
        	theme_advanced_resizing_min_height: 70,
        	// force auto height so no strange defaults are applied to the wrapping container
        	height: 'auto',
        	fullscreen_settings: {
        	    // disable horizontal resizing when in fullscreen, since this only works with table based layout
        	    theme_advanced_resize_horizontal: false
        	},
        	fullscreen_zIndex: 90,
        	// CSS for plugin is provided with categories for easier modification with communote plugins
        	suggestions_options: {
        	    includeCss: false
        	},
        	changesettings_options: {
        	    buttonCssClass: 'mce_changesettings'
        	},
        	gecko_spellcheck : true,
        	submit_patch: false,
        	entity_encoding: 'raw',
            relative_urls: false,
            setup: function(editor) {
                // add hook to resize the editor to the min size, if it is not the fullscreen editor
                // instance or one with enabled autoresize. Using onPostRender here because it is
                // triggered earlier than onInit.
                if (editor.id !== 'mce_fullscreen' && editor.settings.plugins.indexOf('autoresize') === -1) {
                    editor.onPostRender.add(function(ed) {
                        // do nothing if there is a cookie with stored width and height
                        if (ed.theme.settings.theme_advanced_resizing_use_cookie) {
                            if (tinymce.util.Cookie.getHash('TinyMCE_' + ed.id + '_size')) {
                                return;
                            }
                        }
                        // width is not important because horizontal resizing is disabled
                        // height is 0 because we want the theme_advanced_resizing_min_height to be used
                        ed.theme.resizeTo(-1, 0);
                    });
                }
            }
        });
    }
}
// define base URL of tinyMCE resources otherwise tinyMCE would try to extract it from script tags
// which doesn't work when delivering this script as part of an aggregated resource 
tinyMCEPreInit = {
		base: communote.server.resourceUrlBase + '/javascript/editors/tiny_mce',
		query: communote.server.resourceUrlParam // pass build-timestamp to avoid cache effects
};