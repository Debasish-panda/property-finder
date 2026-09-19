package com.propertyfinder.api.auth;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "auth_sessions")
public class SessionEntity extends PanacheEntityBase {
  @Id public UUID id;
  @Column(name = "user_id", nullable = false) public Long userId;
  @Column(name = "expires_at", nullable = false) public Instant expiresAt;
  @Column(name = "created_at", nullable = false) public Instant createdAt = Instant.now();
  @Column(name = "revoked_at") public Instant revokedAt;
}
