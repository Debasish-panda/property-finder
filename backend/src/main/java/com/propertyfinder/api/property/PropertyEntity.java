package com.propertyfinder.api.property;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="properties")
public class PropertyEntity extends PanacheEntityBase {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(name="broker_id", nullable=false) public Long brokerId;
  public String title, locality, address;
  @Column(name="building_name") public String buildingName;
  @Column(name="house_number") public String houseNumber;
  @Column(name="owner_name") public String ownerName;
  @Column(name="owner_mobile") public String ownerMobile;
  public BigDecimal rent, deposit;
  @Column(name="property_type") public String propertyType;
  public String bedrooms;
  public double latitude, longitude;
  @Column(columnDefinition="text[]") public String[] amenities;
  public boolean verified;
  public String status;
  @Column(name="available_from") public LocalDate availableFrom;
  @Column(name="rented_on") public LocalDate rentedOn;
  public boolean negotiable;
  public String furnishing;
  @Column(name="maintenance_charges") public BigDecimal maintenanceCharges;
  @Column(name="duration_days") public int durationDays;
  @Column(name="commission_earned") public BigDecimal commissionEarned;
}
