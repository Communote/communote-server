/**
 *
 */
package com.communote.server.web.api.service.post;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.commons.MessageHelper;

/**
 *
 * Controller for handling api request for marking or unmarking notes as favorites.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead
 */
@Deprecated
public class FavoriteApiController extends BaseRestApiController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws HttpRequestMethodNotSupportedException,
            ApiException {
        Long noteId = getResourceId(request, true);
        Boolean markAsFavorite = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                "favorite");
        if (markAsFavorite == null) {
            throw new ApiException(MessageHelper.getText(request,
                    "error.favorites.create.no.request"));
        }
        String messageKey;
        try {
            FavoriteManagement favManagement = ServiceLocator.findService(FavoriteManagement.class);
            if (markAsFavorite) {
                favManagement.markNoteAsFavorite(noteId);
                messageKey = "notify.success.favorite.add";
            } else {
                favManagement.unmarkNoteAsFavorite(noteId);
                messageKey = "notify.success.favorite.remove";
            }
        } catch (NoteNotFoundException e) {
            if (markAsFavorite) {
                throw new ApiException(MessageHelper.getText(request,
                        "notify.error.favorite.add.note.not.found"));
            } else {
                throw new ApiException(MessageHelper.getText(request,
                        "notify.error.favorite.remove.note.not.found"));
            }
        }

        // write message > user feedback
        apiResult.setMessage(MessageHelper.getText(request, messageKey));

        return StringUtils.EMPTY;
    }
}
