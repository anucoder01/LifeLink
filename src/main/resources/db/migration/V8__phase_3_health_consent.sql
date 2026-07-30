CREATE TABLE donor_health_questionnaires (
    donor_id UUID PRIMARY KEY,
    had_recent_illness BOOLEAN DEFAULT FALSE,
    had_recent_surgery BOOLEAN DEFAULT FALSE,
    on_medication BOOLEAN DEFAULT FALSE,
    is_pregnant BOOLEAN DEFAULT FALSE,
    has_recent_tattoos BOOLEAN DEFAULT FALSE,
    consumed_alcohol_recently BOOLEAN DEFAULT FALSE,
    last_updated TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_health_donor FOREIGN KEY (donor_id) REFERENCES donors(id) ON DELETE CASCADE
);

CREATE TABLE donor_consents (
    donor_id UUID PRIMARY KEY,
    share_location BOOLEAN DEFAULT TRUE,
    allow_emergency_notifications BOOLEAN DEFAULT TRUE,
    allow_camp_notifications BOOLEAN DEFAULT TRUE,
    show_name_on_public_impact_board BOOLEAN DEFAULT FALSE,
    last_updated TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_consent_donor FOREIGN KEY (donor_id) REFERENCES donors(id) ON DELETE CASCADE
);
