package au.com.samcday.bincrawl.web.entities;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

public class Group {
    @NotEmpty
    private String name;
    private Long firstPost;
    private DateTime firstPostDate;
    private Long lastPost;
    private DateTime lastPostDate;
    private Integer maxAge;
    private Long numBinaries;
    private Long numReleases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFirstPost() {
        return firstPost;
    }

    public void setFirstPost(Long firstPost) {
        this.firstPost = firstPost;
    }

    public DateTime getFirstPostDate() {
        return firstPostDate;
    }

    public void setFirstPostDate(DateTime firstPostDate) {
        this.firstPostDate = firstPostDate;
    }

    public Long getLastPost() {
        return lastPost;
    }

    public void setLastPost(Long lastPost) {
        this.lastPost = lastPost;
    }

    public DateTime getLastPostDate() {
        return lastPostDate;
    }

    public void setLastPostDate(DateTime lastPostDate) {
        this.lastPostDate = lastPostDate;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public Long getNumBinaries() {
        return numBinaries;
    }

    public void setNumBinaries(Long numBinaries) {
        this.numBinaries = numBinaries;
    }

    public Long getNumReleases() {
        return numReleases;
    }

    public void setNumReleases(Long numReleases) {
        this.numReleases = numReleases;
    }
}
