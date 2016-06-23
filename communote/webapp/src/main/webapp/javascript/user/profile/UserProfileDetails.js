var UserProfileDetailsWidget = new Class({
    Extends: C_FormWidget,

    widgetGroup: "user/profile",
    tagAutocompleter: null,
    placeholders: null,
    tagTextboxList: null,

    cleanup: function() {
        if (this.tagAutocompleter) {
            this.tagAutocompleter.destroy();
        }
        if (this.placeholders) {
            this.placeholders.destroy();
        }
        if (this.tagTextboxList) {
            this.tagTextboxList.destroy();
        }
    },
    
    beforeRemove: function() {
        this.cleanup();
    },
    
    refreshStart: function() {
        this.cleanup();
    },
    
    refreshComplete: function(responseMetadata) {
        var timeZoneOffset, languageCode, tagInput, autocompleterOptions, suggestionsOptions, textboxListInput, tagListHiddenInput, constructor, tagList, i;
        // save refresh cause since parent method resets it
        var isRefreshCausedBySubmit = this.refreshCausedBySubmit;
        this.parent(responseMetadata);
        searchAndShowRoarNotification(this.domNode);

        if (!isRefreshCausedBySubmit) {
            timeZoneOffset = this.domNode.getElement('.control-timeZoneOffset');
            this.oldTimeZoneOffset = timeZoneOffset.value;

            languageCode = this.domNode.getElement('.control-languageCode');
            this.oldLanguageCode = languageCode.value;
        }
        
        textboxListInput = this.domNode.getElementById(this.widgetId + '_tag-textbox-list');
        tagListHiddenInput = this.domNode.getElementById('tags');

        constructor = communote.getConstructor('TextboxList');
        this.tagTextboxList = new constructor(textboxListInput, {
            addItemOnChar: ',',
            allowDuplicates: false,
            // kind of hacky but is easiest way to avoid duplicates and showing the tag twice in the list
            listCssClass: 'cn-border',
            itemRemoveCssClass: 'textboxlist-item-remove cn-icon',
            itemRemoveTitle: getJSMessage('common.tagManagement.remove.tag.tooltip')
        });
        
        if (tagListHiddenInput.value != "") {
            tagList = tagListHiddenInput.value.split(',');
            if (tagList.length > 0) {
                for (i = 0; i < tagList.length; i++) {
                    this.tagTextboxList.addItem(tagList[i].trim());
                }
            }
        }
        
        this.placeholders = communote.utils.attachPlaceholders(null, this.domNode);

        tagInput = this.domNode.getElement('input[name='+this.widgetId + '_tag-textbox-list]');
        suggestionsOptions = {};
        suggestionsOptions.categories = [];
        suggestionsOptions.categories.push({
            id: 'DefaultEntityTagStore',
            title: '',
            provider: 'DefaultEntityTagStore'
        });
        autocompleterOptions = {
            suggestionsOptions: suggestionsOptions
        };
        this.tagAutocompleter = autocompleterFactory.createTagAutocompleter(tagInput,
                autocompleterOptions, null, 'ENTITY', true, true);
        tagInput.addEvent('keypress', this.suppressFormSubmit);
        
        if (this.tagAutocompleter) {
            this.tagAutocompleter.addEvent('enterPressed', this.submitTags.bind(this, tagInput));
            this.tagAutocompleter.addEvent('onChoiceSelected', this.tagChoiceSelected.bind(this));
        }
        
        init_tips(this.domNode);
    },
    
    submitTags: function(tagInput) {
        var i, itemsCount, tagList;
        if(tagInput.value != "") {
            itemsCount = tagInput.value.indexOf(',');
            if(itemsCount === -1) {
                this.tagTextboxList.addItem(tagInput.value);
            } else {
                tagList = tagInput.value.trim().split(',');
                for (i = 0; i < tagList.length; i++) {
                    if(tagList[i] != "") {
                        this.tagTextboxList.addItem(tagList[i]);
                    }
                }
            }
            this.tagAutocompleter.resetQuery(true);
        }
    },
    
    tagChoiceSelected: function(inputElem, choiceElem, token, value) {
        this.tagTextboxList.addItem(token.name);
        this.tagAutocompleter.resetQuery(true);
    },
    
    /**
     * overwrite
     * 
     * @method refresh taglist before submit
     */
    onFormSubmit: function(params) {
        this.domNode.getElementById('tags').value = this.tagTextboxList.getItems().toString();
        this.parent(params);
    },
    
    /**
     * overwrite
     * 
     * @method refresh taglist before submit
     */
    onFormSubmitButtonClick: function(objSubmit) {
        this.domNode.getElementById('tags').value = this.tagTextboxList.getItems().toString();
        this.parent(objSubmit);
    },

    onSubmitSuccess: function() {
        var userSignatureElems, buttons, dlgContent;
        var profileForm = this.domNode.getElementById(this.widgetId + '_form');

        if (profileForm == undefined) {
            return;
        }
        userSignatureElems = $$('.control-current-user-signature');
        if (userSignatureElems && userSignatureElems.length > 0 && userSignatureElems[0]) {
            var oldName = userSignatureElems[0].get('text');
            var name = '';
            var prefix = '';
            var salutation = profileForm.getElementById('salutation');
            var firstName = profileForm.getElementById('firstName');
            var lastName = profileForm.getElementById('lastName');
            if (salutation && salutation.value) {
                name = salutation.value;
                prefix = ' ';
            }
            if (firstName && firstName.value) {
                name += prefix;
                name += firstName.value;
                prefix = ' ';
            }
            if (lastName && lastName.value) {
                name += prefix;
                name += lastName.value;
                prefix = ' ';
            }

            if (oldName != name) {
                userSignatureElems.set('text', name);
            }
            E('onUserProfileChanged', null);
        }

        var timeZoneOffset = profileForm.getElement('.control-timeZoneOffset');
        if (this.oldTimeZoneOffset != timeZoneOffset.value) {
            communote.currentUser.timeZoneOffset = timeZoneOffset.value.toInt();
            E('onTimeZoneChanged');
        }

        var languageCode = profileForm.getElement('.control-languageCode');
        if (this.oldLanguageCode != languageCode.value) {
            dlgContent = getJSMessage('user.profile.language.change.hint.content');
            buttons = [];
            buttons.push({
                type: 'ok',
                action: function(dialogContainer) {
                    location.reload(true);
                }.bind(this)
            });
            showDialog(getJSMessage('user.profile.language.change.hint.title'), dlgContent, buttons);
        }
    },

    suppressFormSubmit: function(event) {
        if (event.key == 'enter') {
            value = this.value.trim();
            if (value.length > 0 && !value.match(/,$/)) {
                this.value = this.value + ', ';
            }
            event.preventDefault();
        }
    }
});