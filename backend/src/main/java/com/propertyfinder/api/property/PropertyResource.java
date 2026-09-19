package com.propertyfinder.api.property;

import com.propertyfinder.api.auth.JwtService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
import java.util.stream.Collectors;

@Path("/api") @Produces(MediaType.APPLICATION_JSON)
public class PropertyResource {
  @Inject JwtService jwt;
  @GET @Path("/properties") public Map<String,Object> properties(@QueryParam("north") double north,@QueryParam("south") double south,@QueryParam("east") double east,@QueryParam("west") double west) {
    List<PropertyEntity> rows=PropertyEntity.find("latitude between ?1 and ?2 and longitude between ?3 and ?4 and status = 'available'",south,north,west,east).list();
    return Map.of("properties", rows.stream().map(this::dto).toList(), "total", rows.size(), "viewport", Map.of("north",north,"south",south,"east",east,"west",west));
  }
  @GET @Path("/broker/dashboard") public Response dashboard(@HeaderParam("Authorization") String authorization) {
    JwtService.Claims claims=claims(authorization); if(claims==null || !"BROKER".equals(claims.role())) return Response.status(403).entity(Map.of("message","Broker authentication required")).build();
    List<PropertyEntity> rows=PropertyEntity.find("brokerId",claims.userId()).list(); long available=rows.stream().filter(p->"available".equals(p.status)).count();
    return Response.ok(Map.of("properties",rows.stream().map(this::dto).toList(),"summary",Map.of("totalListings",rows.size(),"availableListings",available,"rentedListings",rows.size()-available,"occupancyRate",rows.isEmpty()?0:((rows.size()-available)*100.0/rows.size()),"commissionEarned",rows.stream().mapToDouble(p->p.commissionEarned==null?0:p.commissionEarned.doubleValue()).sum(),"commissionMonths",0))).build();
  }
  private JwtService.Claims claims(String value) { return value!=null&&value.startsWith("Bearer ")?jwt.verify(value.substring(7)):null; }
  private Map<String,Object> dto(PropertyEntity p) { return Map.of("id",p.id,"title",p.title,"locality",p.locality,"rent",p.rent,"type",p.propertyType,"bedrooms",p.bedrooms==null?"":p.bedrooms,"amenities",p.amenities==null?List.of():List.of(p.amenities),"verified",p.verified,"latitude",p.latitude,"longitude",p.longitude,"status",p.status,"availableFrom",p.availableFrom==null?"":p.availableFrom.toString(),"negotiable",p.negotiable,"durationDays",p.durationDays,"commissionEarned",p.commissionEarned==null?0:p.commissionEarned); }
}
