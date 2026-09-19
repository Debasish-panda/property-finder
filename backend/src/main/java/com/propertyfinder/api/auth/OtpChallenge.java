package com.propertyfinder.api.auth;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="otp_challenges")
public class OtpChallenge extends PanacheEntityBase {
  @Id public UUID id; @Column(nullable=false) public String identifier; @Column(name="code_hash", nullable=false) public String codeHash;
  @Column(name="expires_at", nullable=false) public Instant expiresAt; @Column(name="consumed_at") public Instant consumedAt;
  public int attempts; @Column(name="created_at", nullable=false) public Instant createdAt=Instant.now();
}
