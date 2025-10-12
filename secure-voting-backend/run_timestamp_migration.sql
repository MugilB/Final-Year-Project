-- Simple script to run the timestamp to bigint migration
-- Copy and paste these commands into your MySQL client

USE secure_voting;

-- Step 1: Drop existing timestamp columns from candidates table
ALTER TABLE `candidates` 
DROP COLUMN `created_at`,
DROP COLUMN `updated_at`;

-- Step 2: Add new bigint columns with the same names
ALTER TABLE `candidates` 
ADD COLUMN `created_at` bigint DEFAULT NULL AFTER `status`,
ADD COLUMN `updated_at` bigint DEFAULT NULL AFTER `created_at`;

-- Step 3: Set default values for existing records
UPDATE `candidates` SET 
    `created_at` = UNIX_TIMESTAMP(NOW()) * 1000,
    `updated_at` = UNIX_TIMESTAMP(NOW()) * 1000
WHERE `created_at` IS NULL OR `updated_at` IS NULL;

-- Step 4: Also update candidate_details table
-- Drop existing reviewed_at timestamp column
ALTER TABLE `candidate_details` 
DROP COLUMN `reviewed_at`;

-- Add new reviewed_at column as bigint
ALTER TABLE `candidate_details` 
ADD COLUMN `reviewed_at` bigint DEFAULT NULL AFTER `reviewed_by`;

-- Step 5: Verify the changes
DESCRIBE candidates;
DESCRIBE candidate_details;

-- Show sample data
SELECT 
    candidate_id,
    name,
    created_at,
    updated_at
FROM candidates 
LIMIT 3;
