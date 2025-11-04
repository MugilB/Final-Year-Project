-- Migration: Add voter_id as primary key to users table (SAFE VERSION)
-- Date: 2025-01-08
-- Description: Add voter_id column and make it the primary key, handling foreign key constraints

-- Step 1: Check current foreign key constraints
-- Run this query first to see what constraints exist:
-- SELECT 
--     TABLE_NAME,
--     COLUMN_NAME,
--     CONSTRAINT_NAME,
--     REFERENCED_TABLE_NAME,
--     REFERENCED_COLUMN_NAME
-- FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
-- WHERE REFERENCED_TABLE_NAME = 'users';

-- Step 2: Add voter_id column to users table
ALTER TABLE `users`
ADD COLUMN `voter_id` VARCHAR(50) DEFAULT NULL COMMENT 'Voter ID - will become primary key';

-- Step 3: Populate voter_id from user_details table
-- This assumes a 1:1 relationship and that user_details already has voter_id and username
UPDATE `users` u
JOIN `user_details` ud ON u.username = ud.username
SET u.voter_id = ud.voter_id;

-- Step 4: Make voter_id NOT NULL
-- This step should only be performed after all existing users have a voter_id
ALTER TABLE `users`
MODIFY COLUMN `voter_id` VARCHAR(50) NOT NULL;

-- Step 5: Handle foreign key constraints
-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Drop the old primary key constraint
ALTER TABLE `users` DROP PRIMARY KEY;

-- Add new primary key
ALTER TABLE `users` ADD PRIMARY KEY (`voter_id`);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Step 6: Drop the username column (no longer needed)
ALTER TABLE `users` DROP COLUMN `username`;

-- Step 7: Verify the new structure
DESCRIBE `users`;

-- Step 8: Verify foreign key constraints are still intact
-- Run this query to check:
-- SELECT 
--     TABLE_NAME,
--     COLUMN_NAME,
--     CONSTRAINT_NAME,
--     REFERENCED_TABLE_NAME,
--     REFERENCED_COLUMN_NAME
-- FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
-- WHERE REFERENCED_TABLE_NAME = 'users';











