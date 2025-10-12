-- Create test wards data
-- This script creates sample ward data for testing voter registration

-- First, check if wards table exists and what data it contains
SELECT * FROM wards;

-- If the table is empty, create some test wards
-- Note: ward_id is AUTO_INCREMENT, so we don't need to specify it
INSERT INTO wards (ward_name, description) VALUES
('Ward 1 - Central', 'Central ward covering downtown area'),
('Ward 2 - North', 'Northern ward covering residential areas'),
('Ward 3 - South', 'Southern ward covering industrial areas'),
('Ward 4 - East', 'Eastern ward covering suburban areas'),
('Ward 5 - West', 'Western ward covering rural areas');

-- Verify the wards were created
SELECT * FROM wards ORDER BY ward_id;
