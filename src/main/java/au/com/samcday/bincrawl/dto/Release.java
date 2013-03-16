package au.com.samcday.bincrawl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.joda.time.DateTime;

import java.util.List;

public class Release {
    private static final HashFunction SHA1 = Hashing.sha1();

    @JsonProperty("_id")
    @org.codehaus.jackson.annotate.JsonProperty("_id")
    private String id;

    @org.codehaus.jackson.annotate.JsonProperty("_rev")
    private String rev;

    private DateTime date;
    private String name;
    private int count;
    private List<ReleaseBinary> binaries;

    public static String buildId(String name) {
        return SHA1.hashString(name).toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public List<ReleaseBinary> getBinaries() {
        return binaries;
    }

    public void setBinaries(List<ReleaseBinary> binaries) {
        this.binaries = binaries;
    }

    public void addBinary(ReleaseBinary binary, int num) {
        this.binaries.set(num, binary);
    }
}
