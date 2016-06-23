/**
 * @class
 * @augments communote.widget.classes.controls.OptionList
 */
communote.widget.classes.controls.FooterContainer = communote.widget.classes.controls.OptionList
        .extend(
/** 
 * @lends communote.widget.classes.controls.FooterContainer.prototype
 */        	
{
            name: "FooterContainer",
            template: 'optionlist',

            registerListeners: function() {
                this.base();
                communote.widget.EventController.registerListener("moreNoteElementsAvailable",
                        "global", this, "viewMoreNotesButton");
                communote.widget.EventController.registerListener("noMoreNoteElementsAvailable",
                        "global", this, "hideMoreNotesButton");
                communote.widget.EventController.registerListener("viewFollowTopicButton",
                        "global", this);
                communote.widget.EventController.registerListener("hideFollowTopicButton",
                        "global", this);
                communote.widget.EventController.registerListener("IsRenderedNoteList",
                        "global", this);
            },

            parseData: function(data) {
                this.base(data);

                var i, preSel, mustAdd;
                var wConfig = this.widget.configuration;
                var communoteUrl = wConfig.msgHomeUrl || wConfig.baseHost + wConfig.cntPath;
                var tmpOptions = [];

                for (i = 0; i < data.options.length; i++) {
                    mustAdd = true;
                    switch(data.options[i].css) {
                        case "cntwFollowTopic":
                            data.options[i].css += " cntwGrayed";
                            preSel = wConfig.fiPreselectedTopics.toString() || "";
                            mustAdd = wConfig.msgFollowButton 
                                    && preSel !== "" 
                                    && preSel.split(",").length === 1;
                            if (mustAdd) {
                                if (wConfig.topicPreselected) {
                                    this.setFollowTopicButtonStatus(wConfig.topicPreselected.isFollow);
                                } else {
                                    this.controller.ApiController.getTopicByAlias(this.followTopicStatus, this, preSel);
                                }
                            }
                            break;
                        case "cntwMoreNotes":
                            data.options[i].css += " cntwGrayed";
                            break;
                        case "cntwToCommunote":
                            mustAdd = wConfig.msgHomeButton;
                            data.options[i].optionText = '<a href="' + communoteUrl + '" target="_blank">'
                                    + data.options[i].optionText + '</a>';
                            break;
                    }
                    if (mustAdd) {
                        tmpOptions.push(data.options[i]);
                    }
                }
                data.options = tmpOptions;
            },

            viewMoreNotesButton: function() {
                this.viewButton(communote.jQuery(".cntwMoreNotes", this.getDomNode()));
            },

            hideMoreNotesButton: function() {
                this.hideButton(communote.jQuery(".cntwMoreNotes", this.getDomNode()));
            },

            viewFollowTopicButton: function() {
                this.viewButton(communote.jQuery(".cntwFollowTopic", this.getDomNode()));
            },

            followTopicStatus: function(resultData) {
                if (resultData.writableTopics && resultData.writableTopics[0]){this.setFollowTopicButtonStatus(resultData.writableTopics[0].isFollow);}
            },

            setFollowTopicButtonStatus: function(follow) {
                var elem = communote.jQuery(".cntwFollowTopic", this.getDomNode());
                if (follow) {
                    elem.addClass("cntwStrikeThrough");
                    label = "htmlclient.widget.footer.unfollowTopic";
                } else {
                    elem.removeClass("cntwStrikeThrough");
                    label = "htmlclient.widget.footer.followTopic";
                }
                elem.html(this.controller.I18nController.getText(label));
            },

            hideFollowTopicButton: function() {
                this.hideButton(communote.jQuery(".cntwFollowTopic", this.getDomNode()));
            },
            
            viewButton: function(elem) {
                var self = this;
                var $ = communote.jQuery;
                elem.removeClass("cntwGrayed");
                elem.click(function() {
                    self.fireEvent('optionSelected', self.parent.channel, $(this).children());
                    self.fireEvent('optionSelected', self.channel, $(this).children());
                    // return false - prevent event bubbeling
                    return (self.controls.length === 0);
                });
            },

            hideButton: function(elem) {
                elem.addClass("cntwGrayed");
                elem.unbind("click");
            },
            
            IsRenderedNoteList: function() {
                if (this.widget.configuration.msgShowFooter) {
                    this.domNode.parent().fadeIn("slow");
                }
            }

        });
