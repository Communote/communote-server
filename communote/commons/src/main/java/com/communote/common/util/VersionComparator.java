package com.communote.common.util;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * Comparator to compare versions in dot notation like 3.25.2 as used in semantic versioning. The
 * last part can be interpreted as revision.<br>
 * Examples
 * <ul>
 * <li>compare(1.0, 1.0.1) returns a value &lt; 0</li>
 * <li>compare(1.0.1, 1.0.1) returns 0</li>
 * <li>compare(1.0.1, 1.0) returns a value &gt; 0</li>
 * </ul>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class VersionComparator implements Comparator<String> {

    private boolean versionEndsWithRevision;
    private boolean numericIncreasingRevision;

    /**
     * Default constructor which creates a comparator that is not revision aware.
     */
    public VersionComparator() {
        // Do nothing.
    }

    /**
     * Constructor to fine-tune the handling of a revision in the version string
     *
     * @param versionEndsWithRevision
     *            If true, the last part of the version is always interpreted as revision
     * @param numericIncreasingRevision 
     *            if versionEndsWithRevision is true, this parameter defines whether the revision
     *            is a numeric value which increases with every new revision. Only in this case
     *            the revision is compared. 
     * @since 3.5
     */
    public VersionComparator(boolean versionEndsWithRevision, boolean numericIncreasingRevision) {
        this.versionEndsWithRevision = versionEndsWithRevision;
        this.numericIncreasingRevision = numericIncreasingRevision;
    }

    /**
     * Compares the version.
     *
     * @param version1
     *            the version1 (e.g. 1.0.1)
     * @param version2
     *            the version2 (e.g 1.0)
     * @return a value greater 0 if version1 &gt; version2<br>
     *         0 if version1 == version2<br>
     *         a value less 0 if version1 &lt; version2<br>
     */
    @Override
    public int compare(String version1, String version2) {
        Object[] v1 = version1.split("\\.");
        Object[] v2 = version2.split("\\.");
        Object revisionV1 = null;
        Object revisionV2 = null;
        if (versionEndsWithRevision) {
            revisionV1 = v1[v1.length - 1];
            revisionV2 = v2[v2.length - 1];
            v1 = ArrayUtils.remove(v1, v1.length - 1);
            v2 = ArrayUtils.remove(v2, v2.length - 1);
        }
        if (v1.length != v2.length) {
            Object[] filler = new Object[Math.abs(v1.length - v2.length)];
            Arrays.fill(filler, "0");
            if (v1.length > v2.length) {
                v2 = ArrayUtils.addAll(v2, filler);
            } else {
                v1 = ArrayUtils.addAll(v1, filler);
            }
        }
        if (versionEndsWithRevision && numericIncreasingRevision) {
            v1 = ArrayUtils.add(v1, revisionV1);
            v2 = ArrayUtils.add(v2, revisionV2);
        }

        for (int i = 0; i < v1.length && i < v2.length; i++) {
            int v1i = NumberUtils.toInt(v1[i].toString(), -1);
            int v2i = NumberUtils.toInt(v2[i].toString(), -1);
            int diff = v1i - v2i;
            if (diff != 0) {
                return diff;
            }
        }

        // if the versions are still equal, the one which is larger will win (e.g. 1.0.1 > 1.0)
        return v1.length - v2.length;
    }

}
