package com.propertyfinder.api.config;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/api/config") @Produces(MediaType.APPLICATION_JSON)
public class AppConfigResource {
  @GET @Path("/map")
  public Response mapConfig() {
    Map<String,String> values = new LinkedHashMap<>();
    AppConfigEntity.<AppConfigEntity>listAll().forEach(item -> values.put(item.key, item.value));
    return Response.ok(Map.of("provider", values.getOrDefault("map.provider", "openstreetmap"),
        "tileUrl", values.getOrDefault("map.tile_url", ""),
        "attribution", values.getOrDefault("map.attribution", ""),
        "apiKey", values.getOrDefault("map.api_key", ""))).build();
  }
}
