ALTER TABLE users
    ADD COLUMN IF NOT EXISTS hospital_id UUID REFERENCES hospitals(id);
CREATE INDEX IF NOT EXISTS idx_users_hospital_id ON users(hospital_id);
