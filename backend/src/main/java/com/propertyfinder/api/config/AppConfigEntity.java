package com.propertyfinder.api.config;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="app_config")
public class AppConfigEntity extends PanacheEntityBase {
  @Id @Column(name="config_key") public String key;
  @Column(name="config_value", nullable=false, columnDefinition="text") public String value;
  @Column(name="updated_at", nullable=false) public Instant updatedAt;
}
