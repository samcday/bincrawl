package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.BinaryClassifier;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dao.entities.Release;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.ektorp.CouchDbConnector;
import org.ektorp.http.RestTemplate;
import org.ektorp.http.URI;
import org.joda.time.DateTime;

import java.util.List;

public class ReleaseDaoCouchImpl implements ReleaseDao {
    private CouchDbConnector couchDb;
    private ObjectMapper objectMapper;
    private BinaryClassifier binaryClassifier;

    @Inject
    public ReleaseDaoCouchImpl(CouchDbConnector couchDb, ObjectMapper objectMapper, BinaryClassifier binaryClassifier) {
        this.couchDb = couchDb;
        this.objectMapper = objectMapper;
        this.binaryClassifier = binaryClassifier;
    }

    @Override
    public String createReleaseFromBinaries(List<Binary> binary) {
        Binary reference = binary.get(0);
        BinaryClassifier.Classification classification = this.binaryClassifier.classify(reference.getGroup(), reference.getSubject());
        String releaseId = Release.buildId(reference.getGroup(), classification.name);

        Release newRelease = new Release();
        newRelease.setName(classification.name);
        newRelease.setCrawledDate(new DateTime());
        newRelease.setBinaries(binary);

        this.couchDb.create(releaseId, newRelease);

        return releaseId;
    }

    /*@Override
    public Release createRelease(String group, BinaryClassifier.Classification classification) {
        while(true) {

            Release release = this.couchDb.find(Release.class, releaseId);
            if(release == null) {
                release = new Release();
                release.setId(releaseId);
                release.setName(classification.name);
                release.setCount(classification.totalParts);
                release.setCrawledDate(new DateTime());
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
    }*/

    /**
     * Ektorp CouchDbConnector supports update handlers, but doesn't let you send a body.
     */
    private void executeBetterUpdateHandler(String releaseId, Object body) {
        URI uri = URI.of(this.couchDb.path()).append("_design/bincrawl").append("_update").append("addbinary")
            .append(releaseId);

        try {
            new RestTemplate(this.couchDb.getConnection()).put(uri.toString(), this.objectMapper.writeValueAsString(body));
        }
        catch(JsonProcessingException jpe) {
            throw new RuntimeException(jpe);
        }
    }
}
