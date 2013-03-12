package au.com.samcday.bincrawl;

/**
 * TODO: should be putting a lot of the small discrete domain of key names here in flyweights.
 */
public class RedisKeys {
    public static final String binary(String hash) {
        return "b:" + hash;
    }
    public static final String binaryPart(int num) {
        return "p: " + num;
    }
    public static final String binaryGroup = "g";
    public static final String binarySubject = "s";
    public static final String binaryTotalParts = "n";
    public static final String binaryDate = "dt";
    public static final String binaryDone = "d";
    public static final String binaryRelease = "r";
    public static final String binaryReleaseNum = "rn";

    public static final String binaryProcess = "binaryProcess";
    public static final String binaryProcessFailed = "binaryProcessFailed";
    public static final String binaryComplete = "binaryComplete";

    public static final String groups = "groups";

    public static final String group(String group) {
        return "group:" + group;
    }
    public static final String groupEnd = "end";
    public static final String groupFirst = "first";
    public static final String groupMaxAge = "maxAge";

    public static final String missing(String group) {
        return "missing:" + group;
    }
}
