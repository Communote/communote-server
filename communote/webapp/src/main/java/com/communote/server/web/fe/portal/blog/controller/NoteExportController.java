package com.communote.server.web.fe.portal.blog.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.core.blog.export.NoteWriter;
import com.communote.server.core.blog.export.NoteWriterFactory;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Controller to export posts ands post lists to a specific format like rtf or rss.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteExportController extends AbstractController {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NoteExportController.class);

    /** The Constant PARAM_POST_ID. */
    private static final String EXPORT_FORMAT = "format";

    private final QueryParametersParameterNameProvider nameProvider;
    private final TimelineQueryParametersConfigurator<NoteQueryParameters> instanceConfigurator;
    private final NoteQuery query;

    /**
     * Default constructor
     */
    public NoteExportController() {
        nameProvider = new FilterWidgetParameterNameProvider();
        instanceConfigurator = new TimelineQueryParametersConfigurator<NoteQueryParameters>(
                nameProvider, NoteWriterFactory.EXPORT_DEFAULT_MAX_POSTS);
        query = QueryDefinitionRepository.instance().getQueryDefinition(
                NoteQuery.class);
    }

    /**
     * Exports a post list.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws Exception
     *             the exception
     * @return <code>null</code>
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try {
            SecurityHelper.assertCurrentUser();
            NoteQueryParameters queryParameters = query.createInstance();
            queryParameters.setSortByDate(OrderDirection.DESCENDING);

            instanceConfigurator.configure(request.getParameterMap(), queryParameters);

            String fileDate = new SimpleDateFormat("yyyy_MM_dd-HH_mm").format(new Date());
            String filename = ClientHelper.getCurrentClient().getName()
                    .replaceAll("[^\\w-_]+", "_") + "_Export_" + fileDate;
            response.setHeader("Expires", "0");
            response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "public");
            String exportFormat = ServletRequestUtils.getStringParameter(request, EXPORT_FORMAT,
                    "rss");
            NoteWriter noteWriter = NoteWriterFactory.getExporter(exportFormat);
            if (noteWriter != null) {
                response.setContentType(noteWriter.getContentType());
                String contentDisposition = "inline";
                if (noteWriter.isAttachment()) {
                    contentDisposition = "attachment";
                }
                response.setHeader("Content-disposition", contentDisposition + "; filename="
                        + filename + "." + noteWriter.getFileExtension());
                Locale locale = SessionHandler.instance().getCurrentLocale(request);
                NoteRenderContext context;
                if (noteWriter.supportsHtmlContent()) {
                    context = new NoteRenderContext(NoteRenderMode.HTML, locale);
                } else {
                    context = new NoteRenderContext(NoteRenderMode.PLAIN, locale);
                    context.getModeOptions().put(NoteRenderMode.PLAIN_MODE_OPTION_KEY_BEAUTIFY,
                            Boolean.TRUE.toString());
                }
                SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> converter;
                converter = new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(
                        NoteData.class, context);
                PageableList<NoteData> noteItems = ServiceLocator.findService(
                        QueryManagement.class).query(query, queryParameters, converter);

                String requestURI = request.getRequestURL().toString();
                noteWriter
                .write(queryParameters, response.getOutputStream(), noteItems, requestURI);
            } else {
                LOGGER.warn("No such exporter: {}", exportFormat);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("No such exporter: " + exportFormat);
            }

        } catch (Exception e) {
            LOGGER.debug("Export post list failed, got exception. Reasons for that could be: "
                    + "User aborted download, user tried to download with a download "
                    + "manager and so using multiple connections.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return null;
    }
}
