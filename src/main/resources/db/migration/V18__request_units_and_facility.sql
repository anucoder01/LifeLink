ALTER TABLE emergency_requests
    ADD COLUMN IF NOT EXISTS units_required INTEGER NOT NULL DEFAULT 1 CHECK (units_required > 0),
    ADD COLUMN IF NOT EXISTS facility_name VARCHAR(150);

