-- Fix VoterID Data Mismatch
-- This script will fix the voter_id data in users table to match user_details

-- Step 1: Check current state
SELECT 'BEFORE FIX - Users table:' as status;
SELECT voter_id, email, role FROM users LIMIT 5;

SELECT 'BEFORE FIX - User_details table:' as status;
SELECT voter_id, first_name, last_name, email FROM user_details LIMIT 5;

-- Step 2: Check for mismatches
SELECT 'MISMATCH CHECK:' as status;
SELECT 
    u.voter_id as users_voter_id,
    u.email as users_email,
    ud.voter_id as user_details_voter_id,
    ud.first_name,
    ud.last_name
FROM users u
LEFT JOIN user_details ud ON u.voter_id = ud.voter_id
WHERE ud.voter_id IS NULL;

-- Step 3: Fix the voter_id in users table
-- We need to update users.voter_id to match user_details.voter_id
-- But first we need to find the relationship

-- Option A: If there's a way to match users to user_details
-- (This assumes there's some relationship we can use)

-- Let's try to match by email first
UPDATE users u
JOIN user_details ud ON u.email = ud.email
SET u.voter_id = ud.voter_id;

-- Step 4: Check if the fix worked
SELECT 'AFTER FIX - Users table:' as status;
SELECT voter_id, email, role FROM users LIMIT 5;

-- Step 5: Verify the relationship
SELECT 'VERIFICATION - Joined data:' as status;
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

-- Step 6: Check for any remaining mismatches
SELECT 'REMAINING MISMATCHES:' as status;
SELECT 
    u.voter_id as users_voter_id,
    u.email as users_email,
    ud.voter_id as user_details_voter_id,
    ud.first_name,
    ud.last_name
FROM users u
LEFT JOIN user_details ud ON u.voter_id = ud.voter_id
WHERE ud.voter_id IS NULL;




