package com.communote.server.web.commons.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.View;

import com.communote.server.core.vo.content.AttachmentTO;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class RepositoryContentView implements View {

    private final String characterEncoding;

    /** Use this parameter to set a file to be attached instead of inline. */
    public static final String DOWNLOAD_PARAMETER = "download";

    /**
     * Constant for the model map. The value must be of the type {@link BinaryContentTO} and this
     * will contain the data for download.
     */
    public static final String MODEL_ATTRIBUTE_BINARY_CONTENT = "binaryContent";

    /**
     * Calls {@link RepositoryContentView#RepositoryContentView("")}
     */
    public RepositoryContentView() {
        this("");
    }

    /**
     * @param characterEncoding
     *            The character encoding this view should set.
     */
    public RepositoryContentView(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(@SuppressWarnings("rawtypes") Map model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        AttachmentTO content = (AttachmentTO) model.get(MODEL_ATTRIBUTE_BINARY_CONTENT);

        if (content == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {

            content.sendInResponse(request, response, this.characterEncoding,
                    ServletRequestUtils.getBooleanParameter(request,
                            DOWNLOAD_PARAMETER, false));
        }
    }
}
