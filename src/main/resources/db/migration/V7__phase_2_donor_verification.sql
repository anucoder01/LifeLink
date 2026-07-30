ALTER TABLE donors
ADD COLUMN government_id_hash VARCHAR(64),
ADD COLUMN identity_verified BOOLEAN DEFAULT FALSE;
