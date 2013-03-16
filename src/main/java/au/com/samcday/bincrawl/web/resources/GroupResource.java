package au.com.samcday.bincrawl.web.resources;

import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.bincrawl.services.CrawlService;
import au.com.samcday.bincrawl.web.entities.Admin;
import au.com.samcday.bincrawl.web.entities.Group;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import com.sun.jersey.api.ConflictException;
import com.sun.jersey.api.NotFoundException;
import com.yammer.dropwizard.auth.Auth;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.joda.time.DateTime;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/group")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupResource {
    @Context
    UriInfo uriInfo;

    private BetterJedisPool redisPool;
    private CouchDbConnector couchDb;
    private CrawlService crawlService;

    @Inject
    public GroupResource(BetterJedisPool redisPool, CouchDbConnector couchDb, CrawlService crawlService) {
        this.redisPool = redisPool;
        this.couchDb = couchDb;
        this.crawlService = crawlService;
    }

    @GET
    public Response list() {
        try(PooledJedis redisClient = this.redisPool.get()) {
            List<Group> groups = new ArrayList<>();
            for(String groupName : redisClient.smembers(RedisKeys.groups)) {
                groups.add(this.getGroup(redisClient, groupName));
            }

            return Response.ok(groups).build();
        }
    }

    @GET
    @Path("/{group}")
    public Response get(@PathParam("group") String groupName) {
        try(PooledJedis redisClient = this.redisPool.get()) {
            if(!redisClient.sismember(RedisKeys.groups, groupName)) {
                throw new NotFoundException();
            }
            Group group = this.getGroup(redisClient, groupName);

            return Response.ok(group).build();
        }
    }

    @POST
    public Response create(@Auth Admin admin, @Valid Group group) {
        try(PooledJedis redisClient = this.redisPool.get()) {
            if(redisClient.sismember(RedisKeys.groups, group.getName())) {
               throw new ConflictException("Group already exists.");
            }

            if(group.getMaxAge() != null) {
                redisClient.hmset(RedisKeys.group(group.getName()), ImmutableMap.<String, String>of(
                    RedisKeys.groupMaxAge, Integer.toString(group.getMaxAge())
                ));
            }

            redisClient.sadd(RedisKeys.groups, group.getName());

            this.crawlService.addNewGroup(group.getName());

            return Response.created(this.uriInfo.getAbsolutePathBuilder().path(group.getName()).build()).build();
        }
    }

    private Group getGroup(PooledJedis redisClient, String groupName) {
        Map<String, String> groupData = redisClient.hgetAll(RedisKeys.group(groupName));
        Group group = new Group();
        group.setName(groupName);
        group.setFirstPost(Longs.tryParse(Optional.fromNullable(groupData.get(RedisKeys.groupStart)).or("")));
        group.setLastPost(Longs.tryParse(Optional.fromNullable(groupData.get(RedisKeys.groupEnd)).or("")));
        if(groupData.get(RedisKeys.groupStartDate) != null) {
            group.setFirstPostDate(new DateTime(Long.parseLong(groupData.get(RedisKeys.groupStartDate))));
        }
        if(groupData.get(RedisKeys.groupEndDate) != null) {
            group.setLastPostDate(new DateTime(Long.parseLong(groupData.get(RedisKeys.groupEndDate))));
        }

        group.setNumBinaries(this.getGroupBinaryCount(groupName));
        group.setNumReleases(this.getGroupReleaseCount(groupName));

        return group;
    }

    private long getGroupBinaryCount(String groupName) {
        List<ViewResult.Row> rows = this.couchDb.queryView(new ViewQuery().designDocId("_design/bincrawl")
            .viewName("binaries").key(groupName).groupLevel(1)).getRows();
        if(rows.isEmpty()) return 0;
        return rows.get(0).getValueAsNode().path("sum").getValueAsLong();
    }

    private long getGroupReleaseCount(String groupName) {
        List<ViewResult.Row> rows = this.couchDb.queryView(new ViewQuery().designDocId("_design/bincrawl")
            .viewName("releases").key(groupName).groupLevel(1)).getRows();
        if(rows.isEmpty()) return 0;
        return rows.get(0).getValueAsNode().path("sum").getValueAsLong();
    }
}
