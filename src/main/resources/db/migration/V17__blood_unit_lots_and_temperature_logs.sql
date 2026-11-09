CREATE TABLE blood_unit_lots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id UUID NOT NULL REFERENCES hospitals(id) ON DELETE CASCADE,
    blood_type VARCHAR(5) NOT NULL,
    component_type VARCHAR(20) NOT NULL,
    unit_count INTEGER NOT NULL CHECK (unit_count > 0),
    source_lot_id UUID REFERENCES blood_unit_lots(id),
    collected_on DATE NOT NULL,
    expires_on DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (expires_on >= collected_on)
);
CREATE INDEX idx_blood_unit_lots_hospital_expiry ON blood_unit_lots(hospital_id, expires_on);
CREATE TABLE blood_unit_temperature_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lot_id UUID NOT NULL REFERENCES blood_unit_lots(id) ON DELETE CASCADE,
    temperature_c DOUBLE PRECISION NOT NULL,
    within_validated_range BOOLEAN NOT NULL,
    note VARCHAR(300),
    recorded_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_blood_unit_temperature_lot_time ON blood_unit_temperature_logs(lot_id, recorded_at DESC);
CREATE TABLE blood_unit_handoffs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lot_id UUID NOT NULL REFERENCES blood_unit_lots(id),
    from_hospital_id UUID NOT NULL REFERENCES hospitals(id),
    to_hospital_id UUID NOT NULL REFERENCES hospitals(id),
    unit_count INTEGER NOT NULL CHECK (unit_count > 0),
    reference VARCHAR(300),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_TRANSIT',
    recorded_at TIMESTAMP NOT NULL DEFAULT now(),
    received_at TIMESTAMP
);
CREATE INDEX idx_blood_unit_handoffs_lot_time ON blood_unit_handoffs(lot_id, recorded_at DESC);
