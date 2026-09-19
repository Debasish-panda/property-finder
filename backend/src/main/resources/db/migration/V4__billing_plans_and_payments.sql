ALTER TABLE app_users ADD COLUMN IF NOT EXISTS active_subscription_id UUID;

CREATE TABLE plans (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  amount_paise BIGINT NOT NULL CHECK (amount_paise >= 0),
  currency VARCHAR(3) NOT NULL DEFAULT 'INR',
  validity_days INTEGER NOT NULL CHECK (validity_days > 0),
  coverage_info TEXT NOT NULL,
  max_property_listings INTEGER NOT NULL CHECK (max_property_listings > 0),
  daily_load_limit INTEGER NOT NULL CHECK (daily_load_limit > 0),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE subscriptions (
  id UUID PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  plan_id BIGINT NOT NULL REFERENCES plans(id),
  status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','ACTIVE','EXPIRED','CANCELLED')),
  starts_at TIMESTAMPTZ,
  expires_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_subscriptions_user_status ON subscriptions(user_id, status);

CREATE TABLE payment_orders (
  id UUID PRIMARY KEY,
  subscription_id UUID NOT NULL REFERENCES subscriptions(id),
  user_id BIGINT NOT NULL REFERENCES app_users(id),
  plan_id BIGINT NOT NULL REFERENCES plans(id),
  gateway VARCHAR(30) NOT NULL,
  gateway_order_id VARCHAR(100) NOT NULL UNIQUE,
  gateway_payment_id VARCHAR(100),
  amount_paise BIGINT NOT NULL,
  currency VARCHAR(3) NOT NULL,
  status VARCHAR(20) NOT NULL CHECK (status IN ('CREATED','PAID','FAILED','REFUNDED')),
  payment_method VARCHAR(30),
  gateway_signature VARCHAR(255),
  paid_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payment_orders_user ON payment_orders(user_id, created_at DESC);

CREATE TABLE usage_counters (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  usage_date DATE NOT NULL,
  property_loads INTEGER NOT NULL DEFAULT 0,
  UNIQUE(user_id, usage_date)
);

INSERT INTO plans(code,name,amount_paise,validity_days,coverage_info,max_property_listings,daily_load_limit)
VALUES
 ('small','Small broker',50000,30,'Map search and basic listing visibility',100,100),
 ('medium','Medium broker',100000,30,'Map search, locality search and expanded listing coverage',400,400),
 ('large','Large broker',500000,30,'High-volume map, locality and broker dashboard coverage',1000,1000)
ON CONFLICT (code) DO NOTHING;
