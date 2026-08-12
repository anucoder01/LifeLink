ALTER TABLE users 
ADD COLUMN address VARCHAR(255),
ADD COLUMN date_of_birth DATE,
ADD COLUMN gender VARCHAR(20),
ADD COLUMN emergency_contact VARCHAR(50),
ADD COLUMN weight DECIMAL(5,2),
ADD COLUMN medical_conditions TEXT;
