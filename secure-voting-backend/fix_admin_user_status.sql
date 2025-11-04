-- Fix admin user approval status
-- This script ensures admin users are properly enabled

USE secure_voting;

-- Check current admin user status
SELECT 
    voter_id, 
    email, 
    approval_status, 
    is_active, 
    role,
    CASE 
        WHEN approval_status = 1 THEN 'APPROVED'
        WHEN approval_status = 2 THEN 'PENDING'
        WHEN approval_status = 0 THEN 'REJECTED'
        ELSE 'UNKNOWN'
    END as status_description
FROM users 
WHERE voter_id LIKE '%ADMIN%' OR voter_id LIKE '%SYSTEM%' OR role = 'ADMIN';

-- Update admin users to be approved and active
UPDATE users 
SET 
    approval_status = 1,  -- APPROVED
    is_active = 1         -- ACTIVE
WHERE 
    voter_id LIKE '%ADMIN%' 
    OR voter_id LIKE '%SYSTEM%' 
    OR role = 'ADMIN';

-- Verify the changes
SELECT 
    voter_id, 
    email, 
    approval_status, 
    is_active, 
    role,
    CASE 
        WHEN approval_status = 1 THEN 'APPROVED'
        WHEN approval_status = 2 THEN 'PENDING'
        WHEN approval_status = 0 THEN 'REJECTED'
        ELSE 'UNKNOWN'
    END as status_description
FROM users 
WHERE voter_id LIKE '%ADMIN%' OR voter_id LIKE '%SYSTEM%' OR role = 'ADMIN';

-- Also check if there are any users with NULL approval_status
SELECT 
    voter_id, 
    email, 
    approval_status, 
    is_active, 
    role
FROM users 
WHERE approval_status IS NULL;

-- Fix any users with NULL approval_status (set to approved for existing users)
UPDATE users 
SET approval_status = 1 
WHERE approval_status IS NULL;











