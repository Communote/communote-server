/**
 * Roar - Notifications
 *
 * Inspired by Growl
 *
 * @version		1.0.1
 *
 * @license		MIT-style license
 * @author		Harald Kirschner <mail [at] digitarald.de>
 * @copyright	Author
 */

var Roar = new Class({

	Implements: [Options, Events, Chain],

	options: {
		duration: 3000,
		position: 'upperLeft',
		container: null,
		bodyFx: null,
		itemFx: null,
		margin: {x: 10, y: 10},
		offset: 10,
		className: 'roar',
		onShow: function(){},
		onHide: function(){},
		onRender: function(){}
	},

	initialize: function(options) {
		this.setOptions(options);
		this.items = [];
		this.container = $(this.options.container) || document;
	},

	alert: function(title, message, options) {
		var params = Array.link(arguments, {title: Type.isString, message: Type.isString, options: Type.isObject});
		var items = [new Element('h3', {'html': params.title != null ? params.title : ''})];
		if (params.message) items.push(new Element('p', {'html': params.message}));
		return this.inject(items, params.options);
	},

	inject: function(elements, options) {
	    var docSize;
		if (!this.body) this.render();
		options = options || {};

		var offset = [-this.options.offset, 0];
		var last = this.items.getLast();
		if (last) {
			offset[0] = last.retrieve('roar:offset');
			offset[1] = offset[0] + last.offsetHeight + this.options.offset;
		}
		var to = {'opacity': 1};
		to[this.align.y] = offset;

		var item = new Element('div', {
			'class': this.options.className,
			'opacity': 0
		});
		// RWI: avoid scrollbar creation in browser
		if (this.align.y == 'bottom' && document.getScrollSize().y == (docSize = document.getSize()).y) {
			item.setStyle('bottom', -this.options.margin.y);
		}
		// avoid that roar is wider than viewport and thus is truncated on the left
		if (this.align.x == 'right') {
		    item.setStyle('max-width', (docSize ? docSize.x : document.getSize().x) - 2 * this.options.margin.x);
		}
		item = item.adopt(
			new Element('div', {
				'class': 'roar-bg',
				'opacity': 0.8 // DST :: modification, default 0.5
			}),
			elements
		);
		
		item.setStyle(this.align.x, 0).store('roar:offset', offset[1]).set('morph', Object.merge({},{
		    unit: 'px',
			link: 'cancel',
			onStart: Chain.prototype.clearChain,
			transition: Fx.Transitions.Back.easeOut
		}, this.options.itemFx));

		this.items.push(item.addEvent('click', function(event){
		    this.remove.delay(10, this, item);
		}.bind(this)));

		var remove = this.remove.bind(this,item);
		
		if (this.options.duration) {
			var over = false;
			var trigger = (function() {
				trigger = null;
				if (!over) remove.delay(10);
			}).delay(this.options.duration);
			item.addEvents({
				mouseover: function() {
					over = true;
				},
				mouseout: function() {
					over = false;
					if (!trigger) remove.delay(10);
				}
			});
		}
		item.inject(this.body).morph(to);
		// DST :: modification starts
        if (Browser.name === 'ie' && Browser.version == 6) {
            var roarHeight = item.getHeight() - 10;
            // cannot set negative height in IE6
            if (roarHeight < 0) {
                roarHeight = 0;
            }
            item.getElement('div.roar-bg').setStyle('height', roarHeight);
        }
        // DST :: modification ends
		return this.fireEvent('onShow', [item, this.items.length]);
	},

	remove: function(item) {
		var index = this.items.indexOf(item);
		if (index == -1) return this;
		this.items.splice(index, 1);
		item.removeEvents();
		var to = {opacity: 0};
		// RWI: avoid scrollbar creation in browser
		var alignY = this.align.y;
		var yPos = item.getStyle(alignY).toInt() - item.offsetHeight - this.options.offset;
		if (alignY == 'bottom' && yPos < 0) {
			var space = document.getScrollSize().y - item.getCoordinates().bottom;
			yPos = space + yPos > 0 ? yPos : -space + 1; 
		}
		to[alignY] = yPos;
		
		item.morph(to).get('morph').chain(item.destroy.bind(item));
		return this.fireEvent('onHide', [item, this.items.length]).callChain(item);
	},

	empty: function() {
		while (this.items.length) this.remove(this.items[0]);
		return this;
	},

	render: function() {
		this.position = this.options.position;
		if (typeOf(this.position) == 'string') {
			var position = {x: 'center', y: 'center'};
			this.align = {x: 'left', y: 'top'};
			if ((/left|west/i).test(this.position)) position.x = 'left';
			else if ((/right|east/i).test(this.position)) this.align.x = position.x = 'right';
			if ((/upper|top|north/i).test(this.position)) position.y = 'top';
			else if ((/bottom|lower|south/i).test(this.position)) this.align.y = position.y = 'bottom';
			this.position = position;
		}
		this.body = new Element('div', {'class': 'roar-body'}).inject(document.body);
		if (Browser.name === 'ie' && Browser.version == 6) this.body.addClass('roar-body-ugly');
		this.moveTo = this.body.setStyles.bind(this.body);
		this.reposition();
		if (this.options.bodyFx) {
			var morph = new Fx.Morph(this.body, Object.merge({},{
				unit: 'px',
				chain: 'cancel',
				transition: Fx.Transitions.Circ.easeOut
			}, this.options.bodyFx));
			this.moveTo = morph.start.bind(morph);
		}
		var repos = this.reposition.bind(this);
		window.addEvents({
			scroll: repos,
			resize: repos
		});
		this.fireEvent('onRender', this.body);
	},

	reposition: function() {
		var max = document.getCoordinates(), scroll = document.getScroll(), margin = this.options.margin;
		max.left += scroll.x;
		max.right += scroll.x;
		max.top += scroll.y;
		max.bottom += scroll.y;
		var rel = (typeOf(this.container) == 'element') ? this.container.getCoordinates() : max;
		this.moveTo({
			left: (this.position.x == 'right')
				? (Math.min(rel.right, max.right) - margin.x)
				: (Math.max(rel.left, max.left) + margin.x),
			top: (this.position.y == 'bottom')
				? (Math.min(rel.bottom, max.bottom) - margin.y)
				: (Math.max(rel.top, max.top) + margin.y)
		});
	}

});