package com.communote.plugins.htmlclient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.server.web.commons.i18n.JsMessagesExtension;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component
@Provides
@Instantiate(name = "HtmlClientJsMessages")
public class HtmlClientJsMessages implements JsMessagesExtension {

    /**
     * {@inheritDoc}
     */
    public Map<String, Set<String>> getJsMessageKeys() {
        HashSet<String> messageKeys = new HashSet<String>();
        messageKeys.add("htmlclient.topicfilter.topics");
        messageKeys.add("htmlclient.topicselector.autocomplete.results");
        messageKeys.add("htmlclient.topicselector.autocomplete.results.more");
        messageKeys.add("htmlclient.topicselector.mostused.title");
        messageKeys.add("htmlclient.topicselector.placeholder");
        messageKeys.add("htmlclient.common.cancel");
        messageKeys.add("htmlclient.common.connectionError");
        messageKeys.add("htmlclient.common.connectionError.hint");
        messageKeys.add("htmlclient.common.no");
        messageKeys.add("htmlclient.common.send");
        messageKeys.add("htmlclient.common.store");
        messageKeys.add("htmlclient.common.yes");
        messageKeys.add("htmlclient.control.error.noDataToDisplay");
        messageKeys.add("htmlclient.fileupload.attachments");
        messageKeys.add("htmlclient.fileupload.entry.delete");
        messageKeys.add("htmlclient.filter.noResultForFilterRequest");
        messageKeys.add("htmlclient.filtercontainer.authors");
        messageKeys.add("htmlclient.filtercontainer.hideFilter");
        messageKeys.add("htmlclient.filtercontainer.inputfield.placeholder");
        messageKeys.add("htmlclient.filtercontainer.showFilter");
        messageKeys.add("htmlclient.filtercontainer.tags");
        messageKeys.add("htmlclient.filtercontainer.text");
        messageKeys.add("htmlclient.filtersummary.filteredBy");
        messageKeys.add("htmlclient.filtersummary.removeAllFilters");
        messageKeys.add("htmlclient.optionlist.less");
        messageKeys.add("htmlclient.note.delete");
        messageKeys.add("htmlclient.note.deleteNote");
        messageKeys.add("htmlclient.note.edit");
        messageKeys.add("htmlclient.note.favorite");
        messageKeys.add("htmlclient.note.hideDiscussion");
        messageKeys.add("htmlclient.note.images.showMore.plural");
        messageKeys.add("htmlclient.note.images.showMore.singular");
        messageKeys.add("htmlclient.note.like");
        messageKeys.add("htmlclient.note.reply");
        messageKeys.add("htmlclient.notelist.noNotesVisible");
        messageKeys.add("htmlclient.notelist.refresh");
        messageKeys.add("htmlclient.tagfilter.tags");
        messageKeys.add("htmlclient.tagfilter.autocomplete.results");
        messageKeys.add("htmlclient.tagfilter.autocomplete.results.more");
        messageKeys.add("htmlclient.tags.autocomplete.results");
        messageKeys.add("htmlclient.tags.autocomplete.results.more");
        messageKeys.add("htmlclient.userfilter.autocomplete.results");
        messageKeys.add("htmlclient.userfilter.autocomplete.results.more");
        messageKeys.add("htmlclient.userfilter.user.anonymous");
        messageKeys.add("htmlclient.userfilter.users");
        messageKeys.add("htmlclient.viewfilter.activityStream");
        messageKeys.add("htmlclient.viewfilter.title");
        messageKeys.add("htmlclient.viewfilter.viewlabel.all");
        messageKeys.add("htmlclient.viewfilter.viewlabel.favorites");
        messageKeys.add("htmlclient.viewfilter.viewlabel.following");
        messageKeys.add("htmlclient.viewfilter.viewlabel.me");
        messageKeys.add("htmlclient.widget.footer.followTopic");
        messageKeys.add("htmlclient.widget.footer.moreNotes");
        messageKeys.add("htmlclient.widget.footer.toCommunote");
        messageKeys.add("htmlclient.widget.footer.unfollowTopic");
        messageKeys.add("htmlclient.writecontainer.inputfield.placeholder");
        messageKeys.add("htmlclient.writecontainer.attachment");
        messageKeys.add("htmlclient.writecontainer.nowritabletopics");
        messageKeys.add("htmlclient.writecontainer.taglistlabel");
        messageKeys.add("htmlclient.writecontainer.tags");
        messageKeys.add("htmlclient.writecontainer.tags.inputfield.placeholder");
        messageKeys.add("htmlclient.common.dateformat.pattern");
        messageKeys.add("htmlclient.common.dateformat.pattern.date");
        messageKeys.add("htmlclient.common.dateformat.pattern.time");
        messageKeys.add("htmlclient.common.dateformat.days");
        messageKeys.add("htmlclient.common.dateformat.months");
        messageKeys.add("htmlclient.common.dateformat.weekdayOffset");
        messageKeys.add("htmlclient.common.dateformat.timeSuffixes");
        HashMap<String, Set<String>> mapping = new HashMap<String, Set<String>>();
        mapping.put("html-client", messageKeys);
        return mapping;
    }

}
