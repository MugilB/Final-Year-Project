-- Fix Duplicate VoterID Issue
-- This script handles the duplicate voter_id problem

-- Step 1: Check for duplicate voter_ids in user_details
SELECT 'DUPLICATE VOTER_IDs IN USER_DETAILS:' as status;
SELECT 
    voter_id,
    COUNT(*) as count,
    GROUP_CONCAT(first_name, ' ', last_name) as names,
    GROUP_CONCAT(email) as emails
FROM user_details
GROUP BY voter_id
HAVING COUNT(*) > 1;

-- Step 2: Check for duplicate voter_ids in users
SELECT 'DUPLICATE VOTER_IDs IN USERS:' as status;
SELECT 
    voter_id,
    COUNT(*) as count,
    GROUP_CONCAT(email) as emails
FROM users
GROUP BY voter_id
HAVING COUNT(*) > 1;

-- Step 3: Show the problematic records
SELECT 'PROBLEMATIC RECORDS:' as status;
SELECT 
    'user_details' as table_name,
    voter_id,
    first_name,
    last_name,
    email,
    user_voter_id
FROM user_details
WHERE voter_id IN (
    SELECT voter_id 
    FROM user_details 
    GROUP BY voter_id 
    HAVING COUNT(*) > 1
)
ORDER BY voter_id;

-- Step 4: Fix the duplicates by making them unique
-- We'll add a suffix to make duplicate voter_ids unique

-- First, let's see what we're working with
SELECT 'BEFORE FIX - user_details with duplicates:' as status;
SELECT 
    voter_id,
    first_name,
    last_name,
    email,
    ROW_NUMBER() OVER (PARTITION BY voter_id ORDER BY voter_id) as row_num
FROM user_details
WHERE voter_id IN (
    SELECT voter_id 
    FROM user_details 
    GROUP BY voter_id 
    HAVING COUNT(*) > 1
)
ORDER BY voter_id, row_num;

-- Step 5: Update duplicate voter_ids to make them unique
-- We'll add a suffix like _1, _2, etc. to duplicates

UPDATE user_details 
SET voter_id = CONCAT(voter_id, '_', ROW_NUMBER() OVER (PARTITION BY voter_id ORDER BY voter_id))
WHERE voter_id IN (
    SELECT voter_id 
    FROM (
        SELECT voter_id 
        FROM user_details 
        GROUP BY voter_id 
        HAVING COUNT(*) > 1
    ) AS duplicates
);

-- Step 6: Verify the fix
SELECT 'AFTER FIX - user_details:' as status;
SELECT 
    voter_id,
    first_name,
    last_name,
    email
FROM user_details
ORDER BY voter_id;

-- Step 7: Check for any remaining duplicates
SELECT 'REMAINING DUPLICATES:' as status;
SELECT 
    voter_id,
    COUNT(*) as count
FROM user_details
GROUP BY voter_id
HAVING COUNT(*) > 1;

-- Step 8: Now try the original update
SELECT 'ATTEMPTING ORIGINAL UPDATE...' as status;
UPDATE users u
JOIN user_details ud ON u.email = ud.email
SET u.voter_id = ud.voter_id;

-- Step 9: Verify the final result
SELECT 'FINAL VERIFICATION:' as status;
SELECT 
    u.voter_id,
    u.email,
    u.role,
    ud.first_name,
    ud.last_name,
    ud.phone_number
FROM users u
JOIN user_details ud ON u.voter_id = ud.voter_id
ORDER BY u.created_at;








