package au.com.samcday.bincrawl.web.resources;

import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.bincrawl.services.CrawlService;
import au.com.samcday.bincrawl.web.entities.Group;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.sun.jersey.api.ConflictException;
import com.sun.jersey.api.NotFoundException;

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
    private CrawlService crawlService;

    @Inject
    public GroupResource(BetterJedisPool redisPool, CrawlService crawlService) {
        this.redisPool = redisPool;
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
    public Response create(@Valid Group group) {
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
        group.setLastPost(Ints.tryParse(Optional.fromNullable(groupData.get(RedisKeys.groupEnd)).or("")));
        return group;
    }
}
