-- Simple Migration: Add voter_id as primary key to users table
-- Date: 2025-01-08
-- Description: Simple approach - just disable foreign key checks

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

-- Step 4: Disable foreign key checks to allow primary key changes
SET FOREIGN_KEY_CHECKS = 0;

-- Step 5: Drop the old primary key constraint
ALTER TABLE `users` DROP PRIMARY KEY;

-- Step 6: Add new primary key
ALTER TABLE `users` ADD PRIMARY KEY (`voter_id`);

-- Step 7: Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Step 8: Drop the username column from users table
ALTER TABLE `users` DROP COLUMN `username`;

-- Step 9: Verify the new structure
DESCRIBE `users`;

-- Step 10: Verify foreign key constraints are still intact
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users';











