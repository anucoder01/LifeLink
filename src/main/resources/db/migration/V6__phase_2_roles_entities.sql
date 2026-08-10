-- Blood Banks table
CREATE TABLE IF NOT EXISTS blood_banks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    name VARCHAR(150) NOT NULL,
    location geography(Point, 4326) NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    address VARCHAR(300),
    contact_phone VARCHAR(15),
    license_number VARCHAR(50),
    operating_hours VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- NGOs table
CREATE TABLE IF NOT EXISTS ngos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    name VARCHAR(150) NOT NULL,
    location geography(Point, 4326),
    verified BOOLEAN DEFAULT FALSE,
    address VARCHAR(300),
    contact_phone VARCHAR(15),
    registration_number VARCHAR(50),
    focus_areas VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add geo-spatial indexes
CREATE INDEX idx_blood_banks_location ON blood_banks USING GIST (location);
CREATE INDEX idx_ngos_location ON ngos USING GIST (location);
