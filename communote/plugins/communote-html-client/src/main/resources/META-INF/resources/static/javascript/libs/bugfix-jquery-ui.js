// jQuery UI bugfing
//  fix for KENMEI-3976
(function( $, undefined ) {
    $.fn.extend({
        
        zIndex: function( zIndex ) {
            if ( zIndex !== undefined ) {
                return this.css( "zIndex", zIndex );
            }
            
            if ( this.length ) {
                var elem = $( this[ 0 ] ), position, value;
                
                // START BUGFIX
                // bugfix to allow autocompletion in iframes
                // orig: while ( elem.length && elem[ 0 ] !== document ) {
                //
                while ( elem.length && elem[0].nodeType != 9 /*Node.DOCUMENT_NODE*/ ) {
                //
                // END BUGFIX
                    
                    // Ignore z-index if position is set to a value where z-index is ignored by the browser
                    // This makes behavior of this function consistent across browsers
                    // WebKit always returns auto if the element is positioned
                    position = elem.css( "position" );
                    if ( position === "absolute" || position === "relative" || position === "fixed" ) {
                        // IE returns 0 when zIndex is not specified
                        // other browsers return a string
                        // we ignore the case of nested elements with an explicit value of 0
                        // <div style="z-index: -10;"><div style="z-index: 0;"></div></div>
                        value = parseInt( elem.css( "zIndex" ), 10 );
                        if ( !isNaN( value ) && value !== 0 ) {
                            return value;
                        }
                    }
                    elem = elem.parent();
                }
            }
            
            return 0;
        }
    });
}(jQuery));
