package com.communote.server.core.security;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.iprange.CurrentIpNotInRange;
import com.communote.server.core.security.iprange.InvalidIpAddressException;
import com.communote.server.core.security.iprange.IpRangeException;
import com.communote.server.core.security.iprange.IpRangeFilterManagement;
import com.communote.server.core.security.iprange.IpRangeHelper;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.security.IpRange;
import com.communote.server.persistence.security.iprange.IpRangeFilterVO;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;

/**
 * The Class IpRangeFilterManagementTest.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeFilterManagementTest extends CommunoteIntegrationTest {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(IpRangeFilterManagementTest.class);

    /** The Constant CURRENT_IP_V4. */
    private static final String CURRENT_IP_V4 = "127.0.0.1";

    private static final String IP_FILTER_INCLUDING_CURRENT_IP = "IncludeExcludeFilter";

    private static final String IP_FILTER_EMPTY = "emptyfilter";
    private static final ChannelType WEB_CHANNEL = ChannelType.WEB;
    private IpRangeFilterManagement ipRangeFilterManagement;

    /**
     * Creates a new filter and tries to enable it.
     *
     * @param name
     *            Name of filter
     * @param includes
     *            Include IPs
     * @param excludes
     *            exclude IPs
     * @param currentIP
     *            current IP address
     * @param channel
     *            the channel the filter will apply to
     * @return the VO of the created filter
     * @throws Exception
     *             in case creation or activation fails
     */
    private IpRangeFilterVO createFilter(String name, String includes, String excludes,
            String currentIP, ChannelType channel) throws Exception {
        IpRangeFilterVO vo = ipRangeFilterManagement.createFilter(name, includes, excludes);
        ipRangeFilterManagement.setFilterChannelEnabled(vo.getId(), channel, true, currentIP,
                WEB_CHANNEL);
        ipRangeFilterManagement.setFilterEnabled(vo.getId(), true, CURRENT_IP_V4, WEB_CHANNEL);
        return vo;
    }

    /**
     * Retrieves existing filters.
     *
     * @return a list with the existing IP range filters
     */
    private List<IpRangeFilterVO> getFilters() {
        return ipRangeFilterManagement.listFilter();
    }

    /**
     * Remove all filter
     *
     * @throws CurrentIpNotInRange
     *             in case the remove of a filter filed because of current IP being blocked
     * @throws InvalidIpAddressException
     *             in case the current IP is not a valid IP address
     */
    private void removeAllFilter() throws CurrentIpNotInRange, InvalidIpAddressException {
        List<IpRangeFilterVO> filters = ipRangeFilterManagement.listFilter();
        // remove filter that includes the current IP after all the other filters to avoid
        // CurrentIpNotInRange
        Long lastFilterToRemove = null;
        for (IpRangeFilterVO filter : filters) {
            if (filter.getName().equals(IP_FILTER_INCLUDING_CURRENT_IP)) {
                lastFilterToRemove = filter.getId();
            } else {
                removeFilter(filter.getId());
            }
        }
        if (lastFilterToRemove != null) {
            removeFilter(lastFilterToRemove);
        }
    }

    /**
     * Remove filter with the given id
     *
     * @param id
     *            Filter id
     * @throws CurrentIpNotInRange
     *             CurrentIpNotInRange
     * @throws InvalidIpAddressException
     *             in case the current IP is not a valid IP address
     */
    private void removeFilter(Long id) throws CurrentIpNotInRange, InvalidIpAddressException {
        ipRangeFilterManagement.removeFilter(id, CURRENT_IP_V4, WEB_CHANNEL);
    }

    /**
     * Test channel enabled.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setupChannelEnabled() throws Exception {
        ipRangeFilterManagement = ServiceLocator.instance().getService(
                IpRangeFilterManagement.class);
        AuthenticationTestUtils.setManagerContext();
        for (String channel : ChannelType.names()) {
            ipRangeFilterManagement.setChannelEnabled(ChannelType.fromString(channel), true,
                    CURRENT_IP_V4, WEB_CHANNEL);
        }
    }

    /**
     * Test to ensure that you cannot enable a filter that would block your current IP.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testUpdateFilter", expectedExceptions = { CurrentIpNotInRange.class })
    public void testAvoidBlockingCurrentIp() throws Exception {
        AuthenticationTestUtils.setManagerContext();

        removeAllFilter();
        createFilter("filter1", "192.1.123.44", "", CURRENT_IP_V4, ChannelType.WEB);

    }

    /**
     * Tests that one cannot remove the filter that grants access for the current IP.
     *
     * @throws Exception
     *             the expected exception or another if the test failed
     */
    @Test(expectedExceptions = { CurrentIpNotInRange.class }, dependsOnMethods = { "testFindIpRanges" })
    public void testAvoidBlockingCurrentIpWhenRemoving() throws Exception {
        AuthenticationTestUtils.setManagerContext();

        List<IpRangeFilterVO> filters = getFilters();
        Assert.assertEquals(filters.size(), 4);
        for (IpRangeFilterVO filter : filters) {
            if (filter.getName().equals(IP_FILTER_INCLUDING_CURRENT_IP)) {
                removeFilter(filter.getId());
                break;
            }
        }
    }

    /**
     * Test create filter.
     *
     * @throws IpRangeException
     *             the ip range exception
     */
    @Test
    public void testCreateFilter() throws IpRangeException {
        AuthenticationTestUtils.setManagerContext();

        try {
            IpRangeFilterVO filter = ipRangeFilterManagement.createFilter(IP_FILTER_EMPTY, "", "");
            Assert.assertNotNull(filter, "got no filter");
            filter = ipRangeFilterManagement.createFilter(IP_FILTER_INCLUDING_CURRENT_IP,
                    "127.0.0.1", "3.4.5.6");
            LOG.debug("filter: \n"
                    + ToStringBuilder.reflectionToString(filter, ToStringStyle.MULTI_LINE_STYLE));
            filter = ipRangeFilterManagement.createFilter("IncludeFilter", "168.0.0.1", "");
            LOG.debug("filter: \n"
                    + ToStringBuilder.reflectionToString(filter, ToStringStyle.MULTI_LINE_STYLE));
            filter = ipRangeFilterManagement.createFilter("IncludeFilter_2",
                    "::168.0.0.1-::168.0.5.0", "");
            LOG.debug("filter: \n"
                    + ToStringBuilder.reflectionToString(filter, ToStringStyle.MULTI_LINE_STYLE));
        } catch (IpRangeException e) {
            LOG.error(e + ", ip: '" + e.getIp() + "'", e);
            throw e;
        }
    }

    /**
     * Test filter channel enabled.
     *
     * @throws Exception
     *             the exception
     */
    @Test(dependsOnMethods = "testCreateFilter")
    public void testFilterChannelEnabled() throws Exception {
        AuthenticationTestUtils.setManagerContext();

        List<IpRangeFilterVO> filtersVO = getFilters();
        for (IpRangeFilterVO filterVO : filtersVO) {
            ipRangeFilterManagement.setFilterChannelEnabled(filterVO.getId(), ChannelType.API,
                    true, CURRENT_IP_V4, WEB_CHANNEL);
            ipRangeFilterManagement.setFilterChannelEnabled(filterVO.getId(), ChannelType.WEB,
                    true, CURRENT_IP_V4, WEB_CHANNEL);
        }
        for (IpRangeFilterVO filterVO : filtersVO) {
            filterVO = ipRangeFilterManagement.findFilterById(filterVO.getId());
            Assert.assertSame(filterVO.getChannels().length, 2, "Two channel must be setted");
        }
    }

    /**
     * Test filter enabled.
     *
     * @throws Exception
     *             the exception
     */
    @Test(dependsOnMethods = "testListFilter")
    public void testFilterEnabled() throws Exception {
        AuthenticationTestUtils.setManagerContext();

        List<IpRangeFilterVO> filtersVO = getFilters();
        for (IpRangeFilterVO filterVO : filtersVO) {
            ipRangeFilterManagement.setFilterEnabled(filterVO.getId(), true, CURRENT_IP_V4,
                    WEB_CHANNEL);
            filterVO = ipRangeFilterManagement.findFilterById(filterVO.getId());
            Assert.assertTrue(filterVO.isEnabled(), "The filter must be enabled");
        }
    }

    /**
     * Test find all ip ranges
     *
     * @throws Exception
     *             the exception
     */
    @Test(dependsOnMethods = "testFilterEnabled")
    public void testFindIpRanges() throws Exception {
        AuthenticationTestUtils.setManagerContext();

        List<IpRange> includes = ipRangeFilterManagement.findIpRanges(ChannelType.WEB, true);
        List<IpRange> excludes = ipRangeFilterManagement.findIpRanges(ChannelType.WEB, false);

        Assert.assertTrue(includes.size() != 0, "The include list of ip ranges can not be null");
        Assert.assertTrue(excludes.size() != 0, "The exclude list of ip ranges can not be null");
    }

    /**
     * Test some ip addresses validation
     *
     */
    @Test
    public void testIpValidation() {
        String[] ips = new String[4];
        ips[0] = "127.0.0.1";
        ips[1] = "fe80:0:0:0:b136:8d30:bcf5:c2bf";
        ips[2] = "0:0:0:0:0:0:0:1";
        ips[3] = "::c000:9b00";

        for (String ip : ips) {
            boolean isValid = IpRangeHelper.validateIP(ip);
            Assert.assertTrue(isValid, "The ip " + ip + " is not valid");
        }

        ips[0] = "127.:.0.1";
        ips[1] = "fe80:q:0:0:b136:8d30:bcf5:c2bf";
        ips[2] = "0:0:0:0:0:0:1";
        ips[3] = "e.e.e.e.222.tt";

        for (String ip : ips) {
            boolean isValid = IpRangeHelper.validateIP(ip);
            Assert.assertFalse(isValid, "The invalide ip " + ip + " ");
        }
    }

    /**
     * Test list filter.
     *
     * @throws Exception
     *             the exception
     */
    @Test(dependsOnMethods = "testFilterChannelEnabled")
    public void testListFilter() throws Exception {
        AuthenticationTestUtils.setManagerContext();

        List<IpRangeFilterVO> filtersVO = ipRangeFilterManagement.listFilter();
        Assert.assertSame(filtersVO.size(), 4, "Four filters must be defined");
    }

    /**
     * Test whether an ip is in defined ranges
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testIpValidation", "testAvoidBlockingCurrentIp" })
    public void testRange() throws Exception {
        AuthenticationTestUtils.setManagerContext();

        // no blocking if no filters exist
        Assert.assertTrue(ipRangeFilterManagement.isInRange("127.0.0.1", ChannelType.WEB));
        Assert.assertTrue(ipRangeFilterManagement.isInRange("192.168.144.155", ChannelType.WEB));
        // include range
        IpRangeFilterVO vo1 = createFilter("filter1", "127.0.0.1,192.168.1.1-192.168.255.255", "",
                CURRENT_IP_V4, ChannelType.WEB);
        Assert.assertTrue(ipRangeFilterManagement.isInRange("127.0.0.1", ChannelType.WEB));
        Assert.assertTrue(ipRangeFilterManagement.isInRange("192.168.144.155", ChannelType.WEB));
        Assert.assertFalse(ipRangeFilterManagement.isInRange("192.167.144.155", ChannelType.WEB));
        removeFilter(vo1.getId());
        // exclude range
        IpRangeFilterVO vo2 = createFilter("filter2", "",
                "0.0.0.0-127.0.0.0,127.0.0.2-255.255.255.255", CURRENT_IP_V4, ChannelType.WEB);
        Assert.assertTrue(ipRangeFilterManagement.isInRange("127.0.0.1", ChannelType.WEB));
        Assert.assertFalse(ipRangeFilterManagement.isInRange("192.167.144.155", ChannelType.WEB));
        removeFilter(vo2.getId());
        // single include
        IpRangeFilterVO vo3 = createFilter("filter3", CURRENT_IP_V4, "", CURRENT_IP_V4,
                ChannelType.WEB);
        Assert.assertTrue(ipRangeFilterManagement.isInRange(CURRENT_IP_V4, ChannelType.WEB));
        Assert.assertFalse(ipRangeFilterManagement.isInRange("192.167.144.155", ChannelType.WEB));
        removeFilter(vo3.getId());
    }

    /**
     * Test remove filter.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = "testAvoidBlockingCurrentIpWhenRemoving")
    public void testRemoveFilter() throws Exception {
        AuthenticationTestUtils.setManagerContext();

        List<IpRangeFilterVO> filters = getFilters();
        Long id = null;
        for (IpRangeFilterVO filter : filters) {
            // do not remove filter granting access for current IP and also not the empty filter
            // which is needed later
            if (!filter.getName().equals(IP_FILTER_INCLUDING_CURRENT_IP)
                    && !filter.getName().equals(IP_FILTER_EMPTY)) {
                id = filter.getId();
                break;
            }
        }
        Assert.assertNotNull(id, "found no filter to remove.");
        ipRangeFilterManagement.removeFilter(id, CURRENT_IP_V4, WEB_CHANNEL);
        IpRangeFilterVO filterVO = ipRangeFilterManagement.findFilterById(id);
        Assert.assertNull(filterVO, "The filter for id " + id + " was not successfully removed");
    }

    /**
     * Tests updating a filter (the empty filter).
     *
     * @throws Exception
     *             in case the test fails
     */
    @Test(dependsOnMethods = "testRemoveFilter")
    public void testUpdateFilter() throws Exception {
        AuthenticationTestUtils.setManagerContext();

        List<IpRangeFilterVO> filters = getFilters();
        Long id = null;
        for (IpRangeFilterVO filter : filters) {
            if (filter.getName().equals(IP_FILTER_EMPTY)) {
                id = filter.getId();
            }
        }
        Assert.assertNotNull(id, "filter to update not found.");
        String newFilterName = "New Filtername";
        String includesPattern = "127.0.0.1-127.0.0.7";
        String excludesPattern = "127.0.0.8";
        ipRangeFilterManagement.updateFilter(id, newFilterName, includesPattern, excludesPattern,
                CURRENT_IP_V4, WEB_CHANNEL);

        filters = getFilters();
        Assert.assertEquals(filters.size(), 3, "number of filters changed.");
        for (IpRangeFilterVO filter : filters) {
            if (filter.getName().equals(IP_FILTER_EMPTY)) {
                Assert.fail("Filtername was not changed");
            } else if (filter.getName().equals(newFilterName)) {
                Assert.assertEquals(filter.getId(), id, "wrong filter was updated.");
                Assert.assertEquals(filter.getExcludes(), excludesPattern);
                Assert.assertEquals(filter.getIncludes(), includesPattern);
            }
        }
    }

}
