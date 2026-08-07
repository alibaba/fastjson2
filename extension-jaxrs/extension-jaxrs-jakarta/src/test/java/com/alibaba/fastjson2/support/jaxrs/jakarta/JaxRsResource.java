package com.alibaba.fastjson2.support.jaxrs.jakarta;

import com.alibaba.fastjson2.support.jaxrs.jakarta.model.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * @author 张治保
 * @since 2024/10/16
 */
@Path("/test")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JaxRsResource {
    @GET
    @Path("/get")
    public User get() {
        return new User().setName("fastjson2");
    }

    @GET
    @Path("/get/{name}")
    public User getPath(@PathParam("name") String name) {
        return new User().setName(name);
    }

    @POST
    @Path("/post")
    public User post(User user) {
        return user.setAge(user.getAge())
                .setName(user.getName());
    }
}
