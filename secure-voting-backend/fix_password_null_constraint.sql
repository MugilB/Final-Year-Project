-- Fix: Allow null passwords in users table
-- Date: 2025-01-08
-- Description: Modify hashed_password column to allow NULL values for pending users

USE secure_voting;

-- Check current constraint on hashed_password column
SELECT 
    COLUMN_NAME,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'secure_voting' 
  AND TABLE_NAME = 'users' 
  AND COLUMN_NAME = 'hashed_password';

-- Modify the hashed_password column to allow NULL values
ALTER TABLE `users` 
MODIFY COLUMN `hashed_password` VARCHAR(255) NULL COMMENT 'Hashed password - NULL for pending users';

-- Verify the change
SELECT 
    COLUMN_NAME,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'secure_voting' 
  AND TABLE_NAME = 'users' 
  AND COLUMN_NAME = 'hashed_password';

-- Check current users with null passwords (should be none initially)
SELECT 
    voter_id,
    email,
    hashed_password,
    approval_status,
    is_active
FROM users 
WHERE hashed_password IS NULL;

-- Update any existing users with empty approval status to have null passwords
UPDATE users 
SET hashed_password = NULL 
WHERE approval_status = 2 AND (hashed_password = '' OR hashed_password = 'Mugpas@23');

-- Verify the changes
SELECT 
    voter_id,
    email,
    CASE 
        WHEN hashed_password IS NULL THEN 'NULL (Pending)'
        WHEN hashed_password = '' THEN 'EMPTY'
        ELSE 'HAS_PASSWORD'
    END as password_status,
    approval_status,
    is_active
FROM users 
ORDER BY approval_status, voter_id;

















