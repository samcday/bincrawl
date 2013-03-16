package au.com.samcday.bincrawl.web.resources;

import au.com.samcday.bincrawl.web.entities.Admin;
import com.yammer.dropwizard.auth.Auth;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/")
public class RootResource {
    @GET
    public Response get(@Auth(required = false)Admin admin, @HeaderParam("Authorization") String authorization) {
        if(admin == null && authorization != null) {
            throw new WebApplicationException(401);
        }
        return Response.ok().build();
    }
}
