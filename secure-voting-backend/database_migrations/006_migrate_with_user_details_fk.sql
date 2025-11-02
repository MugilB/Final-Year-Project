-- Migration: Add voter_id as primary key to users table
-- Date: 2025-01-08
-- Description: Handle user_details foreign key constraint during migration

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

-- Step 4: Handle the user_details foreign key constraint
-- We need to drop the foreign key constraint first, then recreate it

-- Drop the foreign key constraint from user_details
ALTER TABLE `user_details` DROP FOREIGN KEY `user_details_ibfk_1`;

-- Step 5: Now we can safely change the primary key
-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Drop the old primary key constraint
ALTER TABLE `users` DROP PRIMARY KEY;

-- Add new primary key
ALTER TABLE `users` ADD PRIMARY KEY (`voter_id`);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Step 6: Drop the username column from users table
ALTER TABLE `users` DROP COLUMN `username`;

-- Step 7: Update user_details to reference voter_id instead of username
-- First, add a voter_id column to user_details if it doesn't exist
-- (It should already exist, but let's be safe)
ALTER TABLE `user_details` 
ADD COLUMN `user_voter_id` VARCHAR(50) DEFAULT NULL COMMENT 'Reference to users.voter_id';

-- Populate the new reference column
UPDATE `user_details` ud
JOIN `users` u ON ud.username = u.voter_id  -- This will work after we populate voter_id
SET ud.user_voter_id = u.voter_id;

-- Make the new reference column NOT NULL
ALTER TABLE `user_details`
MODIFY COLUMN `user_voter_id` VARCHAR(50) NOT NULL;

-- Step 8: Recreate the foreign key constraint using voter_id
ALTER TABLE `user_details`
ADD CONSTRAINT `user_details_ibfk_1` 
FOREIGN KEY (`user_voter_id`) REFERENCES `users`(`voter_id`) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- Step 9: Drop the old username column from user_details
ALTER TABLE `user_details` DROP COLUMN `username`;

-- Step 10: Verify the new structure
DESCRIBE `users`;
DESCRIBE `user_details`;

-- Step 11: Verify foreign key constraints
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users';








