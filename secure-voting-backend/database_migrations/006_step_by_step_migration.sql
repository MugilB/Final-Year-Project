-- STEP-BY-STEP MIGRATION: VoterID Primary Key
-- Run these commands one by one and check for errors

-- STEP 1: Check current foreign key constraints
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users';

-- STEP 2: Add voter_id column
ALTER TABLE `users`
ADD COLUMN `voter_id` VARCHAR(50) DEFAULT NULL COMMENT 'Voter ID - will become primary key';

-- STEP 3: Populate voter_id from user_details
UPDATE `users` u
JOIN `user_details` ud ON u.username = ud.username
SET u.voter_id = ud.voter_id;

-- STEP 4: Make voter_id NOT NULL
ALTER TABLE `users`
MODIFY COLUMN `voter_id` VARCHAR(50) NOT NULL;

-- STEP 5: Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- STEP 6: Drop old primary key
ALTER TABLE `users` DROP PRIMARY KEY;

-- STEP 7: Add new primary key
ALTER TABLE `users` ADD PRIMARY KEY (`voter_id`);

-- STEP 8: Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- STEP 9: Drop username column
ALTER TABLE `users` DROP COLUMN `username`;

-- STEP 10: Verify structure
DESCRIBE `users`;

-- STEP 11: Verify foreign keys
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users';


















