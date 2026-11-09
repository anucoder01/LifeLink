-- V15: Add Gamification, Inter-Hospital Transfers, and Post-Donation Recovery Tables

CREATE TABLE donor_gamification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_id UUID UNIQUE REFERENCES donors(id) ON DELETE CASCADE,
    xp INT DEFAULT 0,
    level INT DEFAULT 1,
    current_streak INT DEFAULT 0,
    total_donations INT DEFAULT 0,
    last_donation_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT now()
);

CREATE TABLE donor_badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_id UUID REFERENCES donors(id) ON DELETE CASCADE,
    badge_key VARCHAR(50) NOT NULL,
    badge_name VARCHAR(100) NOT NULL,
    badge_icon VARCHAR(50) NOT NULL,
    earned_at TIMESTAMP DEFAULT now()
);
CREATE INDEX idx_donor_badges_donor ON donor_badges(donor_id);

CREATE TABLE hospital_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requesting_hospital_id UUID REFERENCES hospitals(id),
    providing_hospital_id UUID REFERENCES hospitals(id),
    blood_type VARCHAR(5) NOT NULL,
    component_type VARCHAR(20) NOT NULL,
    units_requested INT NOT NULL,
    urgency VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);
CREATE INDEX idx_hospital_transfers_req ON hospital_transfers(requesting_hospital_id);
CREATE INDEX idx_hospital_transfers_prov ON hospital_transfers(providing_hospital_id);

CREATE TABLE donor_recovery_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_id UUID REFERENCES donors(id) ON DELETE CASCADE,
    donation_date DATE NOT NULL,
    day_number INT NOT NULL,
    water_ml_logged INT DEFAULT 0,
    feeling_status VARCHAR(30) DEFAULT 'GOOD',
    notes TEXT,
    logged_at TIMESTAMP DEFAULT now()
);
CREATE INDEX idx_donor_recovery_donor ON donor_recovery_logs(donor_id);
