package com.communote.server.widgets;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for the {@link WidgetController}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class WidgetControllerTest extends SimpleWidgetController {


    private final String baseUri = "/widgets/" + TestWidget.class.getName().replace('.', '/') + ".widget";

    /**
     * Test for
     * {@link #getWidgetInstance(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * . This mainly tests the pattern used to find widgets.
     */
    @Test
    public void testKENMEI5476() {
        Widget widget = getWidgetInstance(new MockHttpServletRequest("GET", baseUri), null);
        Assert.assertNotNull(widget);

        widget = getWidgetInstance(new MockHttpServletRequest("GET", baseUri
                + ";jsessionid=DEB6E4480A2F66A8FAEAC72EA2BEB76F"), null);
        Assert.assertNotNull(widget);

        widget = getWidgetInstance(new MockHttpServletRequest("GET", baseUri
                + ";jsessionid=DEB6E4480A2F66A8FAEAC72EA2BEB76F.saas1"), null);
        Assert.assertNotNull(widget);
    }
}
