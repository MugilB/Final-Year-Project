-- Verification script to check if timestamp columns were converted to bigint
-- Run this after the migration to verify the changes

USE secure_voting;

-- Check column types in candidates table
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'candidates'
AND COLUMN_NAME IN ('created_at', 'updated_at');

-- Check column types in candidate_details table
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'candidate_details'
AND COLUMN_NAME = 'reviewed_at';

-- Show sample data with bigint timestamps
SELECT 
    candidate_id,
    name,
    created_at,
    updated_at,
    status
FROM candidates 
LIMIT 5;

-- Show the table structure
DESCRIBE candidates;
DESCRIBE candidate_details;
