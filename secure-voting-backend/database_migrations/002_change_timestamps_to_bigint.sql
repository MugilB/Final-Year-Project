-- Migration: Change timestamp columns to bigint for candidates table
-- Date: 2025-01-23
-- Description: Drop timestamp columns and recreate as bigint (Unix timestamp in milliseconds)

-- Step 1: Drop existing timestamp columns
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

-- Step 4: Also update candidate_details table if it has timestamp columns
-- Drop existing reviewed_at timestamp column
ALTER TABLE `candidate_details` 
DROP COLUMN `reviewed_at`;

-- Add new reviewed_at column as bigint
ALTER TABLE `candidate_details` 
ADD COLUMN `reviewed_at` bigint DEFAULT NULL AFTER `reviewed_by`;

-- Step 5: Migration completed successfully
-- All timestamp columns are now bigint storing Unix timestamps in milliseconds
-- Example: 1735689600000 (January 1, 2025 00:00:00 UTC)
