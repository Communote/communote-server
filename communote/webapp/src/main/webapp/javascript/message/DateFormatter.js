// a date time formatter using Mootools
// formating method based on Mootools calendar <http://electricprism.com/aeron/calendar>
var C_DateFormatter = new Class({
    Implements: Options,
    options: {
        // days of the week starting at Sunday
        days: [ 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday' ],
        months: [ 'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August',
            'September', 'October', 'November', 'December' ],
        offset: 0, // first day of the week: 0 = sunday, 1 = monday, etc..
        timeSuffixes: [ 'AM', 'PM' ],
        pattern: 'l, F j, Y h:m:s t' // default formatting pattern
    },

    initialize: function(options) {
        this.setOptions(options);
    },

    // format: formats a date object according to passed in instructions
    // @param date (obj) the date to format
    // @param format (string) any combination of punctuation / separators and d, j, D, l, S, N, n,
    // F, M, y, Y, H, h, m, s, t
    // if undefined the options.pattern is used
    // @returns (string) the formated date

    format: function(date, format) {
        var str = '';
        if (format == null) {
            format = this.options.pattern;
        }
        if (date) {
            var j = date.getDate(); // 1 - 31
            var w = date.getDay(); // 0 - 6
            var l = this.options.days[w]; // Sunday - Saturday
            var n = date.getMonth() + 1; // 1 - 12
            var f = this.options.months[n - 1]; // January - December
            var y = date.getFullYear() + ''; // 19xx - 20xx
            var h = date.getHours();
            var m = date.getMinutes();
            var s = date.getSeconds();
            var t = '';
            if (this.options.timeSuffixes && this.options.timeSuffixes.length > 0) {
                var steps = Math.floor(24 / this.options.timeSuffixes.length);
                t = this.options.timeSuffixes[Math.floor(h / steps)];
            }

            for ( var i = 0, len = format.length; i < len; i++) {
                var cha = format.charAt(i); // format char

                switch (cha) {
                    // year cases
                    case 'y': // xx - xx
                        y = y.substr(2);
                    case 'Y': // 19xx - 20xx
                        str += y;
                        break;

                    // month cases
                    case 'N': // 01 - 12
                        if (n < 10) {
                            n = '0' + n;
                        }
                    case 'n': // 1 - 12
                        str += n;
                        break;

                    case 'M': // Jan - Dec
                        f = f.substr(0, 3);
                    case 'F': // January - December
                        str += f;
                        break;

                    // day cases
                    case 'd': // 01 - 31
                        if (j < 10) {
                            j = '0' + j;
                        }
                    case 'j': // 1 - 31
                        str += j;
                        break;

                    case 'D': // Sun - Sat
                        l = l.substr(0, 3);
                    case 'l': // Sunday - Saturday
                        str += l;
                        break;

                    case 'W': // 1 - 7
                        w += 1;
                    case 'w': // 0 - 6
                        str += w;
                        break;

                    case 'S': // st, nd, rd or th (works well with j)
                        if (j % 10 == 1 && j != '11') {
                            str += 'st';
                        } else if (j % 10 == 2 && j != '12') {
                            str += 'nd';
                        } else if (j % 10 == 3 && j != '13') {
                            str += 'rd';
                        } else {
                            str += 'th';
                        }
                        break;

                    // hour cases
                    case 'h':
                        if (h > 12) {
                            str += h - 12;
                            break;
                        }
                    case 'H':
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
                    default:
                        str += cha;
                }
            }
        }

        return str; // return format with values replaced
    }
});
