package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.BinaryClassifier;
import au.com.samcday.bincrawl.dto.Release;
import org.ektorp.CouchDbConnector;
import org.ektorp.UpdateConflictException;

public class ReleaseDaoCouchImpl implements ReleaseDao {
    private CouchDbConnector couchDb;

    public ReleaseDaoCouchImpl(CouchDbConnector couchDb) {
        this.couchDb = couchDb;
    }

    @Override
    public Release createRelease(BinaryClassifier.Classification classification) {
        while(true) {
            Release release = this.couchDb.find(Release.class, Release.buildId(classification.name));
            if(release == null) {
                release = new Release();
                release.setId(Release.buildId(classification.name));
                release.setName(classification.name);
                release.setCount(classification.totalParts);
                try {
                    this.couchDb.create(release);
                }
                catch(UpdateConflictException uce) {
                    // Ignore, on next loop we'll correctly find the document.
                }
            }
            else if(release.getCount() < classification.totalParts) {
                release.setCount(classification.totalParts);
                this.couchDb.update(release);
            }
            return release;
        }
    }
}
