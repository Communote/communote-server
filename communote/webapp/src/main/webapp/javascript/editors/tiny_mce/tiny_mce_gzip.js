var tinyMCE_GZ = {
    settings: {
        themes: '',
        plugins: '',
        languages: '',
        disk_cache: true,
        page_name: 'tinyMCE_gzipped',
		// set base_url to undefined if base URL should be extracted from script tags
        base_url: communote.server.resourceUrlBase + '/javascript/editors/tiny_mce',
        resourceUrlParam: communote.server.resourceUrlParam,
        debug: false,
        suffix: ''
	},

    init: function(s, cb, sc) {
		var t = this, n, i, nl = document.getElementsByTagName('script');

		for (n in s)
			t.settings[n] = s[n];

		s = t.settings;

		if (s.base_url) {
		    t.baseURL = s.base_url;
		} else {
            for (i = 0; i < nl.length; i++) {
    			n = nl[i];

    			if (n.src && n.src.indexOf('tiny_mce') != -1)
    				t.baseURL = n.src.substring(0, n.src.lastIndexOf('/'));
    		}
		}

		if (!t.coreLoaded)
			t.loadScripts(1, s.themes, s.plugins, s.languages, cb, sc);
	},

    loadScripts: function(co, th, pl, la, cb, sc) {
        var t = this, request, w = window, q, c = 0, ti, s = t.settings;

		function get(s) {
            request = 0;

			try {
                request = new ActiveXObject(s);
			} catch (s) {
			}

            return request;
        }
        ;

		// Build query string
        q = 'js=true&diskcache=' + (s.disk_cache ? 'true' : 'false') + '&core='
            + (co ? 'true' : 'false') + '&suffix=' + escape(s.suffix) + '&themes=' + escape(th)
            + '&plugins=' + escape(pl) + '&languages=' + escape(la);

        // build time stamp
        if (s.resourceUrlParam) {
        	q += '&' + s.resourceUrlParam;
        }
		if (co)
			t.coreLoaded = 1;

		// Send request
        request = w.XMLHttpRequest ? new XMLHttpRequest() : get('Msxml2.XMLHTTP')
            || get('Microsoft.XMLHTTP');
        request.overrideMimeType && request.overrideMimeType('text/javascript');
        request.open('GET', t.baseURL + '/' + s.page_name + '?' + q, !!cb);
        // x.setRequestHeader('Content-Type', 'text/javascript');
        request.send('');

		// Handle asyncronous loading
		if (cb) {
			// Wait for response
			ti = w.setInterval(function() {
                if (request.readyState == 4 || c++ > 10000) {
					w.clearInterval(ti);

                    if (c < 10000 && request.status == 200) {
						t.loaded = 1;
                        t.eval(request.responseText);
						tinymce.dom.Event.domLoaded = true;
                        cb.call(sc || t, request);
					}

                    ti = request = null;
				}
			}, 10);
		} else
            t.eval(request.responseText);
	},

    start: function() {
		var t = this, each = tinymce.each, s = t.settings, ln = s.languages.split(',');

		tinymce.suffix = s.suffix;

		function load(u) {
			tinymce.ScriptLoader.markDone(tinyMCE.baseURI.toAbsolute(u));
        }

		// Add core languages
		each(ln, function(c) {
			if (c)
				load('langs/' + c + '.js');
		});

		// Add themes with languages
		each(s.themes.split(','), function(n) {
			if (n) {
				load('themes/' + n + '/editor_template' + s.suffix + '.js');

                each(ln, function(c) {
					if (c)
						load('themes/' + n + '/langs/' + c + '.js');
				});
			}
		});

		// Add plugins with languages
		each(s.plugins.split(','), function(n) {
			if (n) {
				load('plugins/' + n + '/editor_plugin' + s.suffix + '.js');

				each(ln, function(c) {
					if (c)
						load('plugins/' + n + '/langs/' + c + '.js');
				});
			}
		});
	},

    end: function() {
	},

    eval: function(content) {
        var se = document.createElement('script');

        // Create script
        se.type = 'text/javascript';
        se.text = content;

        // Add it to evaluate it and remove it
        (document.getElementsByTagName('head')[0] || document.documentElement).appendChild(se);
        se.parentNode.removeChild(se);
			}
};
