package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.BinaryClassifier;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dto.Release;

public interface ReleaseDao {
    /**
     * Adds a completed binary to a new or existing Release.
     * @return
     */
    public Release addCompletedBinary(String group, BinaryClassifier.Classification classification, Binary binary);
}
