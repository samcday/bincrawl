package au.com.samcday.bincrawl.dao.entities;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;

import java.util.List;

public class Binary {
    private String binaryHash;
    private String subject;
    private int totalParts;
    private DateTime date;
    private List<BinaryPart> parts;

    public String getBinaryHash() {
        return binaryHash;
    }

    public void setBinaryHash(String binaryHash) {
        this.binaryHash = binaryHash;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getTotalParts() {
        return totalParts;
    }

    public void setTotalParts(int totalParts) {
        this.totalParts = totalParts;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public List<BinaryPart> getParts() {
        return ImmutableList.copyOf(this.parts);
    }

    public void setParts(List<BinaryPart> parts) {
        this.parts = parts;
    }
}
