/**
 * @class 
 * @augments communote.widget.classes.controls.WriteContainer
 */
communote.widget.classes.controls.ReplyContainer = communote.widget.classes.controls.WriteContainer
        .extend(
/** 
 * @lends communote.widget.classes.controls.ReplyContainer.prototype
 */        	
{
            name: 'ReplyContainer',
            confirmAction: 'answer',
            optionSelected: function(item) {
                this.base(item);
                if (item.hasClass('cntwCancelNote')){this.fireEvent('cancelReply', this.parent.channel);}
            }
        });
