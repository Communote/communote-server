package com.communote.server.model.security;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class IpRange implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.security.IpRange}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.security.IpRange}.
         */
        public static com.communote.server.model.security.IpRange newInstance() {
            return new com.communote.server.model.security.IpRangeImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.security.IpRange}, taking
         * all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.security.IpRange newInstance(byte[] startValue,
                byte[] endValue, java.math.BigInteger start, java.math.BigInteger end) {
            final com.communote.server.model.security.IpRange entity = new com.communote.server.model.security.IpRangeImpl();
            entity.setStartValue(startValue);
            entity.setEndValue(endValue);
            entity.setStart(start);
            entity.setEnd(end);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.security.IpRange}, taking
         * all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.security.IpRange newInstance(byte[] startValue,
                byte[] endValue, java.math.BigInteger start, java.math.BigInteger end,
                String stringRepresentation) {
            final com.communote.server.model.security.IpRange entity = new com.communote.server.model.security.IpRangeImpl();
            entity.setStartValue(startValue);
            entity.setEndValue(endValue);
            entity.setStart(start);
            entity.setEnd(end);
            entity.setStringRepresentation(stringRepresentation);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6966604048121362564L;

    private byte[] startValue;

    private byte[] endValue;

    private java.math.BigInteger start;

    private java.math.BigInteger end;

    private String stringRepresentation;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("startValue='");
        sb.append(startValue);
        sb.append("', ");

        sb.append("endValue='");
        sb.append(endValue);
        sb.append("', ");

        sb.append("start='");
        sb.append(start);
        sb.append("', ");

        sb.append("end='");
        sb.append(end);
        sb.append("', ");

        sb.append("stringRepresentation='");
        sb.append(stringRepresentation);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an IpRange instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof IpRange)) {
            return false;
        }
        final IpRange that = (IpRange) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public java.math.BigInteger getEnd() {
        return this.end;
    }

    /**
     * 
     */
    public byte[] getEndValue() {
        return this.endValue;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * 
     */
    public java.math.BigInteger getStart() {
        return this.start;
    }

    /**
     * 
     */
    public byte[] getStartValue() {
        return this.startValue;
    }

    /**
     * <p>
     * The string representation of the IP range as it was used during creation / update of the
     * range.
     * </p>
     */
    public String getStringRepresentation() {
        return this.stringRepresentation;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    /**
     * 
     */
    public abstract boolean isInRange(java.math.BigInteger ip);

    public void setEnd(java.math.BigInteger end) {
        this.end = end;
    }

    public void setEndValue(byte[] endValue) {
        this.endValue = endValue;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStart(java.math.BigInteger start) {
        this.start = start;
    }

    public void setStartValue(byte[] startValue) {
        this.startValue = startValue;
    }

    public void setStringRepresentation(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }
}