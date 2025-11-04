-- Diagnose VoterID Issue
-- Run this first to understand what's wrong

-- Check what's in users table
SELECT 'USERS TABLE:' as table_name;
SELECT 
    voter_id,
    email,
    role,
    created_at,
    is_active,
    approval_status
FROM users
ORDER BY created_at;

-- Check what's in user_details table
SELECT 'USER_DETAILS TABLE:' as table_name;
SELECT 
    voter_id,
    first_name,
    last_name,
    email,
    phone_number,
    user_voter_id
FROM user_details
ORDER BY voter_id;

-- Check the relationship
SELECT 'RELATIONSHIP CHECK:' as check_type;
SELECT 
    u.voter_id as users_voter_id,
    u.email as users_email,
    ud.voter_id as user_details_voter_id,
    ud.user_voter_id as user_details_user_voter_id,
    ud.first_name,
    ud.last_name,
    CASE 
        WHEN u.voter_id = ud.voter_id THEN 'VOTER_ID MATCH'
        WHEN u.voter_id = ud.user_voter_id THEN 'USER_VOTER_ID MATCH'
        ELSE 'NO MATCH'
    END as match_status
FROM users u
LEFT JOIN user_details ud ON (u.voter_id = ud.voter_id OR u.voter_id = ud.user_voter_id)
ORDER BY u.created_at;

-- Check for orphaned records
SELECT 'ORPHANED RECORDS:' as check_type;
SELECT 
    'Users without user_details' as orphan_type,
    COUNT(*) as count
FROM users u
LEFT JOIN user_details ud ON (u.voter_id = ud.voter_id OR u.voter_id = ud.user_voter_id)
WHERE ud.voter_id IS NULL

UNION ALL

SELECT 
    'User_details without users' as orphan_type,
    COUNT(*) as count
FROM user_details ud
LEFT JOIN users u ON (u.voter_id = ud.voter_id OR u.voter_id = ud.user_voter_id)
WHERE u.voter_id IS NULL;











