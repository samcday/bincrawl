package au.com.samcday.bincrawl.web.entities;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

public class Group {
    @NotEmpty
    private String name;
    private Integer firstPost;
    private DateTime firstPostDate;
    private Integer lastPost;
    private DateTime lastPostDate;
    private Integer maxAge;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFirstPost() {
        return firstPost;
    }

    public void setFirstPost(Integer firstPost) {
        this.firstPost = firstPost;
    }

    public DateTime getFirstPostDate() {
        return firstPostDate;
    }

    public void setFirstPostDate(DateTime firstPostDate) {
        this.firstPostDate = firstPostDate;
    }

    public Integer getLastPost() {
        return lastPost;
    }

    public void setLastPost(Integer lastPost) {
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
}
