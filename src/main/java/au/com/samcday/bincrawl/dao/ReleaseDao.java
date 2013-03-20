package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dto.Release;

import java.util.List;

public interface ReleaseDao {
    /**
     * Adds a completed binary to a new or existing Release.
     * @return
     */
    public Release createRelease(List<String> groups);
    public void addCompletedBinary(Binary binary);
}
