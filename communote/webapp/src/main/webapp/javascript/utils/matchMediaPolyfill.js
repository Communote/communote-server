// matchMedia polyfill for recent browsers (IE >= 9)
if (!window.matchMedia) {
	// IE 10: other name but same API
	if (window.msMatchMedia) {
		matchMedia = window.msMatchMedia;
	} else if (window.styleMedia) {
		// IE 9 and some webkits
		matchMedia = (function() {
			var timeout;
			var mqls = [];
			var overallListenerCount = 0;
			var styleMedia = window.styleMedia;
			function handleResizeDebounced() {
				clearTimeout(timeout);
				timeout = setTimeout(function() {
					var i, mqlsLength, mql, j;
					var changedMqls = [];
					for (i = 0, mqlsLength = mqls.length; i < mqlsLength; i++) {
						mql = mqls[i];
						// check if query matches now
						if (mql.matches != styleMedia.matchMedium(mql.media)) {
						    // update matches flag and collect those with listeners to call them later
						    mql.matches = !mql.matches;
						    if (mql.listeners && mql.listeners.length) {
						        changedMqls.push(mql);
						    }
						}
					}
					// after updating all MQLs invoke the listeners of the changed MQLs
					// note: calling listeners after updating all MQLs for cases where a listener
					// checks other MQLs and expects that the matches field is up-to-date
					for (i = 0, mqlsLength = changedMqls.length; i < mqlsLength; i++) {
					    mql = changedMqls[i];
					    for (j = 0; j < mql.listeners.length; j++) {
                            mql.listeners[j].call(null, mql);
                        }
					}
				}, 30);
			}
			function addListener(callback) {
				if (callback) {
					if (!this.listeners) {
						this.listeners = [];
					}
					this.listeners.push(callback);
					overallListenerCount++;
					if (overallListenerCount == 1) {
						window.addEventListener('resize', handleResizeDebounced, true);
					}
				}
			}
			function removeListener(callback) {
				var i;
				if (overallListenerCount > 0 && this.listeners && callback) {
					i = 0;
					while (this.listeners[i]) {
						if (this.listeners[i] === callback) {
							this.listeners.splice(i, 1);
							overallListenerCount--;
						} else {
							i++;
						}
					}
					if (overallListenerCount == 0) {
						window.removeEventListener('resize', handleResizeDebounced);
					}
				}
			}
			return function(mediaQuery) {
				var i, mqlsLength, mql;
				if (!mediaQuery) {
					mediaQuery = 'all';
				}
				for (i = 0, mqlsLength = mqls.length; i < mqlsLength; i++) {
					if (mqls[i].media === mediaQuery) {
						mql = mqls[i];
						break;
					}
				}
				if (!mql) {
					mql = {};
					mql.media = mediaQuery;
					mql.matches = styleMedia.matchMedium(mediaQuery);
					mql.addListener = addListener;
					mql.removeListener = removeListener;
					mqls.push(mql);
				}
				return mql;
			};
		})();
	} else {
	    document.documentElement.className += ' no-matchMedia'; 
	}
}