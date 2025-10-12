-- Fix for 500 error in voting chart
-- This script ensures all required data exists for election 1

USE secure_voting;

-- Step 1: Check if election 1 exists, if not create it
INSERT IGNORE INTO elections (election_id, name, start_date, end_date, status, description, rules) VALUES
(1, 'Test Election 2024', 
 UNIX_TIMESTAMP('2024-01-01 00:00:00') * 1000, 
 UNIX_TIMESTAMP('2024-12-31 23:59:59') * 1000, 
 'SCHEDULED', 
 'Test election for development', 
 'Test rules');

-- Step 2: Ensure party_details table has required data
INSERT IGNORE INTO party_details (party_id, party_name, party_symbol, party_secret_code) VALUES
(1, 'Independent Candidate', 'IND', 'INDEPENDENT_SECRET_2024'),
(2, 'Test Party', 'TEST', 'TEST_SECRET_2024');

-- Step 3: Check if candidate_image_link column exists, if not add it
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'secure_voting' 
    AND TABLE_NAME = 'candidate_details' 
    AND COLUMN_NAME = 'candidate_image_link'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE candidate_details ADD COLUMN candidate_image_link varchar(500) DEFAULT NULL AFTER aadhar_card_link',
    'SELECT "Column already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: Create test candidates for election 1
INSERT IGNORE INTO candidates (candidate_id, name, election_id, party_id, status, created_at, updated_at) VALUES
(1, 'John Doe', 1, 1, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(2, 'Jane Smith', 1, 2, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(3, 'Bob Johnson', 1, 1, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- Step 5: Create candidate_details for the test candidates
INSERT IGNORE INTO candidate_details (candidate_id, email, phone_number, gender, age, address, aadhar_card_link, candidate_image_link) VALUES
(1, 'john.doe@example.com', '1234567890', 'Male', 35, '123 Main St, City, State', 'https://drive.google.com/file/d/john_aadhar', 'https://drive.google.com/file/d/john_image'),
(2, 'jane.smith@example.com', '0987654321', 'Female', 28, '456 Oak Ave, City, State', 'https://drive.google.com/file/d/jane_aadhar', 'https://drive.google.com/file/d/jane_image'),
(3, 'bob.johnson@example.com', '1122334455', 'Male', 42, '789 Pine Rd, City, State', 'https://drive.google.com/file/d/bob_aadhar', 'https://drive.google.com/file/d/bob_image');

-- Step 6: Verify the data
SELECT 'ELECTIONS' as table_name, COUNT(*) as count FROM elections
UNION ALL
SELECT 'PARTY_DETAILS', COUNT(*) FROM party_details
UNION ALL
SELECT 'CANDIDATES', COUNT(*) FROM candidates
UNION ALL
SELECT 'CANDIDATE_DETAILS', COUNT(*) FROM candidate_details;

-- Step 7: Show candidates for election 1
SELECT 
    c.candidate_id,
    c.name,
    c.election_id,
    c.party_id,
    c.status,
    p.party_name,
    cd.email,
    cd.phone_number
FROM candidates c
LEFT JOIN party_details p ON c.party_id = p.party_id
LEFT JOIN candidate_details cd ON c.candidate_id = cd.candidate_id
WHERE c.election_id = 1;

-- Step 8: Check table structures
DESCRIBE candidates;
DESCRIBE candidate_details;
DESCRIBE party_details;
