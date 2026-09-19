package com.propertyfinder.api.billing;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="payment_orders")
public class PaymentOrderEntity extends PanacheEntityBase {
  @Id public UUID id;
  @Column(name="subscription_id", nullable=false) public UUID subscriptionId;
  @Column(name="user_id", nullable=false) public Long userId;
  @Column(name="plan_id", nullable=false) public Long planId;
  @Column(nullable=false) public String gateway;
  @Column(name="gateway_order_id", nullable=false, unique=true) public String gatewayOrderId;
  @Column(name="gateway_payment_id") public String gatewayPaymentId;
  @Column(name="amount_paise", nullable=false) public long amountPaise;
  @Column(nullable=false) public String currency;
  @Column(nullable=false) public String status;
  @Column(name="payment_method") public String paymentMethod;
  @Column(name="gateway_signature") public String gatewaySignature;
  @Column(name="paid_at") public Instant paidAt;
  @Column(name="created_at", nullable=false) public Instant createdAt=Instant.now();
}
