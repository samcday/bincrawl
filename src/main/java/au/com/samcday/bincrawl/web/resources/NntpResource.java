package au.com.samcday.bincrawl.web.resources;


import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.NntpClientPool;
import au.com.samcday.bincrawl.services.CrawlService;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/_nntp")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class NntpResource {
    private CrawlService crawlService;
    private NntpClientPool nntpClientPool;

    @Inject
    public NntpResource(CrawlService crawlService, NntpClientPool nntpClientPool) {
        this.crawlService = crawlService;
        this.nntpClientPool = nntpClientPool;
    }

    @GET
    @Path("/max_connections")
    public String getMaxConnections() {
        return Integer.toString(this.nntpClientPool.getMaxActive());
    }

    @PUT
    @Path("/max_connections")
    public void setMaxConnections(String newMax) {
        Integer newMaxInt = Ints.tryParse(newMax);
        if(newMaxInt == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        this.crawlService.setMaxConnections(newMaxInt);
        this.nntpClientPool.setMaxActive(newMaxInt);
    }

    @GET
    @Path("/current_connections")
    public String getCurrentConnections() {
        return Integer.toString(this.nntpClientPool.getNumActive());
    }
}
