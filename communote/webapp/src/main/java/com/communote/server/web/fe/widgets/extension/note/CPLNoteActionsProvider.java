package com.communote.server.web.fe.widgets.extension.note;

import java.util.List;
import java.util.Map;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.web.fe.widgets.blog.ChronologicalPostListWidget;
import com.communote.server.web.fe.widgets.extension.WidgetExtension;

/**
 * Extension for the {@link ChronologicalPostListWidget} which allows adding or replacing actions of
 * a note. This extensions just provides the names of the actions. Additionally message keys for the
 * actions which adhere to the following naming conventions must be provided and exposed to
 * JavaScript:
 * <ul>
 * <li>widget.chronologicalPostList.note.action.NameOfTheAction.label - the display name of the
 * action</li>
 * <li>widget.chronologicalPostList.note.action.NameOfTheAction.title - to provide a value for the
 * title attribute (optional)</li>
 * </ul>
 * 
 * More over a JavaScript action handler has to be registered to the NoteActionHandler of the
 * widget.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public abstract class CPLNoteActionsProvider implements
        WidgetExtension<CPLNoteActionsProvider, CPLNoteActionsProviderManagement> {

    /**
     * Add the names of the actions which should be available for the given note. The implementation
     * might also clear the actions list if only it's actions should be available in the frontend.
     * In cases where no other provider should or can add actions for the note this method can
     * return false.
     * 
     * @param requestParameters
     *            the parameters of the current request (e.g. full or partial widget refresh)
     * @param data
     *            object holding details of the note
     * @param actions
     *            the list to add the actions to. The names of the actions must only contain ASCII
     *            alphanumeric characters.
     * @return if true subsequent providers should be called, if false no subsequent providers are
     *         called.
     */
    public abstract boolean addNoteActions(Map<String, String> requestParameters,
            NoteData data, List<String> actions);

    @Override
    public Class<CPLNoteActionsProviderManagement> getManagementType() {
        return CPLNoteActionsProviderManagement.class;
    }
}
