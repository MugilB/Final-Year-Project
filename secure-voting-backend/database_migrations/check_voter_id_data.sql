-- Check VoterID Data Mismatch
-- Run this to see what's in both tables

-- Check users table voter_id data
SELECT 
    'users' as table_name,
    voter_id,
    email,
    role,
    created_at
FROM users
ORDER BY created_at;

-- Check user_details table voter_id data
SELECT 
    'user_details' as table_name,
    voter_id,
    first_name,
    last_name,
    email
FROM user_details
ORDER BY voter_id;

-- Check for mismatches
SELECT 
    'Mismatch Check' as check_type,
    u.voter_id as users_voter_id,
    u.email as users_email,
    ud.voter_id as user_details_voter_id,
    ud.first_name,
    ud.last_name
FROM users u
LEFT JOIN user_details ud ON u.voter_id = ud.voter_id
WHERE ud.voter_id IS NULL

UNION ALL

SELECT 
    'Orphaned user_details' as check_type,
    NULL as users_voter_id,
    NULL as users_email,
    ud.voter_id as user_details_voter_id,
    ud.first_name,
    ud.last_name
FROM user_details ud
LEFT JOIN users u ON ud.voter_id = u.voter_id
WHERE u.voter_id IS NULL;


















