package au.com.samcday.bincrawl.dto;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.List;

public class ReleaseBinary {
    public static final Comparator<ReleaseBinary> COMPARATOR  = new Comparator<ReleaseBinary>() {
        @Override
        public int compare(ReleaseBinary o1, ReleaseBinary o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.getSubject(), o2.getSubject());
        }
    };
    private String binaryHash;
    private String group;
    private String poster;
    private DateTime date;
    private String subject;
    private List<BinarySegment> binarySegments;

    public String getBinaryHash() {
        return binaryHash;
    }

    public void setBinaryHash(String binaryHash) {
        this.binaryHash = binaryHash;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<BinarySegment> getBinarySegments() {
        return binarySegments;
    }

    public void setBinarySegments(List<BinarySegment> binarySegments) {
        this.binarySegments = binarySegments;
    }
}
