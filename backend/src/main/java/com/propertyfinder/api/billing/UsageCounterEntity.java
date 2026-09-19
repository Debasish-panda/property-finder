package com.propertyfinder.api.billing;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name="usage_counters", uniqueConstraints=@UniqueConstraint(columnNames={"user_id","usage_date"}))
public class UsageCounterEntity extends PanacheEntityBase {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(name="user_id", nullable=false) public Long userId;
  @Column(name="usage_date", nullable=false) public LocalDate usageDate;
  @Column(name="property_loads", nullable=false) public int propertyLoads;
}
