-- V4: Add address, contact_phone, created_at to hospitals
-- Also adds GIST index on hospital location for efficient geo-queries

ALTER TABLE hospitals
    ADD COLUMN IF NOT EXISTS address       VARCHAR(300),
    ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(15),
    ADD COLUMN IF NOT EXISTS created_at    TIMESTAMP DEFAULT now();

CREATE INDEX IF NOT EXISTS idx_hospital_location ON hospitals USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_blood_inventory_hospital ON blood_inventory (hospital_id);
CREATE INDEX IF NOT EXISTS idx_blood_inventory_lookup  ON blood_inventory (hospital_id, blood_type, component_type);
