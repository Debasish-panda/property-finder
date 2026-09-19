package com.propertyfinder.api.billing;

import com.propertyfinder.api.auth.JwtService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONObject;
import com.razorpay.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Path("/api") @Produces(MediaType.APPLICATION_JSON)
public class BillingResource {
  @Inject JwtService jwt;
  @ConfigProperty(name="razorpay.key-id") String keyId;
  @ConfigProperty(name="razorpay.key-secret") String keySecret;
  @ConfigProperty(name="razorpay.webhook-secret") String webhookSecret;
  @ConfigProperty(name="razorpay.enabled") boolean razorpayEnabled;

  @GET @Path("/plans") public Response plans(){return Response.ok(Map.of("plans",PlanEntity.<PlanEntity>list("enabled",true).stream().map(this::planDto).toList(),"currency","INR")).build();}

  @POST @Path("/billing/orders") @Consumes(MediaType.APPLICATION_JSON) @Transactional
  public Response createOrder(Map<String,Object> body,@HeaderParam("Authorization") String header){
    JwtService.Claims claims=claims(header); if(claims==null)return unauthorized(); if(!"BROKER".equals(claims.role()))return forbidden();
    String code=body==null?null:String.valueOf(body.get("planCode")); PlanEntity plan=PlanEntity.find("code = ?1 and enabled = true",code).firstResult(); if(plan==null)return bad("Unknown or disabled plan");
    String method=body.get("paymentMethod")==null?"all":String.valueOf(body.get("paymentMethod")); if(!Set.of("all","card","upi","netbanking","wallet").contains(method))return bad("Unsupported payment method");
    if(!razorpayEnabled||keyId.isBlank()||keySecret.isBlank())return Response.status(503).entity(Map.of("message","Payment gateway is not configured")).build();
    try { JSONObject options=new JSONObject().put("amount",plan.amountPaise).put("currency",plan.currency).put("receipt","pf_"+UUID.randomUUID()).put("notes",new JSONObject().put("planCode",plan.code).put("userId",claims.userId())); Order order=new RazorpayClient(keyId,keySecret).orders.create(options);
      SubscriptionEntity sub=new SubscriptionEntity();sub.id=UUID.randomUUID();sub.userId=claims.userId();sub.planId=plan.id;sub.status=SubscriptionEntity.Status.PENDING;sub.persist(); PaymentOrderEntity payment=new PaymentOrderEntity();payment.id=UUID.randomUUID();payment.subscriptionId=sub.id;payment.userId=claims.userId();payment.planId=plan.id;payment.gateway="RAZORPAY";payment.gatewayOrderId=order.get("id");payment.amountPaise=plan.amountPaise;payment.currency=plan.currency;payment.status="CREATED";payment.paymentMethod=method;payment.persist();
      return Response.status(201).entity(Map.of("orderId",payment.gatewayOrderId,"amount",plan.amountPaise,"currency",plan.currency,"keyId",keyId,"planCode",plan.code,"paymentMethod",method)).build();
    } catch(Exception e){return bad("Unable to create payment order");}
  }

  @POST @Path("/billing/payments/verify") @Consumes(MediaType.APPLICATION_JSON) @Transactional
  public Response verify(Map<String,String> body,@HeaderParam("Authorization") String header){JwtService.Claims c=claims(header);if(c==null)return unauthorized();String orderId=body==null?null:body.get("razorpayOrderId"),paymentId=body==null?null:body.get("razorpayPaymentId"),signature=body==null?null:body.get("razorpaySignature");PaymentOrderEntity order=PaymentOrderEntity.find("gatewayOrderId = ?1 and userId = ?2",orderId,c.userId()).firstResult();if(order==null)return bad("Payment order not found");if(!constantEquals(signature,hmac(orderId+"|"+paymentId,keySecret)))return bad("Payment signature verification failed");markPaid(order,paymentId,signature);return Response.ok(Map.of("message","Payment verified","status","PAID")).build();}

  @POST @Path("/billing/webhooks/razorpay") @Consumes(MediaType.TEXT_PLAIN) @Transactional
  public Response webhook(String payload,@HeaderParam("X-Razorpay-Signature") String signature){if(webhookSecret==null||webhookSecret.isBlank()||!constantEquals(signature,hmac(payload,webhookSecret)))return Response.status(400).build();try{JSONObject event=new JSONObject(payload);if("payment.captured".equals(event.optString("event"))){JSONObject p=event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");String orderId=p.optString("order_id"),paymentId=p.optString("id");PaymentOrderEntity order=PaymentOrderEntity.find("gatewayOrderId",orderId).firstResult();if(order!=null)markPaid(order,paymentId,signature);}return Response.ok().build();}catch(Exception e){return Response.status(400).build();}}

  @GET @Path("/billing/subscription") public Response subscription(@HeaderParam("Authorization") String header){JwtService.Claims c=claims(header);if(c==null)return unauthorized();SubscriptionEntity sub=SubscriptionEntity.find("userId = ?1 and status = 'ACTIVE' order by expiresAt desc",c.userId()).firstResult();if(sub==null)return Response.ok(Map.of("active",false)).build();if(sub.expiresAt.isBefore(Instant.now())){sub.status=SubscriptionEntity.Status.EXPIRED;return Response.ok(Map.of("active",false)).build();}PlanEntity plan=PlanEntity.findById(sub.planId);return Response.ok(Map.of("active",true,"plan",planDto(plan),"startsAt",sub.startsAt,"expiresAt",sub.expiresAt)).build();}
  @GET @Path("/billing/usage") public Response usage(@HeaderParam("Authorization") String header){JwtService.Claims c=claims(header);if(c==null)return unauthorized();PlanEntity plan=activePlan(c.userId());UsageCounterEntity counter=UsageCounterEntity.find("userId = ?1 and usageDate = ?2",c.userId(),LocalDate.now()).firstResult();int used=counter==null?0:counter.propertyLoads;return Response.ok(Map.of("dailyLoadsUsed",used,"dailyLoadLimit",plan==null?0:plan.dailyLoadLimit,"remainingLoads",plan==null?0:Math.max(0,plan.dailyLoadLimit-used))).build();}
  @POST @Path("/billing/usage/property-load") @Transactional public Response consumeLoad(@HeaderParam("Authorization") String header){JwtService.Claims c=claims(header);if(c==null)return unauthorized();PlanEntity plan=activePlan(c.userId());if(plan==null)return Response.status(402).entity(Map.of("message","An active plan is required")).build();UsageCounterEntity counter=UsageCounterEntity.find("userId = ?1 and usageDate = ?2",c.userId(),LocalDate.now()).firstResult();if(counter==null){counter=new UsageCounterEntity();counter.userId=c.userId();counter.usageDate=LocalDate.now();counter.propertyLoads=0;}if(counter.propertyLoads>=plan.dailyLoadLimit)return Response.status(429).entity(Map.of("message","Daily property-load limit reached")).build();counter.propertyLoads++;counter.persist();return Response.ok(Map.of("remainingLoads",plan.dailyLoadLimit-counter.propertyLoads)).build();}

  private void markPaid(PaymentOrderEntity order,String paymentId,String signature){if("PAID".equals(order.status))return;order.status="PAID";order.gatewayPaymentId=paymentId;order.gatewaySignature=signature;order.paidAt=Instant.now();SubscriptionEntity sub=SubscriptionEntity.findById(order.subscriptionId);SubscriptionEntity old=SubscriptionEntity.find("userId = ?1 and status = 'ACTIVE'",order.userId).firstResult();if(old!=null)old.status=SubscriptionEntity.Status.EXPIRED;sub.status=SubscriptionEntity.Status.ACTIVE;sub.startsAt=Instant.now();PlanEntity plan=PlanEntity.findById(sub.planId);sub.expiresAt=Instant.now().plus(plan.validityDays,java.time.temporal.ChronoUnit.DAYS);}
  private PlanEntity activePlan(long userId){SubscriptionEntity s=SubscriptionEntity.find("userId = ?1 and status = 'ACTIVE' and expiresAt > ?2 order by expiresAt desc",userId,Instant.now()).firstResult();return s==null?null:PlanEntity.findById(s.planId);}
  private Map<String,Object> planDto(PlanEntity p){return Map.of("id",p.id,"code",p.code,"name",p.name,"amount",p.amountPaise/100.0,"amountPaise",p.amountPaise,"currency",p.currency,"validityDays",p.validityDays,"coverageInfo",p.coverageInfo,"maxPropertyListings",p.maxPropertyListings,"dailyLoadLimit",p.dailyLoadLimit);}
  private JwtService.Claims claims(String h){return h!=null&&h.startsWith("Bearer ")?jwt.verify(h.substring(7)):null;}private Response unauthorized(){return Response.status(401).entity(Map.of("message","Authentication required")).build();}private Response forbidden(){return Response.status(403).entity(Map.of("message","Broker account required")).build();}private Response bad(String m){return Response.status(400).entity(Map.of("message",m)).build();}private String hmac(String value,String secret){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return bytes(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}private String bytes(byte[] b){return Base64.getEncoder().encodeToString(b);}private boolean constantEquals(String a,String b){return a!=null&&b!=null&&java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),b.getBytes(StandardCharsets.UTF_8));}
}
