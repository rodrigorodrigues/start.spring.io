package com.example.controller.jersey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Arrays;

/**
 * Created by rodrigo on 30/10/16.
 */
@Path("/cars")
public class CarController {
    @GET
    public Response cars() {
        return Response
                .ok(Arrays.asList("Ferrari", "BMW", "Volvo"))
                .build();
    }
}
