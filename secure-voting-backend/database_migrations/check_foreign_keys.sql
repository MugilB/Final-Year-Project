-- Check Foreign Key Constraints Before Migration
-- Run this first to understand what constraints exist

-- Check all foreign keys that reference the users table
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users';

-- Check all foreign keys in the entire database
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME;

-- Check current users table structure
DESCRIBE `users`;

-- Check current user_details table structure  
DESCRIBE `user_details`;

-- Check if there are any users without corresponding user_details
SELECT u.username, u.email, ud.voter_id
FROM users u
LEFT JOIN user_details ud ON u.username = ud.username
WHERE ud.voter_id IS NULL;











