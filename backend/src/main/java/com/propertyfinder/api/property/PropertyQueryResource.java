package com.propertyfinder.api.property;

import com.propertyfinder.api.auth.JwtService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Path("/api") @Produces(MediaType.APPLICATION_JSON)
public class PropertyQueryResource {
  @Inject JwtService jwt;

  @GET @Path("/search/properties")
  public Response search(@QueryParam("q") String q,@QueryParam("locality") String locality,@QueryParam("minRent") BigDecimal minRent,@QueryParam("maxRent") BigDecimal maxRent,@QueryParam("bedrooms") String bedrooms,@QueryParam("status") @DefaultValue("available") String status,@QueryParam("north") Double north,@QueryParam("south") Double south,@QueryParam("east") Double east,@QueryParam("west") Double west,@QueryParam("page") @DefaultValue("0") int page,@QueryParam("size") @DefaultValue("50") int size) {
    if(size<1||size>100||page<0)return Response.status(400).entity(Map.of("message","page must be >= 0 and size must be between 1 and 100")).build();
    StringBuilder query=new StringBuilder("1=1"); List<Object> args=new ArrayList<>();
    if(q!=null&&!q.isBlank()){query.append(" and (lower(title) like ?%d or lower(locality) like ?%d or lower(address) like ?%d)".formatted(args.size()+1,args.size()+2,args.size()+3));String term="%"+q.trim().toLowerCase()+"%";args.add(term);args.add(term);args.add(term);}
    if(locality!=null&&!locality.isBlank()){query.append(" and lower(locality) = ?").append(args.size()+1);args.add(locality.trim().toLowerCase());}
    if(minRent!=null&&minRent.signum()>=0){query.append(" and rent >= ?").append(args.size()+1);args.add(minRent);}
    if(maxRent!=null&&maxRent.signum()>=0){query.append(" and rent <= ?").append(args.size()+1);args.add(maxRent);}
    if(bedrooms!=null&&!bedrooms.isBlank()){query.append(" and lower(bedrooms) = ?").append(args.size()+1);args.add(bedrooms.trim().toLowerCase());}
    if(status!=null&&!status.isBlank()){query.append(" and status = ?").append(args.size()+1);args.add(status.toLowerCase());}
    if(north!=null&&south!=null&&east!=null&&west!=null){query.append(" and latitude between ?").append(args.size()+1).append(" and ?").append(args.size()+2).append(" and longitude between ?").append(args.size()+3).append(" and ?").append(args.size()+4);args.add(south);args.add(north);args.add(west);args.add(east);}
    var panache=PropertyEntity.find(query.toString(),args.toArray()); long total=panache.count(); List<PropertyEntity> rows=panache.page(page,size).list(); return Response.ok(Map.of("properties",rows.stream().map(this::dto).toList(),"total",total,"page",page,"size",size)).build();
  }

  @GET @Path("/localities") public Response localities(@QueryParam("q") String q,@QueryParam("limit") @DefaultValue("10") int limit){if(limit<1||limit>50)return Response.status(400).entity(Map.of("message","limit must be between 1 and 50")).build();String term=q==null?"":q.trim().toLowerCase();List<String> values=PropertyEntity.getEntityManager().createQuery("select distinct p.locality from PropertyEntity p where lower(p.locality) like :q order by p.locality",String.class).setParameter("q","%"+term+"%").setMaxResults(limit).getResultList();return Response.ok(Map.of("localities",values)).build();}

  @POST @Path("/broker/properties") @Consumes(MediaType.APPLICATION_JSON) @Transactional public Response create(PropertyEntity input,@HeaderParam("Authorization") String auth){JwtService.Claims c=broker(auth);if(c==null)return forbidden();if(input==null||blank(input.title)||blank(input.locality)||input.rent==null||input.rent.signum()<0||input.latitude<-90||input.latitude>90||input.longitude<-180||input.longitude>180)return Response.status(400).entity(Map.of("message","title, locality, non-negative rent, and valid coordinates are required")).build();input.id=null;input.brokerId=c.userId();input.status="available";input.verified=false;if(input.amenities==null)input.amenities=new String[0];input.persist();return Response.status(201).entity(dto(input)).build();}
  @PATCH @Path("/broker/properties/{id}/status") @Consumes(MediaType.APPLICATION_JSON) @Transactional public Response status(@PathParam("id") long id,Map<String,String> body,@HeaderParam("Authorization") String auth){JwtService.Claims c=broker(auth);PropertyEntity p=PropertyEntity.find("id = ?1 and brokerId = ?2",id,c==null?-1:c.userId()).firstResult();if(c==null)return forbidden();if(p==null)return Response.status(404).entity(Map.of("message","Property not found")).build();String status=body==null?null:body.get("status");if(!"available".equals(status)&&!"rented".equals(status))return Response.status(400).entity(Map.of("message","status must be available or rented")).build();p.status=status;p.rentedOn="rented".equals(status)?LocalDate.now():null;return Response.ok(dto(p)).build();}
  @DELETE @Path("/broker/properties/{id}") @Transactional public Response delete(@PathParam("id") long id,@HeaderParam("Authorization") String auth){JwtService.Claims c=broker(auth);if(c==null)return forbidden();PropertyEntity p=PropertyEntity.find("id = ?1 and brokerId = ?2",id,c.userId()).firstResult();if(p==null)return Response.status(404).entity(Map.of("message","Property not found")).build();p.delete();return Response.ok(Map.of("message","Property deleted")).build();}
  private JwtService.Claims broker(String h){JwtService.Claims c=h!=null&&h.startsWith("Bearer ")?jwt.verify(h.substring(7)):null;return c!=null&&"BROKER".equals(c.role())?c:null;}private Response forbidden(){return Response.status(403).entity(Map.of("message","Broker authentication required")).build();}private boolean blank(String s){return s==null||s.isBlank();}
  private Map<String,Object> dto(PropertyEntity p){Map<String,Object> m=new LinkedHashMap<>();m.put("id",p.id);m.put("title",p.title);m.put("locality",p.locality);m.put("address",p.address);m.put("rent",p.rent);m.put("type",p.propertyType);m.put("bedrooms",p.bedrooms==null?"":p.bedrooms);m.put("amenities",p.amenities==null?List.of():Arrays.asList(p.amenities));m.put("verified",p.verified);m.put("latitude",p.latitude);m.put("longitude",p.longitude);m.put("status",p.status);m.put("availableFrom",p.availableFrom==null?"":p.availableFrom.toString());m.put("rentedOn",p.rentedOn==null?null:p.rentedOn.toString());m.put("negotiable",p.negotiable);m.put("durationDays",p.durationDays);m.put("commissionEarned",p.commissionEarned==null?BigDecimal.ZERO:p.commissionEarned);return m;}
}
