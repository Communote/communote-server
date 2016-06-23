/**
 * @class
 * DateFormatter
 */

communote.utils.DateFormatter = 
/** @lends communote.utils.DateFormatter.prototype */	
{
    date: new Date(),
    options: {
        // days of the week starting at Sunday
        days: [ 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday' ],
        months: [ 'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August',
                'September', 'October', 'November', 'December' ],
        // first day of the week: 0 = sunday, 1 = monday, etc.
        offset: 0,
        timeSuffixes: [ 'AM', 'PM' ],
        // default formatting pattern
        pattern: 'l, F j, Y h:m:s t'
    },

    /**
     * @method setOption
     * @param {object} options
     */
    // TODO doesn't support patternDate and patternTime (see also i18ncontroller)
    setOptions: function(options) {
        if (options.days && options.days.length) {
            this.options.days = options.days;
        }
        if (options.months && options.months.length) {
            this.options.months = options.months;
        }
        if (options.offset && options.offset.length) {
            this.options.offset = options.offset;
        }
        if (options.timeSuffixes && options.timeSuffixes.length) {
            this.options.timeSuffixes = options.timeSuffixes;
        }
        if (options.pattern && options.pattern.length) {
            this.options.pattern = options.pattern;
        }
    },

    /**
     * @method setTime
     * set the time
     * @param {number} datetime - time in ms since 1.1.1970
     */
    setTime: function(datetime) {
        if (datetime) {
            this.date.setTime(parseInt(datetime));
        }
    },

    // TODO this method is misleading because it will return date and time if the format contains it
   /**
     * @method getDate
     * return only the date
     * @param {numeric} datetime - unix timestamp
     * @param {string} format - optional
     * @return {string}
     */
    getDate: function(datetime, format) {
        return this.format(datetime, format || this.options.patternDate);
    },

    // TODO this method is misleading because it will return date and time if the format contains it
    /**
     * @method getTime
     * return only the time
     * @param {numeric} datetime - unix timestamp
     * @param {string} format - optional
     * @return {string}
     */
    getTime: function(datetime, format) {
        return this.format(datetime, format || this.options.patternTime);
    },

    /**
     * @method getDateTime
     * return the full date and time
     * @param {numeric} datetime - unix timestamp
     * @param {string} format - optional
     * @return {string}
     */
    getDateTime: function(datetime, format) {
        return this.format(datetime, format || this.options.pattern);
    },

    /**
     * @method format
     * format the given datetime; if undefined the options.pattern is used
     * @param {strin} format - formatstring
     *                         year:       y   xx ... xx
     *                                     Y   19xx ... 20xx
     *                         month:      N   01 ... 12
     *                                     n   1 ... 12
     *                                     M   Jan ... Dec
     *                                     F   January ... December
     *                         day:        d   01 ... 31
     *                                     j   1 ... 31
     *                                     D   Sun ... Sat
     *                                     l   Sunday ... Saturday
     *                         weekday:    W   1 ... 7
     *                                     w   0 ... 6
     *                         hour:       h   0 ... 11
     *                                     H   00 ... 23
     *                         minute:     m   00 ... 59
     *                         second:     s   00 ... 59
     *                         extension for english numerics (j,n,W):
     *                                     S   st/nd/rd/th
     *                         timesuffix: t
     *
     *                         example:
     *                           d.N.Y H:m         01.01.1970 13:30
     *                           n/d/Y h:m t       5/29/1970 1:00 PM
     *                           l jS F Y, H:m t   Friday 29th May 1970, 13:00 PM
     * * @param {number} datetime - time in ms since 1.1.1970
     */
    format: function(datetime, format) {
        var str = '';
        // TODO why isn't the time just a local variable to this method
        // set the time
        this.setTime(datetime);

        if (format == null) {
            format = this.options.pattern;
        }

        if (this.date.getTime()) {

            // 1 - 31
            var j = this.date.getUTCDate();
            // 0 - 6
            var w = this.date.getUTCDay();              
            // Sunday - Saturday
            var l = this.options.days[w];               
            // 1 - 12
            var n = this.date.getUTCMonth() + 1;        
            // January - December
            var f = this.options.months[n - 1];         
            // 19xx - 20xx
            var y = this.date.getUTCFullYear() + '';    
            var h = this.date.getUTCHours();
            var m = this.date.getUTCMinutes();
            var s = this.date.getUTCSeconds();
            var t = '';

            if (this.options.timeSuffixes && (this.options.timeSuffixes.length > 0)) {
                var steps = Math.floor(24 / this.options.timeSuffixes.length);
                t = this.options.timeSuffixes[Math.floor(h / steps)];
            }

            for ( var i = 0, len = format.length; i < len; i++) {
                var cha = format.charAt(i); // format char

                switch (cha) {
                    // year cases
                    // xx - xx
                    case 'y':
                        y = y.substr(2);
                    // 19xx - 20xx
                    case 'Y':
                        str += y;
                        break;

                    // month cases
                    // 01 - 12
                    case 'N':
                        if (n < 10) {
                            str += '0';
                        }
                    // 1 - 12
                    case 'n':
                        str += n;
                        break;
                    // Jan - Dec
                    case 'M':
                        f = f.substr(0, 3);
                    // January - December
                    case 'F': 
                        str += f;
                        break;

                    // day cases
                    // 01 - 31
                    case 'd':
                        if (j < 10) {
                            str += '0';
                        }
                    // 1 - 31
                    case 'j':
                        str += j;
                        break;
                    // Sun - Sat
                    case 'D':
                        l = l.substr(0, 3);
                    // Sunday - Saturday
                    case 'l':
                        str += l;
                        break;
                    // 1 - 7
                    case 'W':
                        w += 1;
                    // 0 - 6
                    case 'w':
                        str += w;
                        break;
                    // hour cases
                    case 'H':
                        if (h < 10) {
                            str += '0';
                        }
                        str += h;
                        break;
                    case 'h':
                        if (h > 12) {
                            h -= 12;
                        }
                        str += h;
                        break;
                    // minute cases
                    case 'm':
                        if (m < 10) {
                            str += '0';
                        }
                        str += m;
                        break;
                    // seconds
                    case 's':
                        if (s < 10) {
                            str += '0';
                        }
                        str += s;
                        break;
                    // time suffix
                    case 't':
                        str += t;
                        break;
                    // st, nd, rd or th (works only with j,n,W)
                    case 'S':
                        if (i) {
                            var lastChar = format.charAt(i - 1);
                            if (lastChar === "j" || lastChar === "n" || lastChar === "W") {
                                if ((j % 10 == 1) && (j != '11')) {
                                    str += 'st';
                                } else if ((j % 10 == 2) && (j != '12')) {
                                    str += 'nd';
                                } else if ((j % 10 == 3) && (j != '13')) {
                                    str += 'rd';
                                } else {
                                    str += 'th';
                                }
                            }
                        }
                        break;
                    default:
                        str += cha;
                }
            }
        }
        // return format with values replaced
        return str;
    }
};
