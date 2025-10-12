-- Diagnostic script for 500 error
USE secure_voting;

-- Check if all required tables exist
SELECT 'TABLES CHECK' as test_type, 'Checking if all tables exist' as description;
SHOW TABLES;

-- Check candidates table structure
SELECT 'CANDIDATES TABLE' as test_type, 'Checking candidates table structure' as description;
DESCRIBE candidates;

-- Check candidate_details table structure
SELECT 'CANDIDATE_DETAILS TABLE' as test_type, 'Checking candidate_details table structure' as description;
DESCRIBE candidate_details;

-- Check if election 1 exists
SELECT 'ELECTION 1 CHECK' as test_type, 'Checking if election 1 exists' as description;
SELECT * FROM elections WHERE election_id = 1;

-- Check if any candidates exist for election 1
SELECT 'CANDIDATES FOR ELECTION 1' as test_type, 'Checking candidates for election 1' as description;
SELECT * FROM candidates WHERE election_id = 1;

-- Check if candidate_image_link column exists
SELECT 'CANDIDATE_IMAGE_LINK COLUMN' as test_type, 'Checking if candidate_image_link column exists' as description;
SHOW COLUMNS FROM candidate_details LIKE 'candidate_image_link';

-- Check party_details table
SELECT 'PARTY_DETAILS TABLE' as test_type, 'Checking party_details table' as description;
SELECT * FROM party_details LIMIT 5;

-- Check for any foreign key issues
SELECT 'FOREIGN KEY CHECK' as test_type, 'Checking for orphaned records' as description;
SELECT 
    c.candidate_id,
    c.name,
    c.election_id,
    c.party_id,
    CASE 
        WHEN e.election_id IS NULL THEN 'MISSING ELECTION'
        WHEN p.party_id IS NULL THEN 'MISSING PARTY'
        ELSE 'OK'
    END as status
FROM candidates c
LEFT JOIN elections e ON c.election_id = e.election_id
LEFT JOIN party_details p ON c.party_id = p.party_id
WHERE c.election_id = 1;
