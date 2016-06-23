package com.communote.plugins.embed;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.BundleContext;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.plugins.core.views.ViewController;
import com.communote.plugins.core.views.ViewControllerException;
import com.communote.plugins.core.views.annotations.Page;
import com.communote.plugins.core.views.annotations.UrlMapping;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component(immediate = true)
@Instantiate
@Provides
@UrlMapping("/*/embed-messenger")
@Page(menu = "embed-messenger", jsMessagesCategory = "portal", jsCategories = { "cnt-embed-messenger" })
public class EmbedMessengerController extends ViewController implements Controller {

    public EmbedMessengerController(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
    }

    @Override
    public String getContentTemplate() {
        // no content template required
        return "";
    }

    @Override
    public String getMainTemplate() {
        return "communote.plugins.embed.messenger.page";
    }
}
