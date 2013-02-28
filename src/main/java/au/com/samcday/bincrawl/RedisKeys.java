package au.com.samcday.bincrawl;

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
}
