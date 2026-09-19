package com.propertyfinder.api.billing;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="subscriptions")
public class SubscriptionEntity extends PanacheEntityBase {
  @Id public UUID id;
  @Column(name="user_id", nullable=false) public Long userId;
  @Column(name="plan_id", nullable=false) public Long planId;
  @Enumerated(EnumType.STRING) @Column(nullable=false) public Status status;
  @Column(name="starts_at") public Instant startsAt;
  @Column(name="expires_at") public Instant expiresAt;
  @Column(name="created_at", nullable=false) public Instant createdAt=Instant.now();
  public enum Status { PENDING, ACTIVE, EXPIRED, CANCELLED }
}
