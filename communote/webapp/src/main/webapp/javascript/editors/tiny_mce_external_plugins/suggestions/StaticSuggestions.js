var StaticSuggestions = function(options) {
    var domUtils = tinymce.DOM;
    var choicesCssClass = options.choicesCssClass || 'mceSuggestionChoices';
    var zIndex = options.zIndex || 42;
    var elem = domUtils.create('ul', {
        'style': 'display:none; position:absolute; z-index:' + zIndex,
        'class': choicesCssClass
    });
    this.choicesElem = domUtils.add(domUtils.doc.body, elem);
    this.staticSuggestions = options.staticSuggestions || {};
    this.staticEmptySuggestions = options.staticEmptySuggestions || [];
    this.minLength = options.minLength == 0 ? 0 : options.minLength || 1;
    this.queryValue = null;
};
StaticSuggestions.prototype.injectChoice = function(choiceData, choicesElem, domUtils) {
    var self = this, liElem, spanElem;
    if (choiceData.suggestion && choiceData.completion) {
        // adding class, value and actions after adding elements to DOM for IE
        liElem = domUtils.add(choicesElem, 'li');
        // avoiding tinyMCE stuff for simple stuff brings some performance
        liElem.className = 'choice';
        liElem.setAttribute('value', choiceData.completion);
        liElem.onmouseover = function(e) {
            var curSelElem;
            if (!domUtils.hasClass(this, 'selected-choice')) {
                curSelElem = self._findChildByClassName(this.parentNode, 'selected-choice', 'li');
                if (curSelElem) {
                    domUtils.removeClass(curSelElem, 'selected-choice');
                }
                domUtils.addClass(this, 'selected-choice');
            }
            return false;
        };
        liElem.onmousedown = function(e) {
            self.choiceSelected(this);
            return domUtils.events.cancel(e);
        };
        spanElem = domUtils.add(liElem, 'span'); // IE6 crashes when adding content here directly
        spanElem.appendChild(domUtils.doc.createTextNode(choiceData.suggestion));
    }
};
StaticSuggestions.prototype.injectSummary = function(summary, choicesElem, domUtils) {
    var liElem, spanElem;
    var children = choicesElem.childNodes;
    var firstNode = null;
    if (children && children[0]) {
        if (children[0].className == 'choice-summary') {
            children[0].childNodes[0].innerHTML = summary;
            return;
        } else {
            firstNode = children[0];
        }
    }
    liElem = domUtils.doc.createElement('li');
    spanElem = domUtils.doc.createElement('span');
    spanElem.appendChild(domUtils.doc.createTextNode(summary));
    liElem.appendChild(spanElem);
    liElem = choicesElem.insertBefore(liElem, firstNode);
    liElem.className = 'choice-summary';
};
StaticSuggestions.prototype.buildChoices = function(searchResult) {
    // remove old suggestions
    var i, suggestions;
    var choicesElem = this.choicesElem;
    var domUtils = tinymce.DOM;
    var children = choicesElem.childNodes;
    // remove in reverse order because children changes when removing from dom
    for ( i = children.length - 1; i >= 0; i--) {
        domUtils.remove(children[i]);
    }
    suggestions = searchResult.suggestions;
    for ( i = 0; i < suggestions.length; i++) {
        this.injectChoice(suggestions[i], choicesElem, domUtils);
    }
    if (searchResult.summary) {
        this.injectSummary(searchResult.summary, choicesElem, domUtils);
    }
};

StaticSuggestions.prototype.showChoices = function() {
    var domUtils, position;
    var choicesElem = this.choicesElem;
    if (!choicesElem.firstChild)
        this.hideChoices();
    if (this.choicesShown || !this.determinePositionCallback)
        return;
    position = this.determinePositionCallback.call(null, false);
    domUtils = tinymce.DOM;
    domUtils.setStyles(choicesElem, position);
    domUtils.show(choicesElem);
    this.choicesShown = true;
    if (this.onShowCallback) {
        this.onShowCallback.call(null, this.choicesElem);
    }
};

StaticSuggestions.prototype.hideChoices = function() {
    if (!this.choicesShown)
        return;
    tinymce.DOM.hide(this.choicesElem);
    this.choicesShown = false;
    if (this.onHideCallback) {
        this.onHideCallback.call(null, this.choicesElem);
    }
};

StaticSuggestions.prototype.repositionChoices = function() {
    var position;
    if (this.choicesShown) {
        position = this.determinePositionCallback.call(null, false);
        tinymce.DOM.setStyles(this.choicesElem, position);
    }
};
StaticSuggestions.prototype.staticQuery = function(searchTerm) {
    var i, suggestion, subString, suggestions;
    var matches = [];
    var searchTermLength = searchTerm.length;
    if (searchTermLength) {
        suggestions = this.staticSuggestions[searchTerm.charAt(0)];
    } else {
        suggestions = this.staticEmptySuggestions;
    }
    if (suggestions) {
        for ( i = 0; i < suggestions.length; i++) {
            suggestion = suggestions[i];
            subString = suggestion.slice(0, searchTermLength);
            if (subString == searchTerm) {
                matches.push({
                    suggestion: suggestion,
                    completion: suggestion
                });
            } else {
                if (matches.length > 0) {
                    // stop because suggestions are sorted alphetically
                    break;
                }
            }
        }
    }
    return {
        suggestions: matches,
        summary: 'found ' + matches.length + ' suggestions'
    };
};
// TODO check if we can use DOMUtils.select (performance)
StaticSuggestions.prototype._findChildByClassName = function(startNode, className, tagName) {
    var foundElem = null, choices;
    if (startNode.getElementsByClassName) {
        // Opera, FF 3
        choices = startNode.getElementsByClassName(className);
        if (choices)
            foundElem = choices[0];
    } else if (startNode.querySelectorAll) {
        // IE8
        var selector = '.' + className;
        if (tagName)
            selector = tagName + selector;
        choices = startNode.querySelectorAll(selector);
        if (choices)
            foundElem = choices[0];
    } else {
        choices = startNode.childNodes;
        for ( var i = 0; i < choices.length; i++) {
            if (domUtils.hasClass(choices[i], className)) {
                foundElem = choices[i];
                break;
            }
        }
    }
    return foundElem;
};
StaticSuggestions.prototype.choiceSelected = function(choiceElem) {
    var value = choiceElem.getAttribute('value');
    this.hideChoices();
    if (this.onChoiceSelectedCallback) {
        this.onChoiceSelectedCallback.call(null, choiceElem, value, value);
    }
};
StaticSuggestions.prototype.focusNextChoice = function(previous, skipLoading) {
    var nextFocusedChoice, curFocusedChoice, choices, domUtils;
    if (this.choicesShown) {
        domUtils = tinymce.DOM;
        nextFocusedChoice = null;
        curFocusedChoice = this._findChildByClassName(this.choicesElem, 'selected-choice', 'li');
        if (curFocusedChoice) {
            if (previous) {
                nextFocusedChoice = curFocusedChoice.previousSibling;
            } else {
                nextFocusedChoice = curFocusedChoice.nextSibling;
            }
            if (nextFocusedChoice && !domUtils.hasClass(nextFocusedChoice, 'choice')) {
                nextFocusedChoice = null;
            }
        } else {
            choices = this.choicesElem.childNodes;
            if (!previous && choices.length > 0) {
                nextFocusedChoice = domUtils.hasClass(choices[0], 'choice') ? choices[0]
                        : choices[0].nextSibling;
            }
        }
        if (curFocusedChoice && nextFocusedChoice) {
            domUtils.removeClass(curFocusedChoice, 'selected-choice');
        }
        if (nextFocusedChoice) {
            domUtils.addClass(nextFocusedChoice, 'selected-choice');
        }
    }
};
StaticSuggestions.prototype.selectFocusedChoice = function() {
    var focusedChoice;
    if (this.choicesShown) {
        focusedChoice = this._findChildByClassName(this.choicesElem, 'selected-choice', 'li');
        if (focusedChoice) {
            this.choiceSelected(focusedChoice);
            return true;
        }
    }
    return false;
};
StaticSuggestions.prototype.queryValueChanged = function(query) {
    var result;
    if (query.length < this.minLength) {
        this.hideChoices();
    } else {
        if (query == this.queryValue) {
            this.showChoices();
            return;
        }
        result = this.staticQuery(query);
        if (this.onChoicesFoundCallback) {
            this.onChoicesFoundCallback.call(null, query, result.suggestions);
        }
        this.buildChoices(result);
        this.queryValue = query;
        this.showChoices();
    }
};

StaticSuggestions.prototype.destroy = function() {
    tinymce.DOM.remove(this.choicesElem);
};
StaticSuggestions.prototype.setOnChoiceFocusedCallback = function(callback) {
    this.onChoiceFocusedCallback = callback;
};
StaticSuggestions.prototype.setOnChoiceSelectedCallback = function(callback) {
    this.onChoiceSelectedCallback = callback;
};
StaticSuggestions.prototype.setOnHideCallback = function(callback) {
    this.onHideCallback = callback;
};
StaticSuggestions.prototype.setOnShowCallback = function(callback) {
    this.onShowCallback = callback;
};
StaticSuggestions.prototype.setDeterminePositionCallback = function(callback) {
    this.determinePositionCallback = callback;
};
StaticSuggestions.prototype.setOnChoicesFoundCallback = function(callback) {
    this.onChoicesFoundCallback = callback;
};