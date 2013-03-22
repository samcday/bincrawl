package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.BinaryClassifier;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dto.Release;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.ektorp.CouchDbConnector;
import org.ektorp.UpdateConflictException;
import org.ektorp.http.RestTemplate;
import org.ektorp.http.URI;
import org.joda.time.DateTime;

public class ReleaseDaoCouchImpl implements ReleaseDao {
    private CouchDbConnector couchDb;
    private ObjectMapper objectMapper;

    @Inject
    public ReleaseDaoCouchImpl(CouchDbConnector couchDb, ObjectMapper objectMapper) {
        this.couchDb = couchDb;
        this.objectMapper = objectMapper;
    }

    @Override
    public Release createRelease(String group, BinaryClassifier.Classification classification) {
        while(true) {
            String releaseId = Release.buildId(group, classification.name);
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
    }

    @Override
    public void addCompletedBinary(String releaseId, Binary binary) {
        this.executeBetterUpdateHandler(releaseId, binary);
    }

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
