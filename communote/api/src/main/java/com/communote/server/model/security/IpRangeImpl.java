package com.communote.server.model.security;

import java.math.BigInteger;

/**
 * The Class IpRangeImpl.
 *
 * @see com.communote.server.model.security.IpRange
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeImpl extends com.communote.server.model.security.IpRange {

    /** The serial version UID of this class. Needed for serialization. */
    private static final long serialVersionUID = -8015889960373452254L;

    /**
     * Checks if is in range.
     *
     * @param ip
     *            the ip
     * @return true, if checks if is in range
     * @see com.communote.server.model.security.IpRange#isInRange(java.math.BigInteger)
     */
    @Override
    public boolean isInRange(java.math.BigInteger ip) {
        if (ip == null) {
            throw new IllegalArgumentException("ip can not be null");
        }
        if (getStart() != null && getEnd() != null) {
            return ip.compareTo(getStart()) >= 0 && ip.compareTo(getEnd()) <= 0;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.model.security.IpRange#setEnd(java.math.BigInteger)
     */
    @Override
    public void setEnd(BigInteger end) {
        if (end == null) {
            throw new IllegalArgumentException("end can not be null");
        }
        super.setEnd(end);
        super.setEndValue(end.toByteArray());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.model.security.IpRange#setEndValue(byte[])
     */
    @Override
    public void setEndValue(byte[] endValue) {
        if (endValue == null) {
            throw new IllegalArgumentException("end value can not be null");
        }
        super.setEndValue(endValue);
        super.setEnd(new BigInteger(endValue));
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.model.security.IpRange#setStart(java.math.BigInteger)
     */
    @Override
    public void setStart(BigInteger start) {
        if (start == null) {
            throw new IllegalArgumentException("start can not be null");
        }
        super.setStart(start);
        super.setStartValue(start.toByteArray());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.model.security.IpRange#setStartValue(byte[])
     */
    @Override
    public void setStartValue(byte[] startValue) {
        if (startValue == null) {
            throw new IllegalArgumentException("start value can not be null");
        }
        super.setStartValue(startValue);
        super.setStart(new BigInteger(startValue));
    }
}
