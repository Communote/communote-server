/**
 * Rewrite A tags to open in a new window if inside an IFrame.
 */
(function(window) {
    if (window != window.parent && (!window.communote.environment
            || window.communote.environment.page !== 'embed')) {
        window.addEvent('load', function() {
            var linkElems, i, l, formElem, submitButton;
            if (window.communote.configuration
                    && window.communote.configuration.openLinksInNewWindow) {
                linkElems = document.getElementsByTagName('a');
                for (i = 0, l = linkElems.length; i < l; i++) {
                    if (linkElems[i].href && linkElems[i].href.indexOf('javascript:') != 0) {
                        linkElems[i].target = '_blank';
                    }
                }
                // if on login page fix registration form to open in new page
                if (window.communote.environment.page === 'login') {
                    formElem = document.getElementById('user_registration_form');
                    if (formElem) {
                        formElem.target = '_blank';
                        // also remove the click handler of the submit button
                        submitButton = document.id(formElem).getElement('input[type=submit]');
                        submitButton.onclick = null;
                    }
                    
                }
            }
        });
    }
})(this);