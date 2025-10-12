-- Comprehensive VoterID Fix
-- This script handles different scenarios for fixing voter_id mismatches

-- Step 1: Analyze the current situation
SELECT '=== CURRENT STATE ANALYSIS ===' as status;

-- Check users table
SELECT 'Users table structure and data:' as info;
SELECT COUNT(*) as total_users FROM users;
SELECT voter_id, email, role, created_at FROM users ORDER BY created_at LIMIT 5;

-- Check user_details table  
SELECT 'User_details table structure and data:' as info;
SELECT COUNT(*) as total_user_details FROM user_details;
SELECT voter_id, first_name, last_name, email FROM user_details ORDER BY voter_id LIMIT 5;

-- Step 2: Find the relationship between users and user_details
-- We need to figure out how to match them

-- Check if email can be used to match
SELECT 'Email matching analysis:' as info;
SELECT 
    u.email as users_email,
    u.voter_id as users_voter_id,
    ud.email as user_details_email,
    ud.voter_id as user_details_voter_id,
    CASE 
        WHEN u.email = ud.email THEN 'MATCH'
        ELSE 'NO MATCH'
    END as email_match
FROM users u
LEFT JOIN user_details ud ON u.email = ud.email
LIMIT 10;

-- Step 3: Fix the voter_id based on email matching
-- This assumes email is the common field

SELECT '=== FIXING VOTER_ID MISMATCH ===' as status;

-- Update users.voter_id to match user_details.voter_id where emails match
UPDATE users u
JOIN user_details ud ON u.email = ud.email
SET u.voter_id = ud.voter_id;

-- Step 4: Verify the fix
SELECT '=== VERIFICATION ===' as status;

-- Check if the fix worked
SELECT 'Users table after fix:' as info;
SELECT voter_id, email, role FROM users ORDER BY created_at LIMIT 5;

-- Check the relationship
SELECT 'Joined data verification:' as info;
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

-- Check for any remaining mismatches
SELECT 'Remaining mismatches:' as info;
SELECT 
    u.voter_id as users_voter_id,
    u.email as users_email,
    ud.voter_id as user_details_voter_id,
    ud.first_name,
    ud.last_name
FROM users u
LEFT JOIN user_details ud ON u.voter_id = ud.voter_id
WHERE ud.voter_id IS NULL;

-- Step 5: Final verification
SELECT '=== FINAL VERIFICATION ===' as status;

-- Count matches
SELECT 
    'Total users' as metric,
    COUNT(*) as count
FROM users

UNION ALL

SELECT 
    'Total user_details' as metric,
    COUNT(*) as count
FROM user_details

UNION ALL

SELECT 
    'Successful matches' as metric,
    COUNT(*) as count
FROM users u
JOIN user_details ud ON u.voter_id = ud.voter_id;




