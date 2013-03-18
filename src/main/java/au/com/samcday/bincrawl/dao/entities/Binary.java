package au.com.samcday.bincrawl.dao.entities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public class Binary {
    private String binaryHash;
    private String releaseId;
    private String subject;
    private int totalParts;
    private DateTime date;
    private Set<String> groups;
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

    public Set<String> getGroups() {
        return ImmutableSet.copyOf(groups);
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }
}
