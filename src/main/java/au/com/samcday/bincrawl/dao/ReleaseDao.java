package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.dao.entities.Binary;

import java.util.List;

public interface ReleaseDao {
    /**
     * Adds a completed binary to a new release.
     * @return
     */
    public String createReleaseFromBinaries(List<Binary> binaries);
}
