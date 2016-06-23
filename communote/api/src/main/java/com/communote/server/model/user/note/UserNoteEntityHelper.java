package com.communote.server.model.user.note;

/**
 * Helper class for the user note entity. It provides methods for converting the rank from a numeric
 * value in the range of 0..1 to an integer. The integer is then used for storing it in the databse.
 * The main reason for using an integer in databse instead of a floating point numeric is to provide
 * better support for indexing and filtering.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * 
 */
public final class UserNoteEntityHelper {

    /**
     * The rank precision is used to convert a normalized rank ( a value between 0 and 1) to an
     * integer by multiplicating it with this precision.
     */
    public static final int RANK_PRECISION = 100000;

    /**
     * 
     * @param rank
     *            the rank to check
     * @return true if the rank is in range
     */
    public static boolean checkValidRank(int rank) {
        return rank >= 0 && rank <= RANK_PRECISION;
    }

    /**
     * Converts the normalized rank to an integer
     * 
     * @param normalizedRank
     *            the normalized rank
     * @return the value transformed to an integer
     */
    public static int convertNormalizedRank(double normalizedRank) {
        int rank = (int) Math.floor(normalizedRank * RANK_PRECISION);
        // limit it to 0..RANK_PRECISION
        rank = Math.max(0, rank);
        rank = Math.min(RANK_PRECISION, rank);
        return rank;
    }

    /**
     * Convert the rank back to a normalized rank
     * 
     * @param rank
     *            the rank
     * @return the normalized rank as a value between 0..1
     */
    public static double convertToNormalizedRank(int rank) {
        double normalizedRank = (double) rank / RANK_PRECISION;
        // in case someone fooled with the database it may happen the result here is incorrect
        normalizedRank = Math.max(0, normalizedRank);
        normalizedRank = Math.min(1, normalizedRank);

        return normalizedRank;
    }

    /**
     * helper class, do not instantiate me
     */
    private UserNoteEntityHelper() {
        // i am a boring helper class and nobody will ever construct me
    }
}
