package com.propertyfinder.api.property;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="properties")
public class PropertyEntity extends PanacheEntityBase {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(name="broker_id") public Long brokerId; public String title, locality;
  public BigDecimal rent; @Column(name="property_type") public String propertyType; public String bedrooms; public double latitude, longitude;
  @Column(columnDefinition="text[]") public String[] amenities; public boolean verified; public String status;
  @Column(name="available_from") public LocalDate availableFrom; @Column(name="rented_on") public LocalDate rentedOn; public boolean negotiable;
  @Column(name="duration_days") public int durationDays; @Column(name="commission_earned") public BigDecimal commissionEarned;
}
