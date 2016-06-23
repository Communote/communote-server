/**
 * Observer - Observe formelements for changes
 *  - Additional code from clientside.cnet.com
 * 
 * @version 1.1
 * 
 * @license MIT-style license
 * @author Harald Kirschner <mail [at] digitarald.de>
 * @copyright Author
 */
var Observer = new Class({

    Implements: [ Options, Events ],

    options: {
        periodical: false,
        delay: 1000
    },

    initialize: function(el, onFired, options) {
        this.element = $(el) || $$(el);
        this.addEvent('onFired', onFired);
        this.setOptions(options);
        this.bound = this.changed.bind(this);
    },

    changed: function() {
        var delay;
        var value = this.element.get('value').trim();
        if (this.value == value) {
            return;
        }
        delay = this.options.delay;
        this.value = value;
        if (delay > 0) {
            this.clear();
            this.timeout = this.onFired.delay(this.options.delay, this);
        } else {
            this.onFired();
        }
    },

    setValue: function(value) {
        this.value = value;
        this.element.set('value', value);
        return this.clear();
    },

    onFired: function() {
        this.fireEvent('onFired', [ this.value, this.element ]);
    },

    clear: function() {
        if (this.timeout) {
            clearTimeout(this.timeout);
        }
        return this;
    },

    pause: function() {
        if (this.timer)
            clearInterval(this.timer);
        else
            this.element.removeEvent('keyup', this.bound);
        return this.clear();
    },

    resume: function() {
        this.value = this.element.get('value');
        if (this.options.periodical)
            this.timer = this.changed.periodical(this.options.periodical, this);
        else
            this.element.addEvent('keyup', this.bound);
        return this;
    }

});
