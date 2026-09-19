CREATE TABLE app_users (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(320),
  mobile VARCHAR(32),
  password_hash VARCHAR(255),
  role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'BROKER')),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT user_email_or_mobile CHECK (email IS NOT NULL OR mobile IS NOT NULL)
);
CREATE UNIQUE INDEX uq_app_users_email ON app_users (lower(email)) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX uq_app_users_mobile ON app_users (mobile) WHERE mobile IS NOT NULL;

CREATE TABLE otp_challenges (
  id UUID PRIMARY KEY,
  identifier VARCHAR(320) NOT NULL,
  code_hash VARCHAR(255) NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  consumed_at TIMESTAMPTZ,
  attempts INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_otp_identifier ON otp_challenges(identifier, created_at DESC);

CREATE TABLE properties (
  id BIGSERIAL PRIMARY KEY,
  broker_id BIGINT NOT NULL REFERENCES app_users(id),
  title VARCHAR(180) NOT NULL,
  locality VARCHAR(180) NOT NULL,
  rent NUMERIC(12,2) NOT NULL,
  property_type VARCHAR(80) NOT NULL,
  bedrooms VARCHAR(40),
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  amenities TEXT[] NOT NULL DEFAULT '{}',
  verified BOOLEAN NOT NULL DEFAULT FALSE,
  status VARCHAR(20) NOT NULL DEFAULT 'available' CHECK (status IN ('available', 'rented')),
  available_from DATE,
  rented_on DATE,
  negotiable BOOLEAN NOT NULL DEFAULT FALSE,
  duration_days INTEGER NOT NULL DEFAULT 0,
  commission_earned NUMERIC(12,2) NOT NULL DEFAULT 0
);
CREATE INDEX idx_properties_coordinates ON properties(latitude, longitude);
CREATE INDEX idx_properties_broker ON properties(broker_id);
