ALTER TABLE blood_inventory
    ADD COLUMN IF NOT EXISTS untracked_units INTEGER NOT NULL DEFAULT 0;

UPDATE blood_inventory
SET untracked_units = COALESCE(units_available, 0)
WHERE untracked_units = 0;
