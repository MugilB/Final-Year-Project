-- Migration: Add voter_id as primary key to users table
-- Date: 2025-01-08
-- Description: Add voter_id column and make it the primary key
-- 
-- IMPORTANT: This script temporarily disables foreign key checks
-- Run check_foreign_keys.sql first to see what constraints exist!

-- Step 1: Add voter_id column to users table
ALTER TABLE `users` 
ADD COLUMN `voter_id` VARCHAR(50) DEFAULT NULL COMMENT 'Voter ID - will become primary key';

-- Step 2: Populate voter_id from user_details table
UPDATE `users` u 
JOIN `user_details` ud ON u.username = ud.username 
SET u.voter_id = ud.voter_id;

-- Step 3: Make voter_id NOT NULL
ALTER TABLE `users` 
MODIFY COLUMN `voter_id` VARCHAR(50) NOT NULL;

-- Step 4: Handle foreign key constraints and primary key migration
-- First, check what foreign keys reference the users table
-- You may need to temporarily disable foreign key checks or drop/recreate constraints

-- Option 1: Disable foreign key checks temporarily (SAFER)
SET FOREIGN_KEY_CHECKS = 0;

-- Drop the old primary key constraint
ALTER TABLE `users` DROP PRIMARY KEY;

-- Add new primary key
ALTER TABLE `users` ADD PRIMARY KEY (`voter_id`);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Step 5: Drop the username column (no longer needed)
ALTER TABLE `users` DROP COLUMN `username`;

-- Step 6: Verify the new structure
DESCRIBE `users`;
