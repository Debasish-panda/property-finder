package com.propertyfinder.api.auth;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_users")
public class UserEntity extends PanacheEntityBase {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @Column(name = "full_name", nullable = false) public String fullName;
  public String email;
  public String mobile;
  @Column(name = "password_hash") public String passwordHash;
  @Enumerated(EnumType.STRING) @Column(nullable = false) public Role role;
  public boolean enabled = true;
  @Column(name = "created_at", nullable = false) public Instant createdAt = Instant.now();
  public enum Role { USER, BROKER }
}
