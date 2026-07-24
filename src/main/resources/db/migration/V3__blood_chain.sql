-- ============================================================
-- Feature A: Blood Chain (Social Vouching Network)
-- ============================================================

-- Stores trusted contacts nominated by a registered donor.
-- No PII from the contact is stored until they opt in via the invite link.
CREATE TABLE donor_vouches (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_id    UUID NOT NULL REFERENCES donors(id) ON DELETE CASCADE,
    contact_phone VARCHAR(15) NOT NULL,
    contact_name  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP DEFAULT now(),

    -- A donor can vouch for the same phone number only once
    CONSTRAINT uq_donor_vouch UNIQUE (donor_id, contact_phone)
);

-- Short-lived, one-time tokens sent to vouched contacts via SMS.
-- Expires after 72 hours. Deleted on use.
CREATE TABLE blood_chain_invite_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token           VARCHAR(64) UNIQUE NOT NULL,
    request_id      UUID NOT NULL REFERENCES emergency_requests(id) ON DELETE CASCADE,
    contact_phone   VARCHAR(15) NOT NULL,
    contact_name    VARCHAR(100) NOT NULL,
    expires_at      TIMESTAMP NOT NULL,
    used            BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_invite_token ON blood_chain_invite_tokens(token);
CREATE INDEX idx_donor_vouches_donor ON donor_vouches(donor_id);
