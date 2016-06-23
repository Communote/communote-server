/*
mediaboxAdvanced v1.5.4 - The ultimate extension of Slimbox and Mediabox; an all-media script
updated 2011.2.19
	(c) 2007-2011 John Einselen - http://iaian7.com
based on Slimbox v1.64 - The ultimate lightweight Lightbox clone
	(c) 2007-2008 Christophe Beyls - http://www.digitalia.be

description: The ultimate extension of Slimbox and Mediabox; an all-media script

license: MIT-style

authors:
- John Einselen
- Christophe Beyls
- Contributions from many others

requires:
- core/1.3.2: [Core, Array, String, Number, Function, Object, Event, Browser, Class, Class.Extras, Slick.*, Element.*, FX.*, DOMReady, Swiff]
- Quickie/2.1: '*'

provides: [Mediabox.open, Mediabox.close, Mediabox.recenter, Mediabox.scanPage]
*/

var Mediabox;

(function() {
	// Global variables, accessible to Mediabox only
	var isOpen, options, mediaArray, activeMedia, prevMedia, nextMedia, top, left, winWidth, winHeight, fx, preload, preloadPrev = new Image(), preloadNext = new Image(),
	// DOM elements
	overlay, center, media, bottom, closeLink, captionSplit, title, caption, number, prevLink, nextLink, download, gridGallery,
	// Mediabox specific vars
	URL, WH, WHL, elrel, mediaWidth, mediaHeight, mediaType = "none", mediaSplit, mediaId = "mediaBox", centerOffset, bottomOffsetX;

	/*	Initialization	*/

	window.addEvent("domready", function() {
	    var container;
		// Create and append the Mediabox HTML code at the bottom of the document
		document.id(document.body).adopt(
			$$([
				overlay = new Element("div", {id: "mbOverlay"}).addEvent("click", close),
				center = new Element("div", {id: "mbCenter"})
			]).setStyle("display", "none")
		);

		container = new Element("div", {id: "mbContainer"}).inject(center, "inside");
		media = new Element("div", {id: "mbMedia"}).inject(container, "inside");
		media.setStyle('display', 'none');
		bottom = new Element("div", {id: "mbBottom"}).inject(center, "inside").adopt(
			nextLink = new Element("a", {id: "mbNextLink", href: "#"}).addEvent("click", next),
			prevLink = new Element("a", {id: "mbPrevLink", href: "#"}).addEvent("click", previous),
			title = new Element("div", {id: "mbTitle"}),
			number = new Element("div", {id: "mbNumber"}),
			caption = new Element("div", {id: "mbCaption"}),
            actions = new Element("div", {id: "mbActions"})
			);
    	closeLink = new Element("a", {id: "mbCloseLink", href: "#"}).addEvent("click", close);
        closeLink.inject(center, 'inside');
		fx = {
			overlay: new Fx.Tween(overlay, {property: "opacity", duration: 360}).set(0),
			media: new Fx.Tween(media, {property: "opacity", duration: 360, onComplete: captionAnimate}),
			bottom: new Fx.Tween(bottom, {property: "opacity", duration: 240}).set(0)
		};
	});

	/*	API		*/

	Mediabox = {
		close: function(){
			close();	// Thanks to Yosha on the google group for fixing the close function API!
		},

		recenter: function(){	// Thanks to Garo Hussenjian (Xapnet Productions http://www.xapnet.com) for suggesting this addition
			if (center && Browser.platform !== 'ios') {
				left = window.getScrollLeft() + (window.getWidth()/2);
				center.setStyles({'left': left, 'margin-left': -(mediaWidth/2) - centerOffset});
//				top = window.getScrollTop() + (window.getHeight()/2);
//				centerOffset = center.getStyle('padding-left').toInt()+media.getStyle('margin-left').toInt()+media.getStyle('padding-left').toInt();
//				center.setStyles({top: top, left: left, marginTop: -(mediaHeight/2)-centerOffset, marginLeft: -(mediaWidth/2)-centerOffset});
			}
		},

		open: function(_mediaArray, startMedia, _options, triggeringEvent) {
		    var scrollOffset;
		    var wasOpen = isOpen;
		    isOpen = true;
			options = {
//			Text options (translate as needed)
				buttonText: ['&nbsp;','&nbsp;','&nbsp;'],		// Array defines "previous", "next", and "close" button content (HTML code should be written as entity codes or properly escaped)
//				buttonText: ['<big>«</big>','<big>»</big>','<big>×</big>'],
//				buttonText: ['<b>P</b>rev','<b>N</b>ext','<b>C</b>lose'],
				counterText: '({x} of {y})',	// Counter text, {x} = current item number, {y} = total gallery length
				linkText: '<a href="{x}" target="_new">{x}</a><br/>open in a new tab</div>',	// Text shown on iOS devices for non-image links
				flashText: '<b>Error</b><br/>Adobe Flash is either not installed or not up to date, please visit <a href="http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash" title="Get Flash" target="_new">Adobe.com</a> to download the free player.',	// Text shown if Flash is not installed.
//			General overlay options
				center: true,					// Set to false for use with custom CSS layouts
				loop: true,					// Navigate from last to first elements in a gallery
				keyboard: true,					// Enables keyboard control; escape key, left arrow, and right arrow
				keyboardAlpha: false,			// Adds 'x', 'c', 'p', and 'n' when keyboard control is also set to true
				keyboardStop: true,			// Stops all default keyboard actions while overlay is open (such as up/down arrows)
												// Does not apply to iFrame content, does not affect mouse scrolling
				overlayOpacity: 0.6,			// 1 is opaque, 0 is completely transparent (change the color in the CSS file)
				resizeOpening: true,			// Determines if box opens small and grows (true) or starts at larger size (false)
				minWidth: 155,
				resizeDuration: 240,			// Duration of each of the box resize animations (in milliseconds)
				initialWidth: 320,				// Initial width of the box (in pixels)
				initialHeight: 180,				// Initial height of the box (in pixels)
				defaultWidth: 640,				// Default width of the box (in pixels) for undefined media (MP4, FLV, etc.)
				defaultHeight: 360,				// Default height of the box (in pixels) for undefined media (MP4, FLV, etc.)
				showCaption: true,				// Display the title and caption, true / false
				showCounter: true,				// If true, a counter will only be shown if there is more than 1 image to display
				countBack: false,				// Inverts the displayed number (so instead of the first element being labeled 1/10, it's 10/10)
                showGridGallery: true,          // If true, a thumbnail grid will be shown
                showDownload: true,             // If true, a download link will be shown
                pathToDownloadScript: '',       // if above is true, specify path to download script
                repositionIfOpen: window == window.parent, // whether to reposition if already open and open method is called again. Disabled by default when in IFrame, because click event position is used when positioning in IFrame. 
				clickBlock: false,				// Adds an event on right-click to block saving of images from the context menu in most browsers (this can't prevent other ways of downloading, but works as a casual deterent)
				
								// due to less than ideal code ordering, clickBlock on links must be removed manually around line 250
//			iOS device options
//				iOSenable: false,				// When set to false, disables overlay entirely (links open in new tab)
												// IMAGES and INLINE content will display normally,
												// while ALL OTHER content will display a direct link (this is required so as to not break mixed-media galleries)
				iOShtml: true,					// If set to true, HTML content is displayed normally as well (useful if your HTML content is minimal and UI oriented instead of external sites)
//			Image options
				imgBackground: false,		// Embed images as CSS background (true) or <img> tag (false)
											// CSS background is naturally non-clickable, preventing downloads
											// IMG tag allows automatic scaling for smaller screens
											// (all images have no-click code applied, albeit not Opera compatible. To remove, comment lines 212 and 822)
				imgPaddingX: 50,            // separate padding for x and y axis because there is no need to waste space on x which is only required on y axis (mbBottom element with title, navigation controls, ...)
				imgPaddingY: 120,			// Clearance necessary for images larger than the window size (only used when imgBackground is false)
											// Change this number only if the CSS style is significantly divergent from the original, and requires different sizes
//			Inline options
				overflow: 'auto',			// If set, overides CSS settings for inline content only, set to "false" to leave CSS settings intact.
				inlineClone: true,			// Clones the inline element instead of moving it from the page to the overlay
				inlineLimitWidth: true,     // whether to limit the width to the width of the viewport, even if a higher value is specified
				inlinePaddingX: 50,         // padding to apply to the viewport width when inlineLimitWidth is true
//			Global media options
				html5: 'true',				// HTML5 settings for YouTube and Vimeo, false = off, true = on
				scriptaccess: 'true',		// Allow script access to flash files
				fullscreen: 'true',			// Use fullscreen
				fullscreenNum: '1',			// 1 = true
				autoplay: 'true',			// Plays the video as soon as it's opened
				autoplayNum: '1',			// 1 = true
				autoplayYes: 'yes',			// yes = true
				volume: '100',				// 0-100, used for NonverBlaster and Quicktime players
				medialoop: 'true',			// Loop video playback, true / false, used for NonverBlaster and Quicktime players
				bgcolor: '#000000',			// Background color, used for flash and QT media
				wmode: 'transparent',			// Background setting for Adobe Flash ('opaque' and 'transparent' are most common)
//			NonverBlaster
				playerpath: 'files/NonverBlaster.swf',	// Path to NonverBlaster.swf
				showTimecode: 'false',		// turn timecode display off or on (true, false)
				controlColor: '0xFFFFFF',	// set the control color
				controlBackColor: '0x0000000',	// set the bakcground color (video only)
//				playerBackColor: '0x0000FF',	// set the player background color (leave blank to allow CSS styles to show through for audio)
				playerBackColor: '',	// set the player background color (leave blank to allow CSS styles to show through)
				wmodeNB: 'transparent',			// Background setting for Adobe Flash (set to 'transparent' for a blank background, 'opaque' in other situations)
//				autoAdvance: 'false',		// placeholder setting only - not currently implemented (intending to add auto gallery list navigation on play-end)
//			Quicktime options
				controller: 'true',			// Show controller, true / false
//			Flickr options
				flInfo: 'true',				// Show title and info at video start
//			Revver options
				revverID: '187866',			// Revver affiliate ID, required for ad revinue sharing
				revverFullscreen: 'true',	// Fullscreen option
				revverBack: '000000',		// Background color
				revverFront: 'ffffff',		// Foreground color
				revverGrad: '000000',		// Gradation color
//			Ustream options
				usViewers: 'true',			// Show online viewer count (true, false)
//			Youtube options
				ytBorder: '0',				// Outline				(1=true, 0=false)
				ytColor1: '000000',			// Outline color
				ytColor2: '333333',			// Base interface color (highlight colors stay consistent)
				ytRel: '0',					// Show related videos	(1=true, 0=false)
				ytInfo: '1',				// Show video info		(1=true, 0=false)
				ytSearch: '0',				// Show search field	(1=true, 0=false)
//			Viddyou options
				vuPlayer: 'basic',			// Use 'full' or 'basic' players
//			Vimeo options
				vmTitle: '1',				// Show video title
				vmByline: '1',				// Show byline
				vmPortrait: '1',			// Show author portrait
				vmColor: 'ffffff'			// Custom controller colors, hex value minus the # sign, defult is 5ca0b5
			};

			prevLink.set('html', options.buttonText[0]);
			nextLink.set('html', options.buttonText[1]);
			closeLink.set('html', options.buttonText[2]);

			// TODO not a good metric! What about iPad or android?
			if (Browser.platform === 'ios') {
				options.keyboard = false;
				options.resizeOpening = false;	// Speeds up interaction on small devices (mobile) or older computers (IE6)
				overlay.className = 'mbMobile';
				bottom.className = 'mbMobile';
//				options.overlayOpacity = 0.001;	// Helps ameliorate the issues with CSS overlays in iOS, leaving a clickable background, but avoiding the visible issues
				position();
			}

			if (Browser.name === 'ie' && Browser.version === 6) {
				options.resizeOpening = false;	// Speeds up interaction on small devices (mobile) or older computers (IE6)
				overlay.className = 'mbOverlayAbsolute';
				position();
			}

			if (typeof _mediaArray == "string") {	// Used for single mediaArray only, with URL and Title as first two arguments
				_mediaArray = [[_mediaArray,startMedia,_options]];
				startMedia = 0;
			}

			mediaArray = _mediaArray;
			options.loop = options.loop && (mediaArray.length > 1);

			size();
			if (!wasOpen) {
			    setup(true);
			}
			if (!wasOpen || options.repositionIfOpen) {
			    scrollOffset = window.getScroll();
    			if (window.parent != window && triggeringEvent && triggeringEvent.page && triggeringEvent.page.y != undefined) {
    			    // use event position information when in IFrame
    			    top = triggeringEvent.page.y;
    			} else {
    			    top = scrollOffset.y + (winHeight/2);
    			}
    			left = scrollOffset.x + (winWidth/2);
			}
			// calculate offset between center element and media element, assume same values for x and y
			centerOffset = getStylePixelValue(center, 'padding-left');
			centerOffset += getStylePixelValue(center, 'border-left-width');
			centerOffset += getStylePixelValue(media, 'margin-left');

			bottomOffsetX = getStylePixelValue(bottom, 'margin-left');
			bottomOffsetX += getStylePixelValue(bottom, 'padding-left');
			bottomOffsetX += getStylePixelValue(bottom, 'margin-right');
            bottomOffsetX += getStylePixelValue(bottom, 'padding-right');


            center.setStyles({
                top: top, 
                left: left, 
                width: options.initialWidth, 
                height: options.initialHeight, 
                marginTop: -(options.initialHeight/2) - centerOffset, 
                marginLeft: -(options.initialWidth/2) - centerOffset, 
                display: ""
            });
			fx.resize = new Fx.Morph(center, {duration: options.resizeDuration, onComplete: mediaAnimate});
			fx.overlay.start(options.overlayOpacity);
			return changeMedia(startMedia);
		}
	};

	Element.implement({
		mediabox: function(_options, linkMapper) {
			$$(this).mediabox(_options, linkMapper);	// The processing of a single element is similar to the processing of a collection with a single element

			return this;
		}
	});

	Elements.implement({
		/*
			options:	Optional options object, see Mediabox.open()
			linkMapper:	Optional function taking a link DOM element and an index as arguments and returning an array containing 3 elements:
						the image URL and the image caption (may contain HTML)
			linksFilter:Optional function taking a link DOM element and an index as arguments and returning true if the element is part of
						the image collection that will be shown on click, false if not. "this" refers to the element that was clicked.
						This function must always return true when the DOM element argument is "this".
		*/
		mediabox: function(_options, linkMapper, linksFilter) {
			linkMapper = linkMapper || function(el) {
				elrel = el.rel.split(/[\[\]]/);
				elrel = elrel[1];
				return [el.get('href'), el.title, elrel];	// thanks to Dušan Medlín for figuring out the URL bug!
			};

			linksFilter = linksFilter || function() {
				return true;
			};

			var links = this;

/*  clickBlock code - remove the following three lines to enable right-clicking on links to images  */
			/*links.addEvent('contextmenu', function(e){
				if (this.toString().match(/\.gif|\.jpg|\.jpeg|\.png/i)) e.stop();
			});*/

			links.removeEvents("click").addEvent("click", function(event) {
				// Build the list of media that will be displayed
				var filteredArray = links.filter(linksFilter, this);
				var filteredLinks = [];
				var filteredHrefs = [];

				filteredArray.each(function(item, index){
					if(filteredHrefs.indexOf(item.toString()) < 0) {
						filteredLinks.include(filteredArray[index]);
						filteredHrefs.include(filteredArray[index].toString());
					}
				});

				return Mediabox.open(filteredLinks.map(linkMapper), filteredHrefs.indexOf(this.toString()), _options, event);
			});

			return links;
		}
	});
	
	function getStylePixelValue(elem, styleAttribute) {
	    var styleValue = elem.getStyle(styleAttribute).toInt();
        // avoid adding NaN, e.g. when style value is auto
        if (styleValue) {
           return styleValue;
        }
        return 0;
	}

	/*	Internal functions	*/

	function position() {
		overlay.setStyles({top: window.getScrollTop(), left: window.getScrollLeft()});
	}

	function size() {
	    var viewPortsize = window.getSize();
		winWidth = viewPortsize.x;
		winHeight = viewPortsize.y;
		overlay.setStyles({width: winWidth, height: winHeight});
	}

	function setup(open) {
		// Hides on-page objects and embeds while the overlay is open, nessesary to counteract Firefox stupidity
		if (Browser.name === 'firefox') {
			["object", window.ie ? "select" : "embed"].forEach(function(tag) {
				Array.forEach($$(tag), function(el) {
					if (open) el._mediabox = el.style.visibility;
					el.style.visibility = open ? "hidden" : el._mediabox;
				});
			});
		}

		overlay.style.display = open ? "" : "none";

		var fn = open ? "addEvent" : "removeEvent";
		if (Browser.platform === 'ios' || (Browser.name === 'ie' && Browser.version === 6)) window[fn]("scroll", position);	// scroll position is updated only after movement has stopped
		// TODO the resize event should be debounced
		window[fn]("resize", size);
		window[fn]("resize", Mediabox.recenter);
		if (options.keyboard) document[fn]("keydown", keyDown);
	}

	function keyDown(event) {
		if (options.keyboardAlpha) {
			switch(event.code) {
				case 27:	// Esc
				case 88:	// 'x'
				case 67:	// 'c'
					close();
					break;
				case 37:	// Left arrow
				case 80:	// 'p'
					previous();
					break;
				case 39:	// Right arrow
				case 78:	// 'n'
					next();
			}
		} else {
			switch(event.code) {
				case 27:	// Esc
					close();
					break;
				case 37:	// Left arrow
					previous();
					break;
				case 39:	// Right arrow
					next();
			}
		}
		if (options.keyboardStop) { return false; }
	}

	function previous() {
		return changeMedia(prevMedia);
	}

	function next() {
		return changeMedia(nextMedia);
	}

	function changeMedia(mediaIndex) {
	    var URLsplit;
		if (mediaIndex >= 0) {
//			if (Browser.Platform.ios && !options.iOSenable) {
//				window.open(mediaArray[mediaIndex][0], "_blank");
//				close();
//				return false;
//			}
			media.set('html', '');
			activeMedia = mediaIndex;
			prevMedia = ((activeMedia || !options.loop) ? activeMedia : mediaArray.length) - 1;
			nextMedia = activeMedia + 1;
			if (nextMedia == mediaArray.length) nextMedia = options.loop ? 0 : -1;
			stop();
			center.className = "mbLoading";
			if (preload && mediaType == "inline" && !options.inlineClone) preload.adopt(media.getChildren());	// prevents loss of adopted data

	/*	mediaboxAdvanced link formatting and media support	*/

			if (!mediaArray[mediaIndex][2]) mediaArray[mediaIndex][2] = '';	// Thanks to Leo Feyer for offering this fix
			WH = mediaArray[mediaIndex][2].split(' ');
			WHL = WH.length;
			if (WHL>1) {
//				mediaWidth = (WH[WHL-2].match("%")) ? (window.getWidth()*((WH[WHL-2].replace("%", ""))*0.01))+"px" : WH[WHL-2]+"px";
				mediaWidth = (WH[WHL-2].match("%")) ? (window.getWidth()*((WH[WHL-2].replace("%", ""))*0.01)) : WH[WHL-2];
//				mediaHeight = (WH[WHL-1].match("%")) ? (window.getHeight()*((WH[WHL-1].replace("%", ""))*0.01))+"px" : WH[WHL-1]+"px";
				mediaHeight = (WH[WHL-1].match("%")) ? (window.getHeight()*((WH[WHL-1].replace("%", ""))*0.01)) : WH[WHL-1];
			} else {
				mediaWidth = "";
				mediaHeight = "";
			}
			URL = mediaArray[mediaIndex][0];
//			URL = encodeURI(URL).replace("(","%28").replace(")","%29");
//			URL = encodeURI(URL).replace("(","%28").replace(")","%29").replace("%20"," ");
			captionSplit = mediaArray[activeMedia][1].split('::');

// Quietube and yFrog support
			if (URL.match(/quietube\.com/i)) {
				mediaSplit = URL.split('v.php/');
				URL = mediaSplit[1];
			} else if (URL.match(/\/\/yfrog/i)) {
				mediaType = (URL.substring(URL.length-1));
				if (mediaType.match(/b|g|j|p|t/i)) mediaType = 'image';
				if (mediaType == 's') mediaType = 'flash';
				if (mediaType.match(/f|z/i)) mediaType = 'video';
				URL = URL+":iphone";
			}

	/*	Specific Media Types	*/

// GIF, JPG, PNG
			if (URL.match(/\.gif|\.jpg|\.jpeg|\.png|twitpic\.com/i) || mediaType == 'image') {
				mediaType = 'img';
				URL = URL.replace(/twitpic\.com/i, "twitpic.com/show/full");
				preload = new Image();
				preload.onload = startEffect;
				preload.src = URL;
// FLV, MP4
			} else if (URL.match(/\.flv|\.mp4/i) || mediaType == 'video') {
				mediaType = 'obj';
				mediaWidth = mediaWidth || options.defaultWidth;
				mediaHeight = mediaHeight || options.defaultHeight;
				preload = new Swiff(''+options.playerpath+'?mediaURL='+URL+'&allowSmoothing=true&autoPlay='+options.autoplay+'&buffer=6&showTimecode='+options.showTimecode+'&loop='+options.medialoop+'&controlColor='+options.controlColor+'&controlBackColor='+options.controlBackColor+'&playerBackColor='+options.playerBackColor+'&defaultVolume='+options.volume+'&scaleIfFullScreen=true&showScalingButton=true&crop=false', {
					id: 'mbVideo',
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmodeNB, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// MP3, AAC
			} else if (URL.match(/\.mp3|\.aac|tweetmic\.com|tmic\.fm/i) || mediaType == 'audio') {
				mediaType = 'obj';
				mediaWidth = mediaWidth || options.defaultWidth;
				mediaHeight = mediaHeight || "17";
				if (URL.match(/tweetmic\.com|tmic\.fm/i)) {
					URL = URL.split('/');
					URL[4] = URL[4] || URL[3];
					URL = "http://media4.fjarnet.net/tweet/tweetmicapp-"+URL[4]+'.mp3';
				}
				preload = new Swiff(''+options.playerpath+'?mediaURL='+URL+'&allowSmoothing=true&autoPlay='+options.autoplay+'&buffer=6&showTimecode='+options.showTimecode+'&loop='+options.medialoop+'&controlColor='+options.controlColor+'&controlBackColor='+options.controlBackColor+'&defaultVolume='+options.volume+'&scaleIfFullScreen=true&showScalingButton=true&crop=false', {
					id: 'mbAudio',
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// SWF
			} else if (URL.match(/\.swf/i) || mediaType == 'flash') {
				mediaType = 'obj';
				mediaWidth = mediaWidth || options.defaultWidth;
				mediaHeight = mediaHeight || options.defaultHeight;
				preload = new Swiff(URL, {
					id: 'mbFlash',
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// MOV, M4V, M4A, MP4, AIFF, etc.
			} else if (URL.match(/\.mov|\.m4v|\.m4a|\.aiff|\.avi|\.caf|\.dv|\.mid|\.m3u|\.mp3|\.mp2|\.mp4|\.qtz/i) || mediaType == 'qt') {
				mediaType = 'qt';
				mediaWidth = mediaWidth || options.defaultWidth;
//				mediaHeight = (parseInt(mediaHeight, 10)+16)+"px" || options.defaultHeight;
				mediaHeight = (parseInt(mediaHeight, 10)+16) || options.defaultHeight;
				preload = new Quickie(URL, {
					id: 'MediaboxQT',
					width: mediaWidth,
					height: mediaHeight,
					attributes: {controller: options.controller, autoplay: options.autoplay, volume: options.volume, loop: options.medialoop, bgcolor: options.bgcolor}
					});
				startEffect();

	/*	Social Media Sites	*/

// Blip.tv
			} else if (URL.match(/blip\.tv/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "640";
				mediaHeight = mediaHeight || "390";
				preload = new Swiff(URL, {
					src: URL,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Break.com
			} else if (URL.match(/break\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "464";
				mediaHeight = mediaHeight || "376";
				mediaId = URL.match(/\d{6}/g);
				preload = new Swiff('http://embed.break.com/'+mediaId, {
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// DailyMotion
			} else if (URL.match(/dailymotion\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "480";
				mediaHeight = mediaHeight || "381";
				preload = new Swiff(URL, {
					id: mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Facebook
			} else if (URL.match(/facebook\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "320";
				mediaHeight = mediaHeight || "240";
				mediaSplit = URL.split('v=');
				mediaSplit = mediaSplit[1].split('&');
				mediaId = mediaSplit[0];
				preload = new Swiff('http://www.facebook.com/v/'+mediaId, {
					movie: 'http://www.facebook.com/v/'+mediaId,
					classid: 'clsid:D27CDB6E-AE6D-11cf-96B8-444553540000',
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Flickr
			} else if (URL.match(/flickr\.com(?!.+\/show\/)/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "500";
				mediaHeight = mediaHeight || "375";
				mediaSplit = URL.split('/');
				mediaId = mediaSplit[5];
				preload = new Swiff('http://www.flickr.com/apps/video/stewart.swf', {
					id: mediaId,
					classid: 'clsid:D27CDB6E-AE6D-11cf-96B8-444553540000',
					width: mediaWidth,
					height: mediaHeight,
					params: {flashvars: 'photo_id='+mediaId+'&amp;show_info_box='+options.flInfo, wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// GameTrailers Video
			} else if (URL.match(/gametrailers\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "480";
				mediaHeight = mediaHeight || "392";
				mediaId = URL.match(/\d{5}/g);
				preload = new Swiff('http://www.gametrailers.com/remote_wrap.php?mid='+mediaId, {
					id: mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Google Video
			} else if (URL.match(/google\.com\/videoplay/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "400";
				mediaHeight = mediaHeight || "326";
				mediaSplit = URL.split('=');
				mediaId = mediaSplit[1];
				preload = new Swiff('http://video.google.com/googleplayer.swf?docId='+mediaId+'&autoplay='+options.autoplayNum, {
					id: mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Megavideo - Thanks to Robert Jandreu for suggesting this code!
			} else if (URL.match(/megavideo\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "640";
				mediaHeight = mediaHeight || "360";
				mediaSplit = URL.split('=');
				mediaId = mediaSplit[1];
				preload = new Swiff('http://wwwstatic.megavideo.com/mv_player.swf?v='+mediaId, {
					id: mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Metacafe
			} else if (URL.match(/metacafe\.com\/watch/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "400";
				mediaHeight = mediaHeight || "345";
				mediaSplit = URL.split('/');
				mediaId = mediaSplit[4];
				preload = new Swiff('http://www.metacafe.com/fplayer/'+mediaId+'/.swf?playerVars=autoPlay='+options.autoplayYes, {
					id: mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Myspace
			} else if (URL.match(/vids\.myspace\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "425";
				mediaHeight = mediaHeight || "360";
				preload = new Swiff(URL, {
					id: mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Revver
			} else if (URL.match(/revver\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "480";
				mediaHeight = mediaHeight || "392";
				mediaSplit = URL.split('/');
				mediaId = mediaSplit[4];
				preload = new Swiff('http://flash.revver.com/player/1.0/player.swf?mediaId='+mediaId+'&affiliateId='+options.revverID+'&allowFullScreen='+options.revverFullscreen+'&autoStart='+options.autoplay+'&backColor=#'+options.revverBack+'&frontColor=#'+options.revverFront+'&gradColor=#'+options.revverGrad+'&shareUrl=revver', {
					id: mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Rutube
			} else if (URL.match(/rutube\.ru/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "470";
				mediaHeight = mediaHeight || "353";
				mediaSplit = URL.split('=');
				mediaId = mediaSplit[1];
				preload = new Swiff('http://video.rutube.ru/'+mediaId, {
					movie: 'http://video.rutube.ru/'+mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Tudou
			} else if (URL.match(/tudou\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "400";
				mediaHeight = mediaHeight || "340";
				mediaSplit = URL.split('/');
				mediaId = mediaSplit[5];
				preload = new Swiff('http://www.tudou.com/v/'+mediaId, {
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Twitcam
			} else if (URL.match(/twitcam\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "320";
				mediaHeight = mediaHeight || "265";
				mediaSplit = URL.split('/');
				mediaId = mediaSplit[3];
				preload = new Swiff('http://static.livestream.com/chromelessPlayer/wrappers/TwitcamPlayer.swf?hash='+mediaId, {
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Twitvid
			} else if (URL.match(/twitvid\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "600";
				mediaHeight = mediaHeight || "338";
				mediaSplit = URL.split('/');
				mediaId = mediaSplit[3];
				preload = new Swiff('http://www.twitvid.com/player/'+mediaId, {
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Ustream.tv
			} else if (URL.match(/ustream\.tv/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "400";
				mediaHeight = mediaHeight || "326";
				preload = new Swiff(URL+'&amp;viewcount='+options.usViewers+'&amp;autoplay='+options.autoplay, {
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// YouKu
			} else if (URL.match(/youku\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "480";
				mediaHeight = mediaHeight || "400";
				mediaSplit = URL.split('id_');
				mediaId = mediaSplit[1];
				preload = new Swiff('http://player.youku.com/player.php/sid/'+mediaId+'=/v.swf', {
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// YouTube Video (now includes HTML5 option)
			} else if (URL.match(/youtube\.com\/watch/i)) {
				mediaSplit = URL.split('v=');
				if (options.html5) {
					mediaType = 'url';
					mediaWidth = mediaWidth || "640";
					mediaHeight = mediaHeight || "385";
					mediaId = "mediaId_"+new Date().getTime();	// Safari may not update iframe content with a static id.
					preload = new Element('iframe', {
						'src': 'http://www.youtube.com/embed/'+mediaSplit[1],
						'id': mediaId,
						'width': mediaWidth,
						'height': mediaHeight,
						'frameborder': 0
						});
					startEffect();
				} else {
					mediaType = 'obj';
					mediaId = mediaSplit[1];
					mediaWidth = mediaWidth || "480";
					mediaHeight = mediaHeight || "385";
					preload = new Swiff('http://www.youtube.com/v/'+mediaId+'&autoplay='+options.autoplayNum+'&fs='+options.fullscreenNum+'&border='+options.ytBorder+'&color1=0x'+options.ytColor1+'&color2=0x'+options.ytColor2+'&rel='+options.ytRel+'&showinfo='+options.ytInfo+'&showsearch='+options.ytSearch, {
						id: mediaId,
						width: mediaWidth,
						height: mediaHeight,
						params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
						});
					startEffect();
				}
// YouTube Playlist
			} else if (URL.match(/youtube\.com\/view/i)) {
				mediaType = 'obj';
				mediaSplit = URL.split('p=');
				mediaId = mediaSplit[1];
				mediaWidth = mediaWidth || "480";
				mediaHeight = mediaHeight || "385";
				preload = new Swiff('http://www.youtube.com/p/'+mediaId+'&autoplay='+options.autoplayNum+'&fs='+options.fullscreenNum+'&border='+options.ytBorder+'&color1=0x'+options.ytColor1+'&color2=0x'+options.ytColor2+'&rel='+options.ytRel+'&showinfo='+options.ytInfo+'&showsearch='+options.ytSearch, {
					id: mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Veoh
			} else if (URL.match(/veoh\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "410";
				mediaHeight = mediaHeight || "341";
				URL = URL.replace('%3D','/');
				mediaSplit = URL.split('watch/');
				mediaId = mediaSplit[1];
				preload = new Swiff('http://www.veoh.com/static/swf/webplayer/WebPlayer.swf?version=AFrontend.5.5.2.1001&permalinkId='+mediaId+'&player=videodetailsembedded&videoAutoPlay='+options.AutoplayNum+'&id=anonymous', {
					id: mediaId,
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
					});
				startEffect();
// Viddler
			} else if (URL.match(/viddler\.com/i)) {
				mediaType = 'obj';
				mediaWidth = mediaWidth || "437";
				mediaHeight = mediaHeight || "370";
				mediaSplit = URL.split('/');
				mediaId = mediaSplit[4];
				preload = new Swiff(URL, {
					id: 'viddler_'+mediaId,
					movie: URL,
					classid: 'clsid:D27CDB6E-AE6D-11cf-96B8-444553540000',
					width: mediaWidth,
					height: mediaHeight,
					params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen, id: 'viddler_'+mediaId, movie: URL}
					});
				startEffect();
// Vimeo (now includes HTML5 option)
			} else if (URL.match(/vimeo\.com/i)) {
				mediaWidth = mediaWidth || "640";		// site defualt: 400px
				mediaHeight = mediaHeight || "360";		// site defualt: 225px
				mediaSplit = URL.split('/');
				mediaId = mediaSplit[3];

				if (options.html5) {
					mediaType = 'url';
					mediaId = "mediaId_"+new Date().getTime();	// Safari may not update iframe content with a static id.
					preload = new Element('iframe', {
						'src': 'http://player.vimeo.com/video/'+mediaSplit[3]+'?portrait='+options.vmPortrait,
						'id': mediaId,
						'width': mediaWidth,
						'height': mediaHeight,
						'frameborder': 0
						});
					startEffect();
				} else {
					mediaType = 'obj';
					preload = new Swiff('http://www.vimeo.com/moogaloop.swf?clip_id='+mediaId+'&amp;server=www.vimeo.com&amp;fullscreen='+options.fullscreenNum+'&amp;autoplay='+options.autoplayNum+'&amp;show_title='+options.vmTitle+'&amp;show_byline='+options.vmByline+'&amp;show_portrait='+options.vmPortrait+'&amp;color='+options.vmColor, {
						id: mediaId,
						width: mediaWidth,
						height: mediaHeight,
						params: {wmode: options.wmode, bgcolor: options.bgcolor, allowscriptaccess: options.scriptaccess, allowfullscreen: options.fullscreen}
						});
					startEffect();
				}
// INLINE
			} else if (URL.match(/\#mb_/i)) {
				mediaType = 'inline';
				mediaWidth = mediaWidth || options.defaultWidth;
				mediaHeight = mediaHeight || options.defaultHeight;
				URLsplit = URL.split('#');
//				preload = new Element("div", {id: "mbMediaInline"}).adopt(document.id(URLsplit[1]).getChildren().clone([true,true]));
				preload = document.id(URLsplit[1]);
				startEffect();
// HTML (applies to ALL links not recognised as a specific media type)
			} else {
				mediaType = 'url';
				mediaWidth = mediaWidth || options.defaultWidth;
				mediaHeight = mediaHeight || options.defaultHeight;
				mediaId = "mediaId_"+new Date().getTime();	// Safari may not update iframe content with a static id.
				preload = new Element('iframe', {
					'src': URL,
					'id': mediaId,
					'width': mediaWidth,
					'height': mediaHeight,
					'frameborder': 0
					});
				startEffect();
			}
		}
		return false;
	}

	function startEffect() {
	    var mTop, mLeft, scrollOffset, distance, finalMediaHeight, finalMediaWidth, pageBottom;
	    var mediaStyles = {
	            'backgroundImage': 'none', 
	            'display': '', 
	            'text-align': ''
	    };
//		if (Browser.Platform.ios && (mediaType == "obj" || mediaType == "qt" || mediaType == "html")) alert("this isn't gonna work");
//		if (Browser.Platform.ios && (mediaType == "obj" || mediaType == "qt" || mediaType == "html")) mediaType = "ios";
		if (mediaType == "img") {
		    media.addEvent("click", next);
		} else {
		    media.removeEvent("click", next);
		}
		if (mediaType == "img"){
			mediaWidth = preload.width;
			mediaHeight = preload.height;
			if (options.imgBackground) {
				mediaStyles.backgroundImage = "url("+URL+")";
			} else {	// Thanks to Dusan Medlin for fixing large 16x9 image errors in a 4x3 browser
				if (mediaHeight >= winHeight - options.imgPaddingY && (mediaHeight / winHeight) >= (mediaWidth / winWidth)) {
					mediaHeight = winHeight - options.imgPaddingY;
					mediaWidth = preload.width = parseInt((mediaHeight / preload.height) * mediaWidth, 10);
					preload.height = mediaHeight;
				} else if (mediaWidth >= winWidth - options.imgPaddingX && (mediaHeight / winHeight) < (mediaWidth / winWidth)) {
					mediaWidth = winWidth-options.imgPaddingX;
					mediaHeight = preload.height = parseInt((mediaWidth / preload.width) * mediaHeight, 10);
					preload.width = mediaWidth;
				}
				if (Browser.name === 'ie') {
				    preload = document.id(preload);
				}
				if (options.clickBlock) {
				    preload.addEvent('mousedown', function(e){ e.stop(); }).addEvent('contextmenu', function(e){ e.stop(); });
				}
				preload.inject(media);
				mediaStyles['text-align'] = 'center';
			}

		} else if (mediaType == "inline") {
//			if (options.overflow) media.setStyles({overflow: options.overflow});

			if (options.inlineClone){
			    var clone = new Element('div', {html: preload.get('html')});
			    media.grab(clone);
			    Mediabox.scanPage(clone);
			} else {
			    media.adopt(preload.getChildren());
			}
			if (mediaWidth > winWidth - options.inlinePaddingX && options.inlineLimitWidth) {
			    mediaWidth = winWidth - options.inlinePaddingX;
			}
		} else if (mediaType == "qt") {
			preload.inject(media);
//			preload;
		} else if (mediaType == "ios" || Browser.platform === 'ios') {
			media.set('html', options.linkText.replace(/\{x\}/gi, URL));
			mediaWidth = options.DefaultWidth;
			mediaHeight = options.DefaultHeight;
		} else if (mediaType == "url") {
			preload.inject(media);
//			if (Browser.safari) options.resizeOpening = false;	// Prevents occasional blank video display errors in Safari, thanks to Kris Gale for the solution
		} else if (mediaType == "obj") {
			preload.inject(media);
//			if (Browser.safari) options.resizeOpening = false;	// Prevents occasional blank video display errors in Safari, thanks to Kris Gale for the solution
		} else {
			media.set('html', options.flashText);
			mediaWidth = options.defaultWidth;
			mediaHeight = options.defaultHeight;
		}

		title.set('html', (options.showCaption) ? captionSplit[0] : "");
		caption.set('html', (options.showCaption && (captionSplit.length > 1)) ? captionSplit[1] : "");
		number.set('html', (options.showCounter && (mediaArray.length > 1)) ? options.counterText.replace(/\{x\}/, (options.countBack)?mediaArray.length-activeMedia:activeMedia+1).replace(/\{y\}/, mediaArray.length) : "");

        // empty elements if its not an image
        actions.getChildren().destroy();
        if(options.showDownload && mediaType == "img"){
            var downloadEl = new Element('a', {
               href:  options.pathToDownloadScript + URL + '?download=true',
               'class': 'mbDownload',
               html: getJSMessage('mediaboxAdvanced.download.title')
            });
            actions.grab(downloadEl);
        }
        if(options.showGridGallery && mediaType == "img"){
            var noteId = WH[0].split('-'); // see changeImage
            if(noteId.length == 2){
                noteId = noteId[1];
                var gridLink = document.id('gridGalleryLink-' + noteId);
                if(gridLink){
                    var clonedLink = gridLink.clone(true, false);
                    clonedLink = clonedLink.cloneEvents(gridLink);
                    clonedLink.set('html', getJSMessage('mediaboxAdvanced.overview.title'));
                    clonedLink.set('class', 'mbGalleryLink');
                    actions.grab(clonedLink, 'top');
                }
            }
        }
        actions.grab(new Element('span', {
            'class': 'mbClear'
         }));

		if ((prevMedia >= 0) && (mediaArray[prevMedia][0].match(/\.gif|\.jpg|\.jpeg|\.png|twitpic\.com/i))) preloadPrev.src = mediaArray[prevMedia][0].replace(/twitpic\.com/i, "twitpic.com/show/full");
		if ((nextMedia >= 0) && (mediaArray[nextMedia][0].match(/\.gif|\.jpg|\.jpeg|\.png|twitpic\.com/i))) preloadNext.src = mediaArray[nextMedia][0].replace(/twitpic\.com/i, "twitpic.com/show/full");
		if (prevMedia >= 0) prevLink.style.display = "";
		if (nextMedia >= 0) nextLink.style.display = "";
		if (mediaWidth < options.minWidth) {
            mediaWidth = options.minWidth;
        }
		
		finalMediaHeight = finalMediaWidth = -1; 
		scrollOffset = window.getScroll();
		if (mediaWidth > 0) {
		    finalMediaWidth = mediaWidth;
		    // avoid horizontal scrollbars when showing, must be done before updating bottom element
		    distance = left + mediaWidth + 2 * centerOffset - scrollOffset.x - winWidth;
		    // when cycling through images the previous image might have been wider. Because this
		    // width is still set at center element assert that center element is not shifted of screen.
		    if (distance > 0 && distance > center.offsetWidth / 2) {
		        center.setStyle('marginLeft',  -distance);
		    }
		    bottom.setStyles({width: mediaWidth - bottomOffsetX + "px"});
		    if (mediaHeight > 0) {
		        finalMediaHeight = mediaHeight + bottom.offsetHeight;
		        mediaStyles.width = mediaWidth + 'px';
		        mediaStyles.height = mediaHeight + 'px';
		        distance = top + finalMediaHeight + 2 * centerOffset - scrollOffset.y - winHeight;
		        if (distance > 0 && distance > center.offsetHeight / 2) {
	                center.setStyle('marginTop',  -distance);
	            }
		    } else {
		        mediaStyles.width = mediaWidth + 'px';
                mediaStyles.height = '';
		    }
		} else if (mediaHeight > 0) {
		    mediaStyles.width = '';
            mediaStyles.height = mediaHeight + 'px';
		}
		// apply all styles and show media
		media.setStyles(mediaStyles);
		// get missing values from shown media and bottom elements
		if (finalMediaHeight == -1) {
		    finalMediaHeight = media.offsetHeight + bottom.offsetHeight;
		}
		if (finalMediaWidth == -1) {
		    finalMediaWidth = media.offsetWidth;
		}
		// get margins to move the center element so that it is centered aroud top and left
		pageBottom = winHeight + scrollOffset.y;
		mTop = (finalMediaHeight / 2) + centerOffset;
		// avoid scrolling/scrollbars if possible, especially if top is not middle of viewport but was taken from the click event
		if (top + mTop > pageBottom) {
		    // position so that bottom of center elem is placed at bottom of viewport
            mTop += top + mTop - pageBottom;
		    if (top - mTop < 0) {
		        // if too height, position at viewport top
		        mTop = -top;
		    }
		}
		mLeft = (finalMediaWidth / 2) + centerOffset;
		if (mLeft > left) {
		    // position so that left of center element is placed at left edge of viewport
		    mLeft = left; 
		}
		if (options.resizeOpening) {
            fx.resize.start({width: finalMediaWidth, height: finalMediaHeight, marginTop: -mTop, marginLeft: -mLeft});
        } else { 
            center.setStyles({width: finalMediaWidth, height: finalMediaHeight, marginTop: -mTop, marginLeft: -mLeft}); 
            mediaAnimate(); 
        }
	}

	function mediaAnimate() {
		fx.media.start(1);
	}

	function captionAnimate() {
		center.className = "";
//		if (prevMedia >= 0) prevLink.style.display = "";
//		if (nextMedia >= 0) nextLink.style.display = "";
		fx.bottom.start(1);
	}

	function stop() {
		if (preload) {
			if (mediaType == "inline" && !options.inlineClone) preload.adopt(media.getChildren());	// prevents loss of adopted data
			preload.onload = function(){}; // $empty replacement
		}
		fx.resize.cancel();
		fx.media.cancel().set(0);
		fx.bottom.cancel().set(0);
		$$(prevLink, nextLink).setStyle("display", "none");
	}

	function close() {
	    var f;
	    isOpen = false;
		if (activeMedia >= 0) {
			if (mediaType == "inline" && !options.inlineClone) preload.adopt(media.getChildren());	// prevents loss of adopted data
			preload.onload = function(){}; // $empty replacement
			media.getChildren().destroy();
			for (f in fx) fx[f].cancel();
			// reset styles
			center.setStyles({display: 'none', width: '', height: '', 'margin-top': '', 'margin-left': ''});
			media.setStyles({width: '', height: ''});
			bottom.setStyles({width: ''});
			// cleanup
			media.removeEvent("click", next);
			actions.getChildren().destroy();
			fx.overlay.chain(setup).start(0);
		}
		return false;
	}
})();

	/*	Autoload code block	*/

Mediabox.scanPage = function(startNode) {
    var links;
//	if (Browser.Platform.ios && !(navigator.userAgent.match(/iPad/i))) return;	// this quits the process if the visitor is using a non-iPad iOS device (iPhone or iPod Touch)
    if (startNode) {
        startNode = document.id(startNode);
    } else {
        startNode = document;
    }
    links = startNode.getElements('a');
    links = links.filter(function(el) {
		return el.rel && el.rel.test(/^lightbox/i);
	});

	links.mediabox({/* Put custom options here */}, null, function(el) {
		var rel0 = this.rel.replace(/[\[\]|]/gi," ");
		var relsize = rel0.split(" ");
//		return (this == el) || ((this.rel.length > 8) && el.rel.match(relsize[1]));

		var relsearch = "\\["+relsize[1]+"[ \\]]";
		var relregexp = new RegExp(relsearch);
		return (this == el) || ((this.rel.length > 8) && el.rel.match(relregexp));
	});
};
