CREATE TABLE app_config (
  config_key VARCHAR(80) PRIMARY KEY,
  config_value TEXT NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO app_config(config_key, config_value) VALUES
 ('map.provider','openstreetmap'),
 ('map.tile_url','https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'),
 ('map.attribution','&copy; OpenStreetMap contributors'),
 ('map.api_key','')
ON CONFLICT (config_key) DO NOTHING;
