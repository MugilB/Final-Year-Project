-- Test Migration Success
-- Verify that the VoterID primary key migration worked correctly

-- Test 1: Check users table structure
DESCRIBE `users`;

-- Test 2: Check user_details table structure
DESCRIBE `user_details`;

-- Test 3: Verify foreign key constraints
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users';

-- Test 4: Test the relationship between users and user_details
SELECT 
    u.voter_id,
    u.email,
    u.role,
    u.is_active,
    u.approval_status,
    ud.first_name,
    ud.last_name,
    ud.phone_number,
    ud.gender
FROM users u
JOIN user_details ud ON u.voter_id = ud.user_voter_id
LIMIT 5;

-- Test 5: Count total users
SELECT COUNT(*) as total_users FROM users;

-- Test 6: Count total user_details
SELECT COUNT(*) as total_user_details FROM user_details;

-- Test 7: Check for any orphaned records
SELECT 
    'Users without user_details' as check_type,
    COUNT(*) as count
FROM users u
LEFT JOIN user_details ud ON u.voter_id = ud.user_voter_id
WHERE ud.user_voter_id IS NULL

UNION ALL

SELECT 
    'User_details without users' as check_type,
    COUNT(*) as count
FROM user_details ud
LEFT JOIN users u ON ud.user_voter_id = u.voter_id
WHERE u.voter_id IS NULL;








