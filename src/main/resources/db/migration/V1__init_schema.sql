CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE,
    password_hash TEXT NOT NULL,
    role VARCHAR(20) NOT NULL, -- DONOR, REQUESTER, HOSPITAL_ADMIN
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE donors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    blood_type VARCHAR(5) NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    last_donation_date DATE,
    reliability_score DECIMAL(5,2) DEFAULT 100.00,
    is_active BOOLEAN DEFAULT TRUE,
    fcm_token TEXT
);
CREATE INDEX idx_donor_location ON donors USING GIST (location);

CREATE TABLE emergency_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID REFERENCES users(id),
    blood_type VARCHAR(5) NOT NULL,
    component_type VARCHAR(20) NOT NULL, -- WHOLE_BLOOD, PLATELETS, PLASMA
    urgency VARCHAR(20) NOT NULL, -- CRITICAL, HIGH, NORMAL
    location GEOGRAPHY(Point, 4326) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, FULFILLED, EXPIRED, CANCELLED
    current_radius_km INT DEFAULT 5,
    created_at TIMESTAMP DEFAULT now(),
    expires_at TIMESTAMP
);
CREATE INDEX idx_request_location ON emergency_requests USING GIST (location);

CREATE TABLE request_responses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES emergency_requests(id),
    donor_id UUID REFERENCES donors(id),
    status VARCHAR(20) NOT NULL, -- NOTIFIED, ACCEPTED, DECLINED, EN_ROUTE, DONATED, NO_SHOW
    responded_at TIMESTAMP
);

CREATE TABLE hospitals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    verified BOOLEAN DEFAULT FALSE
);

CREATE TABLE blood_inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id UUID REFERENCES hospitals(id),
    blood_type VARCHAR(5) NOT NULL,
    component_type VARCHAR(20) NOT NULL,
    units_available INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT now()
);
