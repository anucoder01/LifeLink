-- Add disaster_mode to emergency_requests
ALTER TABLE emergency_requests ADD COLUMN disaster_mode BOOLEAN NOT NULL DEFAULT FALSE;

-- Create blood_banks table
CREATE TABLE blood_banks (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    email VARCHAR(255),
    location geometry(Point,4326) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create hospital_forwards table
CREATE TABLE hospital_forwards (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES emergency_requests(id) ON DELETE CASCADE,
    blood_bank_id UUID NOT NULL REFERENCES blood_banks(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);
