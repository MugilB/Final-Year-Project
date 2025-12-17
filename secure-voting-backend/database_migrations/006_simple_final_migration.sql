-- Simple Final Migration: VoterID Primary Key
-- Date: 2025-01-08
-- Description: Simple approach that preserves user_details structure

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

-- Step 4: Handle the foreign key constraint issue
-- The user_details table has: FOREIGN KEY (username) REFERENCES users (username)
-- We need to temporarily disable foreign key checks

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Drop the old primary key constraint
ALTER TABLE `users` DROP PRIMARY KEY;

-- Add new primary key
ALTER TABLE `users` ADD PRIMARY KEY (`voter_id`);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Step 5: Drop the username column from users table
ALTER TABLE `users` DROP COLUMN `username`;

-- Step 6: Verify the new structure
DESCRIBE `users`;
DESCRIBE `user_details`;

-- Step 7: Verify foreign key constraints
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users';

-- Step 8: Test the relationship (this will show if the foreign key is broken)
SELECT 
    u.voter_id,
    u.email,
    u.role,
    ud.first_name,
    ud.last_name,
    ud.phone_number
FROM users u
JOIN user_details ud ON u.voter_id = ud.voter_id
LIMIT 5;


















