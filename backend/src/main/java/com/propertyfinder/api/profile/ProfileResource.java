package com.propertyfinder.api.profile;

import com.propertyfinder.api.auth.JwtService;
import com.propertyfinder.api.auth.UserEntity;
import com.propertyfinder.api.billing.PlanEntity;
import com.propertyfinder.api.billing.SubscriptionEntity;
import com.propertyfinder.api.billing.UsageCounterEntity;
import com.propertyfinder.api.property.PropertyEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.*;
import java.util.*;

@Path("/api/profile")
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource {
  @Inject JwtService jwt;

  @GET
  public Response profile(@HeaderParam("Authorization") String authorization) {
    JwtService.Claims claims = claims(authorization);
    if (claims == null) return Response.status(Response.Status.UNAUTHORIZED)
        .entity(Map.of("message", "Authentication required")).build();

    UserEntity user = UserEntity.findById(claims.userId());
    if (user == null || !user.enabled) return Response.status(Response.Status.UNAUTHORIZED)
        .entity(Map.of("message", "User account is unavailable")).build();

    SubscriptionEntity subscription = SubscriptionEntity.find(
        "userId = ?1 and status = 'ACTIVE' and expiresAt > ?2 order by expiresAt desc",
        user.id, Instant.now()).firstResult();
    PlanEntity plan = subscription == null ? null : PlanEntity.findById(subscription.planId);
    UsageCounterEntity usage = UsageCounterEntity.find(
        "userId = ?1 and usageDate = ?2", user.id, LocalDate.now()).firstResult();
    int usedLoads = usage == null ? 0 : usage.propertyLoads;
    long listingCount = PropertyEntity.count("brokerId", user.id);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("profile", userDto(user));
    response.put("subscription", subscriptionDto(subscription, plan));
    response.put("usage", usageDto(usedLoads, plan));
    response.put("listingCount", listingCount);
    response.put("session", Map.of("expiresAt", Instant.ofEpochSecond(claims.expiresAt()),
        "remainingSeconds", Math.max(0, claims.expiresAt() - Instant.now().getEpochSecond())));
    return Response.ok(response).build();
  }

  private Map<String, Object> userDto(UserEntity user) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", user.id); dto.put("fullName", user.fullName); dto.put("email", user.email);
    dto.put("mobile", user.mobile); dto.put("role", user.role.name()); dto.put("enabled", user.enabled);
    dto.put("createdAt", user.createdAt); return dto;
  }

  private Map<String, Object> subscriptionDto(SubscriptionEntity subscription, PlanEntity plan) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("active", subscription != null && plan != null);
    dto.put("status", subscription == null ? null : subscription.status.name());
    dto.put("startsAt", subscription == null ? null : subscription.startsAt);
    dto.put("expiresAt", subscription == null ? null : subscription.expiresAt);
    dto.put("plan", plan == null ? null : planDto(plan)); return dto;
  }

  private Map<String, Object> usageDto(int used, PlanEntity plan) {
    int limit = plan == null ? 0 : plan.dailyLoadLimit;
    Map<String, Object> dto = new LinkedHashMap<>(); dto.put("date", LocalDate.now());
    dto.put("dailyLoadsUsed", used); dto.put("dailyLoadLimit", limit);
    dto.put("remainingLoads", Math.max(0, limit - used));
    dto.put("propertyListingLimit", plan == null ? 0 : plan.maxPropertyListings);
    return dto;
  }

  private Map<String, Object> planDto(PlanEntity plan) {
    Map<String, Object> dto = new LinkedHashMap<>(); dto.put("id", plan.id); dto.put("code", plan.code);
    dto.put("name", plan.name); dto.put("amount", plan.amountPaise / 100.0); dto.put("amountPaise", plan.amountPaise);
    dto.put("currency", plan.currency); dto.put("validityDays", plan.validityDays);
    dto.put("coverageInfo", plan.coverageInfo); dto.put("maxPropertyListings", plan.maxPropertyListings);
    dto.put("dailyLoadLimit", plan.dailyLoadLimit); return dto;
  }

  private JwtService.Claims claims(String value) {
    return value != null && value.startsWith("Bearer ") ? jwt.verify(value.substring(7)) : null;
  }
}
