-- Seed 2 dummy users
INSERT INTO users (id, name, phone, email, password_hash, role) VALUES 
('11111111-1111-1111-1111-111111111111', 'John Doe', '1234567890', 'john@example.com', 'hashed_pass', 'DONOR'),
('22222222-2222-2222-2222-222222222222', 'Jane Smith', '0987654321', 'jane@example.com', 'hashed_pass', 'DONOR');

-- Seed 2 donors with location in Bangalore (approx)
INSERT INTO donors (id, user_id, blood_type, location, is_active) VALUES
('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'O+', ST_SetSRID(ST_MakePoint(77.5946, 12.9716), 4326), true),
('44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', 'A-', ST_SetSRID(ST_MakePoint(77.5950, 12.9720), 4326), true);

-- Seed Hospital
INSERT INTO hospitals (id, name, location, verified) VALUES
('55555555-5555-5555-5555-555555555555', 'City Hospital', ST_SetSRID(ST_MakePoint(77.5900, 12.9700), 4326), true);

-- Seed Blood Inventory
INSERT INTO blood_inventory (hospital_id, blood_type, component_type, units_available) VALUES
('55555555-5555-5555-5555-555555555555', 'O+', 'WHOLE_BLOOD', 10);
