ALTER TABLE emergency_requests
    ADD COLUMN IF NOT EXISTS verified BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS verification_reference VARCHAR(500),
    ADD COLUMN IF NOT EXISTS verified_by UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_emergency_requests_verification_queue
    ON emergency_requests (created_at)
    WHERE verified = FALSE;
