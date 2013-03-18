package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.BinaryClassifier;
import au.com.samcday.bincrawl.dto.Release;

public interface ReleaseDao {
    public Release createRelease(BinaryClassifier.Classification classification);
}
