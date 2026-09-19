package com.propertyfinder.api.billing;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="plans")
public class PlanEntity extends PanacheEntityBase {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false, unique=true) public String code;
  @Column(nullable=false) public String name;
  @Column(name="amount_paise", nullable=false) public long amountPaise;
  @Column(nullable=false) public String currency;
  @Column(name="validity_days", nullable=false) public int validityDays;
  @Column(name="coverage_info", nullable=false, columnDefinition="text") public String coverageInfo;
  @Column(name="max_property_listings", nullable=false) public int maxPropertyListings;
  @Column(name="daily_load_limit", nullable=false) public int dailyLoadLimit;
  public boolean enabled;
  @Column(name="created_at", nullable=false) public Instant createdAt;
}
