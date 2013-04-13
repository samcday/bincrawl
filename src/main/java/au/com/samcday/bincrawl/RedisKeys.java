package au.com.samcday.bincrawl;

/**
 * TODO: should be putting a lot of the small discrete domain of key names here in flyweights.
 */
public class RedisKeys {
    public static String releaseNfo = "releaseNfo";
    public static String tvRage = "tvr";

    public static String releaseBinaries(String releaseId) {
        return "releaseBinaries:" + releaseId;
    }

    public static String releaseComplete = "releaseComplete";
    public static String releaseCompleteRetry = "releaseCompleteRetry";

    public static final String binary(String hash) {
        return "b:" + hash;
    }
    public static final String binaryPart(int num) {
        return "p:" + num;
    }
    public static final String binarySubject = "s";
    public static final String binaryTotalParts = "n";
    public static final String binaryDate = "dt";
    public static final String binaryGroup = "g";
    public static final String binaryDone = "d";
    public static final String binaryRelease = "r";
    public static final String binaryReleaseNum = "rn";

    public static final String binaryComplete = "binaryComplete";

    public static final String groups = "groups";

    public static final String group(String group) {
        return "group:" + group;
    }
    public static final String groupEnd = "end";
    public static final String groupEndDate = "endDate";
    public static final String groupStart = "start";
    public static final String groupStartDate = "startDate";
    public static final String groupMaxAge = "maxAge";

    public static final String missing(String group) {
        return "missing:" + group;
    }
}
